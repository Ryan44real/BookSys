package com.tem.booksys.service;

import com.tem.booksys.entiy.Category;

import java.util.List;

public interface CategoryService {
    //添加分类
    void add(Category category);
    //获取分类列表
    List<Category> list();
    //根据Id 获取分类详情
    Category findById(Integer id);
    void update(Category category);
    void delete(Integer id);
}
