package com.sakana.blog.utils;

import com.sakana.blog.dao.pojo.SysUser;

public class UserThreadLocal {

    private UserThreadLocal(){}
    //线程变量隔离
    //想在每个线程执行中存储特定信息就用ThreadLocal，线程安全相关

    private static final ThreadLocal<SysUser> LOCAL =new ThreadLocal<>();

    //放入
    public static void put(SysUser sysUser){
        LOCAL.set(sysUser);
    }

    //取出
    public static SysUser get(){
        return LOCAL.get();
    }

    public static void remove(){
        LOCAL.remove();
    }

}
