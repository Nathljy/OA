package com.myoa.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myoa.model.process.ProcessTemplate;
import com.myoa.model.process.ProcessType;
import com.myoa.process.mapper.ProcessTypeMapper;
import com.myoa.process.service.ProcessTemplateService;
import com.myoa.process.service.ProcessTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.el.LambdaExpression;
import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author ljy
 * @since 2023-06-06
 */
@Service
public class ProcessTypeServiceImpl extends ServiceImpl<ProcessTypeMapper, ProcessType> implements ProcessTypeService {
    @Autowired
    ProcessTemplateService processTemplateService;
    @Override
    public List<ProcessType> findProcessType() {
        List<ProcessType> list = this.list();
        for (ProcessType processType : list) {
            LambdaQueryWrapper<ProcessTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessTemplate::getProcessTypeId, processType.getId());
            List<ProcessTemplate> processTemplateList = processTemplateService.list(wrapper);
            processType.setProcessTemplateList(processTemplateList);
        }
        return list;
    }
}
