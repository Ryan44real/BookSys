package com.tem.booksys.controller;

import com.tem.booksys.entiy.ApplyRecord;
import com.tem.booksys.entiy.BorrowRecord;
import com.tem.booksys.entiy.PageBean;
import com.tem.booksys.entiy.Result;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.BorrowService;
import com.tem.booksys.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BookMapper bookMapper;

//    借书功能
    @GetMapping("/borrowBook")
    public Result borrowBook(@RequestParam Integer bookId,@RequestParam Integer day) throws ParseException {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        String bookid = String.valueOf(bookId);

        //校验该用户是否可以进行借阅
        //1.用户状态为不可借阅2.达到书籍借阅数量上限，是否有逾期；
        //用户状态
        Integer userState = (Integer) map.get("userState");
        if (userState == 2){
            return Result.error("该用户当前不允许借阅");
        }
        //查找该用户当前是否有逾期书籍未归还，或者书籍借阅数量达到上限。

        //该方法的查询的结果是，不是在借阅中状态的数据
        List<BorrowRecord> records = borrowService.findOverdueOrMax(userId);
        for (int i = 0;i<=records.size()-1;i++){
            if (records.get(i).getBorrowState()==3) return Result.error("存在逾期书籍");
        }
        if (records.size()>=6) return Result.error("借阅上限了哥们");

        //先校验该书籍的状态，当不可借阅时直接返回不可借阅，在前端也要做校验。
        //加入线程锁，保证对同一本书进行并发访问时，保证第一个能借到
        synchronized (this){
            String resState = bookMapper.findByBookNum(bookid).getState();
            if (resState.equals("已借出")){
                return Result.error("该书籍已经借出");
            }
            borrowService.borrowBook(bookId,day);
        }


        return Result.success();
    }

    @Autowired
    private UserMapper userMapper;
    //我的借阅记录，和，借阅记录管理
    @GetMapping("/getRecord")
    public Result<PageBean<BorrowRecord>> list(Integer pageNum,
                                           Integer pageSize,
                                           @RequestParam(required = false,name = "bookName") String bookName,
                                           @RequestParam(required = false,name = "username") String username,
                                           @RequestParam(required = false,name = "state") Integer state)
    {
        Integer userId =null;
        Integer bookId = null;
        if (username != null){
            userId = userMapper.findByName(username);
            if (userId == null) return Result.error("查无此人");
        }else userId = null;
        if(bookName != null){
            bookId = bookMapper.findByName(bookName);
            if (bookId == null) return Result.error("没有这本书");
        }else bookId = null;

        PageBean<BorrowRecord> pb = borrowService.list(pageNum,pageSize,bookName,username,state,userId,bookId);
        return Result.success(pb);
    }

    @PostMapping("/returnBook")
    public Result returnBook(@RequestParam(value = "userId",required = false)Integer userId,@RequestParam("bookNum")String bookNum){
        System.out.println("还书"+userId+bookNum);
        borrowService.returnBook(userId,bookNum);
        return Result.success();
    }


    @Autowired
    private BorrowMapper borrowMapper;
    @GetMapping("/applyRenewal")
    public Result extendBorrow(@RequestParam("bookNum") Integer bookNum, @RequestParam("day")Integer day,
                               @RequestParam("borrowRecordId") Integer borrowRecordId){
        //1.获取当前申请用户的ID
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        Integer res =  borrowMapper.checkApply(borrowRecordId);
        if (res == null){
            borrowService.applyRenewal(bookNum,userId,day,borrowRecordId);
            return Result.success();
        }else{
            return Result.error("已经申请过啦");
        }
    }

    @GetMapping("/getApplyRenewalList")
    public Result<PageBean<ApplyRecord>> getApplyRenewalList(@RequestParam Integer pageNum,
                                                             @RequestParam Integer pageSize,
                                                             @RequestParam(required = false,name = "bookName") String bookName,
                                                             @RequestParam(required = false) String username
                                                             ){
        PageBean<ApplyRecord> pb = borrowService.getApplyRenewalList(pageNum,pageSize,bookName,username);
        return Result.success(pb);
    }

    /*
    * id 申请记录的Id
    * borrowRecordId 借阅记录的Id
    * day 续借的天数
    * deadline 原的归还日期
    * */
    @GetMapping("/passApply")
    public Result passApply(@RequestParam(value = "id") Integer id,
                            @RequestParam(name = "borrowRecordId") Integer borrowRecordId,
                            @RequestParam(name = "day") Integer day,
                            @RequestParam(name = "deadline") String deadline) throws ParseException {
        //计算归还日期
        Date date = new Date();
        date = new SimpleDateFormat("yyyy-MM-dd").parse(deadline);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH,day);
        Date dayNum = calendar.getTime();
        borrowService.passApply(id,borrowRecordId,dayNum);
        return Result.success();
    }
    //这里的id 是申请续借记录的id
    @GetMapping("/rejectApply")
    public Result rejectApply(String id){
        borrowMapper.updateApply(Integer.valueOf(id),3);
        String msg = userMapper.getMsg(id);
        if (msg == null) msg="";
        msg = msg + "请注意，你的续借申请已经被驳回，请联系管理员";
        userMapper.updateMsg(msg,id);
        return Result.success();
    }
    @PostMapping("/editRecord")
    public Result editRecord(Integer userId,Integer borrowId,Date date1,Date date2,Integer state,Integer bookNum){
        System.out.println(userId);
        System.out.println(borrowId);
        System.out.println(state);
        System.out.println(bookNum);
        Date current = new Date();
        Calendar calendar =Calendar.getInstance();
        calendar.setTime(current);
        int res = current.compareTo(date1);
        if (res < 0) //当前时间小于借书时间
            return Result.error("当前时间小于借书时间");
        int res2  = current.compareTo(date2);
        if (res2 > 0 && state==1)//当前时间大于还书时间且传来的状态为未归还
            return Result.error("当前时间大于还书时间且传来的状态为未归还");
        if (state == 2){
            borrowMapper.editReord(borrowId,state,date1,date2);
            bookMapper.updateOne(bookNum,"可借阅");
        }
        //date1 借书时间 date2还书时间
        borrowMapper.editReord(borrowId,state,date1,date2);
        return Result.success();
    }

    @Autowired
    private JavaMailSender sender;
    //催促
    @GetMapping("/urge")
    public Result urge(String id){
        String msg = userMapper.getMsg(id);
        if (msg == null) msg="";
        msg = msg + "你有书籍即将逾期，请及时处理；";
        userMapper.updateMsg(msg,id);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText("你有书籍即将逾期，请及时处理；");
        message.setSubject("书籍逾期提醒");
        String mail = userMapper.getMail(id);
        // 指定要接收邮件的用户邮箱账号
        message.setTo(mail);
        // 发送邮件的邮箱账号
        message.setFrom("lsj18938740943@163.com");
        sender.send(message);
        return Result.success();
    }
    //获得借书数量
    @GetMapping("/getAllBorrowNum")
    public Result getBorrowNum(){
        Integer res = borrowMapper.getAllBorrowNum();
        return Result.success(res);
    }

    @GetMapping("/getMyRecordNumService")
    public Result getMyRecordNumService(){
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        Integer res = borrowMapper.getMyRecordNum(userId);
        return Result.success(res);
    }
}
