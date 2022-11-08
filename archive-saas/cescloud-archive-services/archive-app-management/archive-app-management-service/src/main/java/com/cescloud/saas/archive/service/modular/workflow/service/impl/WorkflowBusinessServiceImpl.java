/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service.impl</p>
 * <p>文件名:WorkflowBusinessServiceImpl.java</p>
 * <p>创建时间:2019年12月3日 下午4:03:16</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service.impl;

import com.cescloud.saas.archive.api.modular.workflow.dto.AgreeTaskDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.RollbackPreviousDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO;
import com.cescloud.saas.archive.common.search.Page;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService;
import com.cesgroup.core.util.WorkflowConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月3日
 */
@Component
public class WorkflowBusinessServiceImpl implements WorkflowBusinessService {

    @Autowired
    private WorkflowOpenApiService workflowOpenApiService;

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#agreeTask(java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.AgreeTaskDTO)
     */
    @Override
    public void agreeTask(String userId, AgreeTaskDTO agreeTaskDTO) {
        workflowOpenApiService.completeTask(agreeTaskDTO.getTaskId(),
			userId, true,
            agreeTaskDTO.getComment(), agreeTaskDTO.getAssignees(),agreeTaskDTO.getFormData());
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#refuseTask(java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.RefuseTaskDTO)
     */
//    @Override
//    public void refuseTask(String userId, RefuseTaskDTO refuseTaskDTO) {
//        workflowOpenApiService.terminateTask(refuseTaskDTO.getTaskId(), userId, refuseTaskDTO.getComment());
//    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#rollbackPrevious(java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.RollbackPreviousDTO)
     */
    @Override
    public void rollbackPrevious(String userId, RollbackPreviousDTO rollbackPreviousDTO) {
        workflowOpenApiService.rollbackPrevious(rollbackPreviousDTO.getTaskId(), userId,
            rollbackPreviousDTO.getComment(), null);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#countStartProcess(java.lang.String,
     *      java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Map<String, Integer> countStartProcess(String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return workflowOpenApiService.countStartProcess(tenantId, userId, searchDTO);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#getStartProcessList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Page<?> getStartProcessList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return workflowOpenApiService.getStartProcessList(page, tenantId, userId, searchDTO);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#countUnsponsorTask(java.lang.String,
     *      java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Map<String, Integer> countUnsponsorTask(String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return workflowOpenApiService.countUnsponsorTask(tenantId, userId, searchDTO);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#getUnsponsorTaskList(
     *      com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Page<?> getUnsponsorTaskList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return workflowOpenApiService.getUnsponsorTaskList(page, tenantId, userId, searchDTO);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#countApproveTask(java.lang.String,
     *      java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Map<String, Integer> countApproveTask(String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return workflowOpenApiService.countApproveTask(tenantId, userId, searchDTO);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#getApproveTaskList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Page<?> getApproveTaskList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return workflowOpenApiService.getApproveTaskList(page, tenantId, userId, searchDTO);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#countCopyTask(java.lang.String,
     *      java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Map<String, Integer> countCopyTask(String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return workflowOpenApiService.countCopyTask(tenantId, userId, searchDTO);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#getCopyTaskList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Page<?> getCopyTaskList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return workflowOpenApiService.getCopyTaskList(page, tenantId, userId, searchDTO);
    }

    @Override
    public Map<String, Object> getApproveTaskForHomePage(Long userId, int limit) {
        final WorkflowSearchDTO searchDTO = new WorkflowSearchDTO();
        searchDTO.setStatus(WorkflowConstants.HumanTaskConstants.STATUS_ACTIVE);

        final Map mapResult = new HashMap();
        final Map<String, Integer> mapStatic = workflowOpenApiService
            .countApproveTask(TenantContextHolder.getTenantId().toString(), userId.toString(), searchDTO);
        final Page page = new Page();
        page.setCurrent(1);
        page.setSize(limit);
        final Page pageResult = workflowOpenApiService.getApproveTaskList(page,
            TenantContextHolder.getTenantId().toString(), userId.toString(), searchDTO);
        mapResult.putAll(mapStatic);
        mapResult.put("list", (List) pageResult.getRecords());
        return mapResult;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#getProcessGraphImage(java.lang.String)
     */
    @Override
    public String getProcessGraphImage(String processInstanceId) {
        return workflowOpenApiService.getGraphImageByProcessInstanceId(processInstanceId);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#getProcessGraphLogList(java.lang.String)
     */
    @Override
    public List<?> getProcessGraphLogList(String processInstanceId) {
        return workflowOpenApiService.getTaskLogListByProcessInstanceId(processInstanceId);
    }

	@Override
	public Map<String, Object> getOneProcessResult(String processInstanceId) {
		return workflowOpenApiService.getOneProcessResult(processInstanceId);
	}
}
