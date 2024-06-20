package com.tem.booksys.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tem.booksys.entiy.PageBean;
import com.tem.booksys.entiy.User;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.UserService;
import com.tem.booksys.utils.Md5Util;
import com.tem.booksys.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
//    @Resource
//    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public PageBean<User> list(Integer pageNum, Integer pageSize, String username, Integer state) {
        //        1.创建PageBean对象
        PageBean<User> pb = new PageBean<>();
//        2.开启分页查询 PageHelper
        PageHelper.startPage(pageNum,pageSize);
        List<User> as = userMapper.list(username,state);
        Page<User> p = (Page<User>) as;
        //把数据填充到PageBean中
        pb.setTotal(((Page<User>) as).getTotal());
        pb.setItems(((Page<User>) as).getResult());
        return pb;
    }



    @Override
    public User findByUserName(String username) {
        return userMapper.findByUserName(username);
    }

    @Override
    public void register(String username, String password,String mail,Integer type) {
//        System.out.println(username+"茶小程序"+password);
        //加密，对密码加密
        String md5String = Md5Util.getMD5String(password);
        //添加
        if (type==null) type = 2;
        userMapper.add(username,md5String,mail,type);
    }

    @Override
    public void update( User user) {
        user.setUpdateTime(LocalDateTime.now());
//        System.out.println(user);
        userMapper.update(user);

    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String,Object> map =  ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        //此时可以不用传参now time,直接用mybatis的
        userMapper.updateAvatar(avatarUrl,id);
    }

    @Override
    public void updatePwd(String newPassword) {
        Map<String,Object> map =  ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        String pwd = Md5Util.getMD5String(newPassword);
        userMapper.updatePwd(pwd,id);
    }

    @Override
    public void updatePwds(String password, String id) {
        String pwd = Md5Util.getMD5String(password);
        userMapper.updatePwd(pwd, Integer.valueOf(id));
    }

    @Override
    public Boolean breakUserService(String id) {
//      Boolean res = redisTemplate.delete(id);
        return false;
    }

    @Override
    public Integer getUserNumService() {
        return userMapper.getUserNum();
    }
}
