package com.tem.booksys.utils;

import com.tem.booksys.entiy.BorrowRecord;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class TaskTimeUtil {

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private JavaMailSender sender;


    @Autowired
    private UserMapper userMapper;
    @Scheduled(cron = "30 10 1 * * ?")// cron表达式：每周一 23:40:30 执行
//“30 10 1 * * ?” 每天1点10分30秒触发任务
    public void doTask(){
        Date date =new Date();
        List<BorrowRecord>  borrowRecords =borrowMapper.All();
        for (BorrowRecord record : borrowRecords){
            long lastday = record.getDeadline().getTime() - date.getTime();
            long diffday = lastday / (24*60*60*1000);
            //逾期
            if (diffday ==0) borrowMapper.changeState(record.getId());
            //即将逾期
            if (diffday <=2) {
                userMapper.updateMsg("您有书籍即将逾期，请尽快归还", String.valueOf(record.getUserId()));
                SimpleMailMessage message = new SimpleMailMessage();
                message.setText("你有书籍即将逾期，请及时处理；");
                message.setSubject("书籍逾期提醒");
                String mail = userMapper.getMail(String.valueOf(record.getUserId()));
                // 指定要接收邮件的用户邮箱账号
                message.setTo(mail);
                // 发送邮件的邮箱账号
                message.setFrom("lsj18938740943@163.com");
                sender.send(message);
            }
        }
    }
}
