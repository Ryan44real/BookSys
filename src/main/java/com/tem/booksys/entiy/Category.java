package com.tem.booksys.entiy;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class Category {
    @NotNull(groups = Update.class)
    private Integer id;//主键ID
    @NotEmpty(message = "不能为空")
    private String categoryName;//分类名称
    @NotEmpty(message = "不能为空")
    private String categoryAlias;//分类别名
    private Integer createUser;//创建人ID
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;//更新时间


    //分组校验

    //若某个校验没有指定的分组，默认属于Default分组
    //分组可以继承 A extends B，A就有B所有的校验项
    public interface Add extends Default {

    }

    public  interface Update extends Default{

    }
}
