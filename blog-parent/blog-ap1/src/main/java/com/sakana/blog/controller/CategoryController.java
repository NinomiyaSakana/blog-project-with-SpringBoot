package com.sakana.blog.controller;

import com.sakana.blog.service.CategoryService;
import com.sakana.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("categorys")
public class CategoryController {
    //获取所有文章类别
    @Autowired
    private CategoryService categoryService;

    //categories
    @GetMapping
    public Result categories() {
        return categoryService.findAll();
    }

    ///categorys/detail
    @GetMapping("detail")
    public Result categoriesDetail() {
        return categoryService.findAllDetail();
    }

    ///category/detail/{id}
    @GetMapping("detail/{id}") //路径参数接收
    public Result categoriesDetailById(@PathVariable("id") Long id){
        return categoryService.categoriesDetailById(id);
    }

}
