package com.cesgroup.api.process;

import com.cesgroup.api.form.FormDTO;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import com.cesgroup.bpm.persistence.domain.BpmConfNode;
import com.cesgroup.core.page.Page;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流程连接器接口
 * 
 * @author 国栋
 *
 */
public interface ProcessConnector {

    /**
     * 获得启动表单
     * 
     * @param processDefinitionId
     *            流程定义id
     * @return 表单对象
     */
    FormDTO findStartForm(String processDefinitionId);

    /**
     * 发起流程.
     * 
     * @param userId
     *            发起人id
     * @param businessKey
     *            业务key
     * @param processDefinitionId
     *            流程定义id
     * @param processParemeters
     *            启动携带的参数key value
     * @return String 流程实例ID
     * @throws Exception
     *             执行失败抛出异常
     */
    String startProcess(String userId, String businessKey, String processDefinitionId,
        Map<String, Object> processParemeters, String currentUserId) throws Exception;

    /**
     * 查询未办结流程
     * 
     * @param tenantId
     *            租户id
     * @param page
     *            分页对象
     * @return page
     */
    Page findRunningProcessInstancesWithoutUserId(String tenantId, Page page);

    /**
     * 已结流程(针对用户)
     * 
     * @param userId
     *            用户id
     * @param tenantId
     *            租户id
     * @param page
     *            分页
     * @return page
     */
    @Deprecated
    Page findCompletedProcessInstances(String userId, String tenantId, Page page);

    /**
     * 已结流程
     * 
     * @param tenantId
     *            租户id
     * @param page
     *            分页对象
     * @return page
     */
    @Deprecated
    public Page findCompletedProcessInstancesWithoutUserId(String tenantId, Page page);

    /**
     * 通过过滤条件查询已结流程
     * 
     * @param userId
     *            用户id
     * @param tenantId
     *            租户id
     * @param processInstanceName
     *            流程实例名称
     * @param date
     *            日期
     * @param page
     *            分页对象
     * @return page
     */
    @Deprecated
    public Page findCompletedProcessInstancesByFilter(String userId, String tenantId,
        String processInstanceName, Date date, Page page);

    /**
     * 流程实例与流程干涉专用
     * 
     * @param userId
     *            用户id
     * @param tenantId
     *            租户id
     * @param processInstanceName
     *            流程实例名称
     * @param date
     *            日期
     * @return list
     */
    @Deprecated
    public List<HistoricProcessInstance> findRunningProcessInstancesByFilter(String userId,
        String tenantId, String processInstanceName, Date date);

    /**
     * 查询流程定义
     * 
     * @param tenantId
     *            租户id
     * @param page
     *            分页对象
     */
    Page findProcessDefinitions(String tenantId, Page page);

    /**
     * 流程实例.
     * 
     * @param tenantId
     *            租户id
     * @param page
     *            分页对象
     */
    @Deprecated
    Page findProcessInstances(String tenantId, Page page);

    /**
     * 根据规定条件查询流程实例
     * 
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param processInstanceName
     *            流程实例名称
     * @param startDate
     *            流程开始时间
     * @param isActive
     *            是否是激活状态
     * @param page
     *            分页信息
     * @return list
     */
    List<HistoricProcessInstance> findProcessInstances(String tenantId, String userId,
        String processInstanceName, String startDate, Boolean isActive, Page page);

    /**
     * 统计当前用户办结流程
     * 
     * @param tenantId
     *            租户id
     * @param userId
     *            用户登录名
     * @return long
     */
    long countFinishedTask(String tenantId, String userId);

    /**
     * 根据executionId获得Runtime中参数
     * 
     * @param executionId
     *            路径id
     * @param variableName
     *            变量名称
     * @return Object
     */
    Object getRuntimeVariableValue(String executionId, String variableName);

    /**
     * 根据流程定义查询流程名称
     * 
     * @param processDefinitionId
     *            流程定义ID
     * @return 流程名称
     */
    String queryProcessNameByDefinitionId(String processDefinitionId, String tenantId);

    /**
     * 查询BpmConfBase
     * 
     * @param processDefinitionIdVal
     *            processDefinitionId值
     * @return 流程配置对象
     */
    BpmConfBase queryBpmConfBaseListByDefinitionId(String processDefinitionIdVal);

    /**
     * 检测流程是否挂起
     * 
     * @param processInstanceId
     *            流程实例ID
     * @return boolean true：挂起 / false: 未挂起
     */
    boolean queryIsSuspendedByInstanceId(String processInstanceId);

    /**
     * 修改流程实例的状态
     * 
     * @param tenantId
     *            租户id
     * @param processInstanceId
     *            流程实例id
     * @param status
     *            状态
     */
    void modifyInstanceStatusByProcessInstanceId(String tenantId, String processInstanceId,
        String status, String currentUserId);

