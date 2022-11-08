/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service</p>
 * <p>文件名:WorkflowOpenApiService.java</p>
 * <p>创建时间:2019年11月13日 下午4:28:04</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service;

import com.cescloud.saas.archive.api.modular.workflow.dto.JudgePersonResponseDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowBusinessModelSyncDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO;
import com.cescloud.saas.archive.common.search.Page;
import com.cesgroup.workflow.dto.VoteDTO;

import java.util.List;
import java.util.Map;

/**
 * 工作流对外API接口
 *
 * @author qiucs
 * @version 1.0.0 2019年11月13日
 */
public interface WorkflowOpenApiService {

    /**
     * 启动流程
     *
     * @param bpmModelCode
     *            流程编码
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param businessKey
     *            业务数据ID
     * @param autoCommit
     *            是否自动提交
     * @param paramMap
     *            参数集合
	 * @param processDefinitionId 流程版本唯一标识
     * @return 返回流程实例ID
     */
    String startProcessByBpmModelCode(String bpmModelCode, String tenantId, String userId, String businessKey,
									  boolean autoCommit, String assignees, Map<String, Object> paramMap, String processDefinitionId);

    /**
     * 启动流程（根据用户ID和部门ID集合来获取可见的流程来启动）
     *
     * @param businessCode
     *            业务编码
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param deptIdList
     *            部门ID集合（有优级，越前面的越高）
     * @param businessKey
     *            业务数据ID
     * @param autoCommit
     *            是否自动提交
     * @param paramMap
     *            参数集合
     * @return 返回流程实例ID
     */
    String startProcessByBusinessCode(String businessCode, String tenantId, String userId, List<String> deptIdList,
        String businessKey,
        boolean autoCommit,
		String assignees,
        Map<String, Object> paramMap);

    /**
     * 提交任务
     *
     * @param taskId
     *            工作流任务ID
     * @param userId
     *            处理人
     * @param agreement
     *            是否同意
     * @param comment
     *            审批意见
     * @param paramMap
     *            参数集合
     */
    void completeTask(String taskId, String userId, Boolean agreement, String comment, String assignees, Map<String, Object> paramMap);

    /**
     * 根据流程实例ID提交任务
     *
     * @param processInstanceId
     *            流程实例ID
     * @param userId
     *            处理人
     * @param agreement
     *            是否同意
     * @param comment
     *            审批意见
     * @param paramMap
     *            参数集合
     */
    void completeProcess(String processInstanceId, String userId, Boolean agreement, String comment, String assignees,
        Map<String, Object> paramMap);

    /**
     * 撤回发起任务
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param processInstanceId
     *            流程实例ID
     */
    void withdrawSponsorTaskByProcessInstanceId(String tenantId, String userId, String processInstanceId);

    /**
     * 撤回任务
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param processInstanceId
     *            流程实例ID
     */
    void withdrawTaskByProcessInstanceId(String tenantId, String userId, String processInstanceId);

    /**
     * 撤回任务
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param taskId
     *            任务ID
     */
    void withdrawTaskByTaskId(String tenantId, String userId, String taskId);

    /**
     * 退回
     *
     * @param taskId
     *            工作流任务ID
     * @param userId
     *            处理人
     * @param comment
     *            审批意见
     * @param paramMap
     *            参数集合
     */
    void rollbackPrevious(String taskId, String userId, String comment, Map<String, Object> paramMap);

    /**
     * 退回指定节点
     *
     * @param taskId
     *            工作流任务ID
     * @param activityId
     *            节点ID
     * @param userId
     *            处理人
     * @param comment
     *            审批意见
     * @param paramMap
     *            参数集合
     */
    void rollbackActivity(String taskId, String activityId, String userId, String comment,
        Map<String, Object> paramMap);

    /**
     * 退回上一节点的审批人
     *
     * @param taskId
     *            工作流任务ID
     * @param userId
     *            处理人
     * @param comment
     *            审批意见
     * @param paramMap
     *            参数集合
     */
    void rollbackAssignee(String taskId, String userId, String comment, Map<String, Object> paramMap);

    /**
     * 终止任务
     *
     * @param taskId
     *            工作流任务ID
     * @param userId
     *            用户ID
     */
    void terminateTask(String taskId, String userId, String userName, String agreement,String comment);

    /**
     * 终止任务
     *
     * @param processInstanceId
     *            流程实例ID
     * @param userId
     *            用户ID
     */
    void terminateProcessInstance(String processInstanceId, String userId,String userName, String agreement, String comment);

    /**
     * 删除流程
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param processInstanceIds
     *            流程实例IDS
     */
    void deleteProcessInstance(String tenantId, String userId, String processInstanceIds);

    /**
     * 转发
     *
     * @param taskId
     *            工作流任务ID
     * @param userId
     *            用户ID
     * @param toUserIds
     *            转发用户IDS
     * @param comment
     *            意见
     */
    void deliverTask(String taskId, String userId, String[] toUserIds, String comment);

    /**
     * 查阅/回复
     *
     * @param taskId
     *            工作流任务ID
     * @param comment
     *            意见
     */
    void reactTask(String taskId, String comment);

    /**
     * 转办
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param taskId
     *            工作流任务ID
     * @param toUserId
     *            转办用户ID
     * @param comment
     *            意见
     */
    void reassignTask(String tenantId, String userId, String taskId, String toUserId, String comment);

    /**
     * 判断流程是否结束
     *
     * @param processInstanceId
     *            流程实例ID
     * @return
     */
    boolean isProcessFinished(String processInstanceId);

    /**
     * 从业务系统同步业务模型
     *
     * @param businessModelSyncDTO
     *            同步对象
     * @return
     */
    boolean businessSync(WorkflowBusinessModelSyncDTO businessModelSyncDTO);

