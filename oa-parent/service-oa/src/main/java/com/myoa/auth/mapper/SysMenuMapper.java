package com.myoa.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myoa.model.system.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author ljy
 * @since 2023-06-01
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> findMenuListByUserId(@Param("userId") Long userId);
}
