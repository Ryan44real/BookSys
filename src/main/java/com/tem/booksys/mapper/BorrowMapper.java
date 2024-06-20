package com.tem.booksys.mapper;

import com.tem.booksys.entiy.ApplyRecord;
import com.tem.booksys.entiy.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

@Mapper
public interface BorrowMapper {

    void borrowBook(@Param("deadline")Date res,@Param("bookId") Integer bookId,@Param("userId") Integer userId);

    List<BorrowRecord> findOverdueOrMax(Integer userId);

    List<BorrowRecord> borrowList(@Param("userId") Integer userId,@Param("bookId") Integer bookId,@Param("state") Integer state);


    void returnBook(@Param("bookNum") Integer bookNum,@Param("userId") Integer userId);

    void addApplyRenewal(@Param("bookNum") Integer bookNum, @Param("userId") Integer userId,@Param("day") Integer day,
                         @Param("borrowRecordId") Integer borrowRecordId,@Param("state")Integer state);

    List<ApplyRecord> getApplyRenewalList(String bookName, String username);

    @Select("select id from applyrecord where borrowRecordId=#{borrowRecordId}")
    Integer checkApply(Integer borrowRecordId);

    @Update("update applyrecord set applyState = #{arg1} where id=#{arg0}")
    void updateApply(Integer id,Integer state);

    @Update("update borrow_record set deadline = #{arg1} where id=#{arg0}")
    void updateDeadline(Integer borrowRecordId, Date day);

    @Update("update borrow_record set borrowDate=#{arg2},deadline=#{arg3},borrowState=#{arg1} where id=#{arg0}")
    void editReord(Integer borrowId, Integer state, Date date1, Date date2);

    @Select("select  * from borrow_record where borrowState=1")
    List<BorrowRecord> All();
    @Update("update borrow_record set borrowState=3 where id=#{id}")
    void changeState(Integer id);

    @Select("select count(*) from borrow_record")
    Integer getAllBorrowNum();
    @Select("select count(*) from borrow_record where userId=#{userId}")
    Integer getMyRecordNum(Integer userId);

    @Update("update applyrecord set applyState=3 where bookNum=#{arg0}")
    void editApply(Integer integer, Integer userId);
}
