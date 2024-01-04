package com.myoa.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myoa.auth.service.SysUserService;
import com.myoa.model.process.Process;
import com.myoa.model.process.ProcessRecord;
import com.myoa.model.process.ProcessTemplate;
import com.myoa.model.system.SysUser;
import com.myoa.process.mapper.ProcessMapper;
import com.myoa.process.service.ProcessRecordService;
import com.myoa.process.service.ProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myoa.process.service.ProcessTemplateService;
import com.myoa.security.custom.LoginUserInfoHelper;
import com.myoa.vo.process.ApprovalVo;
import com.myoa.vo.process.ProcessFormVo;
import com.myoa.vo.process.ProcessQueryVo;
import com.myoa.vo.process.ProcessVo;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author ljy
 * @since 2023-06-06
 */
@Service
public class ProcessServiceImpl extends ServiceImpl<ProcessMapper, Process> implements ProcessService {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ProcessTemplateService processTemplateService;
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessRecordService processRecordService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private HistoryService historyService;

    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel = this.getBaseMapper().selectPage(pageParam, processQueryVo);
        return pageModel;
    }

    @Override
    public void deployByZip(String deployPath) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        // 部署
        Deployment deployment = repositoryService.createDeployment()
                            .addZipInputStream(zipInputStream)
                            .deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    @Override
    public void startUp(ProcessFormVo processFormVo) {
        // 根据用户id获取用户信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        // 根据模板id查出模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());
        // 向oa_process表中记录信息
        Process process = new Process();
        //①该方法可以将一个对象中的值复制到另一个空对象中，属性相同就复制
        BeanUtils.copyProperties(processFormVo, process);
        //②其他值
        process.setStatus(1); // 审批中
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        this.save(process);
        // 启动流程实例【RuntimeService】
        //参数 ①流程定义key ②业务key(processId) ③流程参数form表单json数据转换map集合
        String processDefinitionKey = processTemplate.getProcessDefinitionKey(); //①
        String businessKey = String.valueOf(process.getId()); // ②
        String formValues = processFormVo.getFormValues(); // ③
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");
        //遍历formData，参数封装map集合
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry:formData.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("data", map);
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        // 查询下一个审批人 (导包org.activiti.engine.task.Task)
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        List<String> nameList = new ArrayList<>();
        for (Task task : taskList) {
            String assigneeName = task.getAssignee();
            SysUser user = sysUserService.getUserByUserName(assigneeName);
            String name = user.getName();
            nameList.add(name);
            //TODO 推送消息给各个代办用户
        }
        // 业务和流程关联 更新oa_process，包括process_instance_id, description
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待"+ StringUtils.join(nameList.toArray(), ",")+"审批");
        this.updateById(process);
        // 记录操作审批记录
        processRecordService.record(process.getId(), 1, "发起申请");
    }

    // 当前任务列表
    public List<Task> getCurrentTaskList(String id) {
        List<Task> taskList = taskService.createTaskQuery()
                .processInstanceId(id).list();
        return taskList;
    }

    @Override
    public Page<ProcessVo> findPending(Page<Process> pageParam) {
        // 根据用户名封装查询条件TaskQuery
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .orderByTaskCreateTime()
                .desc();
        // 调用方法分页条件查询代办任务集合
        int begin = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) pageParam.getSize();
        List<Task> taskList = query.listPage(begin, size);
        // 封装返回list集合到List<ProcessVo>
        List<ProcessVo> processVoList = new ArrayList<>();
        for (Task task : taskList) {
            // 从task获取流程实例id ProcessInstanceId
            String processInstanceId = task.getProcessInstanceId();
            // 在Process中直接查当时startUp设置的InstanceId并拿到Process对象
            LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId, processInstanceId);
            Process process = processService.getOne(wrapper);
            // Process对象赋值到ProcessVo对象
            if(process!=null) {
                ProcessVo processVo = new ProcessVo();
                BeanUtils.copyProperties(process, processVo);
                // 其他值设置
                processVo.setTaskId(task.getId());
                processVoList.add(processVo);
            }
        }
        // 封装Page对象
        Page<ProcessVo> page = new Page<>(pageParam.getCurrent(),
                                            pageParam.getSize(),
                                            query.count());
        page.setRecords(processVoList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        // 根据流程id获取Process
        Process process = processService.getById(id);
        // 根据流程id获取ProcessRecord
        LambdaQueryWrapper<ProcessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId, id);
        List<ProcessRecord> processRecordList = processRecordService.list(wrapper);
        // 根据模板id查询模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        // 判断当前用户是否可以审批，以及审批过不能重复审批
        boolean isApprove = false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        for (Task task : taskList) {
            // 判断任务审批人是否当前用户
            if(task.getAssignee().equals(LoginUserInfoHelper.getUsername())) {
                isApprove = true;
            }
        }
        //查询数据封装map集合返回
        Map<String, Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        map.put("isApprove", isApprove);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        // 从approvalVo获取任务id，根据id获取流程变量
        String taskId = approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);
        for (Map.Entry<String,Object> entry:variables.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        // 判断审批状态，1：通过 ； -1：驳回
        if(approvalVo.getStatus()==1) {
            taskService.complete(taskId);
        } else {
            this.endTask(taskId);
        }
        String description = approvalVo.getStatus()==1 ? "已通过" : "已驳回";
        // 记录审批相关信息ProcessRecord
        processRecordService.record(approvalVo.getProcessId(),
                                    approvalVo.getStatus(),
                                    description);
        // 查询下一个审批人，更新process表记录
        Process process = this.getById(approvalVo.getProcessId());
        //查询任务
        List<Task> taskList = getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)) {
            List<String> assignList = new ArrayList<>();
            for (Task task : taskList) {
                String assigneeName = task.getAssignee();
                SysUser user = sysUserService.getUserByUserName(assigneeName);
                String name = user.getName();
                assignList.add(name);
            }
            process.setDescription("等待" + StringUtils.join(assignList.toArray(), ",") + "审批");
            process.setStatus(1);
        } else {
            if(approvalVo.getStatus().intValue()==1) {
                process.setDescription("审批通过");
                // process的Status 1审批中 2通过 -1驳回
                process.setStatus(2);
            } else {
                process.setDescription("审批驳回");
                process.setStatus(-1);
            }
        }
        //推送消息给申请人
        this.updateById(process);
    }

    private void endTask(String taskId) {
        // 1.根据taskId获取任务对象Task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 2.获取流程定义模型BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        // 3.获取结束流向节点
        List endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        if(CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        // 4.当前流向节点
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        //临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        // 5.清理当前流动方向
        currentFlowNode.getOutgoingFlows().clear();
        // 6.创建新流向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        // 7.当前节点指向新方向
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);
        // 8.完成当前任务
        taskService.complete(task.getId());
    }

    @Override
    public Page<ProcessVo> findProcessed(Page<Process> pageParam) {
        // 封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished().orderByTaskCreateTime().desc();
        // 调用方法条件分页查询，返回list集合
        int begin = (int) ((pageParam.getCurrent()-1)*pageParam.getSize());
        int size = (int) pageParam.getSize();
        List<HistoricTaskInstance> historicTaskInstanceList = query.listPage(begin, size);
        long count = query.count();
        // 遍历list集合，封装List<ProcessVo>
        List<ProcessVo> processVoList = new ArrayList<>();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstanceList) {
            String processInstanceId = historicTaskInstance.getProcessInstanceId();
            LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId, processInstanceId);
            Process process = this.getOne(wrapper);
            if(process!=null) {
                ProcessVo processVo = new ProcessVo();
                BeanUtils.copyProperties(process, processVo);
                processVoList.add(processVo);
            }
        }
        // Page封装分页查询所有数据返回
        Page<ProcessVo> pageModel = new Page<>(pageParam.getCurrent(), pageParam.getSize(), count);
        pageModel.setRecords(processVoList);
        return pageModel;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> pageModel = this.getBaseMapper().selectPage(pageParam, processQueryVo);
        return pageModel;
    }
}
