package com.tem.booksys.service;

import com.tem.booksys.entity.CreditLog;

import java.util.List;

public interface CreditService {

    /** 根据信用分获取借阅额度 */
    int getBorrowLimit(int creditScore);

    /** 根据信用分判断是否可预约 */
    boolean canReserve(int creditScore);

    /** 获取信用等级描述 */
    String getCreditTier(int creditScore);

    /** 加分（按时归还） */
    void addOnTimeReturn(Integer userId, Integer borrowRecordId);

    /** 减分（逾期，每天-5） */
    void deductOverdue(Integer userId, int overdueDays, Integer borrowRecordId);

    /** 减分（预约未取） */
    void deductReservationNoShow(Integer userId, Integer reservationId);

    /** 加分（书评通过） */
    void addReview(Integer userId, Integer articleId);

    /** 查询信用变动记录 */
    List<CreditLog> getCreditHistory(Integer userId);
}
