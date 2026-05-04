package com.tem.booksys.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookSysOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BookSys 图书数字化管理系统 API")
                        .description("基于 Spring Boot 的图书数字化管理系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BookSys Team")))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new Components()
                        .addSecuritySchemes("Authorization", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .scheme("bearer")
                                .description("JWT Token，登录后获取，格式: <token>")));
    }
}
