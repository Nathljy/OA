package com.myoa.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myoa.auth.service.SysMenuService;
import com.myoa.auth.service.SysUserService;
import com.myoa.common.config.exception.MyException;
import com.myoa.common.jwt.JwtHelper;
import com.myoa.common.result.Result;
import com.myoa.common.utils.MD5;
import com.myoa.model.system.SysMenu;
import com.myoa.model.system.SysUser;
import com.myoa.vo.system.LoginVo;
import com.myoa.vo.system.RouterVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 后台登录登出
 * </p>
 */
@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Autowired
    SysUserService sysUserService;
    @Autowired
    SysMenuService sysMenuService;
    /**
     * 登录
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("token","admin");
//        return Result.ok(map);
        // 获取用户名密码
        String username = loginVo.getUsername();
        String passwordInput = MD5.encrypt(loginVo.getPassword());
        // 根据用户名查询数据库
        LambdaQueryWrapper<SysUser> wrapperUsername = new LambdaQueryWrapper<>();
        wrapperUsername.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserService.getOne(wrapperUsername);
        // 用户信息判断是否存在
        if(sysUser==null) {
            throw new MyException(201, "用户不存在");
        }
        // 判断密码正确与否
        String passwordDB = sysUser.getPassword();
        if(!passwordDB.equals(passwordInput)) {
            throw new MyException(201, "密码错误");
        }
        // 判断用户是否被禁用
        if(sysUser.getStatus().intValue()==0) {
            throw new MyException(201, "用户已经被禁用");
        }
        // 使用jwt根据用户id和用户名生成token字符串
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        // 返回
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        return Result.ok(map);
    }
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("info")
    public Result info(HttpServletRequest request) {
        // 从前端发送的请求头获取信息（token字符串）
        String token = request.getHeader("token");
        // 从token字符串中获取用户id或用户名
        Long userId = JwtHelper.getUserId(token);
//        Long userId = 1L;
        String username = JwtHelper.getUsername(token);
        // 根据用户id查询数据库，获取用户信息
        SysUser sysUser = sysUserService.getById(userId);
        // 根据用户id获取用户可操作菜单，查询数据库动态构建路由结构进行显示
        List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);
        // 根据用户id获取用户可操作性菜单的按钮列表
        List<String> permsList = sysMenuService.findUserPermsByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name", sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        // 返回用户可以操作的菜单
        map.put("routers", routerList);
        // 返回用户可以操作的按钮
        map.put("buttons", permsList);
        return Result.ok(map);
    }
    /**
     * 退出
     * @return
     */
    @PostMapping("logout")
    public Result logout(){
        return Result.ok(null);
    }

}