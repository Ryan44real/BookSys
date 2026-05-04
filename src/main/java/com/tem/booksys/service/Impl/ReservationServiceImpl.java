package com.tem.booksys.service.Impl;

import com.tem.booksys.entity.ReservationRecord;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.mapper.ReservationMapper;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.CreditService;
import com.tem.booksys.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CreditService creditService;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void addReservation(Integer userId, Integer bookNum) {
        int creditScore = userMapper.getCreditScore(userId);
        if (!creditService.canReserve(creditScore)) {
            throw new RuntimeException("您的信用分(" + creditScore + ")不足60分，无法使用预约功能");
        }
        if (reservationMapper.checkDuplicate(userId, bookNum) > 0) {
            throw new RuntimeException("您已预约过该图书，请勿重复预约");
        }
        reservationMapper.add(userId, bookNum);
    }

    @Override
    public List<ReservationRecord> getMyQueue(Integer userId) {
        List<ReservationRecord> list = reservationMapper.getMyQueue(userId);
        // 计算每个预约的队列位置
        for (ReservationRecord r : list) {
            List<ReservationRecord> bookQueue = reservationMapper.getQueueByBook(r.getBookNum());
            int pos = 0;
            for (int i = 0; i < bookQueue.size(); i++) {
                if (bookQueue.get(i).getId().equals(r.getId())) {
                    pos = i + 1;
                    break;
                }
            }
            r.setQueuePosition(pos);
        }
        return list;
    }

    @Override
    public List<ReservationRecord> getBookQueue(Integer bookNum) {
        return reservationMapper.getQueueByBook(bookNum);
    }

    @Override
    public ReservationRecord notifyFirstInQueue(Integer bookNum) {
        ReservationRecord first = reservationMapper.getFirstInQueue(bookNum);
        if (first == null) return null;

        // 更新状态为"已通知"
        reservationMapper.updateStatus(first.getId(), 2);
        first.setStatus(2);

        // 给排队第一的读者发邮件
        String email = userMapper.getMail(String.valueOf(first.getUserId()));
        String bookTitle = bookMapper.findByBookNum(String.valueOf(bookNum)).getTitle();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("【BookSys】您预约的图书《" + bookTitle + "》已到馆");
            message.setText("您好！您预约的《" + bookTitle + "》现已到馆。请于24小时内到馆办理借阅，逾期名额将自动顺延至下一位读者。");
            message.setTo(email);
            message.setFrom("lsj18938740943@163.com");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("预约通知邮件发送失败: reservationId={}, email={}", first.getId(), email, e);
            // 邮件失败不阻塞流程，图书仍处于锁定状态
        }
        return first;
    }

    @Override
    public void processExpiredReservations() {
        List<ReservationRecord> expired = reservationMapper.findExpired();
        for (ReservationRecord r : expired) {
            // 标记为失效
            reservationMapper.updateStatus(r.getId(), 3);
            // 扣除信用分（幂等性保证）
            creditService.deductReservationNoShow(r.getUserId(), r.getId());
            // 顺延：通知下一位
            notifyFirstInQueue(r.getBookNum());
        }
        if (!expired.isEmpty()) {
            log.info("处理过期预约 {} 条", expired.size());
        }
    }

    @Override
    public List<ReservationRecord> getAllReservations() {
        return reservationMapper.getAllReservations();
    }
}
