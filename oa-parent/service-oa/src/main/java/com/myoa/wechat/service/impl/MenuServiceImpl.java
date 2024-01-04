package com.myoa.wechat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.myoa.model.wechat.Menu;

import com.myoa.vo.wechat.MenuVo;
import com.myoa.wechat.mapper.MenuMapper;
import com.myoa.wechat.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.Authorization;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 菜单 服务实现类
 * </p>
 *
 * @author ljy
 * @since 2023-06-08
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    @Autowired
    private WxMpService wxMpService;
    @Override
    public List<MenuVo> findMenuInfo() {
        // 查询所有菜单list集合
        List<Menu> menuList = this.list();
        // 查询所有菜单的一级菜单parent_id=0条件，返回list
        List<Menu> oneMenuList = new ArrayList<>();
        for (Menu menu : menuList) {
            if(menu.getParentId()==0) {
                oneMenuList.add(menu);
            }
        }
        // 遍历一级菜单list
        List<MenuVo> onemenuVoList = new ArrayList<>();
        for (Menu onemenu : oneMenuList) {
            MenuVo onemenuVo = new MenuVo();
            BeanUtils.copyProperties(onemenu, onemenuVo);
            // 获取一级菜单中的所有二级菜单
            List<Menu> twomenuList = new ArrayList<>();
            for (Menu menu : menuList) {
                if(menu.getParentId()==onemenu.getId()) {
                    twomenuList.add(menu);
                }
            }
            // 把所有二级菜单封装到MenuVo菜单的children中
            List<MenuVo> children = new ArrayList<>();
            for (Menu twomenu : twomenuList) {
                MenuVo twoMenuVo = new MenuVo();
                BeanUtils.copyProperties(twomenu, twoMenuVo);
                children.add(twoMenuVo);
            }
            onemenuVo.setChildren(children);
            onemenuVoList.add(onemenuVo);
        }
        return onemenuVoList;
    }

    @Override
    public void syncMenu() {
        // 将菜单数据查询，封装微信要求格式
        List<MenuVo> menuVoList = this.findMenuInfo();
        JSONArray buttonList = new JSONArray();
        for(MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if(CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://myoaweb.vip.cpolar.cn/#"+oneMenuVo.getUrl());
            } else {
                JSONArray subButton = new JSONArray();
                for(MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if(twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://myoaweb.vip.cpolar.cn#"+twoMenuVo.getUrl());
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMeunKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        // 调用工具实现菜单推送
        try {
            wxMpService.getMenuService().menuCreate(button.toJSONString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeMenu() {
        try {
            wxMpService.getMenuService().menuDelete();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
