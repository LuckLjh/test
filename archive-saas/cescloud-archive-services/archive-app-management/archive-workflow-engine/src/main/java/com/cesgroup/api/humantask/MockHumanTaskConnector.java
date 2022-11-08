package com.cesgroup.api.humantask;

import com.cesgroup.api.form.FormDTO;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.core.page.Page;
import com.cesgroup.core.query.PropertyFilter;
import com.cesgroup.humantask.persistence.domain.TaskInfo;
import org.activiti.engine.task.Task;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模拟人工任务连接器
 *
 * @author 国栋
 *
 */
public class MockHumanTaskConnector implements HumanTaskConnector {

    @Override
    public HumanTaskDTO createHumanTask() {
        return null;
    }

    @Override
    public void removeHumanTask(String humanTaskId) {
    }

    @Override
    public boolean checkHumanTaskIsExistOrSuspend(String humanTaskIds) {
        return false;
    }

    @Override
    public void react(final String humanTaskId, final String comment) {

    }

    @Override
    public boolean copyTaskInfo(final String processInstanceId, final Set<String> userSet,
        final String comment, final String currentUserId) {
        return false;
    }

    @Override
    public boolean remindTaskInfoByHumanTaskIds(final String humanTaskIds,
        final String currentTenantId) {
        return false;
    }

    @Override
    public void saveTaskCommentByHumanTaskId(final String humanTaskId, final String submitComment) {

    }

	@Override
	public boolean completeTaskByHumanTaskIds(String humanTaskIds, String currentUserId, String currentUserName, String agreement) {
		return false;
	}

	@Override
	public boolean completeTaskByHumanTaskIds(String humanTaskIds, String currentUserId, String currentUserName, String agreement, String context) {
		return false;
	}

	@Override
    public void removeHumanTaskByTaskId(String taskId) {
    }

    public void removeHumanTaskByProcessInstanceId(String processInstanceId) {
    }

    @Override
    public List<Map<String, Object>> queryHistoryTaskWithCommentByInstanceId(
        String processInstanceId) {
        return null;
    }

    @Override
    public List<Map<String, Object>> queryHistoryTaskWithCommentByInstanceId(
        String processInstanceId, String tableName, String keyField, String... args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void checkHumanTaskParticipants(String humanTaskId) {

    }

    @Override
    public boolean viewTaskByHumanTaskIds(String humanTaskIds) {
        return false;
    }

    @Override
    public void saveAutoCommitTask(String humanTaskId) {

    }

    @Override
    public String findPresentationSubjectByTaskId(String taskId) {
        return null;
    }

    public Page findUnReadCopyTasks(String userId, String tenantId, int pageNo, int pageSize) {
        return null;
    }

    public Page findHasReadCopyTasks(String userId, String tenantId, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto) {
        return null;
    }

    @Override
    public HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto, boolean triggerListener) {
        return null;
    }

    public HumanTaskDTO saveHumanTaskAndProcess(HumanTaskDTO humanTaskDto) {
        return null;
    }

    @Override
    public HumanTaskDTO findHumanTaskByTaskId(String taskId) {
        return null;
    }

    @Override
    public List<HumanTaskDTO> findHumanTasksByProcessInstanceId(String processInstanceId) {
        return null;
    }

    @Override
    public List<HumanTaskDTO> findSubTasks(String parentTaskId) {
        return null;
    }

    @Override
    public List<HumanTaskDTO> findActiveSubTasks(final String parentTaskId) {
        return null;
    }

    @Override
    public HumanTaskDTO findHumanTask(String humanTaskId) {
        return null;
    }

    @Override
    public FormDTO findTaskForm(String humanTaskId) {
        return null;
    }

    public List<HumanTaskDefinition> findHumanTaskDefinitions(String processDefinitionId) {
        return null;
    }

    public void configTaskDefinitions(String businessKey, List<String> taskDefinitionKeys,
        List<String> taskAssigness) {
    }

    public void completeTask(String humanTaskId, String userId, String action, String comment,
        Map<String, Object> taskParameters, String currentUserId) {
    }

    public Page findPersonalTasks(String userId, String tenantId, int pageNo, int pageSize) {
        return null;
    }

    public Page findFinishedTasks(String userId, String tenantId, int pageNo, int pageSize) {
        return null;
    }

    public Page findGroupTasks(String userId, String tenantId, int pageNo, int pageSize) {
        return null;
    }

