package com.tem.booksys.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tem.booksys.entiy.ApplyRecord;
import com.tem.booksys.entiy.Article;
import com.tem.booksys.entiy.BorrowRecord;
import com.tem.booksys.entiy.PageBean;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.BorrowService;
import com.tem.booksys.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
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
    @Override
    public PageBean<BorrowRecord> list(Integer pageNum, Integer pageSize, String bookName, String username,Integer state,Integer userId,Integer bookId) {
        //3.调用mapper,userId是因为当前用户只能访问自己的文章
        Date date = new Date();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Map<String,Object> map = ThreadLocalUtil.get();

        Integer userType = (Integer) map.get("userType");

        //1.创建PageBean对象
        PageBean<BorrowRecord> pb = new PageBean<>();
        //2.开启分页查询 PageHelper
        PageHelper.startPage(pageNum,pageSize);

        //管理员获取借阅记录
        if (userType == 1){
            BorrowRecord record = new BorrowRecord();
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
            Page<BorrowRecord> p = (Page<BorrowRecord>) res;
//            把数据填充到PageBean中
            pb.setTotal(((Page<BorrowRecord>) res).getTotal());
            pb.setItems(((Page<BorrowRecord>) res).getResult());
            return pb;
        }else {
            //用户
            BorrowRecord record = new BorrowRecord();
            userId = (Integer) map.get("id");
            List<BorrowRecord> res =  borrowMapper.borrowList(userId, bookId,state);
            for (int i = 0;i<= res.size()-1;i++){
                record = res.get(i);
                Article book = bookMapper.findByBookNum(String.valueOf(record.getBookNum()));
                record.setBookName(book.getTitle());
                record.setImageName(book.getCoverImg());
                long lastday = record.getDeadline().getTime() - date.getTime();
                long diffday = lastday / (24*60*60*1000);
                System.out.println(diffday);
                record.setLastDay((int) diffday);

            }
            Page<BorrowRecord> p = (Page<BorrowRecord>) res;
//            把数据填充到PageBean中
            pb.setTotal(((Page<BorrowRecord>) res).getTotal());
            pb.setItems(((Page<BorrowRecord>) res).getResult());
            return pb;
        }
    }



    @Override
    public void borrowBook(Integer bookId, Integer day) throws ParseException {
        //获取当前线程的用户Id
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        //计算归还日期
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,day);
        //将日期存入
        Date res = calendar.getTime();
        borrowMapper.borrowBook(res,bookId,userId);
        bookMapper.updateOne(bookId,"已借出");
//        bookMapper.update(article); update的动态sql写法？

    }

    @Override
    public List<BorrowRecord> findOverdueOrMax(Integer userId) {
        return borrowMapper.findOverdueOrMax(userId);
    }

    @Override
    public void returnBook(Integer userId, String bookNum) {
        bookMapper.updateOne(Integer.valueOf(bookNum),"可借阅");
        borrowMapper.returnBook(Integer.valueOf(bookNum),userId);
        borrowMapper.editApply(Integer.valueOf(bookNum),userId);
    }

    @Override
    public void applyRenewal(String bookNum, Integer userId, Integer day, Integer borrowRecordId) {

    }

    @Override
    public void returnBook(Integer userId, Integer bookNum) {
        bookMapper.updateOne(bookNum,"可借阅");
        borrowMapper.returnBook(bookNum,userId);
    }

    @Override
    public void applyRenewal(Integer bookNum, Integer userId, Integer day, Integer borrowRecordId) {
        borrowMapper.addApplyRenewal(bookNum,userId,day,borrowRecordId,1);
    }

    //续借申请的list
    @Override
    public PageBean<ApplyRecord> getApplyRenewalList(Integer pageNum, Integer pageSize, String bookName, String username) {
        //1.创建PageBean对象
        PageBean<ApplyRecord> pb = new PageBean<>();
        //2.开启分页查询 PageHelper
        PageHelper.startPage(pageNum,pageSize);
        List<ApplyRecord> res = borrowMapper.getApplyRenewalList(bookName,username);
        Page<ApplyRecord> p = (Page<ApplyRecord>) res;
//            把数据填充到PageBean中
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
