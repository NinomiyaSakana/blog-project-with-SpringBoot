package com.sakana.blog.controller;

import com.sakana.blog.service.LoginService;
import com.sakana.blog.service.SysUserService;
import com.sakana.blog.vo.Result;
import com.sakana.blog.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("login")
public class LoginController {
//    @Autowired
//    private SysUserService sysUserService;
    //SysUserService专门用于查询用户
    private LoginService loginService;

    @PostMapping
    public Result login(@RequestBody LoginParam loginParam){
        //登陆要验证用户，访问用户表，但是
        return loginService.login(loginParam);
    }

}

