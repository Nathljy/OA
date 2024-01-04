package com.myoa.auth.service;

import com.myoa.model.system.SysUser ;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author ljy
 * @since 2023-05-31
 */
public interface SysUserService extends IService<SysUser> {

    void updateStatus(Long id, Integer status);

    SysUser getUserByUserName(String assigneeName);

    Map<String, Object> getCurrentUser();
}
