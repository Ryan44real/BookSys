package com.tem.booksys.controller;

import com.tem.booksys.entity.Category;
import com.tem.booksys.entity.Result;
import com.tem.booksys.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理", description = "图书分类的增删改查接口")
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Operation(summary = "新增分类")
    @PostMapping("/add")
    public Result add(@RequestBody @Validated(Category.Add.class) Category category){
//        System.out.println(category);
        categoryService.add(category);
        return Result.success();
    }

    @Operation(summary = "查询全部分类")
    @GetMapping("/query")
    public Result<List<Category>> list(){
        List<Category>  categoryList = categoryService.list();
        return Result.success(categoryList);
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/detail")
    public Result<Category> detail(Integer id){
        return Result.success(categoryService.findById(id));
    }

    @Operation(summary = "更新分类")
    @PutMapping
    public Result update(@RequestBody @Validated(Category.Update.class) Category category){
        categoryService.update(category);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping
    public Result delete(Integer id){
        categoryService.delete(id);
        return Result.success();
    }
}
