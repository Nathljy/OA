package com.myoa.process.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myoa.model.process.Process;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myoa.vo.process.ProcessQueryVo;
import com.myoa.vo.process.ProcessVo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author ljy
 * @since 2023-06-06
 */
public interface ProcessMapper extends BaseMapper<Process> {
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam,
                                @Param("vo") ProcessQueryVo processQueryVo);
}
