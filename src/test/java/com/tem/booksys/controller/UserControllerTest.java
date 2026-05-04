package com.tem.booksys.controller;

import com.tem.booksys.config.WebMvcConfig;
import com.tem.booksys.entity.User;
import com.tem.booksys.interceptors.LoginInterceptor;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.UserService;
import com.tem.booksys.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebMvcConfig.class, LoginInterceptor.class}))
@DisplayName("用户控制器测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("e10adc3949ba59abbe56e057f20f883e");
        testUser.setNickname("测试用户");
        testUser.setEmail("test@test.com");
        testUser.setType(2);
        testUser.setState(1);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("登录成功应返回用户ID")
    void login_success() throws Exception {
        when(userService.findByUserName("testuser")).thenReturn(testUser);
        when(jwtUtil.genToken(any())).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/user/login")
                        .param("username", "testuser")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("1"));
    }

    @Test
    @DisplayName("登录失败-密码错误")
    void login_wrongPassword() throws Exception {
        when(userService.findByUserName("testuser")).thenReturn(testUser);

        mockMvc.perform(post("/user/login")
                        .param("username", "testuser")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    @Test
    @DisplayName("登录失败-用户不存在")
    void login_userNotFound() throws Exception {
        when(userService.findByUserName("nouser")).thenReturn(null);

        mockMvc.perform(post("/user/login")
                        .param("username", "nouser")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("该用户不存在"));
    }

    @Test
    @DisplayName("健康检查接口")
    void check() throws Exception {
        when(valueOperations.get("test-token")).thenReturn("test-token");
        when(jwtUtil.parseToken("test-token")).thenReturn(java.util.Map.of("id", 1, "username", "testuser", "userState", 1, "userType", 2));

        mockMvc.perform(get("/user/check")
                        .header("Authorization", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