    /**
     * 校验流程状态
     * 
     * @param processInstanceIds
     *            流程实例ID
     * @param status
     *            目标状态
     * @return boolean
     */
    boolean checkProcessInstanceStatus(String processInstanceIds, String status);

    /**
     * 流程实例基本信息
     * 
     * @param processInstanceId
     *            流程实例ID
     * @param tenantId
     *            租户
     * @return 流程实例
     */
    ProcessInstance getBasicInstanceByProcessInstanceId(String processInstanceId, String tenantId);

    /**
     * 已办箱
     * 
     * @param tenantId
     *            租户ID
     * @param processDefinitionKey
     *            流程定义KEY
     * @param userId
     *            用户ID
     * @return list
     */
    List<Map<String, Object>> getHasdoBox(String tenantId, String processDefinitionKey,
        String userId);

    /**
     * 办结箱
     * 
     * @param tenantId
     *            租户ID
     * @param processDefinitionKey
     *            流程定义KEY
     * @param userId
     *            用户ID
     * @return list
     */
    List<Map<String, Object>> getCompleteBox(String tenantId, String processDefinitionKey,
        String userId);

    /**
     * 启动流程
     * 
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param businessKey
     *            业务表关联值
     * @param processDefinitionKey
     *            流程定义KEY
     * @param processParameters
     *            参数
     * @param autoCommit
     *            是否自动提交
     * @return String 流程实例ID
     */
    String startProcessInstance(String tenantId, String userId, String businessKey,
        String processDefinitionKey, Map<String, Object> processParameters, Boolean autoCommit, 
        String currentUserId);

    /**
     * 启动流程（不自动提交第一个节点）
     * 
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param businessKey
     *            业务表关联值
     * @param processDefinitionKey
     *            流程定义KEY
     * @param processParameters
     *            参数
     * @return String 流程实例ID
     */
    String startProcessInstance(String tenantId, String userId, String businessKey,
        String processDefinitionKey, Map<String, Object> processParameters, String currentUserId);

    /**
     * 启动流程
     * 
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param processDefinitionKey
     *            流程定义KEY
     * @param processParameters
     *            参数
     * @param autoCommit
     *            是否自动提交
     * @return String 流程实例ID
     */
    String startProcessInstance(String tenantId, String userId, String processDefinitionKey,
        Map<String, Object> processParameters, Boolean autoCommit, String currentUserId);

    /**
     * 启动流程（不自动提交第一个节点）
     * 
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param processDefinitionKey
     *            流程定义KEY
     * @param processParameters
     *            参数
     * @return String 流程实例ID
     */
    String startProcessInstance(String tenantId, String userId, String processDefinitionKey,
        Map<String, Object> processParameters, String currentUserId);

    /**
     * 获取流程中运行节点任务的办理人
     *
     * @param processInstanceId
     *            流程实例ID
     * @return list
     */
    List<Map<String, Object>> queryRunningProcessTaskAndAssigneeByProcessInstanceId(
        String processInstanceId);

    /**
     * 检测流程变量唯一性
     * 
     * @param executionId
     *            执行ID
     * @param name
     *            流程变量名
     * @return boolean true: 未包含/ false：已包含
     */
    boolean checkVariableUnique(String executionId, String name);

    /**
     * 根据路程定义ID获取流程定义对象
     * 
     * @param processDefinitionId
     *            流程定义ID
     * @return 流程定义实体对象
     */
    ProcessDefinitionEntity getDefinitionEntityById(String processDefinitionId);

    /**
     * 根据执行ID设置流程变量
     * 
     * @param executionId
     *            执行ID
     * @param name
     *            流程变量名
     * @param value
     *            流程变量值
     */
    void setRuntimeVariableVariable(String executionId, String name, String value);

    /**
     * 根据执行ID获取所有的流程变量
     * 
     * @param executionId
     *            执行ID
     * @return Map 封装流程变量的集合
     */
    Map<String, Object> getRuntimeVariables(String executionId);

    /**
     * 根据流程实例ID获取当前流程所有的运行时任务
     * 
     * @param processInstanceId
     *            流程实例ID
     * @return List 封装所有任务的集合
     */
    List<Task> getTaskListByProcessInstanceId(String processInstanceId);

    /**
     * 根据流程定义ID获取流程定义实体类
     * 
     * @param processDefinitionId
     *            流程定义id
     * @return ProcessDefinitionEntity
     */
    ProcessDefinitionEntity getProcessDefinitionEntityByProcessDefinitionId(
        String processDefinitionId);

