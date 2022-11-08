package com.cesgroup.api.core.workflow;

import com.cesgroup.api.core.workflow.interf.WorkflowAPI;
import com.cesgroup.api.form.FormDTO;
import com.cesgroup.api.humantask.HumanTaskConnector;
import com.cesgroup.api.humantask.HumanTaskDTO;
import com.cesgroup.api.humantask.RollbackActivityDTO;
import com.cesgroup.api.process.ProcessConnector;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.bpm.cmd.*;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import com.cesgroup.bpm.persistence.manager.BpmConfBaseManager;
import com.cesgroup.bpm.persistence.manager.BpmConfUserManager;
import com.cesgroup.bpm.persistence.manager.BpmProcessManager;
import com.cesgroup.bpm.proxy.CesProcessEngine;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.NodeConfInfo;
import com.cesgroup.humantask.persistence.domain.TaskInfo;
import com.cesgroup.humantask.persistence.manager.TaskInfoManager;
import com.cesgroup.sync.process.service.interf.CesSyncProcess4TenantService;
import com.cesgroup.workflow.configuration.service.interf.CesListenerConfigService;
import com.cesgroup.workflow.dto.VoteDTO;
import com.cesgroup.workflow.manager.WorkflowListenerManager;
import com.cesgroup.workflow.persistence.domain.WorkflowListener;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工作流通用接口实现类
 *
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class WorkflowAPIImpl implements WorkflowAPI {

    @Autowired
    private CesProcessEngine processEngine;

    @Autowired
    private HumanTaskConnector humanTaskConnector;

    @Autowired
    private ProcessConnector processConnector;

    @Autowired
    private BpmConfBaseManager bpmConfBaseManager;

    @Autowired
    private TaskInfoManager taskInfoManager;

    @Autowired
    private BpmConfUserManager bpmConfUserManager;

    @Autowired
    private BpmProcessManager bpmProcessManager;

    @Autowired
    private CesSyncProcess4TenantService cesSyncProcess4TenantService;

    @Autowired
    private WorkflowListenerManager workflowListenerManager;

    @Autowired
    private CesListenerConfigService cesListenerConfigService;

    @Override
    public HistoryService getHistoryService() {
        return processEngine.getHistoryService();
    }

    @Override
    public ManagementService getManagementService() {
        return processEngine.getManagementService();
    }

    @Override
    public RepositoryService getRepositoryService() {
        return processEngine.getRepositoryService();
    }

    @Override
    public RuntimeService getRuntimeService() {
        return processEngine.getRuntimeService();
    }

    @Override
    public HumanTaskDTO findHumanTask(String humanTaskId) {
        return humanTaskConnector.findHumanTask(humanTaskId);
    }

    @Override
    public void claimTask(String humanTaskId, String userId, String currentUserId) {
        humanTaskConnector.claimTask(humanTaskId, userId, currentUserId);
    }

    @Override
    public FormDTO findTaskForm(String humanTaskId) {
        return humanTaskConnector.findTaskForm(humanTaskId);
    }

    @Override
    public void saveHumanTask(HumanTaskDTO humanTaskDto, boolean triggerListener) {
        humanTaskConnector.saveHumanTask(humanTaskDto, triggerListener);
    }

    @Override
    public List<HumanTaskDTO> findSubTasks(String humanTaskId) {
        return humanTaskConnector.findSubTasks(humanTaskId);
    }

    @Override
    public FormDTO findStartForm(String processDefinitionId) {
        return processConnector.findStartForm(processDefinitionId);
    }

    @Override
    public BpmConfBase findUniqueConfBaseBy(String name, String value) {
        return bpmConfBaseManager.findUniqueBy(name, value);
    }

    @Override
    public TaskInfo queryTaskInfoByProcessInstanceId(String processInstanceId) {
        return taskInfoManager.queryTaskInfoByProcessInstanceId(processInstanceId);
    }

    @Override
    @Deprecated
    public String startProcessInstance(String userId, String businessKey,
        String processDefinitionId, Map<String, Object> processParameters,
        String currentUserId) throws Exception {
        return processConnector.startProcess(userId, businessKey, processDefinitionId,
            processParameters, currentUserId);
    }

    @Override
    public void completeTask(String humanTaskId, String userId, String action, Boolean agreement, String comment,
        Map<String, Object> taskParameters, String currentUserId) throws Exception {
        humanTaskConnector.completeTask(humanTaskId, userId, action, agreement, comment, taskParameters,
            currentUserId);
    }

    @Override
    public void completeTask(String humanTaskId, String userId, String action, String comment,
        Map<String, Object> taskParameters, String currentUserId) throws Exception {
        humanTaskConnector.completeTask(humanTaskId, userId, action, true, comment, taskParameters,
            currentUserId);
    }

    @Override
    public void completeTask(String humanTaskId, String userId, String comment,
        Map<String, Object> taskParameters,
        String currentUserId) throws Exception {

        humanTaskConnector.completeTask(humanTaskId, userId, WorkflowConstants.HumanTaskConstants.ACTION_COMPLETE,
            true, comment, taskParameters, currentUserId);
    }

    @Override
    public TaskService getTaskService() {
        return processEngine.getTaskService();
    }

    @Override
    public void submitTaskInfoAuto(String processInstanceId, String userId,
        Map<String, Object> taskParameters,
        String currentUserId) throws Exception {
        final TaskInfo nextTaskInfo = queryTaskInfoByProcessInstanceId(processInstanceId);
        completeTask(String.valueOf(nextTaskInfo.getId()), userId,
            WorkflowConstants.HumanTaskConstants.ACTION_COMPLETE, "完成",
            taskParameters, currentUserId);
    }

    @Override
    public void saveAutoCommitTask(String humanTaskId) {
        humanTaskConnector.saveAutoCommitTask(humanTaskId);
    }

    @Override
    public void saveTaskCommentByHumanTaskId(String humanTaskId, String submitComment) {
        humanTaskConnector.saveTaskCommentByHumanTaskId(humanTaskId, submitComment);
    }

    @Override
    public void rollbackPrevious(String humanTaskId, String comment, Map<String, Object> variables,
        String currentUserId) {
        humanTaskConnector.rollbackPrevious(humanTaskId, comment, variables, currentUserId);
    }

    @Override
    public void rollbackActivity(String humanTaskId, String activityId, String comment,
        Map<String, Object> variables, String currentUserId) {
        humanTaskConnector.rollbackActivity(humanTaskId, activityId, comment,
            variables, currentUserId);
    }

    @Override
    public void rollbackAssignee(String humanTaskId, String comment, Map<String, Object> variables,
        String currentUserId) {
        humanTaskConnector.rollbackAssignee(humanTaskId, comment, variables, currentUserId);
    }

    @Override
    public void withdraw(String humanTaskId, String comment, String currentUserId) {
        humanTaskConnector.withdraw(humanTaskId, comment, currentUserId);
    }

    @Override
    public boolean withdrawCheck(String humanTaskId) {
        return humanTaskConnector.withdrawCheck(humanTaskId);
    }

    @Override
    public boolean withdrawCheck2(String humanTaskId) {
        return humanTaskConnector.withdrawCheck2(humanTaskId);
    }

    @Override
    public void transfer(String humanTaskId, String userId, String comment, String currentUserId,
        String currentTenantId) {
        humanTaskConnector.transfer(humanTaskId, userId, comment, currentUserId, currentTenantId);
    }

    @Override
    public boolean remindTaskInfoByHumanTaskIds(String humanTaskIds, String currentTenantId) {
        return humanTaskConnector.remindTaskInfoByHumanTaskIds(humanTaskIds, currentTenantId);
    }

    @Override
    public void communicate(String humanTaskId, Set<String> userSet, String comment,
        String currentUserId) {
        humanTaskConnector.communicate(humanTaskId, userSet, comment, currentUserId);
    }

    @Override
    public void react(String humanTaskId, String comment) {
        humanTaskConnector.react(humanTaskId, comment);
    }

    @Override
    public void addVote(String humanTaskId, Set<String> userIdSet) {
        humanTaskConnector.addVote(humanTaskId, userIdSet);
    }

    @Override
    public boolean checkHumanTaskIsExistOrSuspend(String humanTaskIds) {
        return humanTaskConnector.checkHumanTaskIsExistOrSuspend(humanTaskIds);
    }

    @Override
    public boolean copyTaskInfo(String processInstanceId, Set<String> userSet, String comment,
        String currentUserId) {
        return humanTaskConnector.copyTaskInfo(processInstanceId, userSet, comment, currentUserId);
    }

    @Override
    public boolean viewTaskByHumanTaskIds(String humanTaskIds) {
        return humanTaskConnector.viewTaskByHumanTaskIds(humanTaskIds);
    }

    @Override
    public boolean completeTaskByHumanTaskIds(String humanTaskIds, String currentUserId, String currentUserName, String agreement, String comment) {
        return humanTaskConnector.completeTaskByHumanTaskIds(humanTaskIds, currentUserId, currentUserName, agreement, comment);
    }

    @Override
    public void checkHumanTaskParticipants(String humanTaskId) {
        humanTaskConnector.checkHumanTaskParticipants(humanTaskId);
    }

    @Override
    public void releaseTask(String humanTaskId, String comment, String currentUserId) {
        humanTaskConnector.releaseTask(humanTaskId, comment, currentUserId);
    }

    @Override
    public List<NodeConfInfo> getFakeNodeConfInfoByTaskIdAndMap(String tenantId,
        String processDefinitionKey,
        String activityId, Map<String, Object> map) {
        final ProcessDefinition processDefinition = this.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .processDefinitionTenantId(tenantId).active().singleResult();
        if (null != processDefinition) {
            final Command<List<NodeConfInfo>> cmd = new FindNewNextActivitiesCmd(
                processDefinition.getId(), activityId, map, tenantId);
            return processEngine.getManagementService().executeCommand(cmd);
        } else {
            throw new RuntimeException("当前没有激活的模型版本");
        }

    }

    @Override
    public List<NodeConfInfo> getFakeNodeConfInfoByProcessDefinitionIdAndMap(
        String tenantId, String processDefinitionId,
        String activityId, Map<String, Object> map) {
        final ProcessDefinition processDefinition = this.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionId(processDefinitionId)
            .processDefinitionTenantId(tenantId).singleResult();
        if (null != processDefinition) {
            final Command<List<NodeConfInfo>> cmd = new FindNewNextActivitiesCmd(
                processDefinition.getId(), activityId, map, tenantId);
            return processEngine.getManagementService().executeCommand(cmd);
        } else {
            throw new RuntimeException("模型版本[" + processDefinitionId + "]找不到");
        }
    }

    /**
     *
     * @see com.cesgroup.api.core.workflow.interf.WorkflowAPI#checkGraphIsConfMajorLine()
     */
    @Override
    public Boolean checkGraphIsConfMajorLine(String processDefinitionId) {
        final Command<Boolean> cmd = new CheckGraphIsConfMajorLineCmd(processDefinitionId);
        return processEngine.getManagementService().executeCommand(cmd);
    }

    @Override
    public ProcessDefinition findProcessDefinitionById(String processDefinitionId) {
        return processConnector.getDefinitionEntityById(processDefinitionId);
    }

    @Override
    public CesProcessEngine getProcessEngine() {
        return processEngine;
    }

    public void setProcessEngine(CesProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
    }

    public void setBpmConfBaseManager(BpmConfBaseManager bpmConfBaseManager) {
        this.bpmConfBaseManager = bpmConfBaseManager;
    }

    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }

    public void setBpmConfUserManager(BpmConfUserManager bpmConfUserManager) {
        this.bpmConfUserManager = bpmConfUserManager;
    }

    public void setBpmProcessManager(BpmProcessManager bpmProcessManager) {
        this.bpmProcessManager = bpmProcessManager;
    }

    @Override
    public List<Map<String, String>> getVoteUsers(String humanTaskId, String currentUserId) {
        return humanTaskConnector.getVoteUsers(humanTaskId, currentUserId);
    }

    @Override
    public String startProcessInstanceByprocessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey, Map<String, Object> processParameters,
        String currentUserId) {
        return processConnector.startProcessInstance(tenantId, userId, processDefinitionKey,
            processParameters,
            currentUserId);
    }

    @Override
    public String startProcessInstanceByprocessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey, Map<String, Object> processParameters, Boolean autoCommit,
        String currentUserId) {
        return processConnector.startProcessInstance(tenantId, userId, processDefinitionKey,
            processParameters,
            autoCommit, currentUserId);
    }

    @Override
    public String startProcessInstanceByprocessDefinitionKey(String tenantId,
        String userId, String businessKey, String processDefinitionKey,
        Map<String, Object> processParameters, Boolean autoCommit,
        String currentUserId) {
        return processConnector.startProcessInstance(tenantId, userId, businessKey,
            processDefinitionKey, processParameters, autoCommit, currentUserId);
    }

    @Override
    public String startProcessInstanceByprocessDefinitionKey(String tenantId,
        String userId, String businessKey, String processDefinitionKey,
        Map<String, Object> processParameters, String currentUserId) {
        return processConnector.startProcessInstance(tenantId, userId,
            businessKey, processDefinitionKey, processParameters, currentUserId);
    }

    @Override
    public boolean removeVote(String humanTaskId, String params, String currentUserId) {
        // 判断是串行的还是并行的
        final String voteType = getHumanTaskVoteType(humanTaskId);
        if (WorkflowConstants.HumanTaskConstants.COUNTERSIGN_SEQUENTIAL.equals(voteType)) {
            checkHumanTaskIsExistOrSuspend(humanTaskId);
            return removeSequentialVote(params, humanTaskId);
        } else if (WorkflowConstants.HumanTaskConstants.COUNTERSIGN_PARALLEL.equals(voteType)) {
            checkHumanTaskIsExistOrSuspend(params);
            return removeParallelVote(currentUserId, params);
        }
        return false;
    }

    /**
     * 获取任务的会签类型
     *
     * @param humantaskId
     *            任务ID
     * @return parallel：并行/sequential：串行
     */
    private String getHumanTaskVoteType(String humantaskId) {
        // 1. 获取当前节点
        final TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(humantaskId));
        // 2. 判断是否是会签节点, 不是会签节点直接返回
        if (!WorkflowConstants.YES.equals(taskInfo.getIsCountersign())) {
            return null;
        }

        final String processDefinitionId = taskInfo.getProcessDefinitionId();
        final ProcessDefinitionEntity processDefinitionEntity = processConnector
            .getProcessDefinitionEntityByProcessDefinitionId(processDefinitionId);
        final ActivityImpl activityImpl = processDefinitionEntity.findActivity(taskInfo.getCode());
        final String multiInstance = activityImpl.getProperty("multiInstance") == null ? ""
            : activityImpl.getProperty("multiInstance").toString();
        return multiInstance;
    }

    /**
     * 串行会签减签
     *
     * @param removeIds
     *            用户ID集合
     * @param humantaskId
     *            任务ID
     * @return boolean
     */
    private boolean removeSequentialVote(String removeIds, String humantaskId) {
        final Set<String> users = new HashSet<String>();
        final String[] userList = removeIds.split(",");
        for (final String userId : userList) {
            users.add(userId);
        }
        if (users.size() > 0) {
            return humanTaskConnector.removeSequentialVote(users, humantaskId);
        } else {
            return false;
        }
    }

    /**
     * 并行会签减签
     *
     * @param userId
     *            用户ID
     * @param humanTaskIds
     *            任务ID集合
     * @return boolean
     */
    private boolean removeParallelVote(String userId, String humanTaskIds) {
        return humanTaskConnector.removeVote(userId, humanTaskIds);
    }

    @Override
    public long countTakenTaskByProcessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey) {
        return processConnector.countTakenTaskByProcessDefinitionKey(tenantId, userId,
            processDefinitionKey);
    }

    @Override
    public HistoricProcessInstance queryProcessInstanceByProcessInstanceId(
        String processInstanceId) {
        return getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(
            processInstanceId)
            .singleResult();
    }

    @Override
    public Set<UserDTO> findUsersByTaskInfoId(String taskInfoId) {
        return humanTaskConnector.findUsersByTaskInfoId(taskInfoId);
    }

    @Override
    public String getStartActivityId(String engineId) {
        return bpmConfBaseManager.getStartActivityId(engineId);
    }

    @Override
    public List<String> getStartBehindActivityIds(String engineId) {
        return bpmConfBaseManager.getStartBehindActivityIds(engineId);
    }

    @Override
    public List<String> getEndActivityIds(String engineId) {
        return bpmConfBaseManager.getEndActivityIds(engineId);
    }

    @Override
    public Boolean checkIsBeginBehindNodeByHumantaskId(String humantaskId) {
        final HumanTaskDTO humanTaskDTO = humanTaskConnector.findHumanTask(humantaskId);
        final String processDefinitionId = humanTaskDTO.getProcessDefinitionId();
        final ProcessDefinitionEntity processDefinitionEntity = processConnector
            .getProcessDefinitionEntityByProcessDefinitionId(processDefinitionId);
        final ActivityImpl activity = processDefinitionEntity.findActivity(humanTaskDTO.getCode());
        final List<PvmTransition> incomingTransitions = activity.getIncomingTransitions();
        // 获取当前节点的来源
        for (final PvmTransition pvmTransition : incomingTransitions) {
            if ("startEvent".equals(pvmTransition.getSource().getProperty("type"))) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @see com.cesgroup.api.core.workflow.interf.WorkflowAPI#
     *      findPreviousActivityImpl(java.lang.String)
     */
    @Override
    public PvmActivity findPreviousActivityImplByHumantaskId(String humantaskId) {
        final HumanTaskDTO humanTaskDTO = humanTaskConnector.findHumanTask(humantaskId);
        // 查找当前节点上一个节点的cmd
        final FindPreviousActivityImplCmd previousActivityImplCmd = new FindPreviousActivityImplCmd(
            humanTaskDTO.getTaskId(), null);
        return processEngine.getManagementService().executeCommand(previousActivityImplCmd);
    }

    /**
     *
     * @see com.cesgroup.api.core.workflow.interf.WorkflowAPI#findNextActivityImpls(
     *      java.lang.String)
     */
    @Override
    public List<PvmActivity> findNextActivityImplByHumantaskId(String humantaskId) {
        final HumanTaskDTO humanTaskDTO = humanTaskConnector.findHumanTask(humantaskId);
        final FindNextActivitiesCmd findNextActivitiesCmd = new FindNextActivitiesCmd(
            humanTaskDTO.getProcessDefinitionId(), humanTaskDTO.getCode());
        return processEngine.getManagementService().executeCommand(findNextActivitiesCmd);
    }

    /**
     *
     * @see com.cesgroup.api.core.workflow.interf.WorkflowAPI#
     *      findActivityImplByHumantaskId(java.lang.String)
     */
    @Override
    public PvmActivity findActivityImplByHumantaskId(String humantaskId) {
        final HumanTaskDTO humanTaskDTO = humanTaskConnector.findHumanTask(humantaskId);
        final GetDeploymentProcessDefinitionCmd getDeploymentProcessDefinitionCmd = new GetDeploymentProcessDefinitionCmd(
            humanTaskDTO.getProcessDefinitionId());
        final ProcessDefinitionEntity processDefinitionEntity = processEngine.getManagementService()
            .executeCommand(getDeploymentProcessDefinitionCmd);
        return processDefinitionEntity.findActivity(humanTaskDTO.getCode());
    }

    @Override
    public Boolean checkIsCountersignTask(String humantaskId) {
        final HumanTaskDTO humanTaskDTO = humanTaskConnector.findHumanTask(humantaskId);
        final String taskId = humanTaskDTO.getTaskId();
        final Command<Boolean> cmd = new CheckIsCountersignTaskCmd(taskId);
        return processEngine.getManagementService().executeCommand(cmd);
    }

    @Override
    public void syncProcess4Tenant(String sourceTenantId, String targetTenantId) throws Exception {
        cesSyncProcess4TenantService.syncProcess4Tenant(sourceTenantId, targetTenantId);
    }

    @Override
    public List<WorkflowListener> queryWorkflowListenerByCategory(String category) {
        return workflowListenerManager.queryWorkflowListenerByCategory(category);
    }

    @Override
    public WorkflowListener createOrUpdateWorkflowListener(WorkflowListener workflowListener) {
        return cesListenerConfigService.createOrUpdateWorkflowListener(workflowListener);
    }

    @Override
    public Boolean removeWorkflowListenerByIds(List<String> idList) {
        return cesListenerConfigService.removeWorkflowListenerByIds(idList);
    }

    @Override
    public Boolean checkUnique(WorkflowListener workflowListener) {
        return cesListenerConfigService.checkUnique(workflowListener);
    }

    @Override
    public Boolean findActivityImplSubmitTaskWithPerson(String humanTaskId, String modelId) {
        return humanTaskConnector.findActivityImplSubmitTaskWithPerson(humanTaskId, modelId);
    }

    @Override
    public List<Map<String, String>> queryNextActivityListByHumanTaskId(String humanTaskId) {
        final HumanTaskDTO humanTaskDTO = humanTaskConnector.findHumanTask(humanTaskId);
        return bpmProcessManager.queryNextActivityListByHumanTaskId(humanTaskDTO.getExecutionId());
    }

    @Override
    public Boolean compareLoginAndAssignee(String humanTaskId, String currentUserId) {
        return humanTaskConnector.compareLoginAndAssignee(humanTaskId, currentUserId);
    }

    @Override
    public List<Map<String, String>> queryNextActivityListByModelId(String modelId) {
        return processConnector.queryNextActivityListByModelId(modelId);
    }

    @Override
    public String queryProcessStarter(String processInstanceId) {
        return humanTaskConnector.queryProcessStarter(processInstanceId);
    }

    @Override
    public String judgeSubmitTaskWithPerson(String humanTaskId, String modelId) {
        return humanTaskConnector.judgeSubmitTaskWithPerson(humanTaskId, modelId);
    }

    @Override
    public Boolean judgeSubmitTaskWithPersonExclusive(String modelId, String humanTaskId) {
        return humanTaskConnector.judgeSubmitTaskWithPersonExclusive(modelId, humanTaskId);
    }

    @Override
    public List<String> getUsersByCandidateByUserId(String currentUserId, String humanTaskId, String customRule) {
        return humanTaskConnector.getUsersByCandidateByUserId(currentUserId, humanTaskId, customRule);
    }

    @Override
    public List<String> getUsersByCandidateByNodeIdAndUserId(String humanTaskId, String customRule) {
        return humanTaskConnector.getUsersByCandidateByNodeIdAndUserId(humanTaskId, customRule);
    }

    @Override
    public List<String> getUsersByCandidateByOrgId(String customRule) {
        return humanTaskConnector.getUsersByCandidateByOrgId(customRule);
    }

    @Override
    public List<Map<String, Object>> getAssigneesByProcessInstanceId(String processInstanceId) {
        return humanTaskConnector.getAssigneesByProcessInstanceId(processInstanceId);
    }

    @Override
    public String queryProcessdefinitionIdByModelId(String modelId) {
        return humanTaskConnector.queryProcessdefinitionIdByModelId(modelId);
    }

    @Override
    public Boolean judgeProcessEnd(String processInstanceId) {
        return processConnector.judgeProcessEnd(processInstanceId);
    }

    @Override
    public Boolean judgeNextSubProcess(String humanTaskId, String modelId) {
        return humanTaskConnector.judgeNextSubProcess(humanTaskId, modelId);
    }

    /**
     *
     * @throws IOException
     * @see com.cesgroup.api.core.workflow.interf.WorkflowAPI#getGraphImageByProcessInstanceId(java.lang.String)
     */
    @Override
    public String getGraphImageByProcessInstanceId(String processInstanceId) throws IOException {
        return bpmProcessManager.getImageByProcessInstanceId(processInstanceId);
    }

    /**
     *
     * @see com.cesgroup.api.core.workflow.interf.WorkflowAPI#getRollbackActivityListByProcessInstanceIdAndActivityId(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public List<?> getRollbackActivityListByProcessInstanceIdAndActivityId(String processInstanceId,
        String activityId) {
        final List<RollbackActivityDTO> pvmActivityList = processEngine.getManagementService()
            .executeCommand(new FindRollbackActivitiesCmd(processInstanceId, activityId));
        return pvmActivityList;
    }

	@Override
	public VoteDTO getVote(String processInstanceId, String activityId) {
		if (judgeProcessEnd(processInstanceId)){
			List<HistoricVariableInstance> list = processEngine.getHistoryService().createHistoricVariableInstanceQuery().
					processInstanceId(processInstanceId).variableName("T_WF_VOTE_" + activityId).list();
			if (list != null && list.size() == 1) {
				return (VoteDTO) list.get(0).getValue();
			}else{
				throw new RuntimeException("获取投票结果失败");
			}
		}else{
			return (VoteDTO) processEngine.getRuntimeService().getVariable(processInstanceId, "T_WF_VOTE_" + activityId);
		}
	}
}
