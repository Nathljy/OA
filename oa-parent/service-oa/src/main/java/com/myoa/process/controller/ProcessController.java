package com.myoa.process.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myoa.common.result.Result;
import com.myoa.process.service.ProcessService;
import com.myoa.vo.process.ProcessQueryVo;
import com.myoa.vo.process.ProcessVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.myoa.model.process.Process;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author ljy
 * @since 2023-06-06
 */
@Api(tags = "审批流管理")
@RestController
@RequestMapping(value = "/admin/process")
public class ProcessController {
    @Autowired
    private ProcessService processService;

    // 根据前端传入数据封装的ProcessQueryVo对象，进行搜索，并分页
    @PreAuthorize("hasAuthority('bnt.process.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit,
                        ProcessQueryVo processQueryVo) {
        // mp可以根据查询自动封装vo对象
        Page<ProcessVo> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> pageModel = processService.selectPage(pageParam, processQueryVo);
        return Result.ok(pageModel);
    }
}

