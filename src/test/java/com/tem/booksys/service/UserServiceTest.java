package com.tem.booksys.service;

import com.tem.booksys.entity.User;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.Impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

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
    }

    @Test
    @DisplayName("通过用户名查找用户")
    void findByUserName_shouldReturnUser() {
        when(userMapper.findByUserName("testuser")).thenReturn(testUser);

        User result = userService.findByUserName("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userMapper).findByUserName("testuser");
    }

    @Test
    @DisplayName("查找不存在的用户返回null")
    void findByUserName_notFound_shouldReturnNull() {
        when(userMapper.findByUserName("nouser")).thenReturn(null);

        User result = userService.findByUserName("nouser");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("注册新用户")
    void register_shouldAddUser() {
        userService.register("newuser", "password", "new@test.com", 2);

        verify(userMapper).add(any(), any(), any(), any());
    }

    @Test
    @DisplayName("获取用户总数")
    void getUserNumService_shouldReturnCount() {
        when(userMapper.getUserNum()).thenReturn(100);

        Integer count = userService.getUserNumService();

        assertThat(count).isEqualTo(100);
    }
}
