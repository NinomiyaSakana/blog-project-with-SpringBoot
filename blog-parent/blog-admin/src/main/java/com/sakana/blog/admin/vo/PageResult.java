package com.sakana.blog.admin.vo;

import lombok.Data;

import java.util.List;

//分页结果
@Data
public class PageResult<T> {

    private List<T> list;

    private Long total;

}
