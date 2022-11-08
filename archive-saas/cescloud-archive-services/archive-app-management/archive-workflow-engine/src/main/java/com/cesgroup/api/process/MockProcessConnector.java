package com.cesgroup.api.process;

import com.cesgroup.api.form.FormDTO;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import com.cesgroup.bpm.persistence.domain.BpmConfNode;
import com.cesgroup.core.page.Page;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 模拟流程连接器
 * 
 * @author 国栋
 *
 */
public class MockProcessConnector implements ProcessConnector {

    /**
     * 获得启动表单.
     */
    @Override
    public FormDTO findStartForm(String processDefinitionId) {
        return null;
    }

    /**
     * 获得流程配置.
     */
    public ProcessDTO findProcess(String processId) {
        return null;
    }

    /**
     * 发起流程.
     */
    @Override
    public String startProcess(String userId, String businessKey, String processDefinitionId,
                               Map<String, Object> processParemeters, String currentUserId) {
        return null;
    }

    @Override
    public boolean queryIsSuspendedByInstanceId(String processInstanceId) {
        return false;
    }

    @Override
    public void modifyInstanceStatusByProcessInstanceId(String tenantId, String processInstanceId,
        String status, String currentUserId) {

    }

    @Override
    public boolean checkProcessInstanceStatus(String processInstanceIds, String status) {
        return false;
    }

    @Override
    public ProcessInstance getBasicInstanceByProcessInstanceId(String processInstanceId,
        String tenantId) {
        return null;
    }

    /**
     * 未结流程.
     */
    public Page findRunningProcessInstances(String userId, String tenantId, Page page) {
        return null;
    }

    @Override
    public Page findRunningProcessInstancesWithoutUserId(String tenantId, Page page) {
        return null;
    }

    /**
     * 已结流程.
     */
    @Override
    public Page findCompletedProcessInstances(String userId, String tenantId, Page page) {
        return null;
    }

    @Override
    public Page findCompletedProcessInstancesWithoutUserId(String tenantId, Page page) {
        return null;
    }

    /**
     * 通过过滤条件查询已结流程
     * 
     * @param userId
     *            用户ID
     * @param tenantId
     *            租户ID
     * @param processInstanceName
     *            流程实例名称
     * @param date
     *            日期
     * @param page
     *            分页对象
     * @return page
     */
    @Override
    public Page findCompletedProcessInstancesByFilter(String userId, String tenantId,
                                                      String processInstanceName, Date date, Page page) {
        return null;
    }

    @Override
    public List<HistoricProcessInstance> findRunningProcessInstancesByFilter(String userId,
        String tenantId, String processInstanceName, Date date) {
        return null;
    }

    /**
     * 参与流程.
     */
    public Page findInvolvedProcessInstances(String userId, String tenantId, Page page) {
        return null;
    }

    /**
     * 待办任务（个人任务）.
     */
    public Page findPersonalTasks(String userId, String tenantId, Page page) {
        return null;
    }

    /**
     * 代领任务（组任务）.
     */
    public Page findGroupTasks(String userId, String tenantId, Page page) {
        return null;
    }

    /**
     * 已办任务（历史任务）.
     */
    public Page findHistoryTasks(String userId, String tenantId, Page page) {
        return null;
    }

    /**
     * 代理中的任务（办理人还未完成该任务）.
     */
    public Page findDelegatedTasks(String userId, String tenantId, Page page) {
        return null;
    }

    /**
     * 同时返回已领取和未领取的任务.
     */
    public Page findCandidateOrAssignedTasks(String userId, String tenantId, Page page) {
        return null;
    }

    /**
     * 流程定义.
     */
    @Override
    public Page findProcessDefinitions(String tenantId, Page page) {
        return null;
    }

