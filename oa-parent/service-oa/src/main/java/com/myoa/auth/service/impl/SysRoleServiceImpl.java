package com.myoa.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myoa.auth.mapper.SysRoleMapper;
import com.myoa.auth.service.SysRoleService;
import com.myoa.auth.service.SysUserRoleService;
import com.myoa.model.system.SysRole;
import com.myoa.model.system.SysUserRole;
import com.myoa.vo.system.AssginRoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    SysUserRoleService sysUserRoleService;
    @Override
    public Map<String, Object> findRoleDataByUserId(Long userId) {
        // 查询所有角色，返回list集合
        // service自身的list方法底层调用this.getBaseMapper().selectList(queryWrapper);
        List<SysRole> allRolesList = this.list();
        // 根据userId查询角色用户关系表，查询userId对应所有角色id
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> existUserRoleList = sysUserRoleService.list(wrapper);
        // 获取根据userId查到的SysUserRole列表的所有roleId
        List<Long> existRoleIdList = new ArrayList<>();
        for (SysUserRole sysUserRole : existUserRoleList) {
            existRoleIdList.add(sysUserRole.getRoleId());
        }
        // 根据查询所有角色id，找到对应角色信息
        List<SysRole> assginRoleList = new ArrayList<>();
        for (SysRole role : allRolesList) {
            //已分配
            if(existRoleIdList.contains(role.getId())) {
                assginRoleList.add(role);
            }
        }
        // 把根据userId查询的角色列表以及所有角色列表封装为map集合并返回
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assginRoleList);
        roleMap.put("allRolesList", allRolesList);
        return roleMap;
    }

    // 为用户分配角色
    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        // 获取分配的userId以及其所有的roleId
        Long userId = assginRoleVo.getUserId();
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        // 将之前用户角色关系中userId对应数据删除
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        sysUserRoleService.remove(wrapper);
        // 重新分配
        for (Long roleId : roleIdList) {
            if(StringUtils.isEmpty(roleId)) {
                continue;
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setRoleId(roleId);
            sysUserRole.setUserId(userId);
            sysUserRoleService.save(sysUserRole);
        }
    }
}
