package com.tem.booksys.utils;

import com.tem.booksys.entity.BorrowRecord;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.DashboardService;
import com.tem.booksys.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class TaskTimeUtil {

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private JavaMailSender sender;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private DashboardService dashboardService;

    @Value("${spring.mail.username}")
    private String mailFrom;

    // 每日凌晨 1:10:30 检查逾期
    @Scheduled(cron = "30 10 1 * * ?")
    public void doTask(){
        Date date = new Date();
        List<BorrowRecord> borrowRecords = borrowMapper.All();
        for (BorrowRecord record : borrowRecords){
            long lastday = record.getDeadline().getTime() - date.getTime();
            long diffday = lastday / (24*60*60*1000);
            if (diffday == 0) borrowMapper.changeState(record.getId());
            if (diffday <= 2) {
                userMapper.updateMsg("您有书籍即将逾期，请尽快归还", String.valueOf(record.getUserId()));
                SimpleMailMessage message = new SimpleMailMessage();
                message.setText("你有书籍即将逾期，请及时处理；");
                message.setSubject("书籍逾期提醒");
                String mail = userMapper.getMail(String.valueOf(record.getUserId()));
                message.setTo(mail);
                message.setFrom(mailFrom);
                sender.send(message);
            }
        }
    }

    // 每小时检查预约过期（24h未取书）
    @Scheduled(cron = "0 0 * * * ?")
    public void processExpiredReservations() {
        reservationService.processExpiredReservations();
    }

    // 每月1号上午9:00发送上月阅读简报
    @Scheduled(cron = "0 0 9 1 * ?")
    public void sendMonthlyReport() {
        List<Integer> userIds = userMapper.getAllUserIds();
        for (Integer uid : userIds) {
            Map<String, Object> dashboard = dashboardService.getDashboard(uid);
            String email = userMapper.getMail(String.valueOf(uid));
            if (email == null) continue;
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setSubject("【BookSys】您上月的阅读简报");
                msg.setText("Hi！上月您共借阅了 " + dashboard.get("totalBorrowed") + " 本书。"
                        + "当前信用分：" + dashboard.get("creditScore") + "。"
                        + "登录系统查看更多阅读数据！");
                msg.setTo(email);
                msg.setFrom(mailFrom);
                sender.send(msg);
            } catch (Exception e) {
                // 单个用户邮件失败不阻塞其他用户
            }
        }
    }
}
