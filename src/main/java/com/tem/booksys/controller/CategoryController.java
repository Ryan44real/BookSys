package com.tem.booksys.controller;

import com.tem.booksys.entiy.Category;
import com.tem.booksys.entiy.Result;
import com.tem.booksys.service.CategoryService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @PostMapping("/add")
    public Result add(@RequestBody @Validated(Category.Add.class) Category category){
//        System.out.println(category);
        categoryService.add(category);
        return Result.success();
    }

    @GetMapping("/query")
    public Result<List<Category>> list(){
        List<Category>  categoryList = categoryService.list();
        return Result.success(categoryList);
    }

    @GetMapping("/detail")
    public Result<Category> detail(Integer id){
        return Result.success(categoryService.findById(id));
    }

    @PutMapping
    public Result update(@RequestBody @Validated(Category.Update.class) Category category){
        categoryService.update(category);
        return Result.success();
    }

    @DeleteMapping
    public Result delete(Integer id){
        categoryService.delete(id);
        return Result.success();
    }
}
