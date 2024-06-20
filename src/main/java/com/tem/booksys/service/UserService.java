package com.tem.booksys.service;

import com.tem.booksys.entiy.PageBean;
import com.tem.booksys.entiy.User;

public interface UserService {
    //根据用户名找用户
    User findByUserName(String username);

    void register(String username, String password,String mail,Integer type);

    //更新
    void update(User user);

    //更新头像
    void updateAvatar(String avatarUrl);

    //更新密码
    void updatePwd(String newPassword);

    PageBean<User> list(Integer pageNum, Integer pageSize, String username, Integer state);

    void updatePwds(String password, String id);

    Boolean breakUserService(String id);

    Integer getUserNumService();
}
