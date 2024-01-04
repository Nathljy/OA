package com.myoa.auth.utils;

import com.myoa.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuHelper {
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        // 创建存放树形结果的list集合
        List<SysMenu> trees = new ArrayList<>();
        // 把所有菜单数据进行遍历
        for(SysMenu sysMenu : sysMenuList) {
            // 找到递归入口，即parentId==0
            if(sysMenu.getParentId()==0) {
                trees.add(getChildren(sysMenu, sysMenuList));
            }
        }
        return trees;
    }

    private static SysMenu getChildren(SysMenu sysMenu, List<SysMenu> sysMenuList) {
        sysMenu.setChildren(new ArrayList<>());
        for(SysMenu sysMenu1 : sysMenuList) {
            if(sysMenu.getId().longValue()==sysMenu1.getParentId().longValue()) {
                sysMenu.getChildren().add(getChildren(sysMenu1, sysMenuList));
            }
        }
        return sysMenu;
    }
}
