//package com.tem.booksys;
//
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.connection.StringRedisConnection;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//import java.util.concurrent.TimeUnit;
//
//@SpringBootTest //初始化springboot容器
//public class RedisTest {
//
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
////    @Test
//    public void testSet(){
//        //让redis存储一个键值对 StringRedisTemplate
//        ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
//        operations.set("username","zhangsan");
//        //设定id的过期时间
//        operations.set("id","1",15, TimeUnit.SECONDS);
//    }
////    @Test
//    public void testGet(){
//        ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
//        System.out.println(operations.get("username"));
//    }
//}
