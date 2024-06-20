package com.tem.booksys.mapper;

import com.tem.booksys.entiy.Article;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookMapper {
    void add(Article article);

    List<Article> list(@Param("userId") Integer userId, @Param("categoryId")String categoryId,@Param("title")String title, @Param("state")String state);
    @Select("select * from article where id=#{id}")
    Article findById(String id);

    void update(Article article);
    @Delete("delete from article where id=#{id}")
    void delete(String id);

    void updateOne(@Param("id")Integer id,@Param("state")String state);
    @Select("select bookNum from article where title = #{bookName}")
    Integer findByName(String bookName);
    @Select("select * from article where bookNum=#{bookid}")
    Article findByBookNum(String bookid);

    @Select("select count(*) from article where isbn=#{isbn}")
    Integer checkIsbn(String isbn);

    @Select("select count(*) from article")
    Integer getBookNum();

    @Select("select count(*) from article where state='可借阅  '")
    Integer getBookNumUse();
}
