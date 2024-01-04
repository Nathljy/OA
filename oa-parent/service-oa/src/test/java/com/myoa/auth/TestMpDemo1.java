package com.myoa.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.myoa.auth.mapper.SysRoleMapper;
import com.myoa.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TestMpDemo1 {
    @Autowired
    private SysRoleMapper mapper;
    @Test
    public void getAll() {
        List<SysRole> list = mapper.selectList(null);
        System.out.println(list);
    }
    @Test
    public void add() {
        SysRole role = new SysRole();
        role.setRoleName("角色管理员");
        role.setRoleCode("role");
        role.setDescription("角色管理员");
        System.out.println(role.getId());
        int rows = mapper.insert(role);
        System.out.println(rows+" records is inserted");
        System.out.println(role.getId());
    }
    @Test
    public void update() {
        SysRole role = mapper.selectById(12);
        role.setRoleName("不是角色管理员");
        int rows = mapper.updateById(role);
        System.out.println(rows);
    }
    @Test
    public void deleteId() {
        mapper.deleteById(12);
        mapper.deleteBatchIds(Arrays.asList(1,2));
    }
    @Test
    public void selectByRoleName() {
        // 创建QueryWrapper对象，调用方法封装查询的条件
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_name", "总经理");
        // 调用mp方法实现查询操作
        List<SysRole> list = mapper.selectList(wrapper);
        System.out.println(list);
    }
}