    public Page findDelegateTasks(String userId, String tenantId, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public void claimTask(String humanTaskId, String userId, String currentUserId) {
    }

    @Override
    public void releaseTask(String humanTaskId, String comment, String currentUserId) {
    }

    @Override
    public void transfer(String humanTaskId, String userId, String comment, String currentUserId,
        String currentTenantId) {
    }

    public void cancel(String humanTaskId, String userId, String comment) {
    }

    /**
     * 回退，指定节点，重新分配.
     */
    @Override
    public void rollbackActivity(String humanTaskId, String activityId, String comment,
        Map<String, Object> variables, String currentUserId) {
    }

    /**
     * 回退，指定节点，上个执行人.
     */
    public void rollbackActivityLast(String humanTaskId, String activityId, String comment) {
    }

    /**
     * 回退，指定节点，指定执行人.
     */
    public void rollbackActivityAssignee(String humanTaskId, String activityId, String userId,
        String comment) {
    }

    /**
     * 回退，上个节点，重新分配.
     */
    @Override
    public void rollbackPrevious(String humanTaskId, String comment,
        Map<String, Object> variables, String currentUserId) {
    }

    @Override
    public Page queryDataByFilter(String userId, String tenantId, List<PropertyFilter> filters,
        Page page) {
        return null;
    }

    /**
     * 回退，上个节点，上个执行人.
     */
    public void rollbackPreviousLast(String humanTaskId, String comment) {
    }

    /**
     * 回退，上个节点，指定执行人.
     */
    public void rollbackPreviousAssignee(String humanTaskId, String userId, String comment) {
    }

    /**
     * 回退，开始事件，流程发起人.
     */
    public void rollbackStart(String humanTaskId, String comment) {
    }

    /**
     * 回退，流程发起人.
     */
    public void rollbackInitiator(String humanTaskId, String comment) {
    }

    @Override
    public void withdraw(String humanTaskId, String comment, String currentUserId) {
    }

    public void delegateTask(String humanTaskId, String userId, String comment) {
    }

    public void delegateTaskCreate(String humanTaskId, String userId, String comment) {
    }

    public void saveParticipant(ParticipantDTO participantDto) {
    }

    @Override
    public void communicate(String humanTaskId, Set<String> userSet, String comment,
        String currentUserId) {
    }

    public void callback(String humanTaskId, String comment) {
    }

    public void skip(String humanTaskId, String userId, String comment) {
    }

    @Override
    public void addVote(String humanTaskId, Set<String> userIds) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean removeVote(String currentUserId, String humanTaskIds) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public TaskInfo findActiveTaskInfoByProcessInstanceIdAndCode(String processInstanceId,
        String code) {
        return null;
    }

    /**
     * 流程干涉,选择节点跳转
     */
    @Override
    public void interfereJumpTask(String currentActivityId, String processInstanceId,
        String activityId, String userId, String assigneeValue,
        Map<String, Object> taskParameters, String currentUserId) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean withdrawCheck(String humanTaskId) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     *
     * @see com.cesgroup.api.humantask.HumanTaskConnector#replaceTaskAssignee(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void replaceTaskAssignee(String taskId, String userId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void jump(String humanTaskId, String activityId, String userId, String assigneeValue,
        Map<String, Object> taskParameters, String currentUserId) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean removeSequentialVote(Set<String> users, String humantaskId) {
        return false;
    }

    @Override
    public String checkHumanTaskVoteType(String humantaskId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Map<String, String>> getVoteUsers(String humanTaskId, String currentUserId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void endProcessInstanceByHumanTaskId(String humanTaskId) {
        // TODO Auto-generated method stub

    }

    /**
     *
     * @see com.cesgroup.api.humantask.HumanTaskConnector#withdrawCheck2(java.lang.String)
     */
    @Override
    public boolean withdrawCheck2(String humanTaskId) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     *
     * @see com.cesgroup.api.humantask.HumanTaskConnector#queryTimeOutTasks(java.lang.String,
     *      com.cesgroup.core.page.Page)
     */
    @Override
    public List<Task> queryTimeOutTasks(String tenantId, Page page) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *
     * @see com.cesgroup.api.humantask.HumanTaskConnector#countTimeoutTaskInstance(java.lang.String)
     */
    @Override
    public long countTimeoutTaskInstance(String tenantId) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *
     * @see com.cesgroup.api.humantask.HumanTaskConnector#findUsersByTaskInfoId(java.lang.String)
     */
    @Override
    public Set<UserDTO> findUsersByTaskInfoId(String taskInfoId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean findActivityImplSubmitTaskWithPerson(String humanTaskId, String modelId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean compareLoginAndAssignee(String humanTaskId, String currentUserId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String queryProcessStarter(String processInstanceId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String judgeSubmitTaskWithPerson(String humanTaskId, String modelId) {
        return null;
    }

    @Override
    public List<String> getUsersByCandidateByUserId(String currentUserId, String humanTaskId, String customRule) {
        return null;
    }

    @Override
    public List<String> getUsersByCandidateByNodeIdAndUserId(String humanTaskId, String customRule) {
        return null;
    }

    @Override
    public List<String> getUsersByCandidateByOrgId(String customRule) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getAssigneesByProcessInstanceId(String processInstanceId) {
        return null;
    }

    @Override
    public Boolean judgeSubmitTaskWithPersonExclusive(String modelId, String humanTaskId) {
        return null;
    }

    @Override
    public String queryProcessdefinitionIdByModelId(String modelId) {
        return null;
    }

    @Override
    public Boolean judgeNextSubProcess(String humanTaskId, String modelId) {
        return null;
    }

    /**
     *
     * @see com.cesgroup.api.humantask.HumanTaskConnector#completeTask(java.lang.String,
     *      java.lang.String, java.lang.String, boolean, java.lang.String,
     *      java.util.Map, java.lang.String)
     */
    @Override
    public void completeTask(String humanTaskId, String userId, String action, Boolean agreement, String comment,
        Map<String, Object> taskParameters, String currentUserId) throws Exception {

    }

    /**
     *
     * @see com.cesgroup.api.humantask.HumanTaskConnector#rollbackAssignee(java.lang.String,
     *      java.lang.String, java.util.Map, java.lang.String)
     */
    @Override
    public void rollbackAssignee(String humanTaskId, String comment, Map<String, Object> variables,
        String currentUserId) {
        // TODO Auto-generated method stub

    }
}
