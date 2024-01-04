package com.myoa.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myoa.auth.service.SysRoleMenuService;
import com.myoa.common.config.exception.MyException;
import com.myoa.common.result.Result;
import com.myoa.model.system.SysMenu;
import com.myoa.auth.mapper.SysMenuMapper;
import com.myoa.auth.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myoa.model.system.SysRole;
import com.myoa.model.system.SysRoleMenu;
import com.myoa.vo.system.AssginMenuVo;
import com.myoa.vo.system.MetaVo;
import com.myoa.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.myoa.auth.utils.MenuHelper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author ljy
 * @since 2023-06-01
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Autowired
    SysRoleMenuService sysRoleMenuService;
    @Override
    public List<SysMenu> findNodes() {
        // 查询所有菜单数据
        List<SysMenu> sysMenuList = this.list();
        // 构建菜单的属性结构（递归）
        List<SysMenu> resultList = MenuHelper.buildTree(sysMenuList);
        return resultList;
    }

    @Override
    public void removeMenuById(Long id) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        // 底层 SqlHelper.retCount(this.getBaseMapper().selectCount(queryWrapper));
        // baseMapper.selectCount()计数，SqlHelper.retCount()判断null时返回0
        int count = this.count(wrapper);
        if(count>0) {
            throw new MyException(201, "菜单不能删除");
        }
        this.removeById(id);
    }

    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        // 查询所有菜单SysMenu，并添加条件判断status=1
        LambdaQueryWrapper<SysMenu> wrapperStatus = new LambdaQueryWrapper<>();
        wrapperStatus.eq(SysMenu::getStatus, 1);
        List<SysMenu> allSysMenuList = this.list(wrapperStatus);
        // 在sys_role_menu对应表根据roleId查询menuId
        LambdaQueryWrapper<SysRoleMenu> wrapperRoleMenu = new LambdaQueryWrapper<>();
        wrapperRoleMenu.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(wrapperRoleMenu);
        // 根据menuId获取菜单对象
        List<Long> menuIdList = new ArrayList<>();
        for (SysRoleMenu sysRoleMenu : sysRoleMenuList) {
            menuIdList.add(sysRoleMenu.getMenuId());
        }
        // 返回规定格式菜单列表
        for (SysMenu sysMenu : allSysMenuList) {
            if(menuIdList.contains(sysMenu.getId())) {
                sysMenu.setSelect(true);
            } else {
                sysMenu.setSelect(false);
            }
        }
        List<SysMenu> list = MenuHelper.buildTree(allSysMenuList);
        return list;
    }

    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        Long roleId = assginMenuVo.getRoleId();
        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        // 删除对应角色id的菜单分配数据
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuService.remove(wrapper);
        // 获取新分配的菜单id列表，遍历并添加到菜单角色表SysRoleMenu
        for (Long menuId : menuIdList) {
            if(StringUtils.isEmpty(menuId)) {
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(roleId);
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenuService.save(sysRoleMenu);
        }
    }

    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList = null;
        // 判断当前用户是否管理员，本项目中userId为1就是管理员
        // 如果是管理员，则查询所有的菜单列表
        if(userId.longValue()==1) {
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = this.list(wrapper);
        } else { // 如果不是管理员，则根据userId查询可以操作的菜单列表，多表关联
            // 自己在mapper的xml中编写sql语句，此处获取mapper并调用该方法
            sysMenuList = this.getBaseMapper().findMenuListByUserId(userId);
        }
        // 查询结果构建为框架要求的路由数据机构，返回
        // 1.构建树形结构
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        // 2.构建框架要求的路由结构
        List<RouterVo> routerList = this.buildRouter(sysMenuTreeList);
        return routerList;
    }

    // 将树形结构的数据再构建为框架要求的结构，标准对象是RouterVo
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        // 创建list集合，存储结果数据
        List<RouterVo> routers = new ArrayList<>();
        // menus遍历
        for(SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            // 下一层数据的部分
            List<SysMenu> children = menu.getChildren();
            if(menu.getType().intValue()==1) {
                // 加载下面隐藏路由
                List<SysMenu> hiddenMenuList = new ArrayList<>();
                for (SysMenu item : children) {
                    if(!StringUtils.isEmpty(item.getComponent())) {
                        hiddenMenuList.add((item));
                    }
                }
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    // hidden设置为true，隐藏路由
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            } else {
                if(!CollectionUtils.isEmpty(children)) {
                    if(children.size()>0) {
                        router.setAlwaysShow(true);
                    }
                    // 递归
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    // 获取路由地址
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        List<String> permsList = new ArrayList<>();
        List<SysMenu> sysMenuList = null;
        // 判断当前用户是否管理员，本项目中userId为1就是管理员
        // 如果是管理员，则查询所有的按钮列表
        if(userId.longValue()==1) {
            // 查询所有菜单列表
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            sysMenuList = this.list(wrapper);
        } else { // 如果不是管理员，则根据userId查询可以操作的按钮列表，多表关联
            sysMenuList = this.getBaseMapper().findMenuListByUserId(userId);
        }
        // 查询结果数据获取可以操作按钮值的list集合，返回
        for (SysMenu sysMenu : sysMenuList) {
            if(sysMenu.getType()==2) {
                permsList.add(sysMenu.getPerms());
            }
        }
        return permsList;
    }
}
