package com.myoa.process.service.impl;

import com.myoa.auth.service.SysUserService;
import com.myoa.model.process.ProcessRecord;
import com.myoa.model.system.SysUser;
import com.myoa.process.mapper.ProcessRecordMapper;
import com.myoa.process.service.ProcessRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myoa.security.custom.LoginUserInfoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 审批记录 服务实现类
 * </p>
 *
 * @author ljy
 * @since 2023-06-07
 */
@Service
public class ProcessRecordServiceImpl extends ServiceImpl<ProcessRecordMapper, ProcessRecord> implements ProcessRecordService {
    @Autowired
    private SysUserService sysUserService;
    // 记录
    @Override
    public void record(Long processId, Integer status, String description) {
        Long userId = LoginUserInfoHelper.getUserId();
        SysUser sysUser = sysUserService.getById(userId);
        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setProcessId(processId);
        processRecord.setStatus(status);
        processRecord.setDescription(description);
        processRecord.setOperateUserId(LoginUserInfoHelper.getUserId());
        processRecord.setOperateUser(sysUser.getName());
        this.save(processRecord);
    }
}
