package com.tem.booksys.mapper;

import com.tem.booksys.entiy.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    void add(Category category);

    List<Category> list(Integer createUser);

    Category findById(Integer id);

    void update(Category category);

    void delete(Integer id);
}
