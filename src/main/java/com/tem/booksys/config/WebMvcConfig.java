package com.tem.booksys.config;

import com.tem.booksys.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).excludePathPatterns(new String[]{"/user/login","/user/register","/user/sendmail","/user/editPswByEmail","/category/query","/article/getBookList"});
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        // 允许所有的路径可以跨域
//        registry.addMapping("/**")
//                // 允许所有来源都可以跨域
//                .allowedOriginPatterns("*")
//                // 允许跨域的请求
//                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS","PATCH")
//                // 允许证书凭证（如果这里设置为true，设置来源为所有只能使用allowedOriginPatterns）
//                .allowCredentials(true)
//                // 跨域时间3600秒
//                .maxAge(3600)
//                // 允许所以头标题
//                .allowedHeaders("*");
//    }
}


