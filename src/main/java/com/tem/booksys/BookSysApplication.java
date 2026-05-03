package com.tem.booksys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan("com.tem.booksys.entity")
public class BookSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookSysApplication.class, args);
    }

}
