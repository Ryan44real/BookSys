package com.tem.booksys.service;

import com.tem.booksys.entity.Comment;

import java.util.List;

public interface CommentService {

    /** 提交评论（需校验借阅记录） */
    Comment addComment(Integer userId, Integer bookNum, Integer rating, String content);

    /** 获取图书评论列表 */
    List<Comment> getBookComments(Integer bookNum);

    /** 获取图书平均评分 */
    Double getAvgRating(Integer bookNum);

    /** 获取图书评论数 */
    Integer getCommentCount(Integer bookNum);

    /** 获取用户评论 */
    List<Comment> getUserComments(Integer userId);

    /** 审核评论（管理员） */
    void reviewComment(Long id, Integer status);

    /** 获取待审核评论（管理员） */
    List<Comment> getPendingComments();
}
