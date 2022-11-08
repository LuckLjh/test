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
 * 人工任务连接器
 *
 * @author 国栋
 *
 */
public interface HumanTaskConnector {

    /**
     * 催办下一个人工节点
     *
     * @param humanTaskIds
     *            任务id的集合，逗号拼接
     * @return boolean
     */
    boolean remindTaskInfoByHumanTaskIds(String humanTaskIds, String currentTenantId);

    /**
     * 保存表单意见
     *
     * @param humanTaskId
     *            任务id
     * @param submitComment
     *            意见
     */
    void saveTaskCommentByHumanTaskId(String humanTaskId, String submitComment);

    /**
     * 批量终止任务
     *
     * @param humanTaskIds
     *            humanTaskId集合，逗号拼接
     * @return boolean
     */
    boolean completeTaskByHumanTaskIds(String humanTaskIds, String currentUserId, String currentUserName, String agreement);

    /**
     * 批量终止任务
     *
     * @param humanTaskIds
     *            humanTaskId集合，逗号拼接
     * @param context
     *            备注信息
     * @return boolean
     */
    boolean completeTaskByHumanTaskIds(String humanTaskIds, String currentUserId, String currentUserName, String agreement, String context);

    /**
     * 校验任务是否存在或者挂起
     *
     * @param humanTaskIds
     *            humanTaskId集合，逗号拼接
     * @return boolean
     */
    boolean checkHumanTaskIsExistOrSuspend(String humanTaskIds);

    /**
     * 创建任务.
     *
     * @return 人工任务dto
     */
    HumanTaskDTO createHumanTask();

    /**
     * 沟通内容回复
     *
     * @param humanTaskId
     *            任务id
     * @param comment
     *            意见
     */
    void react(String humanTaskId, final String comment);

    /**
     * 通过任务id删除人工任务
     *
     * @param taskId
     *            任务id
     */
    void removeHumanTaskByTaskId(String taskId);

