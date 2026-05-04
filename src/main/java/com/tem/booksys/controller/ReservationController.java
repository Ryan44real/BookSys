package com.tem.booksys.controller;

import com.tem.booksys.entity.PageBean;
import com.tem.booksys.entity.ReservationRecord;
import com.tem.booksys.entity.Result;
import com.tem.booksys.entity.User;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.service.ReservationService;
import com.tem.booksys.service.UserService;
import com.tem.booksys.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "预约管理", description = "图书预约排队相关接口")
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BookMapper bookMapper;

    @Operation(summary = "提交预约申请", description = "当图书库存为0时可预约，信用分需≥60")
    @PostMapping("/add")
    public Result add(@RequestParam Integer bookNum) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        // 校验库存是否为0
        var article = bookMapper.findByBookNum(String.valueOf(bookNum));
        if (article == null) return Result.error("图书不存在");
        if (!"已借出".equals(article.getState()) && !"预约锁定".equals(article.getState())) {
            return Result.error("该图书当前可借阅，无需预约，直接借阅即可");
        }

        try {
            reservationService.addReservation(userId, bookNum);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "我的预约队列", description = "查看当前用户的预约排队状态和位置")
    @GetMapping("/myQueue")
    public Result<List<ReservationRecord>> myQueue() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        List<ReservationRecord> list = reservationService.getMyQueue(userId);
        return Result.success(list);
    }

    @Operation(summary = "获取图书预约队列", description = "管理员查看某图书的排队情况")
    @GetMapping("/bookQueue")
    public Result<List<ReservationRecord>> bookQueue(@Parameter(description = "图书编号") @RequestParam Integer bookNum) {
        List<ReservationRecord> list = reservationService.getBookQueue(bookNum);
        return Result.success(list);
    }

    @Operation(summary = "获取全部预约记录", description = "管理员查看所有预约")
    @GetMapping("/all")
    public Result<List<ReservationRecord>> all() {
        List<ReservationRecord> list = reservationService.getAllReservations();
        return Result.success(list);
    }
}
