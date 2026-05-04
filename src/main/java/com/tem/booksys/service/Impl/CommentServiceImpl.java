package com.tem.booksys.service.Impl;

import com.tem.booksys.entity.Comment;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.mapper.CommentMapper;
import com.tem.booksys.service.CommentService;
import com.tem.booksys.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private CreditService creditService;

    @Override
    public Comment addComment(Integer userId, Integer bookNum, Integer rating, String content) {
        // 防刷：必须有该书的归还记录
        var records = borrowMapper.borrowList(userId, bookNum, 2); // status=2 已归还
        if (records == null || records.isEmpty()) {
            throw new RuntimeException("您尚未借阅并归还此书，无法评价");
        }
        // 防止重复评价
        if (commentMapper.checkDuplicate(userId, bookNum) > 0) {
            throw new RuntimeException("您已评价过此书");
        }
        Comment c = new Comment();
        c.setUserId(userId);
        c.setBookNum(bookNum);
        c.setRating(rating);
        c.setContent(content);
        commentMapper.add(c);

        // 高质量评论奖励：字数 > 20
        if (content != null && content.length() > 20) {
            creditService.addReview(userId, bookNum);
        }
        return c;
    }

    @Override
    public List<Comment> getBookComments(Integer bookNum) {
        return commentMapper.findByBook(bookNum);
    }

    @Override
    public Double getAvgRating(Integer bookNum) {
        Double avg = commentMapper.getAvgRating(bookNum);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : null;
    }

    @Override
    public Integer getCommentCount(Integer bookNum) {
        return commentMapper.getCommentCount(bookNum);
    }

    @Override
    public List<Comment> getUserComments(Integer userId) {
        return commentMapper.findByUser(userId);
    }

    @Override
    public void reviewComment(Long id, Integer status) {
        commentMapper.updateStatus(id, status);
    }

    @Override
    public List<Comment> getPendingComments() {
        return commentMapper.findPending();
    }
}
