package com.tem.booksys.service;

import com.tem.booksys.entiy.Article;
import com.tem.booksys.entiy.PageBean;

public interface BookService {
//    新增文章or书籍
    void add(Article article);
    //条件分页查询
    PageBean<Article> list(Integer pageNum, Integer pageSize, String categoryId,String title,String state);

    Article findById(String id);

    void update(Article article);

    void delete(String id);

    Integer checkIsbn(String isbn);

    Integer getBookNum();
}