    /**
     * 更新人工任务
     *
     * @param humanTaskDto
     *            人工任务dto对象
     * @return 人工任务dto
     */
    HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto);

    /**
     * 更新人工任务
     *
     * @param humanTaskDto
     *            人工任务dto对象
     * @param triggerListener
     *            是否触发监听器
     * @return 人工任务dto对象
     */
    HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto, boolean triggerListener);

    /**
     * 完成任务
     *
     * @param humanTaskId
     *            人工任务id
     * @param userId
     *            处理人id
     * @param action
     *            完成任务的动作
     * @param agreement
     *            是否同意
     * @param comment
     *            提交说明
     * @param taskParameters
     *            完成任务时携带的参数变量
     * @throws Exception
     *             执行失败时抛出异常
     */
    void completeTask(String humanTaskId, String userId, String action, Boolean agreement, String comment,
        Map<String, Object> taskParameters, String currentUserId) throws Exception;

    /**
     * 领取任务.
     *
     * @param humanTaskId
     *            待领取任务id
     * @param userId
     *            领取人id
     * @throws Exception
     *             执行失败时抛出异常
     */
    void claimTask(String humanTaskId, String userId, String currentUserId);

    /**
     * 释放任务
     *
     * @param humanTaskId
     *            任务id
     * @param comment
     *            意见
     */
    void releaseTask(String humanTaskId, String comment, String currentUserId);

    /**
     * 转发任务.
     *
     * @param humanTaskId
     *            人工任务id
     * @param userId
     *            当前用户id
     * @param comment
     *            转发说明
     */
    void transfer(String humanTaskId, String userId, String comment, String currentUserId,
        String currentTenantId);

    /**
     * 回退，指定节点，重新分配.
     *
     * @param humanTaskId
     *            当前任务id
     * @param activityId
     *            回退到指定节点的id
     * @param comment
     *            注释
     */
    void rollbackActivity(String humanTaskId, String activityId, String comment,
        Map<String, Object> variables, String currentUserId);

    /**
     * 回退，上个节点，重新分配.
     *
     * @param humanTaskId
     *            当前任务id
     * @param comment
     *            意见
     * @param variables
     *            流程参数
     */
    void rollbackPrevious(String humanTaskId, String comment, Map<String, Object> variables,
        String currentUserId);

    /**
     * 回退到上一节点审批人
     *
     * @param humanTaskId
     *            当前任务id
     * @param comment
     *            注释
     */
    void rollbackAssignee(String humanTaskId, String comment,
        Map<String, Object> variables, String currentUserId);

    /**
     * 撤销.
     *
     * @param humanTaskId
     *            人工任务id
     * @param comment
     *            撤销注释
     */
    void withdraw(String humanTaskId, String comment, String currentUserId);

    /**
     * 沟通.
     *
     * @param humanTaskId
     *            任务id
     * @param userIds
     *            沟通人员的id
     * @param comment
     *            沟通意见
     */
    void communicate(String humanTaskId, Set<String> userIds, String comment, String currentUserId);

    /**
     * 查找humantaskDTO
     *
     * @param taskId
     *            任务id
     * @return HumanTaskDTO 任务DTO
     */
    HumanTaskDTO findHumanTaskByTaskId(String taskId);

    /**
     * 查找任务的任务标题
     *
     * @param taskId
     *            任务id
     * @return String
     */
    @Deprecated
    String findPresentationSubjectByTaskId(String taskId);

    /**
     * 查找任务类型是正常，转发，会签，沟通的 humantaskdto
     *
     * @param processInstanceId
     *            流程实例id
     * @return List
     */
    List<HumanTaskDTO> findHumanTasksByProcessInstanceId(String processInstanceId);

    /**
     * 查找humantaskDTO
     *
     * @param humanTaskId
     *            任务id
     * @return 任务对象
     */
    HumanTaskDTO findHumanTask(String humanTaskId);

    /**
     * 查找humantaskDTO
     *
     * @param humanTaskId
     *            任务id
     * @return list
     */
    List<HumanTaskDTO> findSubTasks(String humanTaskId);

    /**
     * 加载有效的任务列表
     *
     * @param humanTaskId
     *            任务id
     * @return list
     */
    List<HumanTaskDTO> findActiveSubTasks(String humanTaskId);

    /**
     * 获取任务表单
     *
     * @param humanTaskId
     *            任务id
     * @return 表单对象
     */
    FormDTO findTaskForm(String humanTaskId);

    /**
     * 通过过滤条件获取任务信息
     *
     * @param userId
     *            用户id
     * @param tenantId
     *            租户id
     * @param filters
     *            用户角色id或用户组织id的集合
     * @param page
     *            分页对象
     * @return page
     */
    @Deprecated
    Page queryDataByFilter(String userId, String tenantId, List<PropertyFilter> filters, Page page);

    /**
     * 加签
     *
     * @param humanTaskId
     *            任务id
     * @param userIdSet
     *            需要加签的人员id
     */
    void addVote(String humanTaskId, Set<String> userIdSet);

    /**
     * 减签
     *
     * @param currentUserId
     *            当前的用户id
     * @param humanTaskIds
     *            人工任务id，以逗号分隔
     */
    boolean removeVote(String currentUserId, String humanTaskIds);

    /**
     * 根据ProcessInstanceId和Code查询任务
     *
     * @param processInstanceId
     *            流程实例id
     * @param code
     *            节点code
     * @return TaskInfo
     */
    TaskInfo findActiveTaskInfoByProcessInstanceIdAndCode(String processInstanceId, String code);

    /**
     * 流程干涉,选择节点跳转
     *
     * @param currentActivityId
     *            当前节点的id
     * @param processInstanceId
     *            流程实例id
     * @param activityId
     *            目标节点ID
     * @param userId
     *            用户ID
     * @param assigneeValue
     *            目标节点操作人
     * @param taskParameters
     *            流程参数
     */
    void interfereJumpTask(String currentActivityId, String processInstanceId, String activityId,
        String userId, String assigneeValue, Map<String, Object> taskParameters,
        String currentUserId);

    /**
     * 批量抄送
     *
     * @param processInstanceId
     *            流程实例ID
     * @param userSet
     *            用户id集合
     * @param comment
     *            抄送意见
     * @return boolean
     */
    boolean copyTaskInfo(final String processInstanceId, final Set<String> userSet,
        final String comment, final String currentUserId);

    /**
     * 审阅
     *
     * @param humanTaskIds
     *            任务id集合
     * @return boolean
     */
    boolean viewTaskByHumanTaskIds(String humanTaskIds);

    /**
     * 自动保存沟通的任务
     *
     * @param humanTaskId
     *            任务id集合
     */
    void saveAutoCommitTask(String humanTaskId);

    /**
     * 释放任务时校验任务的参与人
     *
     * @param humanTaskId
     *            任务id
     */
    void checkHumanTaskParticipants(String humanTaskId);

    /**
     * 查询带提交意见的历史流转记录
     *
     * @param processInstanceId
     *            流程实例ID
     * @return List
     */
    List<Map<String, Object>> queryHistoryTaskWithCommentByInstanceId(String processInstanceId);

    /**
     * 获取流程历史记录
     *
     * @param processInstanceId
     *            流程实例id
     * @param keyField
     *            关联字段名
     * @param tableName
     *            关联表名
     * @param args
     *            查询需要返回的字段名
     * @return list
     */
    List<Map<String, Object>> queryHistoryTaskWithCommentByInstanceId(String processInstanceId,
        String tableName, String keyField, String... args);

    /**
     * 撤回规则的前置效验
     *
     * @param humanTaskId
     *            任务id
     * @return boolean
     */
    boolean withdrawCheck(String humanTaskId);

    /**
     * 撤回规则前置校验
     *
     * @param humanTaskId
     *            任务id
     * @return boolean
     */
    boolean withdrawCheck2(String humanTaskId);

    /**
     * 替换流程办理人
     *
     * @param taskId
     *            任务ID
     * @param userId
     *            用户ID
     */
    void replaceTaskAssignee(String taskId, String userId);

    /**
     * 自由跳
     *
     * @param humanTaskId
     *            任务ID
     * @param activityId
     *            目标节点ID
     * @param userId
     *            操作用户ID
     * @param assigneeValue
     *            目标节点操作人（可以为空，使用默认操作人）
     * @param taskParameters
     *            流程相关参数
     * @throws Exception
     *             执行失败时抛出异常
     */
    void jump(String humanTaskId, String activityId, String userId, String assigneeValue,
        Map<String, Object> taskParameters, String currentUserId) throws Exception;

    /**
     * 串行会签减签
     *
     * @param users
     *            减签的人员id
     * @param humantaskId
     *            任务id
     * @return boolean
     */
    boolean removeSequentialVote(Set<String> users, String humantaskId);

    /**
     * 获取任务的会签类型
     *
     * @param humantaskId
     *            任务ID
     * @return String 任务的会签类型
     */
    @Deprecated
    String checkHumanTaskVoteType(String humantaskId);

    /**
     * 获取会签用户
     *
     * @param humanTaskId
     *            任务ID
     * @return list
     */
    List<Map<String, String>> getVoteUsers(String humanTaskId, String currentUserId);

    /**
     * 删除任务
     *
     * @param humanTaskId
     *            任务id
     */
    @Deprecated
    void removeHumanTask(String humanTaskId);

    /**
     * 根据humantaskId 终止流程实例
     *
     * @param humanTaskId
     *            任务id
     */
    @Deprecated
    void endProcessInstanceByHumanTaskId(String humanTaskId);

    /**
     * 查询超期任务
     *
     * @param tenantId
     *            租户ID
     * @param page
     *            分页对象
     * @return list
     */
    List<Task> queryTimeOutTasks(String tenantId, Page page);

    /**
     * 根据租户ID 查询所有超时任务实例数量
     *
     * @param tenantId
     *            租户ID
     * @return long
     */
    long countTimeoutTaskInstance(String tenantId);

    /**
     * 根据任务ID查询任务详情
     *
     * @param taskInfoId
     *            任务ID
     * @return Set
     */
    Set<UserDTO> findUsersByTaskInfoId(String taskInfoId);

    Boolean findActivityImplSubmitTaskWithPerson(String humanTaskId, String modelId);

    Boolean compareLoginAndAssignee(String humanTaskId, String currentUserId);

    String queryProcessStarter(String processInstanceId);

    String judgeSubmitTaskWithPerson(String humanTaskId, String modelId);

    List<String> getUsersByCandidateByUserId(String currentUserId, String humanTaskId, String customRule);

    List<String> getUsersByCandidateByNodeIdAndUserId(String humanTaskId, String customRule);

    List<String> getUsersByCandidateByOrgId(String customRule);

    List<Map<String, Object>> getAssigneesByProcessInstanceId(String processDefinitionId);

    Boolean judgeSubmitTaskWithPersonExclusive(String modelId, String humanTaskId);

    String queryProcessdefinitionIdByModelId(String modelId);

    Boolean judgeNextSubProcess(String humanTaskId, String modelId);
}
