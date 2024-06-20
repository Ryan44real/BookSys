package com.tem.booksys;

import com.tem.booksys.controller.UserController;
import com.tem.booksys.entiy.BorrowRecord;
import com.tem.booksys.mapper.BorrowMapper;
import com.tem.booksys.service.BookService;
import com.tem.booksys.service.BorrowService;
import com.tem.booksys.utils.ChineseGPT;
import com.tem.booksys.utils.ThreadLocalUtil;
import org.codehaus.jettison.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
@SpringBootTest
public class DateAdd {

    @Autowired
    private BorrowMapper borrowMapper;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private UserController userController;
    @Test
    public void main() throws ParseException, JSONException, IOException {

        ChineseGPT.GptResult("法治的细节","9787222204331");
//        System.out.println(userController.sendMail("731995024@qq.com"));
    }
}
