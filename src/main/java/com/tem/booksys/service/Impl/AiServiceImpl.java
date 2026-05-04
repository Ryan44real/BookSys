package com.tem.booksys.service.Impl;

import com.tem.booksys.entity.AiCallLog;
import com.tem.booksys.entity.Article;
import com.tem.booksys.entity.BorrowRecord;
import com.tem.booksys.mapper.AiCallLogMapper;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.service.AiService;
import com.tem.booksys.utils.ChineseGPT;
import com.tem.booksys.utils.ContentValidator;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ChineseGPT chineseGPT;

    @Autowired
    private AiCallLogMapper aiCallLogMapper;

    @Autowired
    private ContentValidator contentValidator;

    @Override
    public String recommend(Integer userId, String query, String model) {
        // 1. 内容校验
        if (!contentValidator.isValidBookQuery(query)) {
            throw new IllegalArgumentException("请输入与图书推荐相关的内容");
        }

        // 2. 获取用户借阅历史（最近5-10本）
        List<BorrowRecord> borrowRecords = borrowMapper.borrowList(userId, null, null);
        StringBuilder history = new StringBuilder();
        Set<Integer> seen = new HashSet<>();
        int count = 0;
        for (int i = borrowRecords.size() - 1; i >= 0 && count < 10; i--) {
            BorrowRecord r = borrowRecords.get(i);
            Article a = bookMapper.findByBookNum(String.valueOf(r.getBookNum()));
            if (a != null && seen.add(a.getBookNum())) {
                history.append("- 《").append(a.getTitle()).append("》(")
                       .append(a.getPublisher() != null ? a.getPublisher() : "").append(")\n");
                count++;
            }
        }
        String historyStr = history.length() > 0 ? history.toString() : "暂无借阅记录";

        // 3. 获取候选书库（基于关键词模糊检索，最多20本）
        String keyword = extractKeyword(query);
        List<Article> candidates = bookMapper.list(null, null, keyword, null, null, null);
        StringBuilder candidateStr = new StringBuilder();
        int maxCandidates = Math.min(candidates.size(), 20);
        for (int i = 0; i < maxCandidates; i++) {
            Article a = candidates.get(i);
            candidateStr.append("- 《").append(a.getTitle()).append("》")
                        .append(" | 简介：").append(truncate(a.getContent(), 100))
                        .append(" | ISBN：").append(a.getIsbn() != null ? a.getIsbn() : "无")
                        .append("\n");
        }

        // 4. 调用 AI
        AiCallLog aiLog = new AiCallLog();
        aiLog.setUserId(userId);
        aiLog.setModel(model != null ? model : "deepseek-v4-pro");
        aiLog.setOperation("recommend");
        aiLog.setInputSummary(contentValidator.summarize(query, 200));
        long start = System.currentTimeMillis();

        try {
            String result = chineseGPT.recommend(query, historyStr, candidateStr.toString(), model);
            long elapsed = System.currentTimeMillis() - start;
            aiLog.setStatus(0);
            aiLog.setResponseTimeMs((int) elapsed);
            aiLog.setOutputSummary(contentValidator.summarize(result, 200));
            aiCallLogMapper.add(aiLog);
            return result;
        } catch (IOException e) {
            long elapsed = System.currentTimeMillis() - start;
            aiLog.setStatus(isTimeout(e) ? 2 : 1);
            aiLog.setResponseTimeMs((int) elapsed);
            aiLog.setErrorMsg(e.getMessage());
            aiCallLogMapper.add(aiLog);
            log.error("AI recommend failed: userId={}, query={}", userId, query, e);
            throw new RuntimeException("AI服务暂时不可用，请稍后重试");
        } catch (JSONException e) {
            long elapsed = System.currentTimeMillis() - start;
            aiLog.setStatus(1);
            aiLog.setResponseTimeMs((int) elapsed);
            aiLog.setErrorMsg("JSON parse error: " + e.getMessage());
            aiCallLogMapper.add(aiLog);
            log.error("AI response parse failed: userId={}", userId, e);
            throw new RuntimeException("AI响应解析失败");
        }
    }

    @Override
    public Object getAiStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("successCount", aiCallLogMapper.countSuccess());
        stats.put("failedCount", aiCallLogMapper.countFailed());
        stats.put("avgResponseTimeMs", aiCallLogMapper.avgResponseTime());
        stats.put("recentCalls", aiCallLogMapper.findRecent());
        return stats;
    }

    private String extractKeyword(String query) {
        // 简单关键词提取：取用户输入中较长的词
        String cleaned = query.replaceAll("[，。！？,.!?]", " ");
        String[] words = cleaned.split("\\s+");
        String longest = query;
        for (String w : words) {
            if (w.length() >= 2 && w.length() < query.length()) {
                return w;
            }
        }
        return query.length() > 10 ? query.substring(0, 10) : query;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }

    private boolean isTimeout(IOException e) {
        return e.getMessage() != null && (
                e.getMessage().contains("timeout") ||
                e.getMessage().contains("Timeout") ||
                e.getMessage().contains("timed out"));
    }
}
