package com.tem.booksys.mapper;

import com.tem.booksys.entiy.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByUserName(String username);
    void add(String username, String md5String,String mail,Integer type);


    void update(User user);

    void updateAvatar(String avatarUrl,Integer id);

    void updatePwd(String pwd, Integer id);
    @Select("select id from user where username = #{username}")
    Integer findByName(String username);
    @Select("select username from user where id = #{userId}")
    String findById(Integer userId);
    @Select("select count(*) from borrow_record where userId=#{userId}")
    Integer userInfoForBorrow(String userId);
    @Select("select count(*) from borrow_record where (borrowState=3 or borrowState = 4) and userId=#{userId}")
    Integer getOverdue(String userId);

    List<User> list(@Param("username") String username, @Param("state") Integer state);

    @Update("update user set nickname=#{arg0} where id=#{arg1}")
    void updateNickname(String nickname, Integer id);
    @Update("update user set state=#{arg1} where id=#{arg0}")
    void updateState(String id, Integer state);

    @Select("select msg from user where id=#{arg0}")
    String getMsg(String id);
    @Update("update user set msg=#{arg0} where id=#{arg1}")
    void updateMsg(String msg,String id);

    @Update("update user set msg=null where id=#{id}")
    void deleteUserMsg(Integer id);

    @Delete("delete from user where id = #{id}")
    void deleteUser(String id);

    @Select("select count(*) from user")
    Integer getUserNum();

    @Select("select email from user where username = #{username}")
    String getEmailByUsername(String username);

    @Update("update user set password=#{arg2} where username=#{arg0} and email=#{arg1}")
    void updatePswByEmail(String username, String mail, String password);

    @Select("select email from user where id = #{id}")
    String getMail(String id);
}
