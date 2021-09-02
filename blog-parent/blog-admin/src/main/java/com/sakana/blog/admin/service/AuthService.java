package com.sakana.blog.admin.service;

import com.sakana.blog.admin.pojo.Admin;
import com.sakana.blog.admin.pojo.Permission;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private AdminService adminService;

    public boolean auth(HttpServletRequest request, Authentication authentication){
        String requestURI = request.getRequestURI();
        log.info("request url:{}", requestURI);
        //true代表放行 false 代表拦截
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)){
            //未登录
            return false;
        }
        UserDetails userDetails = (UserDetails) principal;
        //得到username
        String username = userDetails.getUsername();
        Admin admin = adminService.findAdminByUserName(username);
        if (admin == null){
            return false;
        }
        if (admin.getId() == 1){
            //认为是超级管理员
            //直接放行
            return true;
        }
        List<Permission> permissions = adminService.findPermissionsByAdminId(admin.getId());
        //url里面会有？传参数，所以取？前面的几位
        requestURI = StringUtils.split(requestURI,'?')[0];
        for (Permission permission : permissions) {
            if (requestURI.equals(permission.getPath())){
                log.info("权限通过");
                return true;
            }
        }
        return false;
    }

}
