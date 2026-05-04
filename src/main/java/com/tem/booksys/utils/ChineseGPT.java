package com.tem.booksys.utils;

import com.tem.booksys.config.AppConfigProperties;
import okhttp3.*;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ChineseGPT {

    private final String apiKey;
    private final String secretKey;
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public ChineseGPT(AppConfigProperties config) {
        this.apiKey = config.getBaiduAi().getApiKey();
        this.secretKey = config.getBaiduAi().getSecretKey();
    }

    public String GptResult(String bookName, String bookNum) throws IOException, JSONException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\"messages\":[{\"role\":\"user\",\"content\":\"书名为《" + bookName + "》且ISBN为" + bookNum + "的书籍简介。只输出书籍简介。\"}],\"top_p\":0}");

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie_speed?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("result");
    }

    private String getAccessToken() throws IOException, JSONException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,
                "grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }
}
