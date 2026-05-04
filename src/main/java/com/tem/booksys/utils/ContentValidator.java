package com.tem.booksys.utils;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

@Component
public class ContentValidator {

    // 简单敏感词/非法话题过滤
    private static final Set<String> BLOCKED_KEYWORDS = Set.of(
            "政治", "色情", "暴力", "赌博", "毒品", "枪支"
    );

    private static final Pattern BOOK_QUERY_PATTERN =
            Pattern.compile(".*(书|推荐|读|借|学习|入门|进阶|编程|小说|文学|历史|哲学|科学|计算机|技术|架构|设计|算法).*", Pattern.DOTALL);

    /** 校验用户输入是否与图书推荐相关 */
    public boolean isValidBookQuery(String query) {
        if (query == null || query.isBlank()) return false;
        if (query.length() > 500) return false;
        // 检查屏蔽关键词
        String lower = query.toLowerCase();
        for (String kw : BLOCKED_KEYWORDS) {
            if (lower.contains(kw)) return false;
        }
        // 检查是否与图书相关（宽松匹配）
        return BOOK_QUERY_PATTERN.matcher(lower).matches();
    }

    /** 截断输入用于日志摘要 */
    public String summarize(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
