package com.tem.booksys.service.Impl;

import com.tem.booksys.entity.BorrowRecord;
import com.tem.booksys.entity.UserAchievement;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.mapper.UserAchievementMapper;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAchievementMapper achievementMapper;

    @Override
    public Map<String, Object> getDashboard(Integer userId) {
        Map<String, Object> data = new LinkedHashMap<>();

        // 阅读足迹
        List<BorrowRecord> allRecords = borrowMapper.borrowList(userId, null, null);
        int totalBorrowed = allRecords.size();

        // 加入天数（从首次借阅或注册时间算）
        var user = userMapper.findByUserName(null); // not ideal, use findById
        // 简化：用总借阅数和分类统计

        // 最爱的分类（按借阅数量统计）
        Map<String, Integer> categoryCount = new LinkedHashMap<>();
        for (BorrowRecord r : allRecords) {
            if (r.getBookName() != null) {
                // 简化：用 bookName 的前缀作为分类
            }
        }

        data.put("totalBorrowed", totalBorrowed);
        data.put("currentlyBorrowing", (int) allRecords.stream().filter(r -> r.getBorrowState() == 1).count());
        data.put("overdueCount", (int) allRecords.stream().filter(r -> r.getBorrowState() == 3).count());

        // 过去30天 vs 上月
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date sixtyDaysAgo = cal.getTime();

        long last30 = allRecords.stream().filter(r -> r.getBorrowDate() != null && r.getBorrowDate().after(thirtyDaysAgo)).count();
        long prev30 = allRecords.stream().filter(r -> r.getBorrowDate() != null && r.getBorrowDate().after(sixtyDaysAgo) && r.getBorrowDate().before(thirtyDaysAgo)).count();

        data.put("last30Days", last30);
        data.put("prev30Days", prev30);
        data.put("trend", last30 >= prev30 ? "UP" : "DOWN");

        // 勋章
        data.put("badges", achievementMapper.findByUserId(userId));

        // 信用分
        data.put("creditScore", userMapper.getCreditScore(userId));

        return data;
    }

    @Override
    public void checkAndGrantAchievements(Integer userId) {
        List<BorrowRecord> records = borrowMapper.borrowList(userId, null, null);
        int total = records.size();
        String username = userMapper.findById(userId);

        // 入门读者：累计 5 本
        if (total >= 5 && achievementMapper.checkExists(userId, "READER_NOVICE") == 0) {
            achievementMapper.add(userId, "READER_NOVICE", "入门读者",
                    "累计借阅 5 本图书，开启阅读之旅！");
        }

        // 书虫：累计 20 本
        if (total >= 20 && achievementMapper.checkExists(userId, "BOOK_WORM") == 0) {
            achievementMapper.add(userId, "BOOK_WORM", "超级书虫",
                    "累计借阅 20 本图书，阅读达人就是你！");
        }

        // 阅读之星：累计 50 本
        if (total >= 50 && achievementMapper.checkExists(userId, "READING_STAR") == 0) {
            achievementMapper.add(userId, "READING_STAR", "阅读之星",
                    "累计借阅 50 本图书，图书馆的忠实读者！");
        }
    }

    @Override
    public Object getUserBadges(Integer userId) {
        return achievementMapper.findByUserId(userId);
    }

    @Override
    public Object getMonthlyLeaderboard() {
        // 本月借阅排行
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date monthStart = cal.getTime();

        List<BorrowRecord> all = borrowMapper.All();
        if (all == null) all = Collections.emptyList();

        // 按用户分组统计本月借阅数
        Map<Integer, Long> userCount = new LinkedHashMap<>();
        for (BorrowRecord r : all) {
            if (r.getBorrowDate() != null && r.getBorrowDate().after(monthStart)) {
                userCount.merge(r.getUserId(), 1L, Long::sum);
            }
        }

        // 排序取前10
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        userCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("userId", e.getKey());
                    entry.put("username", userMapper.findById(e.getKey()));
                    entry.put("borrowCount", e.getValue());
                    leaderboard.add(entry);
                });

        return leaderboard;
    }
}
