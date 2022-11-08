/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.feign</p>
 * <p>文件名:RemoteWorkflowApiService.java</p>
 * <p>创建时间:2019年11月13日 下午3:55:39</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.workflow.dto.*;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月13日
 */
@FeignClient(contextId = "remoteWorkflowApiService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT, path = "/workflow-open-api")
public interface RemoteWorkflowApiService {

    /**
     * 按流程编码启动流程
     *
     * @param startProcessDTO
     * @return 流程实例ID
     */
    @PostMapping("/start/bpm")
    R<String> startProcessByBpmModelCode(@RequestBody StartBpmModelProcessDTO startProcessDTO);

    /**
     * 按业务编码启动流程
     *
     * @param startProcessDTO
     * @return 流程实例ID
     */
    @PostMapping("/start/biz")
    R<String> startProcessByBusinessCode(@RequestBody StartBusinessProcessDTO startProcessDTO);

    /**
     * 提交任务
     *
     * @param completeTaskDTO
     * @return
     */
    @PostMapping("/complete-task")
    R<Boolean> completeTask(@RequestBody CompleteTaskDTO completeTaskDTO);

    /**
     * 提交流程
     *
     * @param completeProcessDTO
     * @return
     */
    @PostMapping("/complete-process")
    R<Boolean> completeProcess(@RequestBody CompleteProcessDTO completeProcessDTO);


	/***
	 * 拒绝流程
	 * @param completeProcessDTO
	 * @return
	 */
	@PostMapping("/refuse-process")
	R<Boolean> refuseProcess(@RequestBody TerminateProcessDTO terminateProcessDTO);

    /**
     * 撤回发起节点提交
     *
     * @param processInstanceId
     * @return
     */
    @PostMapping("/withdraw-sponsor-task/{processInstanceId}")
    R<Boolean> withdrawSponsorTaskByProcessInstanceId(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 撤回
     *
     * @param processInstanceId
     * @return
     */
    @PostMapping("/withdraw-process/{processInstanceId}")
    R<Boolean> withdrawTaskByProcessInstanceId(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 撤回
     *
     * @param taskId
     * @return
     */
    @PostMapping("/withdraw-task/{taskId}")
    R<Boolean> withdrawTaskByTaskId(@PathVariable("taskId") String taskId);

    /**
     * 退回上一节点（流程配置审批人）
     *
     * @param rollbackPreviousDTO
     * @return
     */
    @PostMapping("/rollback-previous")
    R<Boolean> rollbackPrevious(@RequestBody RollbackPreviousDTO rollbackPreviousDTO);

    /**
     * 退回指定节点
     *
     * @param rollbackActivityDTO
     * @return
     */
    @PostMapping("/rollback-activity")
    R<Boolean> rollbackActivity(@RequestBody RollbackActivityDTO rollbackActivityDTO);

    /**
     * 退回上一节点审批人
     *
     * @param rollbackPreviousDTO
     * @return
     */
    @PostMapping("/rollback-assignee")
    R<Boolean> rollbackAssignee(@RequestBody RollbackPreviousDTO rollbackPreviousDTO);

    /**
     * 终止任务
     *
     * @param terminateTaskDTO
     *            任务id
     * @return
     */
    @PostMapping("/terminate-task")
    R<Boolean> terminateTask(@RequestBody TerminateTaskDTO terminateTaskDTO);

    /**
     * 终止流程实例
     *
     * @param terminateProcessDTO
     *            流程实例id
     * @return
     */
    @PostMapping("/terminate-process")
    R<Boolean> terminateProcessInstance(@RequestBody TerminateProcessDTO terminateProcessDTO);

    /**
     * 转办
     *
     * @param reassignTaskDTO
     * @return
     */
    @PostMapping("/reassign-task")
    R<Boolean> reassignTask(@RequestBody ReassignTaskDTO reassignTaskDTO);

    /**
     * 转发
     *
     * @param deliverTaskDTO
     * @return
     */
    @PostMapping("/deliver-task")
    R<Boolean> deliverTask(@RequestBody DeliverTaskDTO deliverTaskDTO);

    /**
     * 查阅/回复
     *
     * @param reactTaskDTO
     * @return
     */
    @PostMapping("/react-task")
    R<Boolean> reactTask(@RequestBody ReactTaskDTO reactTaskDTO);

    /**
     * 业务模型同步
     *
     * @param businessModelSyncDTO
     * @return
     */
    @PostMapping("/model/sync")
    R<Boolean> businessSync(@RequestBody WorkflowBusinessModelSyncDTO businessModelSyncDTO,
        @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 判断流程是否结束
     *
     * @param processInstanceId
     *            流程实例ID
     * @return
     */
    @GetMapping("/process-finished/{processInstanceId}")
    R<Boolean> isProcessFinished(
        @PathVariable("processInstanceId") String processInstanceId);

    /**
     * 流程跟踪图
     *
     * @param processInstanceId
     *            流程实例ID
     * @return
     */
    @GetMapping("/graph-image/{processInstanceId}")
    R<String> getGraphImageByProcessInstanceId(
        @PathVariable("processInstanceId") String processInstanceId);

    /**
     * 流程跟踪日志
     *
     * @param processInstanceId
     *            流程实例ID
     * @return
     */
    @GetMapping("/task-logs/{processInstanceId}")
    R<List<Map<String, String>>> getTaskLogListByProcessInstanceId(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 获取退回节点集合
     *
     * @param processInstanceId
     *            流程实例ID
     * @param activityId
     *            节点ID
     * @return
     */
    @GetMapping("/rollback-activities")
    R<List<?>> getRollbackActivityListByProcessInstanceIdAndActivityId(
        @RequestParam("processInstanceId") String processInstanceId, @RequestParam("activityId") String activityId);

    /**
     * 获取审批列表
     *
     * @param current
     * @param size
     * @param businessCode
     * @param status
     * @return
     */
    @GetMapping("/approve-task")
    R<Page<?>> getApproveTaskList(@RequestParam("current") long current, @RequestParam("size") long size,
        @RequestParam("businessCode") String businessCode,
        @RequestParam("status") String status);

    /**
     * 根据租户id 删除 workflow 库中租户数据
     *
     * @param tenantId
     * @return
     */
    @DeleteMapping("/remove/{tenantId}")
    R clearWorkflowTenantInfo(@PathVariable(value = "tenantId") Long tenantId);

	/***
	 * 判断是否是人工选择
	 * @return
	 */
	@PostMapping("/judge-person-submit")
	R<JudgePersonResponseDTO> judgeSubmitTaskWithPersonExclusive( @RequestBody JudgePersonProcessDTO judgePersonProcessDTO);

	/**
	 * 判断是否是会签节点
	 * @param taskId
	 * @return
	 */
	@GetMapping("/is-countersigntask/{taskId}")
	public R<Boolean> isCountersignTask(@PathVariable(value = "taskId") String taskId);

	/**
	 * 获得投票结果
	 * @param processInstanceId
	 * @param activityId
	 * @return
	 */
	@GetMapping("/vote-result")
	public R<VoteResultDTO> getVoteResult(@RequestParam("processInstanceId") String processInstanceId, @RequestParam("activityId") String activityId);

	/**
	 * 并行会签，投票不通过时更新状态为terminate
	 * @param processInstanceId
	 * @return
	 */
	@GetMapping("/updateParallelStatus")
	public void updateParallelStatus(@RequestParam("processInstanceId") String processInstanceId, @RequestParam("id") String id);

    @GetMapping("/getProcessById/{tenantId}/{businessCode}")
    public R<String> getProcessById(@PathVariable("tenantId") String tenantId,@PathVariable("businessCode") String businessCode);

}
