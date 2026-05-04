package com.tem.booksys.controller;

import com.tem.booksys.entity.Result;
import com.tem.booksys.service.AiService;
import com.tem.booksys.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI 智能服务", description = "AI 荐书、智能标签等接口")
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Operation(summary = "AI 智能荐书", description = "根据用户自然语言需求和借阅历史，用 AI 推荐图书。支持 model 参数指定模型。")
    @PostMapping("/chat/recommend")
    public Result<String> recommend(
            @Parameter(description = "用户提问内容（自然语言）") @RequestParam String query,
            @Parameter(description = "模型名称，默认 deepseek-v4-pro") @RequestParam(required = false, defaultValue = "deepseek-v4-pro") String model) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        try {
            String result = aiService.recommend(userId, query, model);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "AI 调用监控统计", description = "查看 AI 调用的成功率、平均响应时间、最近调用记录")
    @GetMapping("/stats")
    public Result<Object> stats() {
        return Result.success(aiService.getAiStats());
    }
}
