package com.tem.booksys.utils;

import com.tem.booksys.config.AppConfigProperties;
import okhttp3.*;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class ChineseGPT {

    private final String apiKey;
    private final String defaultModel;
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private static final String BASE_URL = "https://api.deepseek.com/chat/completions";

    public ChineseGPT(AppConfigProperties config) {
        this.apiKey = config.getDeepseek().getApiKey();
        this.defaultModel = config.getDeepseek().getDefaultModel();
    }

    /** 通用对话方法，支持指定模型和 reasoning_effort */
    public String chat(String systemPrompt, String userContent, String model) throws IOException, JSONException {
        return chat(systemPrompt, userContent, model, "high");
    }

    /** 通用对话方法，完整参数 */
    public String chat(String systemPrompt, String userContent, String model, String reasoningEffort) throws IOException, JSONException {
        if (model == null || model.isBlank()) model = defaultModel;
        if (reasoningEffort == null || reasoningEffort.isBlank()) reasoningEffort = "high";

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("model", model);
        bodyJson.put("stream", false);

        JSONArray messages = new JSONArray();
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.put(sysMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userContent);
        messages.put(userMsg);

        bodyJson.put("messages", messages);

        JSONObject thinking = new JSONObject();
        thinking.put("type", "enabled");
        bodyJson.put("thinking", thinking);
        bodyJson.put("reasoning_effort", reasoningEffort);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, bodyJson.toString());
        Request request = new Request.Builder()
                .url(BASE_URL)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errBody = response.body() != null ? response.body().string() : "";
                throw new IOException("DeepSeek API error: " + response.code() + " " + errBody);
            }
            String respBody = response.body() != null ? response.body().string() : "{}";
            JSONObject respJson = new JSONObject(respBody);
            return respJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    /** 提取图书标签 */
    public String extractTags(String bookTitle, String content, String model) throws IOException, JSONException {
        String systemPrompt = "你是一位专业的图书分类专家。根据图书标题和简介，提取3-5个核心关键词作为标签。标签间用逗号分隔，尽量客观（如技术栈、难度等级、领域、风格）。只输出标签，不要任何解释。";
        String userContent = "书名：《" + bookTitle + "》\n简介：" + (content != null ? content : "无");
        return chat(systemPrompt, userContent, model);
    }

    /** 生成图书简介（兼容旧接口） */
    public String GptResult(String bookName, String bookNum) throws IOException, JSONException {
        return GptResult(bookName, bookNum, defaultModel);
    }

    public String GptResult(String bookName, String bookNum, String model) throws IOException, JSONException {
        String userContent = "书名为《" + bookName + "》且ISBN为" + bookNum + "的书籍简介。只输出书籍简介。";
        return chat("你是一位图书馆管理员，请为图书撰写简洁的专业简介。", userContent, model);
    }

    /** 智能荐书 */
    public String recommend(String userQuery, String history, String candidates, String model) throws IOException, JSONException {
        String systemPrompt = "你是一位专业的图书馆资深管理员，善于根据读者的阅读历史和偏好推荐图书。请从候选书库中筛选3-5本最匹配的图书，用Markdown格式输出，包含书名、推荐理由。";
        String userContent = "读者历史借阅偏好：" + history +
                "\n当前需求：" + userQuery +
                "\n候选书库：" + candidates;
        return chat(systemPrompt, userContent, model);
    }
}