    /**
     * 查询已经完成的实例数量
     * 
     * @param tenantId
     *            租户id
     * @param userId
     *            用户id
     * @param processDefinitionKey
     *            流程定义key
     * @return long 查询已经完成的实例数量
     */
    long countCompleteTaskByProcessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey);

    /**
     * 报销示例已办条目
     * 
     * @param tenantId
     *            租户id
     * @param userId
     *            用户id
     * @param processDefinitionKey
     *            流程定义key
     * @param filter
     *            筛选条件
     * @return long
     */
    long countCompleteTaskByProcessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey, String filter);

    /**
     * 查询已办的任务实例
     * 
     * @param tenantId
     *            租户id
     * @param processDefinitionKey
     *            流程定义key
     * @param userId
     *            用户id
     * @param filter
     *            过滤器
     * @param page
     *            page对象
     * @return page
     */
    Page takenTaskList(String tenantId, String processDefinitionKey, String userId, String filter,
        Page page);

    /**
     * 报销示例已办条目
     * 
     * @param tenantId
     *            租户id
     * @param userId
     *            用户id
     * @param processDefinitionKey
     *            流程定义key
     * @return long
     */
    long countTakenTaskByProcessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey);

    /**
     * 报销示例已办条目
     * 
     * @param tenantId
     *            租户id
     * @param userId
     *            用户id
     * @param processDefinitionKey
     *            流程定义key
     * @param filter
     *            筛选条件
     * @return long
     */
    long countTakenTaskByProcessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey, String filter);

    /**
     * 根据modelId启动流程
     * 
     * @param userId
     *            发起人id
     * @param businessKey
     *            业务key
     * @param modelId
     *            模型id
     * @param processParameters
     *            启动携带参数 key value
     * @return 流程实例ID
     * @throws Exception
     *             执行失败抛出异常
     */
    @Deprecated
    String startProcessByModelId(String userId, String businessKey, String modelId,
        Map<String, Object> processParameters, String currentUserId) throws Exception;

    /**
     * @param userId
     * 
     * @param businessKey
     * 
     * @param modelId
     * 
     * @param processParameters
     * 
     * @param autoCommit
     * 
     * @return
     * 
     * @throws Exception
     * 
     */
    String startProcessByModelId(String userId, String businessKey, 
            String modelId,Map<String, Object> processParameters, Boolean autoCommit, 
        String currentUserId) throws Exception;
    
    /**
     * 挂起某一版本的流程
     * 
     * @param modelId
     *            模型id
     */
    @Deprecated
    void suspendProcessDefinitionByModelId(String modelId);

    /**
     * 根据流程实例id获得modelId
     * 
     * @param processInstanceId
     *            流程实例id
     * @return modelId
     */
    @Deprecated
    String getModelIdByProcessInstanceId(String processInstanceId);

    /**
     * 根据modelId获取所有节点信息
     * 
     * @param modelId
     *            模型id
     * @return list
     */
    @Deprecated
    List<BpmConfNode> getNodesByModelId(String modelId);

    /**
     * 激活某一版本的流程
     * 
     * @param modelId
     *            模型id
     */
    @Deprecated
    void activeProcessDefinitionByModelId(String modelId);

    /**
     * 查询办结的流程
     * 
     * @param tenantId
     *            租户id
     * @param processDefinitionKey
     *            流程定义id
     * @param userId
     *            用户id
     * @param filter
     *            筛选条件
     * @param page
     *            分页对象
     * @return page
     */
    Page completeTaskList(String tenantId, String processDefinitionKey, String userId,
        String filter, Page page);

    /**
     * 根据租户ID获取所有运行时流程实例的数量
     * 
     * @param tenantId
     *            租户ID
     * @return long
     */
    long countRunningProcessInstance(String tenantId);

    /**
     * 根据租户ID获取所有完结流程实例的数量
     * 
     * @param tenantId
     *            租户ID
     * @return long
     */
    long countFinishedProcessInstance(String tenantId);

    

    /**
     * @param tenantId
     * 
     * @param userId
     * 
     * @param processDefinitionKeys
     * 
     * @param filter
     * 
     * @return
     * 
     */
    long countTakenTaskByProcessDefinitionKeys(String tenantId, String userId,
        List<String> processDefinitionKeys, String filter);

    /**
     * @param tenantId
     * 
     * @param userId
     * 
     * @param processDefinitionKeys
     * 
     * @return
     * 
     */
    long countCompleteTaskByProcessDefinitionKeys(String tenantId, String userId,
        List<String> processDefinitionKeys);

    List<Map<String, String>> queryNextActivityListByModelId(String modelId);


    Boolean judgeProcessEnd(String processInstanceId);
}
