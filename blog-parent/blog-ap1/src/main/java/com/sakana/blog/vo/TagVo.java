package com.sakana.blog.vo;

import lombok.Data;

//vo相当于和页面交互的数据
//理论上应该与数据库的数据分开
@Data
public class TagVo {

    private String id;

    private String tagName;

    private String avatar;
}
