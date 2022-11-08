/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.controller</p>
 * <p>文件名:WorkflowBusinessController.java</p>
 * <p>创建时间:2019年12月3日 下午4:07:45</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.controller;

import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO;
import com.cescloud.saas.archive.common.search.Page;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService;
import com.cesgroup.humantask.persistence.manager.TaskInfoManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月3日
 */
@Api(value = "工作流业务处理", tags = "工作流业务处理")
@RestController
@AllArgsConstructor
@RequestMapping("/workflow-business")
@Validated
public class WorkflowBusinessController {

    private final WorkflowBusinessService workflowBusinessService;

	@Autowired
	private TaskInfoManager taskInfoManager;

    /*@ApiOperation(value = "同意")
    @PostMapping("/agreement")
    public R<Boolean> agreeTask(@RequestBody AgreeTaskDTO taskDTO) {

        return new R<Boolean>(true);
    }

    @ApiOperation(value = "拒绝/不同意")
    @PostMapping("/rejection")
    public R<Boolean> refuseTask(@RequestBody RefuseTaskDTO taskDTO) {

        return new R<Boolean>(true);
    }*/

    @ApiOperation(value = "发起流程统计")
    @GetMapping("/count/start-process")
    public R<?> countStartProcess(WorkflowSearchDTO dto) {
        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(
            workflowBusinessService.countStartProcess(user.getTenantId().toString(), user.getId().toString(), dto));
    }

    @ApiOperation(value = "发起流程列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "status", value = "状态：active-未办，complete-已办", required = true, paramType = "string")
    })
    @GetMapping("/page/start-process")
    public R<Page<?>> getStartProcessList(Page<?> page, WorkflowSearchDTO dto) {
        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(
            workflowBusinessService.getStartProcessList(page, user.getTenantId().toString(), user.getId().toString(),
                dto));
    }

    @ApiOperation(value = "待发任务统计")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "status", value = "状态", required = true, paramType = "string")
    })
    @GetMapping("/count/unsponsor-task")
    public R<?> countUnsponsorTask(Page<?> page, WorkflowSearchDTO dto) {
        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(
            workflowBusinessService.countUnsponsorTask(user.getTenantId().toString(), user.getId().toString(), dto));
    }

    @ApiOperation(value = "待发任务列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "status", value = "状态：rollback-退回，withdraw-撤回", required = true, paramType = "string")
    })
    @GetMapping("/page/unsponsor-task")
    public R<Page<?>> getUnsponsorTaskList(Page<?> page, WorkflowSearchDTO dto) {
        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(
            workflowBusinessService.getUnsponsorTaskList(page, user.getTenantId().toString(), user.getId().toString(),
                dto));
    }

    @ApiOperation(value = "审批任务统计")
    @GetMapping("/count/approve-task")
    public R<?> countApproveCompletedTask(Page<?> page, WorkflowSearchDTO dto) {
        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(
            workflowBusinessService.countApproveTask(user.getTenantId().toString(), user.getId().toString(), dto));
    }

    @ApiOperation(value = "审批列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "status", value = "状态：active-未办，complete-已办", paramType = "string")
    })
    @GetMapping("/page/approve-task")
    public R<Page<?>> getApproveTaskList(Page<?> page, WorkflowSearchDTO dto) {
        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(
            workflowBusinessService.getApproveTaskList(page, user.getTenantId().toString(), user.getId().toString(),
                dto));
    }


    @GetMapping("getInfoByPId/{id}")
	public R getInfoByProcessInstanceId(@PathVariable("id") String processInstanceId) {
    	return new R(taskInfoManager.findOneApproveTaskList(processInstanceId));
	}

    @ApiOperation(value = "首页我的待办")
    @ApiImplicitParams({ @ApiImplicitParam(name = "limit", value = "显示前x条记录，可以为空1 、2、3等数值", paramType = "string") })
    @GetMapping("/homepage/approve-task")
    public R<Map<String, Object>> getApproveTaskForHomePage(
        @RequestParam(value = "limit", required = false) Integer limit) {
        limit = Optional.ofNullable(limit).orElse(7);
        return new R<>(workflowBusinessService.getApproveTaskForHomePage(SecurityUtils.getUser().getId(), limit));
    }

    @ApiOperation(value = "抄送任务统计")
    @GetMapping("/count/copy-task")
    public R<?> countCopyReadTask(Page<?> page, WorkflowSearchDTO dto) {
        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(
            workflowBusinessService.countCopyTask(user.getTenantId().toString(), user.getId().toString(), dto));
    }

    @ApiOperation(value = "抄送列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "status", value = "状态：active-未读，complete-已读", paramType = "string")
    })
    @GetMapping("/page/copy-task")
    public R<Page<?>> getCopyTaskList(Page<?> page, WorkflowSearchDTO dto) {
        final CesCloudUser user = SecurityUtils.getUser();
        return new R<>(
            workflowBusinessService.getCopyTaskList(page, user.getTenantId().toString(), user.getId().toString(), dto));
    }

    @ApiOperation(value = "流程跟踪图")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processInstanceId", value = "流程实例ID", paramType = "string")
    })
    @GetMapping("/graph/image")
    public R<String> getProcessGraphImage(String processInstanceId) {
        return new R<>(
            workflowBusinessService.getProcessGraphImage(processInstanceId));
    }

    @ApiOperation(value = "流程跟踪日志")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "processInstanceId", value = "流程实例ID", paramType = "string")
    })
    @GetMapping("/graph/logs")
    public R<?> getProcessGraphLogs(String processInstanceId) {
        return new R<>(
            workflowBusinessService.getProcessGraphLogList(processInstanceId));
    }

    @GetMapping("/process/result")
	public R getOneProcessResult(@RequestParam("processInstanceId") String processInstanceId){
    	return new R(workflowBusinessService.getOneProcessResult(processInstanceId));
	}
}
