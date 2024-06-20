package com.tem.booksys.entiy;


import com.tem.booksys.anno.State;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
@Data
public class Article {
    private Integer id;//主键ID

    @NotEmpty
    @Pattern(regexp = "^\\S{1,10}$")
    private String title;//文章标题
    @NotEmpty
    private String content;//文章内容
    @NotEmpty
    private String coverImg;//封面图像
    @State
    //可以使用正则表达式判断
//    @Pattern(regexp = "^\\{已发布|草稿}")
    private String state;//发布状态 已发布|草稿
    @NotNull
    private Integer categoryId;//文章分类id
    private String location;
    private Integer bookNum;
    private Integer createUser;//创建人ID
    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//更新时间
    private String publisher;   //出版社publisher

    private String isbn;

    private String isbnPhoto;//isbn图像

}
