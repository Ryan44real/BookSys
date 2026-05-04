package com.tem.booksys.service;

import com.tem.booksys.entity.ReservationRecord;

import java.util.List;

public interface ReservationService {

    /** 提交预约 */
    void addReservation(Integer userId, Integer bookNum);

    /** 我的预约队列 */
    List<ReservationRecord> getMyQueue(Integer userId);

    /** 获取某本书的预约队列 */
    List<ReservationRecord> getBookQueue(Integer bookNum);

    /** 归还时：通知排队第一人并锁定图书 */
    ReservationRecord notifyFirstInQueue(Integer bookNum);

    /** 定时任务：使过期预约失效并传递给下一位 */
    void processExpiredReservations();

    /** 获取所有预约记录（管理员） */
    List<ReservationRecord> getAllReservations();
}