    /**
     * 获取当前用户对应的流程
     *
     * @param businessCode
     *            业务编码
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param deptIdList
     *            部门ID集合
     * @return
     */
    List<String> getBpmModelCodeListByBusinessCode(String businessCode, String tenantId, String userId,
        List<String> deptIdList);

    /**
     * 待办列表
     *
     * @param page
     *            分页信息
     * @param bpmModelCode
     *            流程编码
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param tableName
     *            业务表名（包含schema），如：db1.t_ar_borrow
     * @param fields
     *            业务字段
     * @param filter
     *            业务表过滤条件
     * @param orders
     *            业务表排序，如：create_time:desc
     * @return
     */
    Page<?> getUncompletedTaskList(Page<?> page, String bpmModelCode, String tenantId, String userId, String tableName,
        String fields, String filter, String orders);

    /**
     * 已办列表
     *
     * @param page
     *            分页信息
     * @param bpmModelCode
     *            流程编码
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param tableName
     *            业务表名（包含schema），如：db1.t_ar_borrow
     * @param fields
     *            业务字段
     * @param filter
     *            业务表过滤条件
     * @param orders
     *            业务表排序，如：create_time:desc
     * @return
     */
    Page<?> getCompletedTaskList(Page<?> page, String bpmModelCode, String tenantId, String userId, String tableName,
        String fields, String filter, String orders);

    /**
     * 办结列表
     *
     * @param page
     *            分页信息
     * @param bpmModelCode
     *            流程编码
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param tableName
     *            业务表名（包含schema），如：db1.t_ar_borrow
     * @param fields
     *            业务字段
     * @param filter
     *            业务表过滤条件
     * @param orders
     *            业务表排序，如：create_time:desc
     * @return
     */
    Page<?> getFinishedProcessList(Page<?> page, String bpmModelCode, String tenantId, String userId, String tableName,
        String fields, String filter, String orders);

    /**
     * 办结流程数量
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param searchDTO
     *            过滤条件
     * @return
     */
    Map<String, Integer> countStartProcess(String tenantId, String userId, WorkflowSearchDTO searchDTO);

    /**
     * 发起列表
     *
     * @param page
     *            分页信息
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param searchDTO
     *            过滤条件
     * @return
     */
    Page<?> getStartProcessList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO);

    /**
     * 待发任务数量
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param searchDTO
     *            过滤条件
     * @return
     */
    Map<String, Integer> countUnsponsorTask(String tenantId, String userId, WorkflowSearchDTO searchDTO);

    /**
     * 待发任务列表
     *
     * @param page
     *            分页信息
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param searchDTO
     *            过滤条件
     * @return
     */
    Page<?> getUnsponsorTaskList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO);

    /**
     * 已办任务数量
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param searchDTO
     *            过滤条件
     * @return
     */
    Map<String, Integer> countApproveTask(String tenantId, String userId, WorkflowSearchDTO searchDTO);

	/**
     * 审批列表
     *
     * @param page
     *            分页信息
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param searchDTO
     *            过滤条件
     * @return
     */
    Page<?> getApproveTaskList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO);

	/**
	 * 审批列表
	 *
	 * @param tenantId
	 *            租户ID
	 * @param processInstanceId
	 *            用户ID
	 * @return
	 */
	List<Map<String, Object>> getApproveTaskList(String tenantId,String processInstanceId);

    /**
     * 抄送已读数量
     *
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param searchDTO
     *            过滤条件
     * @return
     */
    Map<String, Integer> countCopyTask(String tenantId, String userId, WorkflowSearchDTO searchDTO);

    /**
     * 抄送列表
     *
     * @param page
     *            分页信息
     * @param tenantId
     *            租户ID
     * @param userId
     *            用户ID
     * @param searchDTO
     *            过滤条件
     * @return
     */
    Page<?> getCopyTaskList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO);

    /**
     * 流程跟踪图
     *
     * @param processInstanceId
     *            流程实例ID
     * @return
     */
    String getGraphImageByProcessInstanceId(String processInstanceId);

    /**
     * 流程日志
     *
     * @param processInstanceId
     *            流程实例ID
     * @return
     */
    List<?> getTaskLogListByProcessInstanceId(String processInstanceId);

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
     * 根据租户id 删除 workflow
     *
     * @param tenantId
     * @return
     */
    Boolean clearWorkflowTenantInfo(Long tenantId);

	/**
	 * 判断是否是人工选择
	 *
	 */
	JudgePersonResponseDTO judgeSubmitTaskWithPersonExclusive(String bpmModelCode, String tenantId, String userId, List<String> deptIdList,
															  String businessKey, String activityId, String taskId, Map<String, Object> paramMap);

	/***
	 * 检查任务是否是会签任务
	 * 由于多审批人无法拒绝的情况，现将 并行网关、包含网关、并行会签 3个类型全部按照投票形式走
	 * 并行网关、包含网关的默认策略: 绝对票数、表决制·无、表决人数达到多数后，不结束会签、一票拒绝制
	 * @return
	 */
	Boolean checkIsCountersignTask(String taskId);

	/**
	 * 获得投票结果
	 * @param ProcessInstanceId
	 * @return
	 */
	VoteDTO getVote(String processInstanceId, String activityId);

	void updateParallelStatus(String processInstanceId, String Id);

	/**
	 * 获取一个流程的结果，原理是通过 我发起的 页面的流程状态获取
	 * @param processInstanceId
	 * @return
	 */
	Map<String, Object> getOneProcessResult(String processInstanceId);

	/**
	 * 根据processDefinitionId查找这个版本是否被流程使用过
	 * @param processDefinitionId
	 */
	boolean getProcessWasUsed(String processDefinitionId);

	String getProcessById(String tenantId, String businessCode);
}
