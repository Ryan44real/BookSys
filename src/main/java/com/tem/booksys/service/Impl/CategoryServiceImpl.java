package com.tem.booksys.service.Impl;

import com.tem.booksys.entiy.Category;
import com.tem.booksys.mapper.CategoryMapper;
import com.tem.booksys.service.CategoryService;
import com.tem.booksys.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public void add(Category category) {
        //补充属性值
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        Map<String,Object> o = ThreadLocalUtil.get();
        Integer id = (Integer) o.get("id");
        category.setCreateUser(id);
//        System.out.println(category);
        categoryMapper.add(category);
    }

    @Override
    public List<Category> list() {
        //获取当前用户ID
        Map<String,Object> o = ThreadLocalUtil.get();
//        Integer id = (Integer) o.get("id");
        List<Category> categoryList =categoryMapper.list(1);
//        System.out.println(categoryList);
//        System.out.println(id);
        return  categoryList;
    }

    @Override
    public Category findById(Integer id) {

        return categoryMapper.findById(id);
    }

    @Override
    public void update(Category category) {
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(category);
    }

    @Override
    public void delete(Integer id) {
        categoryMapper.delete(id);
    }
}
