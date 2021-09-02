package com.sakana.blog.controller;

import com.sakana.blog.service.LoginService;
import com.sakana.blog.vo.Result;
import com.sakana.blog.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("register")
public class RegisterController {
    @Autowired
    private LoginService loginService;

    @PostMapping
    public Result register(@RequestBody LoginParam loginParam){
        //sso 单点登录，后期如果把登陆注册功能提出去（单独的服务，可以独立提供接口服务）
        //SSO的定义是在多个应用系统中，用户只需要登录一次就可以访问所有相互信任的应用系统
        return loginService.register(loginParam);
    }
}
