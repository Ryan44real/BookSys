package com.tem.booksys.mapper;

import com.tem.booksys.entity.ReservationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReservationMapper {

    @Insert("INSERT INTO reservation_record(userId, bookNum, status, reservation_time) VALUES(#{userId}, #{bookNum}, 1, NOW())")
    void add(@Param("userId") Integer userId, @Param("bookNum") Integer bookNum);

    @Select("SELECT count(*) FROM reservation_record WHERE userId=#{userId} AND bookNum=#{bookNum} AND status=1")
    Integer checkDuplicate(@Param("userId") Integer userId, @Param("bookNum") Integer bookNum);

    @Select("SELECT * FROM reservation_record WHERE bookNum=#{bookNum} AND status=1 ORDER BY reservation_time ASC")
    List<ReservationRecord> getQueueByBook(@Param("bookNum") Integer bookNum);

    @Select("SELECT r.*, u.username, a.title AS bookName, a.cover_img AS coverImg " +
            "FROM reservation_record r " +
            "LEFT JOIN user u ON r.userId=u.id " +
            "LEFT JOIN article a ON r.bookNum=a.bookNum " +
            "WHERE r.userId=#{userId} AND r.status IN (1,2) " +
            "ORDER BY r.reservation_time ASC")
    List<ReservationRecord> getMyQueue(@Param("userId") Integer userId);

    @Select("SELECT r.*, u.username, a.title AS bookName, a.cover_img AS coverImg " +
            "FROM reservation_record r " +
            "LEFT JOIN user u ON r.userId=u.id " +
            "LEFT JOIN article a ON r.bookNum=a.bookNum " +
            "ORDER BY r.reservation_time DESC")
    List<ReservationRecord> getAllReservations();

    @Select("SELECT * FROM reservation_record WHERE id=#{id}")
    ReservationRecord findById(@Param("id") Integer id);

    @Update("UPDATE reservation_record SET status=#{status}, notify_time=NOW() WHERE id=#{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    @Update("UPDATE reservation_record SET status=3 WHERE status=2 AND notify_time < DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    int expireOverdue();

    @Select("SELECT * FROM reservation_record WHERE status=2 AND notify_time < DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    List<ReservationRecord> findExpired();

    @Select("SELECT * FROM reservation_record WHERE bookNum=#{bookNum} AND status=1 ORDER BY reservation_time ASC LIMIT 1")
    ReservationRecord getFirstInQueue(@Param("bookNum") Integer bookNum);

    @Select("SELECT count(*) FROM reservation_record WHERE userId=#{userId} AND bookNum=#{bookNum} AND status=2 AND notify_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    Integer countNotifiedIn24h(@Param("userId") Integer userId, @Param("bookNum") Integer bookNum);

    @Select("SELECT * FROM reservation_record WHERE status=1")
    List<ReservationRecord> getAllQueuing();

    @Select("SELECT r.*, u.username FROM reservation_record r LEFT JOIN user u ON r.userId=u.id WHERE r.status=2 AND r.notify_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    List<ReservationRecord> getActiveNotified();
}
