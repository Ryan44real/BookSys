package com.tem.booksys.service.Impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tem.booksys.entiy.Article;
import com.tem.booksys.entiy.PageBean;
import com.tem.booksys.mapper.BookMapper;
import com.tem.booksys.service.BookService;
import com.tem.booksys.utils.GetThreadLocal;
import com.tem.booksys.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    private GetThreadLocal getNowThreadLocal;
    @Override
    public void add(Article article) {
        //补充属性值
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        article.setCreateUser(userId);
        bookMapper.add(article);
    }

    @Override
    public PageBean<Article> list(Integer pageNum, Integer pageSize, String categoryId,String title,String state) {
//        1.创建PageBean对象
        PageBean<Article> pb = new PageBean<>();
//        2.开启分页查询 PageHelper
        PageHelper.startPage(pageNum,pageSize);
//        3.调用mapper,userId是因为当前用户只能访问自己的
        Map<String,Object> map = ThreadLocalUtil.get();
//        Integer userId = (Integer) map.get("id");
        List<Article> as = bookMapper.list(1,categoryId,title,state);
        //Page中提供了方法，可以获取PageHelper分页查询后得到的总记录数据和当前页数据
//       强转是因为，不能从一个父类对象中中取出子对象的属性,不用以下的方法用as强转为as也可以，idea会帮之处理
        Page<Article> p = (Page<Article>) as;
        //把数据填充到PageBean中
        pb.setTotal(((Page<Article>) as).getTotal());
        pb.setItems(((Page<Article>) as).getResult());
        return pb;
    }

    @Override
    public Article findById(String id) {


        return bookMapper.findById(id);
    }

    @Override
    public void update(Article article) {
        Map<String,Object> map = ThreadLocalUtil.get();
        article.setCreateUser((Integer) map.get("id"));
        article.setUpdateTime(LocalDateTime.now());
//        System.out.println(article);
        bookMapper.update(article);
    }

    @Override
    public void delete(String id) {
        bookMapper.delete(id);
    }

    @Override
    public Integer checkIsbn(String isbn) {
        return bookMapper.checkIsbn(isbn);
    }

    @Override
    public Integer getBookNum() {
        return bookMapper.getBookNum();
    }
}
