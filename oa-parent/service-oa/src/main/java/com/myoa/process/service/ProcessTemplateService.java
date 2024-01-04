package com.myoa.process.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myoa.model.process.ProcessTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author ljy
 * @since 2023-06-06
 */
public interface ProcessTemplateService extends IService<ProcessTemplate> {

    Page<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageParam);

    void publish(Long id);

}
