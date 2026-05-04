package com.tem.booksys.mapper;

import com.tem.booksys.entity.CreditLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CreditLogMapper {

    @Insert("INSERT INTO credit_log(userId, change_amount, reason, create_time, related_id) " +
            "VALUES(#{userId}, #{changeAmount}, #{reason}, NOW(), #{relatedId})")
    void add(@Param("userId") Integer userId, @Param("changeAmount") Integer changeAmount,
             @Param("reason") String reason, @Param("relatedId") Integer relatedId);

    @Select("SELECT * FROM credit_log WHERE userId=#{userId} ORDER BY create_time DESC")
    List<CreditLog> findByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM credit_log ORDER BY create_time DESC")
    List<CreditLog> findAll();

    @Select("SELECT count(*) FROM credit_log WHERE related_id=#{relatedId} AND reason=#{reason}")
    Integer checkDuplicate(@Param("relatedId") Integer relatedId, @Param("reason") String reason);
}
