package com.sakana.blog.controller;

import com.sakana.blog.dao.pojo.SysUser;
import com.sakana.blog.utils.UserThreadLocal;
import com.sakana.blog.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {
    @RequestMapping
    public Result test(){
        SysUser sysUser= UserThreadLocal.get();
        System.out.println(sysUser);
        return Result.success(null);
    }
}
