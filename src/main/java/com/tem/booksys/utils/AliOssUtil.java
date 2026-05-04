package com.tem.booksys.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.tem.booksys.config.AppConfigProperties;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class AliOssUtil {

    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;
    private final String BASE_PATH = "booksys/";

    public AliOssUtil(AppConfigProperties config) {
        this.endpoint = config.getOss().getEndpoint();
        this.accessKeyId = config.getOss().getAccessKeyId();
        this.accessKeySecret = config.getOss().getAccessKeySecret();
        this.bucketName = config.getOss().getBucketName();
    }

    public String uploadFile(String objectName, InputStream in) {
        // 统一上传至 booksys 文件夹下
        String fullObjectName = BASE_PATH + objectName;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String url = "";
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fullObjectName, in);
            ossClient.putObject(putObjectRequest);
            url = "https://" + bucketName + "." + endpoint.substring(endpoint.lastIndexOf("/") + 1) + "/" + fullObjectName;
        } catch (OSSException oe) {
            System.err.println("OSS error: " + oe.getErrorMessage());
        } catch (ClientException ce) {
            System.err.println("OSS client error: " + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return url;
    }
}
