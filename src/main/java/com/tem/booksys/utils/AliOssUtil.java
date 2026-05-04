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

    public AliOssUtil(AppConfigProperties config) {
        this.endpoint = config.getOss().getEndpoint();
        this.accessKeyId = config.getOss().getAccessKeyId();
        this.accessKeySecret = config.getOss().getAccessKeySecret();
        this.bucketName = config.getOss().getBucketName();
    }

    public String uploadFile(String objectName, InputStream in) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String url = "";
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, in);
            ossClient.putObject(putObjectRequest);
            url = "https://" + bucketName + "." + endpoint.substring(endpoint.lastIndexOf("/") + 1) + "/" + objectName;
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
