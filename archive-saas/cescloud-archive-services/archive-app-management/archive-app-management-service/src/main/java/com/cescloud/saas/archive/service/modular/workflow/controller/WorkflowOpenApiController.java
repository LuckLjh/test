/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.controller</p>
 * <p>文件名:WorkflowOpenApiController.java</p>
 * <p>创建时间:2019年11月14日 下午4:04:08</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.controller;

import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.api.modular.workflow.dto.*;
import com.cescloud.saas.archive.common.search.Page;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.workflow.dto.VoteDTO;
import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月14日
 */
@Api(value = "工作流对外api", tags = "工作流对外api", hidden = true)
@RestController
@AllArgsConstructor
@RequestMapping("/workflow-open-api")
@Validated
public class WorkflowOpenApiController {

    private final WorkflowOpenApiService workflowOpenApiService;

    @ApiOperation(value = "根据流程编码启动流程")
    @PostMapping("/start/bpm")
    public R<String> startProcessByBpmModelCode(
        @Valid @RequestBody @ApiParam(name = "startProcessDTO", value = "启动流程参数", required = true) StartBpmModelProcessDTO startProcessDTO) {

        if (StrUtil.isBlank(startProcessDTO.getUserId())) {
            final CesCloudUser user = SecurityUtils.getUser();
            startProcessDTO.setUserId(user.getId().toString());
            startProcessDTO.setTenantId(user.getTenantId().toString());
        }
        final String processInstanceId = workflowOpenApiService.startProcessByBpmModelCode(
            startProcessDTO.getBpmModelCode(),
            startProcessDTO.getTenantId(),
            startProcessDTO.getUserId(),
            startProcessDTO.getBusinessKey(),
            startProcessDTO.isAutoCommit(),
			startProcessDTO.getAssignees(),
            startProcessDTO.getParamMap(), "");
        return new R<>(processInstanceId);
    }

    @ApiOperation(value = "根据业务编码启动流程")
    @PostMapping("/start/biz")
    public R<String> startProcessByBusinessCode(
        @Valid @RequestBody @ApiParam(name = "startProcessDTO", value = "启动流程参数", required = true) StartBusinessProcessDTO startProcessDTO) {

        if (StrUtil.isBlank(startProcessDTO.getUserId())) {
            final CesCloudUser user = SecurityUtils.getUser();
            startProcessDTO.setTenantId(user.getTenantId().toString());
            startProcessDTO.setUserId(user.getId().toString());
            startProcessDTO.setDeptIdList(Lists.newArrayList(user.getDeptId().toString()));
        }
        final String processInstanceId = workflowOpenApiService.startProcessByBusinessCode(
            startProcessDTO.getBusinessCode(),
            startProcessDTO.getTenantId(),
            startProcessDTO.getUserId(),
            startProcessDTO.getDeptIdList(),
            startProcessDTO.getBusinessKey(),
            startProcessDTO.isAutoCommit(),
			startProcessDTO.getAssignees(),
            startProcessDTO.getParamMap());

        return new R<>(processInstanceId);
    }

    @ApiOperation(value = "提交任务")
    @PostMapping("/complete-task")
    public R<Boolean> completeTask(
        @Valid @RequestBody @ApiParam(name = "completeTaskDTO", value = "提交任务参数", required = true) CompleteTaskDTO completeTaskDTO) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.completeTask(completeTaskDTO.getTaskId(), user.getId().toString(),
            completeTaskDTO.isAgreement(),
            completeTaskDTO.getComment(),
			completeTaskDTO.getAssignees(),
			completeTaskDTO.getParamMap());

