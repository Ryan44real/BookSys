package com.tem.booksys.mapper;

import com.tem.booksys.entity.AiCallLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiCallLogMapper {

    @Insert("INSERT INTO ai_call_log(userId, model, operation, input_summary, output_summary, status, response_time_ms, error_msg, create_time) " +
            "VALUES(#{userId}, #{model}, #{operation}, #{inputSummary}, #{outputSummary}, #{status}, #{responseTimeMs}, #{errorMsg}, NOW())")
    void add(AiCallLog log);

    @Select("SELECT * FROM ai_call_log ORDER BY create_time DESC LIMIT 200")
    List<AiCallLog> findRecent();

    @Select("SELECT * FROM ai_call_log WHERE userId=#{userId} ORDER BY create_time DESC")
    List<AiCallLog> findByUserId(Integer userId);

    @Select("SELECT count(*) FROM ai_call_log WHERE status=0")
    Integer countSuccess();

    @Select("SELECT count(*) FROM ai_call_log WHERE status!=0")
    Integer countFailed();

    @Select("SELECT AVG(response_time_ms) FROM ai_call_log WHERE status=0")
    Double avgResponseTime();
}
