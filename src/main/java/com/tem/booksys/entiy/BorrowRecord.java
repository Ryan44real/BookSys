package com.tem.booksys.entiy;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class BorrowRecord {
    //记录主键id
    private Integer id;
    //借阅时间
//    @JsonFormat(pattern="yyyy-MM-dd”,timezone=“GMT+8”)
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date borrowDate;
    //归还日期
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date deadline;
    //借阅状态
    private Integer borrowState;
    //借书人id
    private Integer userId;

    private Integer lastDay;
    //图书id
    private String bookNum;
    private String bookName;
    /**
     * 借书人用户名
     */
    private String userName;
    /**
     * 借阅的书籍图片
     */
    private String imageName;
}
