package com.tem.booksys.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "booksys")
public class AppConfigProperties {

    private OssConfig oss = new OssConfig();
    private BaiduAiConfig baiduAi = new BaiduAiConfig();
    private JwtConfig jwt = new JwtConfig();
    private PythonConfig python = new PythonConfig();

    @Data
    public static class OssConfig {
        private String endpoint = "https://oss-cn-guangzhou.aliyuncs.com";
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
    }

    @Data
    public static class BaiduAiConfig {
        private String apiKey;
        private String secretKey;
    }

    @Data
    public static class JwtConfig {
        private String secret = "itheima";
        private long expirationHours = 1;
    }

    @Data
    public static class PythonConfig {
        private String pythonPath;
        private String scriptPath;
    }
}
