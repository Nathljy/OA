package com.myoa.auth.service;

import com.myoa.model.system.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myoa.vo.system.AssginMenuVo;
import com.myoa.vo.system.RouterVo;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author ljy
 * @since 2023-06-01
 */
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> findNodes();

    void removeMenuById(Long id);

    List<SysMenu> findMenuByRoleId(Long roleId);

    void doAssign(AssginMenuVo assginMenuVo);

    List<RouterVo> findUserMenuListByUserId(Long userId);

    List<String> findUserPermsByUserId(Long userId);
}
