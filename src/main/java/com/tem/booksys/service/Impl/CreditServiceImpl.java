package com.tem.booksys.service.Impl;

import com.tem.booksys.entity.CreditLog;
import com.tem.booksys.mapper.CreditLogMapper;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditServiceImpl implements CreditService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CreditLogMapper creditLogMapper;

    @Override
    public int getBorrowLimit(int creditScore) {
        if (creditScore > 120)  return 10;
        if (creditScore >= 80)  return 6;
        if (creditScore >= 60)  return 2;
        return 0;
    }

    @Override
    public boolean canReserve(int creditScore) {
        return creditScore >= 60;
    }

    @Override
    public String getCreditTier(int creditScore) {
        if (creditScore > 120)  return "资深读者";
        if (creditScore >= 80)  return "普通读者";
        if (creditScore >= 60)  return "受限读者";
        return "封禁名单";
    }

    @Override
    public void addOnTimeReturn(Integer userId, Integer borrowRecordId) {
        if (creditLogMapper.checkDuplicate(borrowRecordId, "按时归还") > 0) return;
        userMapper.updateCreditScore(2, userId);
        creditLogMapper.add(userId, 2, "按时归还", borrowRecordId);
    }

    @Override
    public void deductOverdue(Integer userId, int overdueDays, Integer borrowRecordId) {
        int penalty = -5 * overdueDays;
        userMapper.updateCreditScore(penalty, userId);
        creditLogMapper.add(userId, penalty, "逾期归还(" + overdueDays + "天)", borrowRecordId);
    }

    @Override
    public void deductReservationNoShow(Integer userId, Integer reservationId) {
        if (creditLogMapper.checkDuplicate(reservationId, "预约未取") > 0) return;
        userMapper.updateCreditScore(-10, userId);
        creditLogMapper.add(userId, -10, "预约未取", reservationId);
    }

    @Override
    public void addReview(Integer userId, Integer articleId) {
        if (creditLogMapper.checkDuplicate(articleId, "书评奖励") > 0) return;
        userMapper.updateCreditScore(1, userId);
        creditLogMapper.add(userId, 1, "书评奖励", articleId);
    }

    @Override
    public List<CreditLog> getCreditHistory(Integer userId) {
        return creditLogMapper.findByUserId(userId);
    }
}
