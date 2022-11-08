/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service</p>
 * <p>文件名:WorkflowBusinessService.java</p>
 * <p>创建时间:2019年12月3日 下午3:26:50</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service;

import com.cescloud.saas.archive.api.modular.workflow.dto.AgreeTaskDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.RollbackPreviousDTO;
import com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO;
import com.cescloud.saas.archive.common.search.Page;

import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月3日
 */
public interface WorkflowBusinessService {

    /**
     * 同意
     *
     * @param agreeTaskDTO
     */
    void agreeTask(String userId, AgreeTaskDTO agreeTaskDTO);

    /**
     * 不同意（终止）
     *
     * @param refuseTaskDTO
     */
    //void refuseTask(String userId, RefuseTaskDTO refuseTaskDTO);

    /**
     * 退回上一节点
     *
     * @param rollbackPreviousDTO
     */
    void rollbackPrevious(String userId, RollbackPreviousDTO rollbackPreviousDTO);

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
     * 首页我的待办
     *
     * @param userId
     *            用户id
     * @param limit
     *            前x条记录
     * @return
     */
    Map<String, Object> getApproveTaskForHomePage(Long userId, int limit);

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
     * 流程跟踪图表
     *
     * @param processInstanceId
     *            流程实例ID
     * @return
     */
    String getProcessGraphImage(String processInstanceId);

    /**
     * 流程跟踪日志
     *
     * @param processInstanceId
     *            流程实例ID
     * @return
     */
    List<?> getProcessGraphLogList(String processInstanceId);

	/**
	 * 获取一个流程的结果，原理是通过 我发起的 页面的流程状态获取
	 * @param processInstanceId
	 * @return
	 */
	Map<String, Object> getOneProcessResult(String processInstanceId);
}
