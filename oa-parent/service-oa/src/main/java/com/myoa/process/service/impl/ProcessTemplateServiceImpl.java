package com.myoa.process.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myoa.model.process.ProcessTemplate;
import com.myoa.model.process.ProcessType;
import com.myoa.process.mapper.ProcessTemplateMapper;
import com.myoa.process.service.ProcessService;
import com.myoa.process.service.ProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myoa.process.service.ProcessTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author ljy
 * @since 2023-06-06
 */
@Service
public class ProcessTemplateServiceImpl extends ServiceImpl<ProcessTemplateMapper, ProcessTemplate> implements ProcessTemplateService {
    @Autowired
    private ProcessTypeService processTypeService;
    @Autowired
    private ProcessService processService;
    @Override
    public Page<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageParam) {
        // 调用mapper方法实现分页查询
        Page<ProcessTemplate> processTemplatePage = this.page(pageParam);
        // 分页查询返回分页数据，从分页数据中获取列表list集合
        List<ProcessTemplate> processTemplateList = processTemplatePage.getRecords();
        // 遍历list集合
        for (ProcessTemplate processTemplate : processTemplateList) {
            // 得到每个对象的审批类型id
            Long id = processTemplate.getProcessTypeId();
            // 根据审批类型id，获取类型名称
            ProcessType processType = processTypeService.getById(id);
            if(processType==null) {
                continue;
            }
            // 封装
            processTemplate.setProcessTypeName(processType.getName());
        }
        return processTemplatePage;
    }

    @Override
    public void publish(Long id) {
        // 修改模板发布状态为1
        ProcessTemplate processTemplate = this.getById(id);
        processTemplate.setStatus(1);
        this.updateById(processTemplate);
        // 完善流程定义部署
        String path = processTemplate.getProcessDefinitionPath();
        if(!StringUtils.isEmpty(path)) {
            processService.deployByZip(path);
        }
    }
}
