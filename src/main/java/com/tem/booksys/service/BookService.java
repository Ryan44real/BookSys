package com.tem.booksys.service;

import com.tem.booksys.entity.Article;
import com.tem.booksys.entity.PageBean;

import java.util.List;

public interface BookService {
    void add(Article article);

    PageBean<Article> list(Integer pageNum, Integer pageSize, String categoryId, String title, String state,
                           String tag, List<String> tagList);

    Article findById(String id);

    void update(Article article);

    void delete(String id);

    Integer checkIsbn(String isbn);

    Integer getBookNum();
}
