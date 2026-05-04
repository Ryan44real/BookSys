package com.tem.booksys.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tem.booksys.entity.ApplyRecord;
import com.tem.booksys.entity.Article;
import com.tem.booksys.entity.BorrowRecord;
import com.tem.booksys.entity.PageBean;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.BorrowService;
import com.tem.booksys.service.CreditService;
import com.tem.booksys.service.ReservationService;
import com.tem.booksys.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BorrowServiceImpl implements BorrowService {
    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CreditService creditService;

    @Autowired
    private ReservationService reservationService;

    @Override
    public PageBean<BorrowRecord> list(Integer pageNum, Integer pageSize, String bookName, String username,Integer state,Integer userId,Integer bookId) {
        Date date = new Date();
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userType = (Integer) map.get("userType");

        PageBean<BorrowRecord> pb = new PageBean<>();
        PageHelper.startPage(pageNum,pageSize);

        if (userType == 1){
            BorrowRecord record;
            List<BorrowRecord> res =  borrowMapper.borrowList(userId, bookId,state);
            for (int i = 0;i<= res.size()-1;i++){
                record = res.get(i);
                Article book = bookMapper.findByBookNum(String.valueOf(record.getBookNum()));
                record.setBookName(book.getTitle());
                record.setImageName(book.getCoverImg());
                record.setUserName(userMapper.findById(record.getUserId()));
                long lastday = record.getDeadline().getTime() - date.getTime();
                long diffday = lastday / (24*60*60*1000);
                record.setLastDay((int) diffday);
            }
            pb.setTotal(((Page<BorrowRecord>) res).getTotal());
            pb.setItems(((Page<BorrowRecord>) res).getResult());
        } else {
            BorrowRecord record;
            userId = (Integer) map.get("id");
            List<BorrowRecord> res =  borrowMapper.borrowList(userId, bookId,state);
            for (int i = 0;i<= res.size()-1;i++){
                record = res.get(i);
                Article book = bookMapper.findByBookNum(String.valueOf(record.getBookNum()));
                record.setBookName(book.getTitle());
                record.setImageName(book.getCoverImg());
                long lastday = record.getDeadline().getTime() - date.getTime();
                long diffday = lastday / (24*60*60*1000);
                record.setLastDay((int) diffday);
            }
            pb.setTotal(((Page<BorrowRecord>) res).getTotal());
            pb.setItems(((Page<BorrowRecord>) res).getResult());
        }
        return pb;
    }

    @Override
    public void borrowBook(Integer bookId, Integer day) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,day);
        Date res = calendar.getTime();

        borrowMapper.borrowBook(res,bookId,userId);
        bookMapper.updateOne(bookId,"已借出");
    }

    @Override
    public List<BorrowRecord> findOverdueOrMax(Integer userId) {
        return borrowMapper.findOverdueOrMax(userId);
    }

    @Override
    public void returnBook(Integer userId, String bookNum) {
        // 1. 先更新数据库：归还
        bookMapper.updateOne(Integer.valueOf(bookNum), "可借阅");
        int borrowState = getBorrowState(userId, Integer.valueOf(bookNum));
        borrowMapper.returnBook(Integer.valueOf(bookNum), userId);
        borrowMapper.editApply(Integer.valueOf(bookNum), userId);

        // 2. 信用分计算
        if (borrowState == 1) {
            creditService.addOnTimeReturn(userId, findBorrowRecordId(userId, Integer.valueOf(bookNum)));
        }

        // 3. 检查预约队列
        var first = reservationService.notifyFirstInQueue(Integer.valueOf(bookNum));
        if (first != null) {
            bookMapper.updateOne(Integer.valueOf(bookNum), "预约锁定");
        }
    }

    @Override
    public void returnBook(Integer userId, Integer bookNum) {
        returnBook(userId, String.valueOf(bookNum));
    }

    private int getBorrowState(Integer userId, Integer bookNum) {
        List<BorrowRecord> records = borrowMapper.borrowList(userId, bookNum, null);
        return records.isEmpty() ? 1 : records.get(0).getBorrowState();
    }

    private Integer findBorrowRecordId(Integer userId, Integer bookNum) {
        List<BorrowRecord> records = borrowMapper.borrowList(userId, bookNum, null);
        return records.isEmpty() ? null : records.get(0).getId();
    }

    @Override
    public void applyRenewal(String bookNum, Integer userId, Integer day, Integer borrowRecordId) {
        // unused overload
    }

    @Override
    public void applyRenewal(Integer bookNum, Integer userId, Integer day, Integer borrowRecordId) {
        borrowMapper.addApplyRenewal(bookNum,userId,day,borrowRecordId,1);
    }

    @Override
    public PageBean<ApplyRecord> getApplyRenewalList(Integer pageNum, Integer pageSize, String bookName, String username) {
        PageBean<ApplyRecord> pb = new PageBean<>();
        PageHelper.startPage(pageNum,pageSize);
        List<ApplyRecord> res = borrowMapper.getApplyRenewalList(bookName,username);
        Page<ApplyRecord> p = (Page<ApplyRecord>) res;
        pb.setTotal(((Page<ApplyRecord>) res).getTotal());
        pb.setItems(((Page<ApplyRecord>) res).getResult());
        return pb;
    }

    @Override
    public void passApply(Integer id, Integer borrowRecordId, Date day) {
        borrowMapper.updateApply(id,2);
        borrowMapper.updateDeadline(borrowRecordId,day);
    }
}
