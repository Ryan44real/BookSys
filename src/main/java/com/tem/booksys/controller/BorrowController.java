package com.tem.booksys.controller;

import com.tem.booksys.entity.ApplyRecord;
import com.tem.booksys.entity.Article;
import com.tem.booksys.entity.BorrowRecord;
import com.tem.booksys.entity.PageBean;
import com.tem.booksys.entity.Result;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.mapper.UserMapper;
import com.tem.booksys.service.BorrowService;
import com.tem.booksys.mapper.ReservationMapper;
import com.tem.booksys.service.CreditService;
import com.tem.booksys.service.DashboardService;
import com.tem.booksys.utils.BookLockManager;
import com.tem.booksys.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.concurrent.locks.ReentrantLock;

@Tag(name = "借阅管理", description = "图书借阅、归还、续借申请与审批等接口")
@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private BookLockManager bookLockManager;
    @Autowired
    private CreditService creditService;
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private DashboardService dashboardService;

//    借书功能
    @Operation(summary = "借阅图书", description = "借阅指定图书，验证用户状态/信用额度/逾期情况，使用per-book锁")
    @GetMapping("/borrowBook")
    public Result borrowBook(@RequestParam Integer bookId,@RequestParam Integer day) throws ParseException {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");

        String bookid = String.valueOf(bookId);

        // 1. 用户状态校验
        Integer userState = (Integer) map.get("userState");
        if (userState == 2){
            return Result.error("该用户当前不允许借阅");
        }

        // 2. 信用分校验：获取借阅额度上限
        Article article = bookMapper.findByBookNum(bookid);
        int creditScore = userMapper.getCreditScore(userId);
        int borrowLimit = creditService.getBorrowLimit(creditScore);
        String creditTier = creditService.getCreditTier(creditScore);
        if (borrowLimit == 0) {
            return Result.error("您的信用分(" + creditScore + ")过低，账户已冻结，请联系管理员");
        }

        // 3. 逾期检查 + 借阅数量检查
        List<BorrowRecord> records = borrowService.findOverdueOrMax(userId);
        for (int i = 0; i < records.size(); i++){
            if (records.get(i).getBorrowState() == 3) return Result.error("存在逾期书籍，请先归还");
        }
        if (records.size() >= borrowLimit) {
            return Result.error("借阅已达上限(" + borrowLimit + "本)，当前信用等级：" + creditTier + "(" + creditScore + "分)");
        }

        // 4. 预约锁定检查：只有预约人本人可借
        if ("预约锁定".equals(article.getState())) {
            var firstReservation = reservationMapper.getFirstInQueue(bookId);
            if (firstReservation == null || !firstReservation.getUserId().equals(userId)) {
                return Result.error("该图书已被预约锁定，仅预约人可借阅");
            }
            // 预约人取书后清除预约记录
            reservationMapper.updateStatus(firstReservation.getId(), 4);
        }

        // 5. 使用per-book细粒度锁，保证同一本书的并发安全
        ReentrantLock lock = bookLockManager.getLock(bookid);
        lock.lock();
        try {
            String resState = bookMapper.findByBookNum(bookid).getState();
            if ("已借出".equals(resState)){
                return Result.error("该书籍已经借出");
            }
            borrowService.borrowBook(bookId,day);
        } finally {
            lock.unlock();
        }


        return Result.success();
    }

    @Autowired
    private UserMapper userMapper;
    //我的借阅记录，和，借阅记录管理
    @Operation(summary = "获取借阅记录", description = "分页查询借阅记录，管理员可查全部，用户仅看自己的")
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

    @Operation(summary = "归还图书")
    @PostMapping("/returnBook")
    public Result returnBook(@RequestParam(value = "userId",required = false)Integer userId,@RequestParam("bookNum")String bookNum){
        System.out.println("还书"+userId+bookNum);
        borrowService.returnBook(userId,bookNum);
        // 异步触发成就检查
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer currentUserId = (map != null) ? (Integer) map.get("id") : userId;
        if (currentUserId != null) {
            dashboardService.checkAndGrantAchievements(currentUserId);
        }
        return Result.success();
    }


    @Autowired
    private BorrowMapper borrowMapper;
    @Operation(summary = "申请续借", description = "对已借阅的图书申请延长借阅期限")
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

    @Operation(summary = "获取续借申请列表")
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
    @Operation(summary = "批准续借", description = "管理员批准续借申请，更新归还日期")
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
    @Operation(summary = "拒绝续借", description = "管理员拒绝续借申请")
    @GetMapping("/rejectApply")
    public Result rejectApply(String id){
        borrowMapper.updateApply(Integer.valueOf(id),3);
        String msg = userMapper.getMsg(id);
        if (msg == null) msg="";
        msg = msg + "请注意，你的续借申请已经被驳回，请联系管理员";
        userMapper.updateMsg(msg,id);
        return Result.success();
    }
    @Operation(summary = "编辑借阅记录", description = "管理员修改借阅记录")
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
    @Operation(summary = "催还提醒", description = "发送邮件提醒用户归还即将逾期图书")
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
    @Operation(summary = "获取借阅总数")
    @GetMapping("/getAllBorrowNum")
    public Result getBorrowNum(){
        Integer res = borrowMapper.getAllBorrowNum();
        return Result.success(res);
    }

    @Operation(summary = "获取我的借阅数量")
    @GetMapping("/getMyRecordNumService")
    public Result getMyRecordNumService(){
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        Integer res = borrowMapper.getMyRecordNum(userId);
        return Result.success(res);
    }
}
