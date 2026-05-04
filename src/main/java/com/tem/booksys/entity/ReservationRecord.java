package com.tem.booksys.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationRecord {
    private Integer id;
    private Integer userId;
    private String username;
    private Integer bookNum;
    private String bookName;
    private String coverImg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservationTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime notifyTime;
    private Integer status;       // 1=排队中, 2=已通知, 3=已失效, 4=已取消
    private Integer queuePosition; // 队列位置（实时计算）
}
