package com.tem.booksys.service;

import java.util.Map;

public interface DashboardService {

    /** 获取个人阅读面板数据 */
    Map<String, Object> getDashboard(Integer userId);

    /** 检查并授予成就（异步调用） */
    void checkAndGrantAchievements(Integer userId);

    /** 获取用户勋章列表 */
    Object getUserBadges(Integer userId);

    /** 获取月度阅读之星排行 */
    Object getMonthlyLeaderboard();
}
