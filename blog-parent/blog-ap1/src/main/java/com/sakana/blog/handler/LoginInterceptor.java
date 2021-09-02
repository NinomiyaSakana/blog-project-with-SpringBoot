package com.sakana.blog.handler;

import com.alibaba.fastjson.JSON;
import com.sakana.blog.dao.pojo.SysUser;
import com.sakana.blog.service.LoginService;
import com.sakana.blog.utils.UserThreadLocal;
import com.sakana.blog.vo.ErrorCode;
import com.sakana.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j //log对象创建
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginService loginService;

    //提供了三个默认的实现
    //重写一个实现
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在实行controller方法（handler）之前进行执行
        /**
         * 1、需要判断请求的接口路径是否为handlerMethod（controller方法）
         * 2、判断token是否为空，如果为空 未登陆
         * 3、如果token不为空，登录验证loginService checkToken
         * 4、如果认证成功 放行即可
         */

        if (!(handler instanceof HandlerMethod)) {
            //handler可能是RequestResourceHandler
            // springboot程序访问静态资源默认去classpath下的static目录去查询
            return true;
        }
        String token = request.getHeader("Authorization");

        //打印日志
        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");

        if (StringUtils.isBlank(token)) {
            //token是空的
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登陆");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        //token不为空
        SysUser sysUser = loginService.checkToken(token);
        if (sysUser == null) {
            //跟上面一样
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登陆");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        //登陆验证成功，放行
        //我希望在controller中直接获取用户信息，怎么获取
        UserThreadLocal.put(sysUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //如果不删除ThreadLocal中用完的信息 会有内存泄漏的风险
        //其中存储的信息没用了，所以要删除UserThreadLocal，但是线程还没完
        UserThreadLocal.remove();
    }


}
