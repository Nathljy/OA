package com.myoa.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myoa.model.process.Process;
import com.myoa.vo.process.ApprovalVo;
import com.myoa.vo.process.ProcessFormVo;
import com.myoa.vo.process.ProcessQueryVo;
import com.myoa.vo.process.ProcessVo;
import org.activiti.engine.task.Task;


import java.util.List;
import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author ljy
 * @since 2023-06-06
 */
public interface ProcessService extends IService<Process> {

    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);
    // 部署流程定义
    void deployByZip(String deployPath);

    void startUp(ProcessFormVo processFormVo);

    Page<ProcessVo> findPending(Page<Process> pageParam);

    Map<String, Object> show(Long id);

    void approve(ApprovalVo approvalVo);

    Page<ProcessVo> findProcessed(Page<Process> pageParam);

    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);
}
