package com.myoa.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myoa.model.system.SysUser;
import com.myoa.auth.mapper.SysUserMapper;
import com.myoa.auth.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myoa.security.custom.LoginUserInfoHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author ljy
 * @since 2023-05-31
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public void updateStatus(Long id, Integer status) {
        // 根据userId查询用户对象
        // service的getById()方法底层用的是this.getBaseMapper().selectById(id);
        SysUser sysUser = this.getById(id);
        // 设置修改状态值
        sysUser.setStatus(status);
        // 调用方法修改
        this.updateById(sysUser);
    }

    @Override
    public SysUser getUserByUserName(String assigneeName) {
        if(StringUtils.isEmpty(assigneeName)) {
            return null;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, assigneeName);
        return this.getOne(wrapper);
    }

    @Override
    public Map<String, Object> getCurrentUser() {
        SysUser sysUser = this.getById(LoginUserInfoHelper.getUserId());
        //SysDept sysDept = sysDeptService.getById(sysUser.getDeptId());
        //SysPost sysPost = sysPostService.getById(sysUser.getPostId());
        Map<String, Object> map = new HashMap<>();
        map.put("name", sysUser.getName());
        map.put("phone", sysUser.getPhone());
        //map.put("deptName", sysDept.getName());
        //map.put("postName", sysPost.getName());
        return map;
    }
}
