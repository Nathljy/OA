package com.myoa.wechat.service;

import com.myoa.model.wechat.Menu;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myoa.vo.wechat.MenuVo;

import java.util.List;

/**
 * <p>
 * 菜单 服务类
 * </p>
 *
 * @author ljy
 * @since 2023-06-08
 */
public interface MenuService extends IService<Menu> {

    List<MenuVo> findMenuInfo();

    void syncMenu();

    void removeMenu();
}