        return new R<>(true);
    }

    @ApiOperation(value = "提交流程")
    @PostMapping("/complete-process")
    public R<Boolean> completeProcess(
        @Valid @RequestBody @ApiParam(name = "completeProcessDTO", value = "提交流程参数", required = true) CompleteProcessDTO completeProcessDTO) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.completeProcess(completeProcessDTO.getProcessInstanceId(),
			user.getId().toString(),
            completeProcessDTO.isAgreement(),
			completeProcessDTO.getComment(),
			completeProcessDTO.getAssignees(),
			completeProcessDTO.getParamMap());

        return new R<>(true);
    }

	@ApiOperation(value = "拒绝流程")
	@PostMapping("/refuse-process")
	public R<Boolean> refuseProcess(
			@Valid @RequestBody @ApiParam(name = "terminateProcessDTO", value = "终止任务参数", required = true) TerminateProcessDTO terminateProcessDTO) {
		final CesCloudUser user = SecurityUtils.getUser();
		workflowOpenApiService.terminateProcessInstance(terminateProcessDTO.getProcessInstanceId(),
				user.getId().toString(),user.getChineseName(), WorkflowConstants.NOT,terminateProcessDTO.getComment());

		return new R<>(true);
	}

    @ApiOperation(value = "撤回发起节点提交")
    @PostMapping("/withdraw-sponsor-task/{processInstanceId}")
    public R<Boolean> withdrawSponsorTaskByProcessInstanceId(
        @NotBlank(message = "流程实例ID不能为空") @PathVariable("processInstanceId") String processInstanceId) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.withdrawTaskByProcessInstanceId(user.getTenantId().toString(), user.getId().toString(),
            processInstanceId);

        return new R<>(true);
    }

    @ApiOperation(value = "撤回")
    @PostMapping("/withdraw-process/{processInstanceId}")
    public R<Boolean> withdrawTaskByProcessInstanceId(
        @NotBlank(message = "流程实例ID不能为空") @PathVariable("processInstanceId") String processInstanceId) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.withdrawTaskByProcessInstanceId(user.getTenantId().toString(), user.getId().toString(),
            processInstanceId);

        return new R<>(true);
    }

    @ApiOperation(value = "撤回")
    @PostMapping("/withdraw-task/{taskId}")
    public R<Boolean> withdrawTaskByTaskId(
        @NotBlank(message = "任务ID不能为空") @PathVariable("taskId") String taskId) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.withdrawTaskByTaskId(user.getTenantId().toString(), user.getId().toString(), taskId);

        return new R<>(true);
    }

    @ApiOperation(value = "退回上一节点（流程配置审批人）")
    @PostMapping("/rollback-previous")
    public R<Boolean> rollbackPrevious(
        @Valid @RequestBody @ApiParam(name = "rollbackPreviousDTO", value = "退回上一节点参数", required = true) RollbackPreviousDTO rollbackPreviousDTO) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.rollbackPrevious(rollbackPreviousDTO.getTaskId(), user.getId().toString(),
            rollbackPreviousDTO.getComment(), rollbackPreviousDTO.getParamMap());

        return new R<>(true);
    }

    @ApiOperation(value = "退回指定节点")
    @PostMapping("/rollback-activity")
    public R<Boolean> rollbackActivity(
        @Valid @RequestBody @ApiParam(name = "rollbackActivityDTO", value = "退回上一节点参数", required = true) RollbackActivityDTO rollbackActivityDTO) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.rollbackActivity(rollbackActivityDTO.getTaskId(), rollbackActivityDTO.getActivityId(),
            user.getId().toString(), rollbackActivityDTO.getComment(), rollbackActivityDTO.getParamMap());

        return new R<>(true);
    }

    @ApiOperation(value = "退回上一节点审批人")
    @PostMapping("/rollback-assignee")
    public R<Boolean> rollbackAssignee(
        @Valid @RequestBody @ApiParam(name = "rollbackActivityDTO", value = "退回上一节点参数", required = true) RollbackPreviousDTO rollbackPreviousDTO) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.rollbackAssignee(rollbackPreviousDTO.getTaskId(), user.getId().toString(),
            rollbackPreviousDTO.getComment(), rollbackPreviousDTO.getParamMap());

        return new R<>(true);
    }

    @ApiOperation(value = "终止任务")
    @PostMapping("/terminate-task")
    public R<Boolean> terminateTask(
        @Valid @RequestBody @ApiParam(name = "terminateTaskDTO", value = "终止任务参数", required = true) TerminateTaskDTO terminateTaskDTO) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.terminateTask(terminateTaskDTO.getTaskId(), user.getId().toString(),user.getChineseName(),
				null, terminateTaskDTO.getComment());

        return new R<>(true);
    }

    @ApiOperation(value = "终止流程实例")
    @PostMapping("/terminate-process")
    public R<Boolean> terminateProcessInstance(
        @Valid @RequestBody @ApiParam(name = "terminateProcessDTO", value = "终止任务参数", required = true) TerminateProcessDTO terminateProcessDTO) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.terminateProcessInstance(terminateProcessDTO.getProcessInstanceId(),
            user.getId().toString(),user.getChineseName(),WorkflowConstants.TERMINATE,terminateProcessDTO.getComment());

        return new R<>(true);
    }

    @ApiOperation(value = "转办")
    @PostMapping("/reassign-task")
    public R<Boolean> reassignTask(
        @Valid @RequestBody @ApiParam(name = "reassignTaskDTO", value = "转办任务参数", required = true) ReassignTaskDTO reassignTaskDTO) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.reassignTask(user.getTenantId().toString(), user.getId().toString(),
            reassignTaskDTO.getTaskId(), reassignTaskDTO.getToUserId(), reassignTaskDTO.getComment());

        return new R<>(true);
    }

    @ApiOperation(value = "转发")
    @PostMapping("/deliver-task")
    public R<Boolean> deliverTask(
        @Valid @RequestBody @ApiParam(name = "deliverTaskDTO", value = "终止任务参数", required = true) DeliverTaskDTO deliverTaskDTO) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.deliverTask(deliverTaskDTO.getTaskId(), user.getId().toString(),
            deliverTaskDTO.getToUserIds(), deliverTaskDTO.getComment());

        return new R<>(true);
    }

    @ApiOperation(value = "查阅/回复")
    @PostMapping("/react-task")
    public R<Boolean> reactTask(
        @Valid @RequestBody @ApiParam(name = "reactTaskDTO", value = "查阅/回复参数", required = true) ReactTaskDTO reactTaskDTO) {

        workflowOpenApiService.reactTask(reactTaskDTO.getTaskId(), reactTaskDTO.getComment());

        return new R<>(true);
    }

    @ApiOperation(value = "判断流程是否结束")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processInstanceId", value = "流程实例ID", required = true, dataType = "string")
    })
    @GetMapping("/process-finished/{processInstanceId}")
    public R<Boolean> isProcessFinished(
        @NotBlank(message = "流程实例ID不能为空") @PathVariable("processInstanceId") String processInstanceId) {

        return new R<>(workflowOpenApiService.isProcessFinished(processInstanceId));
    }

    @ApiOperation(value = "删除流程实例")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processInstanceId", value = "流程实例ID", required = true, dataType = "string")
    })
    @DeleteMapping("/process/{processInstanceId}")
    public R<Boolean> deleteProcessInstanceByProcessInstanceId(
        @NotBlank(message = "流程实例ID不能为空") @PathVariable("processInstanceId") String processInstanceId) {

        final CesCloudUser user = SecurityUtils.getUser();
        workflowOpenApiService.deleteProcessInstance(user.getTenantId().toString(), user.getId().toString(),
            processInstanceId);
        return new R<>(true);
    }

    @ApiOperation(value = "业务模型同步", hidden = true)
    @PostMapping("/model/sync")
    @Inner
    public R<Boolean> modelSync(@RequestBody WorkflowBusinessModelSyncDTO businessModelSyncDTO) {
        return new R<Boolean>(workflowOpenApiService.businessSync(businessModelSyncDTO));
    }

    @ApiOperation(value = "待办列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int"),
        @ApiImplicitParam(name = "bpmModelCode", value = "流程编码", required = true, dataType = "string"),
        @ApiImplicitParam(name = "tableName", value = "业务表名", required = false, dataType = "string"),
        @ApiImplicitParam(name = "fields", value = "业务表字段", required = false, dataType = "string"),
        @ApiImplicitParam(name = "filter", value = "业务表过滤条件", required = false, dataType = "string"),
        @ApiImplicitParam(name = "orders", value = "业务表排序", required = false, dataType = "string")
    })
    @GetMapping("/page/uncompleted-task")
    public R<Page<?>> getUncompletedTaskList(Page<?> page, String bpmModelCode, String tableName, String fields,
        String filter,
        String orders) {

        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(workflowOpenApiService.getUncompletedTaskList(page, bpmModelCode, user.getTenantId().toString(),
            user.getId().toString(), tableName, fields, filter, orders));
    }

    @ApiOperation(value = "已办列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int"),
        @ApiImplicitParam(name = "bpmModelCode", value = "流程编码", required = true, dataType = "string"),
        @ApiImplicitParam(name = "tableName", value = "业务表名", required = false, dataType = "string"),
        @ApiImplicitParam(name = "fields", value = "业务表字段", required = false, dataType = "string"),
        @ApiImplicitParam(name = "filter", value = "业务表过滤条件", required = false, dataType = "string"),
        @ApiImplicitParam(name = "orders", value = "业务表排序", required = false, dataType = "string")
    })
    @GetMapping("/page/completed-task")
    public R<Page<?>> getCompletedTaskList(Page<?> page, String bpmModelCode, String tableName, String fields,
        String filter,
        String orders) {

        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(workflowOpenApiService.getCompletedTaskList(page, bpmModelCode, user.getTenantId().toString(),
            user.getId().toString(), tableName, fields, filter, orders));
    }

    @ApiOperation(value = "办结列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int"),
        @ApiImplicitParam(name = "bpmModelCode", value = "流程编码", required = true, dataType = "string"),
        @ApiImplicitParam(name = "tableName", value = "业务表名", required = false, dataType = "string"),
        @ApiImplicitParam(name = "fields", value = "业务表字段", required = false, dataType = "string"),
        @ApiImplicitParam(name = "filter", value = "业务表过滤条件", required = false, dataType = "string"),
        @ApiImplicitParam(name = "orders", value = "业务表排序", required = false, dataType = "string")
    })
    @GetMapping("/page/finished-process")
    public R<Page<?>> getFinishedProcessList(Page<?> page, String bpmModelCode, String tableName, String fields,
        String filter,
        String orders) {

        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(workflowOpenApiService.getFinishedProcessList(page, bpmModelCode, user.getTenantId().toString(),
            user.getId().toString(), tableName, fields, filter, orders));
    }

    @ApiOperation(value = "跟踪流程图")
    @GetMapping("/graph-image/{processInstanceId}")
    public R<String> getGraphImageByProcessInstanceId(
        @NotBlank(message = "流程实例ID不能为空") @PathVariable("processInstanceId") String processInstanceId) {

        return new R<>(workflowOpenApiService.getGraphImageByProcessInstanceId(processInstanceId));
    }

    @ApiOperation(value = "跟踪日志")
    @GetMapping("/task-logs/{processInstanceId}")
    public R<List<?>> getTaskLogListByProcessInstanceId(
        @NotBlank(message = "流程实例ID不能为空") @PathVariable("processInstanceId") String processInstanceId) {

        return new R<>(workflowOpenApiService.getTaskLogListByProcessInstanceId(processInstanceId));
    }

    @ApiOperation(value = "获取退回节点集合")
    @GetMapping("/rollback-activities")
    public R<List<?>> getRollbackActivityListByProcessInstanceIdAndActivityId(
        @NotBlank(message = "流程实例ID不能为空") String processInstanceId,
        @NotBlank(message = "节点ID不能为空") String activityId) {

        return new R<>(workflowOpenApiService.getRollbackActivityListByProcessInstanceIdAndActivityId(processInstanceId,
            activityId));
    }

    @ApiOperation(value = "审批列表")
    @GetMapping("/approve-task")
    public R<Page<?>> getApproveTaskList(Page<?> page, WorkflowSearchDTO searchDTO) {
        final CesCloudUser user = SecurityUtils.getUser();
        return new R(workflowOpenApiService.getApproveTaskList(page, user.getTenantId().toString(),
            user.getId().toString(), searchDTO));
    }

    @ApiOperation(value = "测试", hidden = true)
    @GetMapping("/test")
    public R<?> test() {
        return new R<>(
            workflowOpenApiService.getBpmModelCodeListByBusinessCode("usingProcess", "150", "12256",
                Lists.newArrayList("391471")));
    }

    @ApiOperation(value = "根据租户id清除Workflow表数据", hidden = true)
    @DeleteMapping("/remove/{tenantId}")
    public R clearWorkflowTenantInfo(@PathVariable(value = "tenantId") Long tenantId) {
        return new R(workflowOpenApiService.clearWorkflowTenantInfo(tenantId));
    }

	@ApiOperation(value = "判断是否是人工选择")
	@PostMapping("/judge-person-submit")
	public R<JudgePersonResponseDTO> judgeSubmitTaskWithPersonExclusive(@Valid @RequestBody @ApiParam(name = "judgePersonProcessDTO", value = "判断是否是人工选择参数", required = true)
																	 JudgePersonProcessDTO judgePersonProcessDTO) {
		if (StrUtil.isBlank(judgePersonProcessDTO.getUserId())) {
			final CesCloudUser user = SecurityUtils.getUser();
			judgePersonProcessDTO.setTenantId(user.getTenantId().toString());
			judgePersonProcessDTO.setUserId(user.getId().toString());
			judgePersonProcessDTO.setDeptIdList(Lists.newArrayList(user.getDeptId().toString()));
		}

		JudgePersonResponseDTO judgePersonResponseDTO = workflowOpenApiService.judgeSubmitTaskWithPersonExclusive(
				judgePersonProcessDTO.getBusinessCode(),
				judgePersonProcessDTO.getTenantId(),
				judgePersonProcessDTO.getUserId(),
				judgePersonProcessDTO.getDeptIdList(),
				judgePersonProcessDTO.getBusinessKey(),
				judgePersonProcessDTO.getActivityId(),
				judgePersonProcessDTO.getTaskId(),
				judgePersonProcessDTO.getParamMap());
		return new R<>(judgePersonResponseDTO);
	}

	@ApiOperation(value = "是否是会签节点")
	@GetMapping("/is-countersigntask/{taskId}")
	public R<Boolean> isCountersignTask(@PathVariable(value = "taskId") String taskId){
		return new R().success(workflowOpenApiService.checkIsCountersignTask(taskId),null);
	}

	@ApiOperation(value = "获得投票结果")
	@GetMapping("/vote-result")
	public R<VoteResultDTO> getVoteResult(@NotBlank(message = "流程实例ID不能为空") String processInstanceId,
										@NotBlank(message = "节点ID不能为空") String activityId){
		VoteDTO vote = workflowOpenApiService.getVote(processInstanceId, activityId);
		VoteResultDTO voteResultDTO = VoteResultDTO.builder().completed(vote.getCompleted()).result(vote.getResult()).build();
		return new R().success(voteResultDTO,null);
	}


	@ApiOperation(value = "根据实例id查询下一步的待执行任务", hidden = true)
	@GetMapping("/task-list/{processInstanceId}")
	public R<List<Map<String, Object>>> getApproveTaskList(@PathVariable(value = "processInstanceId") String processInstanceId) {
		List<Map<String, Object>> taskList = workflowOpenApiService.getApproveTaskList(SecurityUtils.getUser().getTenantId().toString(),processInstanceId);
		return new R(taskList);
	}

	@ApiOperation(value = "并行会签，投票不通过时更新状态为terminate")
	@GetMapping("/updateParallelStatus")
	public void updateParallelStatus(String processInstanceId, String id) {
		workflowOpenApiService.updateParallelStatus(processInstanceId, id);
	}

	@ApiOperation(value = "判断这个版本是否被流程使用过")
	@GetMapping("/process-was-used")
	public R<Boolean> getProcessWasUsed(@RequestParam("processDefinitionId") String processDefinitionId) {
		return new R<>(workflowOpenApiService.getProcessWasUsed(processDefinitionId));
	}
    @ApiOperation(value = "根据流程id获取名字")
    @GetMapping("/getProcessById/{tenantId}/{businessCode}")
	public R<String> getProcessById(@PathVariable("tenantId") String tenantId,@PathVariable("businessCode") String businessCode){
        return new R<String>(workflowOpenApiService.getProcessById(tenantId,businessCode));
    }
}
