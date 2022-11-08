package com.cesgroup.api.core.workflow.interf;

import com.cesgroup.api.form.FormDTO;
import com.cesgroup.api.humantask.HumanTaskDTO;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import com.cesgroup.bpm.proxy.CesProcessEngine;
import com.cesgroup.humantask.persistence.domain.NodeConfInfo;
import com.cesgroup.humantask.persistence.domain.TaskInfo;
import com.cesgroup.workflow.dto.VoteDTO;
import com.cesgroup.workflow.persistence.domain.WorkflowListener;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.repository.ProcessDefinition;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工作流通用接口
 *
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public interface WorkflowAPI {

    /**
     * 发起流程
     *
     * @param userId
     *            用户id
     * @param businessKey
     *            业务表主键id，有业务表生成
     * @param processDefinitionId
     *            流程定义Id
     * @param processParameters
     *            需要放入后续会影响流程的变量(如网关中的变量)
     * @param currentUserId
     *            当前登录人
     * @return String
     */
    String startProcessInstance(String userId, String businessKey, String processDefinitionId,
        Map<String, Object> processParameters, String currentUserId) throws Exception;

    /**
     * 处理任务
     *
     * @param humanTaskId
     *            任务id
     * @param userId
     *            登录名
     * @param action
     *            任务行为, 完成任务：completeTask
     * @param agreement
     *            是否同意
     * @param comment
     *            意见
     * @param currentUserId
     *            当前登录人
     * @param taskParameters
     *            处理任务所需参数
     */
    void completeTask(String humanTaskId, String userId, String action, Boolean agreement, String comment,
        Map<String, Object> taskParameters, String currentUserId) throws Exception;

    /**
     * 处理任务
     *
     * @param humanTaskId
     *            任务id
     * @param userId
     *            登录名
     * @param action
     *            任务行为, 完成任务：completeTask
     * @param comment
     *            意见
     * @param currentUserId
     *            当前登录人
     * @param taskParameters
     *            处理任务所需参数
     */
    void completeTask(String humanTaskId, String userId, String action, String comment,
        Map<String, Object> taskParameters, String currentUserId) throws Exception;

    /**
     * 完成任务
     *
     * @param humanTaskId
     *            人工任务id
     * @param userId
     *            处理人id
     * @param comment
     *            提交说明
     * @param taskParameters
     *            完成任务时携带的参数变量
     * @param currentUserId
     *            当前登录人
     * @throws Exception
     *             运行时异常
     */
    void completeTask(String humanTaskId, String userId, String comment,
        Map<String, Object> taskParameters,
        String currentUserId) throws Exception;

    /**
     * 回退上一个节点
     *
     * @param humanTaskId
     *            任务id
     * @param comment
     *            提交意见
     * @param variables
     *            流程变量，可以为null
     * @param currentUserId
     *            当前登录人
     */
    void rollbackPrevious(String humanTaskId, String comment, Map<String, Object> variables,
        String currentUserId);

    /**
     * 回退，指定节点
     *
     * @param humanTaskId
     *            任务id
     * @param activityId
     *            回退节点id
     * @param comment
     *            提交意见
     * @param variables
     *            流程变量，可以为null
     * @param currentUserId
     *            当前登录人
     */
    void rollbackActivity(String humanTaskId, String activityId, String comment,
        Map<String, Object> variables,
        String currentUserId);

    /**
     * 回退到上一节点审批人
     *
     * @param humanTaskId
     *            任务id
     * @param activityId
     *            回退节点id
     * @param comment
     *            提交意见
     * @param variables
     *            流程变量，可以为null
     * @param currentUserId
     *            当前登录人
     */
    void rollbackAssignee(String humanTaskId, String comment, Map<String, Object> variables,
        String currentUserId);

    /**
     * 撤销
     *
     * @param humanTaskId
     *            任务id
     * @param comment
     *            意见
     * @param currentUserId
     *            当前登录人
     */
    void withdraw(String humanTaskId, String comment, String currentUserId);

    /**
     * 撤回规则前置校验
     *
     * @param humanTaskId
     *            任务id
     * @return boolean
     */
    @Deprecated
    boolean withdrawCheck(String humanTaskId);

    /**
     * 撤回规则前置校验（改）根据ExecutionId进行判断（不使用arrt4）
     *
     * @param humanTaskId
     *            任务id
     * @return boolean
     */
    boolean withdrawCheck2(String humanTaskId);

    /**
     * 转发任务.
     *
     * @param humanTaskId
     *            人工任务id
     * @param userId
     *            当前用户id
     * @param comment
     *            转发说明
     * @param currentUserId
     *            当前登录人
     */
    void transfer(String humanTaskId, String userId, String comment, String currentUserId,
        String currentTenantId);

    /**
     * 催办（批量操作）
     *
     * @param humanTaskIds
     *            任务id集合，以逗号分隔
     * @param currentTenantId
     *            当前租户
     * @return boolean
     */
    boolean remindTaskInfoByHumanTaskIds(String humanTaskIds, String currentTenantId);

    /**
     * 沟通
     *
     * @param humanTaskId
     *            任务id
     * @param userSet
     *            沟通用户id的set集合
     * @param comment
     *            意见
     * @param currentUserId
     *            当前登录人
     */
    void communicate(String humanTaskId, Set<String> userSet, String comment, String currentUserId);

    /**
     * 沟通回复
     *
     * @param humanTaskId
     *            任务id
     * @param comment
     *            意见
     */
    void react(String humanTaskId, String comment);

    /**
     * 加签
     *
     * @param humanTaskId
     *            任务id
     * @param userIdsSet
     *            加签用户id集合
     */
    void addVote(String humanTaskId, Set<String> userIdsSet);

    /**
     * 审阅
     *
     * @param humanTaskIds
     *            任务id集合
     * @return boolean
     */
    boolean viewTaskByHumanTaskIds(String humanTaskIds);

    /**
     * 批量校验任务是否存在或挂起
     *
     * @param humanTaskIds
     *            任务ID集合
     * @return boolean
     */
    boolean checkHumanTaskIsExistOrSuspend(String humanTaskIds);

    /**
     * 终止任务（批量操作）
     *
     * @param humanTaskIds
     *            任务id集合
     * @param currentUserId
     *            当前登录人
     * @param currentUserName
	 * @param agreement
	 * @param comment
	 *            意见
	 * @return boolean
     */
    boolean completeTaskByHumanTaskIds(String humanTaskIds, String currentUserId, String currentUserName, String agreement, String comment);

    /**
     * 签收任务(抢占任务需要先进行签收)
     *
     * @param humanTaskId
     *            待签收任务id
     * @param userId
     *            领取人id
     * @param currentUserId
     *            当前登录人id
     */
    void claimTask(String humanTaskId, String userId, String currentUserId);

    /**
     * 释放任务，校验任务参与人
     *
     * @param humanTaskId
     *            任务id
     */
    void checkHumanTaskParticipants(String humanTaskId);

    /**
     * 释放任务
     *
     * @param humanTaskId
     *            任务id
     * @param comment
     *            意见
     * @param currentUserId
     *            当前登录人
     */
    void releaseTask(String humanTaskId, String comment, String currentUserId);

    /**
     * 获取历史服务组件(引擎内部HistoryService)，管理历史数据
     *
     * @return HistoryService
     */
    HistoryService getHistoryService();

    /**
     * 获取ManagementService ，提供对流程引擎的管理和维护的功能
     *
     * @return ManagementService
     */
    ManagementService getManagementService();

    /**
     * 获取RepositoryService，管理流程仓库，例如部署，删除，读取流程资源等
     *
     * @return RepositoryService
     */
    RepositoryService getRepositoryService();

    /**
     * 获取RuntimeService，流程控制API
     *
     * @return RuntimeService
     */
    RuntimeService getRuntimeService();

    /**
     * 根据id查询任务
     *
     * @param humanTaskId
     *            任务id
     * @return HumanTaskDTO 任务DTO
     */
    HumanTaskDTO findHumanTask(String humanTaskId);

    /**
     * 获取任务表单
     *
     * @param humanTaskId
     *            任务ID
     * @return 任务表单DTO
     */
    FormDTO findTaskForm(String humanTaskId);

    /**
     * 更新人工任务
     *
     * @param humanTaskDto
     *            人工任务dto对象
     * @param triggerListener
     *            是否触发监听器
     */
    void saveHumanTask(HumanTaskDTO humanTaskDto, boolean triggerListener);

    /**
     * 查找humantaskDTO
     *
     * @param humanTaskId
     *            任务id
     * @return List 任务信息集合
     */
    List<HumanTaskDTO> findSubTasks(String humanTaskId);

    /**
     * 获得启动表单
     *
     * @param processDefinitionId
     *            流程定义id
     * @return FormDTO
     */
    @Deprecated
    FormDTO findStartForm(String processDefinitionId);

    /**
     * 根据字段名和字段值查询模型版本信息
     *
     * @param name
     *            字段名
     * @param value
     *            字段值
     * @return BpmConfBase
     */
    BpmConfBase findUniqueConfBaseBy(String name, String value);

    /**
     * 根据流程实例Id查询激活任务
     *
     * @param processInstanceId
     *            流程实例ID
     * @return TaskInfo
     */
    TaskInfo queryTaskInfoByProcessInstanceId(String processInstanceId);

    /**
     * 自动提交任务节点（不做任何处理，将下一个节点提交）
     *
     * @param processInstanceId
     *            流程实例id
     * @param userId
     *            用户id
     * @param taskParameters
     *            流程变量参数
     * @throws Exception
     *             异常
     */
    void submitTaskInfoAuto(String processInstanceId, String userId,
        Map<String, Object> taskParameters,
        String currentUserId) throws Exception;

    /**
     * 自动提交沟通任务
     *
     * @param humanTaskId
     *            任务ID
     */
    void saveAutoCommitTask(String humanTaskId);

    /**
     * 保存表单意见
     *
     * @param humanTaskId
     *            任务id
     * @param submitComment
     *            提交意见
     */
    void saveTaskCommentByHumanTaskId(String humanTaskId, String submitComment);

    /**
     * 获取所有下一步节点
     *
     * @param humanTaskId
     *            任务id
     * @return
     */
    List<Map<String, String>> queryNextActivityListByHumanTaskId(String humanTaskId);

    /**
     * 获取所有下一步节点
     *
     * @param modelId
     *            模型Id
     * @return
     */
    List<Map<String, String>> queryNextActivityListByModelId(String modelId);

    /**
     * 查询流程定义实体
     *
     * @param processDefinitionId
     *            流程定义id
     * @return ProcessDefinition 流程定义接口
     */
    ProcessDefinition findProcessDefinitionById(String processDefinitionId);

    /**
     * 根据流程图中的节点id 去找下一个运行节点的信息
     *
     * @param tenantId
     *            租户id
     * @param processDefinitionKey
     *            流程定义的key
     * @param activityId
     *            流程图中节点的id
     * @param map
     *            模拟流程运行中，表达式所需要解析的参数
     *            （特殊情况：如果是没有发起流程，第一个节点又是配置的流程发起人，请给initiator赋值，值为流程发起人的id）
     * @return List 下一个运行节点的信息的集合
     */
    List<NodeConfInfo> getFakeNodeConfInfoByTaskIdAndMap(String tenantId,
        String processDefinitionKey,
        String activityId, Map<String, Object> map);

    /**
     * 根据流程图中的节点id 去找下一个运行节点的信息
     *
     * @param tenantId
     *            租户id
     * @param processDefinitionId
     *            流程定义Id
     * @param activityId
     *            流程图中节点的id
     * @param map
     *            模拟流程运行中，表达式所需要解析的参数
     *            （特殊情况：如果是没有发起流程，第一个节点又是配置的流程convertNodeDtoconvertNodeDto发起人，请给initiator赋值，值为流程发起人的id）
     * @return List 下一个运行节点的信息的集合
     */
    List<NodeConfInfo> getFakeNodeConfInfoByProcessDefinitionIdAndMap(String tenantId,
        String processDefinitionId,
        String activityId, Map<String, Object> map);

    /**
     * 获取流程引擎
     *
     * @return CesProcessEngine
     */
    CesProcessEngine getProcessEngine();

    /**
     * 根据任务ID信息获取该节点上能够被减签的人员信息
     *
     * @param humanTaskId
     *            任务ID
     * @param currentUserId
     *            当前登录人
     */
    List<Map<String, String>> getVoteUsers(String humanTaskId, String currentUserId);

    /**
     * 发起流程(不关联业务表)
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            操作人（用户）ID
     * @param processDefinitionKey
     *            流程定义KEY
     * @param processParameters
     *            流程相关参数
     * @param currentUserId
     *            当前登录人
     * @return String processInstanceId 流程实例ID
     */
    String startProcessInstanceByprocessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey,
        Map<String, Object> processParameters, String currentUserId);

    /**
     * 发起流程(不关联业务表)
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            操作人（用户）ID
     * @param processDefinitionKey
     *            流程定义KEY
     * @param processParameters
     *            流程相关参数
     * @param autoCommit
     *            是否自动提交第一个节点，默认为false
     * @param currentUserId
     *            当前登录人
     * @return String processInstanceId 流程实例ID
     */
    String startProcessInstanceByprocessDefinitionKey(String tenantId, String userId,
        String processDefinitionKey,
        Map<String, Object> processParameters, Boolean autoCommit, String currentUserId);

    /**
     * 发起流程(关联业务表)
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            操作人（用户）ID
     * @param businessKey
     *            业务表关联Key
     * @param processDefinitionKey
     *            流程定义KEY
     * @param processParameters
     *            流程相关参数
     * @param autoCommit
     *            是否自动提交第一个节点，默认为false
     * @param currentUserId
     *            当前登录人
     * @return String processInstanceId 流程实例ID
     */
    String startProcessInstanceByprocessDefinitionKey(String tenantId, String userId,
        String businessKey, String processDefinitionKey, Map<String, Object> processParameters,
        Boolean autoCommit, String currentUserId);

    /**
     * 发起流程(关联业务表)
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            操作人（用户）ID
     * @param businessKey
     *            业务表关联Key
     * @param processDefinitionKey
     *            流程定义KEY
     * @param processParameters
     *            流程相关参数
     * @param currentUserId
     *            当前登录人
     * @return String processInstanceId 流程实例ID
     */
    String startProcessInstanceByprocessDefinitionKey(String tenantId, String userId,
        String businessKey, String processDefinitionKey,
        Map<String, Object> processParameters, String currentUserId);

    /**
     * 减签
     *
     * @param humanTaskId
     *            减签任务的id
     * @param params
     *            如果是串行减签的话
     *            传的是被减签的人员的id集合，以逗号分隔，如果是并行减签的情况下，传的是被减签的任务的humantaskid的集合，
     *            以逗号分隔
     * @param currentUserId
     *            当前登录人的id
     * @return boolean
     */
    boolean removeVote(String humanTaskId, String params, String currentUserId);

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
     * 根据流程实例id查询流程
     *
     * @param processInstanceId
     *            流程实例id
     * @return 历史流程实例
     */
    HistoricProcessInstance queryProcessInstanceByProcessInstanceId(String processInstanceId);

    /**
     * 获取任务service
     *
     * @return TaskService
     */
    TaskService getTaskService();

    /**
     * 批量抄送
     *
     * @param processInstanceId
     *            流程实例ID
     * @param userSet
     *            用户id集合
     * @param comment
     *            抄送意见
     * @param currentUserId
     *            当前登录人
     * @return boolean
     */
    boolean copyTaskInfo(String processInstanceId, Set<String> userSet, String comment,
        String currentUserId);

    /**
     * 根据任务ID查询任务详情
     *
     * @param taskInfoId
     *            任务ID
     * @return Set
     */
    Set<UserDTO> findUsersByTaskInfoId(String taskInfoId);

    /**
     * 查找开始节点的id
     *
     * @param modelId
     *            模型id
     * @return 开始节点的id
     */
    String getStartActivityId(String modelId);

    /**
     * 查找开始节点后面的节点
     *
     * @param modelId
     *            模型id
     * @return 开始节点后面的节点id
     */
    List<String> getStartBehindActivityIds(String modelId);

    /**
     * 查找结束节点
     *
     * @param modelId
     *            模型id
     * @return 结束节点的id
     */
    List<String> getEndActivityIds(String modelId);

    /**
     * 查询是不是开始节点后面的节点
     *
     * @param humantaskId
     *            任务id
     * @return 是不是开始节点后面的节点
     */
    Boolean checkIsBeginBehindNodeByHumantaskId(String humantaskId);

    /**
     * 查询进入当前节点的上一个人工节点
     *
     * @param humantaskId
     *            任务id
     * @return PvmActivity 节点
     */
    PvmActivity findPreviousActivityImplByHumantaskId(String humantaskId);

    /**
     * 查询当前节点之后的节点
     *
     * @param humantaskId
     *            任务id
     * @return List 节点集合
     */
    List<PvmActivity> findNextActivityImplByHumantaskId(String humantaskId);

    /**
     * 查询进入当前节点的上一个人工节点
     *
     * @param humantaskId
     *            任务id
     * @return PvmActivity 节点
     */
    PvmActivity findActivityImplByHumantaskId(String humantaskId);

    /**
     * 检查流程是否配置了专业线
     *
     * @param processDefinitionId
     *            流程定义id
     * @return Boolean 默认是false
     */
    Boolean checkGraphIsConfMajorLine(String processDefinitionId);

    /**
     * 检查任务是否是会签任务
	 * 由于多审批人无法拒绝的情况，现将 并行网关、包含网关、并行会签 3个类型全部按照投票形式走
	 * 并行网关、包含网关的默认策略: 绝对票数、表决制·无、表决人数达到多数后，不结束会签、一票拒绝制
     *
     * @param humantaskId
     *            人工任务id
     * @return Boolean 默认是false
     */
    Boolean checkIsCountersignTask(String humantaskId);

    /**
     * 复制租户流程
     *
     * @param sourceTenantId
     *            来源租户ID
     * @param targetTenantId
     *            目标租户ID
     * @throws Exception
     *             抛出的异常
     */
    void syncProcess4Tenant(String sourceTenantId, String targetTenantId) throws Exception;

    /**
     * 通过监听器类别查询监听器
     *
     * @param category
     *            监听器类别
     */
    List<WorkflowListener> queryWorkflowListenerByCategory(String category);

    /**
     * 添加或者修改监听器
     *
     * @param workflowListener
     *            监听器
     * @return
     */
    WorkflowListener createOrUpdateWorkflowListener(WorkflowListener workflowListener);

    /**
     * 删除监听器
     *
     * @param idList
     *            监听器id集合
     * @return
     */
    Boolean removeWorkflowListenerByIds(List<String> idList);

    /**
     * 校验唯一性
     *
     * @param workflowListener
     * @return
     */
    Boolean checkUnique(WorkflowListener workflowListener);

    /**
     * 查看节点是否配置了选择提交
     *
     * @param humanTaskId
     *            任务id
     * @return
     */
    Boolean findActivityImplSubmitTaskWithPerson(String humanTaskId, String modelId);

    /**
     * 判断登录人和当前任务的办理人是否相同
     *
     * @param humantaskId
     *            任务id
     * @param currentUserId
     *            当前登录人
     * @return
     */
    Boolean compareLoginAndAssignee(String humantaskId, String currentUserId);

    /**
     * 查询流程发起人
     *
     * @param processInstanceId
     *            流程实例id
     * @return 用户id
     */
    String queryProcessStarter(String processInstanceId);

    /**
     * 判断选择提交类型(所用用户和预设用户)
     *
     * @param humanTaskId
     *            任务id
     * @return "1"所有用户 "2"预设用户
     */
    String judgeSubmitTaskWithPerson(String humanTaskId, String modelId);

    /**
     * 判断选择提交类型后面是否配置了排他网关
     *
     * @param modelId
     *            任务id
     * @return "1"所有用户 "2"预设用户
     */
    Boolean judgeSubmitTaskWithPersonExclusive(String modelId, String humanTaskId);

    /**
     * 通过自定义规则查询出办理人集合
     *
     * @param currentUserId
     *            当前登录人id(流程未启动时需传递)
     * @param humanTaskId
     *            任务id
     * @param customRule
     *            自定义规则表达式
     * @return 办理人id集合
     */
    List<String> getUsersByCandidateByUserId(String currentUserId, String humanTaskId, String customRule);

    /**
     * 通过自定义规则查询出办理人集合
     *
     * @param humanTaskId
     *            任务id
     * @param customRule
     *            自定义规则表达式
     * @return 办理人id集合
     */
    List<String> getUsersByCandidateByNodeIdAndUserId(String humanTaskId, String customRule);

    /**
     * 通过自定义规则查询出办理人集合
     *
     * @param customRule
     *            自定义规则表达式
     * @return 办理人id集合
     */
    List<String> getUsersByCandidateByOrgId(String customRule);

    /**
     * 通过流程实例id查询流程办理人
     *
     * @param processInstanceId
     *            流程实例id
     * @return 用户id和姓名
     */
    List<Map<String, Object>> getAssigneesByProcessInstanceId(String processInstanceId);

    /**
     * 通过modelId查询流程定义Id
     *
     * @param modelId
     *            模型id
     * @return
     */
    String queryProcessdefinitionIdByModelId(String modelId);

    /**
     * 判断流程是否结束
     *
     * @param processInstanceId
     *            流程实例id
     * @return
     *
     */
    Boolean judgeProcessEnd(String processInstanceId);

    /**
     * 判断下个节点是否是子流程
     *
     * @param humanTaskId
     * @return
     */
    Boolean judgeNextSubProcess(String humanTaskId, String modelId);

    /**
     * 流程跟踪图
     *
     * @param processInstanceId
     *            流程实例ID
     * @return
     */
    String getGraphImageByProcessInstanceId(String processInstanceId) throws IOException;

    /**
     * 获取退回节点集合
     *
     * @param processInstanceId
     *            流程实例ID
     * @param activityId
     *            节点ID
     * @return
     */
    List<?> getRollbackActivityListByProcessInstanceIdAndActivityId(String processInstanceId, String activityId);

	/**
	 * 获得投票结果
	 * @param processInstanceId
	 *            流程实例ID
	 * @param activityId
	 *            节点ID
	 * @return
	 */
	VoteDTO getVote(String ProcessInstanceId, String activityId);

}