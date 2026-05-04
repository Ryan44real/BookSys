package com.tem.booksys.mapper;

import com.tem.booksys.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO comment(user_id, book_num, rating, content, status, create_time) " +
            "VALUES(#{userId}, #{bookNum}, #{rating}, #{content}, 1, NOW())")
    void add(Comment comment);

    @Select("SELECT c.*, u.username, u.user_pic AS userPic, a.title AS bookName " +
            "FROM comment c LEFT JOIN user u ON c.user_id=u.id LEFT JOIN article a ON c.book_num=a.bookNum " +
            "WHERE c.book_num=#{bookNum} AND c.status=1 ORDER BY c.create_time DESC")
    List<Comment> findByBook(@Param("bookNum") Integer bookNum);

    @Select("SELECT c.*, u.username, a.title AS bookName " +
            "FROM comment c LEFT JOIN user u ON c.user_id=u.id LEFT JOIN article a ON c.book_num=a.bookNum " +
            "WHERE c.user_id=#{userId} ORDER BY c.create_time DESC")
    List<Comment> findByUser(@Param("userId") Integer userId);

    @Select("SELECT AVG(rating) FROM comment WHERE book_num=#{bookNum} AND status=1")
    Double getAvgRating(@Param("bookNum") Integer bookNum);

    @Select("SELECT count(*) FROM comment WHERE book_num=#{bookNum} AND status=1")
    Integer getCommentCount(@Param("bookNum") Integer bookNum);

    @Select("SELECT * FROM comment WHERE id=#{id}")
    Comment findById(@Param("id") Long id);

    @Update("UPDATE comment SET status=#{status} WHERE id=#{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM comment WHERE id=#{id}")
    void delete(@Param("id") Long id);

    @Select("SELECT * FROM comment WHERE status=0 ORDER BY create_time DESC")
    List<Comment> findPending();

    @Select("SELECT count(*) FROM comment WHERE user_id=#{userId} AND book_num=#{bookNum}")
    Integer checkDuplicate(@Param("userId") Integer userId, @Param("bookNum") Integer bookNum);
}