    /**
     * 流程实例.
     */
    @Override
    public Page findProcessInstances(String tenantId, Page page) {
        return null;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#findProcessInstances(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.Boolean, com.cesgroup.core.page.Page)
     */
    @Override
    public List<HistoricProcessInstance> findProcessInstances(String tenantId, String userId,
        String processInstanceName, String startDate, Boolean isActive, Page page) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 任务.
     */
    public Page findTasks(String tenantId, Page page) {
        return null;
    }

    /**
     * 部署.
     */
    public Page findDeployments(String tenantId, Page page) {
        return null;
    }

    /**
     * 历史流程实例.
     */
    public Page findHistoricProcessInstances(String tenantId, Page page) {
        return null;
    }

    /**
     * 历史节点.
     */
    public Page findHistoricActivityInstances(String tenantId, Page page) {
        return null;
    }

    /**
     * 历史任务.
     */
    public Page findHistoricTaskInstances(String tenantId, Page page) {
        return null;
    }

    /**
     * 作业.
     */
    public Page findJobs(String tenantId, Page page) {
        return null;
    }

    @Override
    public long countFinishedTask(String tenantId, String userId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object getRuntimeVariableValue(String executionId, String variableName) {
        return null;
    }

    public ActivityImpl getActivityImplByProDefIdAndExecutionId(String processDefinitionId,
        String executionId) {
        return null;
    }

    @Override
    public String queryProcessNameByDefinitionId(String processDefinitionId, String tenantId) {
        return null;
    }

    @Override
    public BpmConfBase queryBpmConfBaseListByDefinitionId(String processDefinitionIdVal) {
        return null;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#getHasdoBox(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getHasdoBox(String tenantId, String processDefinitionKey,
        String userId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#getCompleteBox(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getCompleteBox(String tenantId, String processDefinitionKey,
        String userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String startProcessInstance(String tenantId, String userId, String businessKey,
        String processDefinitionKey, Map<String, Object> processParameters, Boolean autoCommit,
        String currentUserId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String startProcessInstance(String tenantId, String userId, String businessKey,
        String processDefinitionKey, Map<String, Object> processParameters, String currentUserId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String startProcessInstance(String tenantId, String userId, String processDefinitionKey,
        Map<String, Object> processParameters, Boolean autoCommit, String currentUserId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String startProcessInstance(String tenantId, String userId, String processDefinitionKey,
        Map<String, Object> processParameters, String currentUserId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#
     *      queryRunningProcessTaskAndAssigneeByProcessInstanceId(java.lang.String)
     */
    @Override
    public List<Map<String, Object>> queryRunningProcessTaskAndAssigneeByProcessInstanceId(
        String processInstanceId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#checkVariableUnique(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public boolean checkVariableUnique(String executionId, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#getDefinitionEntityById(java.lang.String)
     */
    @Override
    public ProcessDefinitionEntity getDefinitionEntityById(String processDefinitionId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#setVariable(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void setRuntimeVariableVariable(String executionId, String name, String value) {
        // TODO Auto-generated method stub

    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#getRuntimeVariables(java.lang.String)
     */
    @Override
    public Map<String, Object> getRuntimeVariables(String executionId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#
     *      getTaskListByProcessInstanceId(java.lang.String)
     */
    @Override
    public List<Task> getTaskListByProcessInstanceId(String processInstanceId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#
     *      getProcessDefinitionEntityByProcessDefinitionId(java.lang.String)
     */
    @Override
    public ProcessDefinitionEntity getProcessDefinitionEntityByProcessDefinitionId(
        String processDefinitionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long countCompleteTaskByProcessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#countCompleteTaskByProcessDefinitionKey(
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public long countCompleteTaskByProcessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey, String filter) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Page takenTaskList(String tenantId, String processDefinitionKey, String userId,
        String filter, Page page) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String startProcessByModelId(String userId, String businessKey, String modelId,
        Map<String, Object> processParameters, String currentUserId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * 
     * @see com.cesgroup.api.process.ProcessConnector#
     * startProcessByModelId(
     *     java.lang.String, java.lang.String, java.lang.String, java.util.Map,
     *     java.lang.Boolean) 
     */  
    @Override
    public String startProcessByModelId(String userId, String businessKey, String modelId,
        Map<String, Object> processParameters, Boolean autoCommit, String currentUserId) 
                throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void suspendProcessDefinitionByModelId(String modelId) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getModelIdByProcessInstanceId(String processInstanceId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<BpmConfNode> getNodesByModelId(String modelId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void activeProcessDefinitionByModelId(String modelId) {
        // TODO Auto-generated method stub

    }

    @Override
    public long countTakenTaskByProcessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#countTakenTaskByProcessDefinitionKey(
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public long countTakenTaskByProcessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey, String filter) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Page completeTaskList(String tenantId, String processDefinitionKey, String userId,
        String filter, Page page) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#countRunningProcessInstance(java.lang.String)
     */
    @Override
    public long countRunningProcessInstance(String tenantId) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * 
     * @see com.cesgroup.api.process.ProcessConnector#countFinishedProcessInstance(java.lang.String)
     */
    @Override
    public long countFinishedProcessInstance(String tenantId) {
        // TODO Auto-generated method stub
        return 0;
    }


    /** 
     * 
     * @see com.cesgroup.api.process.ProcessConnector#
     * countTakenTaskByProcessDefinitionKeys(
     *     java.lang.String, java.lang.String, java.util.List, java.lang.String) 
     */  
    @Override
    public long countTakenTaskByProcessDefinitionKeys(String tenantId, String userId,
        List<String> processDefinitionKeys, String filter) {
        // TODO Auto-generated method stub
        return 0;
    }

    /** 
     * 
     * @see com.cesgroup.api.process.ProcessConnector#
     * countCompleteTaskByProcessDefinitionKeys(
     *     java.lang.String, java.lang.String, java.util.List) 
     */  
    @Override
    public long countCompleteTaskByProcessDefinitionKeys(String tenantId, String userId,
        List<String> processDefinitionKeys) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Map<String, String>> queryNextActivityListByModelId(String modelId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean judgeProcessEnd(String processInstanceId) {
        return null;
    }

}
