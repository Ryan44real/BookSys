package com.tem.booksys.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiCallLog {
    private Integer id;
    private Integer userId;
    private String model;           // 调用的模型名称
    private String operation;       // 操作类型: recommend / extractTags / genDescription
    private String inputSummary;    // 输入摘要（截断）
    private String outputSummary;   // 输出摘要（截断）
    private Integer status;         // 0=成功, 1=失败, 2=超时
    private Integer responseTimeMs; // 响应耗时(毫秒)
    private String errorMsg;        // 错误信息
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
