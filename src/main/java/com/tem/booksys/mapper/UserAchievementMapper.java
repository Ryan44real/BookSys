package com.tem.booksys.mapper;

import com.tem.booksys.entity.UserAchievement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserAchievementMapper {

    @Select("SELECT * FROM user_achievement WHERE user_id=#{userId} ORDER BY achieve_time DESC")
    List<UserAchievement> findByUserId(@Param("userId") Integer userId);

    @Insert("INSERT INTO user_achievement(user_id, achievement_code, achievement_name, achievement_desc, achieve_time) " +
            "VALUES(#{userId}, #{code}, #{name}, #{desc}, NOW())")
    void add(@Param("userId") Integer userId, @Param("code") String code,
             @Param("name") String name, @Param("desc") String desc);

    @Select("SELECT count(*) FROM user_achievement WHERE user_id=#{userId} AND achievement_code=#{code}")
    Integer checkExists(@Param("userId") Integer userId, @Param("code") String code);

    @Select("SELECT ua.*, u.username FROM user_achievement ua LEFT JOIN user u ON ua.user_id=u.id " +
            "ORDER BY ua.achieve_time DESC LIMIT 100")
    List<UserAchievement> findRecent();
}
