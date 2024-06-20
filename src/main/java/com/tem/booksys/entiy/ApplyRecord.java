package com.tem.booksys.entiy;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ApplyRecord {
    //借阅记录号
    private Integer borrowRecordId;
    //主键id
    private Integer id;
    //书号
    private String bookNum;
    //书名
    private String title;
    private String coverImg;
    private String username;
    private Integer userId;
    //原始借阅时间
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date  borrowDate;
    //原定归还时间
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date  deadline;
    //要续借的天数
    private Integer dayNum;

    //申请的状态 1.申请中，2.拒绝，3.申请完成
    private Integer applyState;
    //申请信息
    private String msg;

}
