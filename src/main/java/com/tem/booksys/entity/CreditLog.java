package com.tem.booksys.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreditLog {
    private Integer id;
    private Integer userId;
    private Integer changeAmount;  // 变动量（正加分、负减分）
    private String reason;        // 变动原因
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer relatedId;    // 关联记录ID（借阅/预约ID）
}
