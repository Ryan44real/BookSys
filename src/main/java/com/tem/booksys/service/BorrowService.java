package com.tem.booksys.service;

import com.tem.booksys.entiy.ApplyRecord;
import com.tem.booksys.entiy.BorrowRecord;
import com.tem.booksys.entiy.PageBean;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public interface BorrowService {


    public PageBean<BorrowRecord> list(Integer pageNum, Integer pageSize, String bookName, String username,Integer state,Integer userId,Integer bookId);

    public void borrowBook(Integer bookId, Integer day) throws ParseException;

    List<BorrowRecord> findOverdueOrMax(Integer userId);

    void returnBook(Integer userId, String bookNum);


    void applyRenewal(String bookNum, Integer userId, Integer day, Integer borrowRecordId);

    void returnBook(Integer userId, Integer bookNum);

    void applyRenewal(Integer bookNum, Integer userId, Integer day, Integer borrowRecordId);

    PageBean<ApplyRecord> getApplyRenewalList(Integer pageNum, Integer pageSize, String bookName, String username);


    void passApply(Integer id, Integer borrowRecordId, Date day);
}
