package com.tem.booksys.mapper;

import com.tem.booksys.entity.Article;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BookMapper {
    void add(Article article);

    List<Article> list(@Param("userId") Integer userId, @Param("categoryId")String categoryId,
                       @Param("title")String title, @Param("state")String state,
                       @Param("tag")String tag, @Param("tagList")List<String> tagList);
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

    @Select("SELECT DISTINCT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(tags, ',', n.n), ',', -1)) AS tag " +
            "FROM article JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) n " +
            "ON CHAR_LENGTH(tags) - CHAR_LENGTH(REPLACE(tags, ',', '')) >= n.n - 1 " +
            "WHERE tags IS NOT NULL AND tags != ''")
    List<String> getAllTags();

    @Update("UPDATE article SET tags=#{tags} WHERE bookNum=#{bookNum}")
    void updateTags(@Param("bookNum") Integer bookNum, @Param("tags") String tags);
}
