package com.tem.booksys.controller;

import com.tem.booksys.entity.Result;
import com.tem.booksys.service.DashboardService;
import com.tem.booksys.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "个人面板", description = "阅读账单、成就系统、排行榜接口")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "个人阅读面板", description = "返回阅读足迹、借阅趋势、勋章、信用分等综合面板数据")
    @GetMapping("/my")
    public Result<Map<String, Object>> myDashboard() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        return Result.success(dashboardService.getDashboard(userId));
    }

    @Operation(summary = "我的勋章", description = "获取当前用户已获得的成就勋章列表")
    @GetMapping("/badges")
    public Result<Object> myBadges() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        return Result.success(dashboardService.getUserBadges(userId));
    }

    @Operation(summary = "月度阅读之星", description = "返回本月借阅数量排行前10的用户")
    @GetMapping("/leaderboard")
    public Result<Object> leaderboard() {
        return Result.success(dashboardService.getMonthlyLeaderboard());
    }
}
