package com.sakana.blog.service;

import com.sakana.blog.vo.CategoryVo;
import com.sakana.blog.vo.Result;

import java.util.List;

public interface CategoryService {
    CategoryVo findCategoryById(Long categoryId);

    Result findAll();

    Result findAllDetail();

    Result categoriesDetailById(Long id);
}
