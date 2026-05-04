package com.tem.booksys.controller;

import com.tem.booksys.entity.Comment;
import com.tem.booksys.entity.Result;
import com.tem.booksys.service.CommentService;
import com.tem.booksys.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "评论管理", description = "图书评分与短评接口")
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(summary = "提交评论", description = "归还图书后可评价，1-5星+短评内容。字数>20奖励信用分。不可重复评价。")
    @PostMapping("/add")
    public Result add(
            @Parameter(description = "图书编号") @RequestParam Integer bookNum,
            @Parameter(description = "评分 1-5") @RequestParam Integer rating,
            @Parameter(description = "评论内容") @RequestParam String content) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        try {
            commentService.addComment(userId, bookNum, rating, content);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取图书评论", description = "查看某本图书的所有已通过评论")
    @GetMapping("/book")
    public Result<List<Comment>> bookComments(@Parameter(description = "图书编号") @RequestParam Integer bookNum) {
        return Result.success(commentService.getBookComments(bookNum));
    }

    @Operation(summary = "获取图书评分", description = "返回该书的平均评分和评论数")
    @GetMapping("/rating")
    public Result<Map<String, Object>> bookRating(@Parameter(description = "图书编号") @RequestParam Integer bookNum) {
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("avgRating", commentService.getAvgRating(bookNum));
        data.put("commentCount", commentService.getCommentCount(bookNum));
        return Result.success(data);
    }

    @Operation(summary = "我的评论")
    @GetMapping("/my")
    public Result<List<Comment>> myComments() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        return Result.success(commentService.getUserComments(userId));
    }

    @Operation(summary = "审核评论（管理员）", description = "status: 1=通过, 2=拒绝")
    @PutMapping("/review")
    public Result review(
            @Parameter(description = "评论ID") @RequestParam Long id,
            @Parameter(description = "状态: 1=通过, 2=拒绝") @RequestParam Integer status) {
        commentService.reviewComment(id, status);
        return Result.success();
    }

    @Operation(summary = "待审核评论（管理员）")
    @GetMapping("/pending")
    public Result<List<Comment>> pending() {
        return Result.success(commentService.getPendingComments());
    }
}
