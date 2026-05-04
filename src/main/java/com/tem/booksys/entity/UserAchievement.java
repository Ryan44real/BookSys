package com.tem.booksys.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAchievement {
    private Integer id;
    private Integer userId;
    private String achievementCode;   // 成就编码: READER_NOVICE / COMPUTER_EXPERT / NIGHT_OWL / BOOK_WORM 等
    private String achievementName;   // 成就名称
    private String achievementDesc;   // 成就描述
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime achieveTime;
}
