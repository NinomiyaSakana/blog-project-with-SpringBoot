package com.sakana.blog.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakana.blog.admin.pojo.Permission;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    List<Permission> findPermissionsByAdminId(Long adminId);
}
