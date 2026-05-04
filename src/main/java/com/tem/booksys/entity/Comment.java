package com.tem.booksys.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Integer userId;
    private String username;    // 查询时填充
    private String userPic;     // 查询时填充
    private Integer bookNum;
    private String bookName;    // 查询时填充
    @Min(1) @Max(5)
    private Integer rating;     // 1-5 星
    @NotEmpty
    private String content;
    private Integer status;     // 0=待审核, 1=已通过, 2=已拒绝
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
