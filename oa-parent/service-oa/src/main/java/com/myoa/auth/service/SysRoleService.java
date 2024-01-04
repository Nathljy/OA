package com.myoa.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myoa.model.system.SysRole;
import com.myoa.vo.system.AssginRoleVo;

import java.util.Map;

public interface SysRoleService extends IService<SysRole> {
    Map<String, Object> findRoleDataByUserId(Long userId);
    void doAssign(AssginRoleVo assginRoleVo);
}
