/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service.impl</p>
 * <p>文件名:WorkflowOpenApiServiceImpl.java</p>
 * <p>创建时间:2019年11月14日 上午11:15:46</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.api.modular.dept.entity.SysDept;
import com.cescloud.saas.archive.api.modular.dept.feign.RemoteDeptService;
import com.cescloud.saas.archive.api.modular.user.entity.SysUser;
import com.cescloud.saas.archive.api.modular.user.feign.RemoteUserService;
import com.cescloud.saas.archive.api.modular.workflow.dto.*;
import com.cescloud.saas.archive.api.modular.workflow.feign.RemoteWorkflowApiService;
import com.cescloud.saas.archive.common.constants.business.FilingFieldConstants;
import com.cescloud.saas.archive.common.search.Page;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.workflow.mapper.WorkflowMapper;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService;
import com.cescloud.saas.archive.service.modular.workflow.utils.WorkflowUtil;
import com.cesgroup.api.core.workflow.interf.WorkflowAPI;
import com.cesgroup.api.humantask.HumanTaskConnector;
import com.cesgroup.api.process.ProcessConnector;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import com.cesgroup.bpm.persistence.domain.BpmModelEntity;
import com.cesgroup.bpm.persistence.domain.BpmModelPurview;
import com.cesgroup.bpm.persistence.manager.BpmConfBaseManager;
import com.cesgroup.bpm.persistence.manager.BpmModelManager;
import com.cesgroup.bpm.persistence.manager.BpmModelPurviewManager;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.NodeConfInfo;
import com.cesgroup.humantask.persistence.domain.TaskInfo;
import com.cesgroup.humantask.persistence.domain.TaskInfoHis;
import com.cesgroup.humantask.persistence.manager.TaskInfoManager;
import com.cesgroup.workflow.dto.VoteDTO;
import com.cesgroup.workflow.persistence.domain.BusinessMetadata;
import com.cesgroup.workflow.persistence.domain.BusinessModel;
import com.cesgroup.workflow.persistence.manager.BusinessMetadataManager;
import com.cesgroup.workflow.persistence.manager.BusinessModelManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.impl.identity.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年11月14日
 */
@Component
@Slf4j
public class WorkflowOpenApiServiceImpl implements WorkflowOpenApiService {

    @Autowired
    private WorkflowAPI workflowAPI;

    @Autowired
    private ProcessConnector processConnector;

    @Autowired
    private BpmModelManager bpmModelManager;

    @Autowired
    private BpmConfBaseManager bpmConfBaseManager;

    @Autowired
    private TaskInfoManager taskInfoManager;

    @Autowired
    private BusinessModelManager businessModelManager;

    @Autowired
    private BusinessMetadataManager businessMetadataManager;

    @Autowired
    private BpmModelPurviewManager bpmModelPurviewManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
	private HumanTaskConnector humanTaskConnector;

	@Autowired
	@Lazy
	private RemoteWorkflowApiService remoteWorkflowApiService;

	@Autowired
	@Lazy
	private WorkflowMapper workflowMapper;

	@Autowired
	@Lazy
	private RemoteDeptService remoteDeptService;

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#startProcessByBpmModelCode(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, boolean,
     *      java.util.Map)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startProcessByBpmModelCode(String bpmModelCode, String tenantId, String userId, String businessKey,
        boolean autoCommit, String assignees, Map<String, Object> paramMap, String processDefinitionId) {
        if (null == paramMap) {
            paramMap = new HashMap<String, Object>(8);
        }
        // 工作流表达式用到，如果不设置，则审批人为表式时会报错
        if (!paramMap.containsKey("roleId")) {
            paramMap.put("roleId", null);
        }

        //放入指定的候选人
		if (StrUtil.isNotEmpty(assignees)) {
			paramMap.put("codeAssigneeList", assignees);
			paramMap.put("assigneeList", assignees);
		}

		BpmConfBase bpmConfBase = getActiveProcessDefinitionKeyByBpmModelCode(tenantId, bpmModelCode, processDefinitionId);
		processFormulaMap(paramMap, bpmModelCode, tenantId, userId, businessKey);
        return workflowAPI.startProcessInstanceByprocessDefinitionKey(tenantId, userId, businessKey,
				bpmConfBase.getProcessDefinitionKey(), paramMap, autoCommit, userId);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#startProcessByBusinessCode(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.List,
     *      java.lang.String, boolean, java.util.Map)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startProcessByBusinessCode(String businessCode, String tenantId, String userId,
        List<String> deptIdList, String businessKey,
        boolean autoCommit, String assignees, Map<String, Object> paramMap) {

        final List<String> bpmModelCodeList = getBpmModelCodeListByBusinessCode(businessCode,
            tenantId, userId, deptIdList);

        if (bpmModelCodeList.isEmpty()) {
            throw new ArchiveRuntimeException(String.format("业务编码[%s]未找到与当前用户绑定的流程", businessCode));
        }
		String processDefinitionId = getProcessDefinitionId(tenantId, businessCode);

		return startProcessByBpmModelCode(bpmModelCodeList.get(0), tenantId, userId, businessKey, autoCommit, assignees, paramMap, processDefinitionId);
    }

    public String getProcessDefinitionId(String tenantId, String businessCode){
    	CesCloudUser user = SecurityUtils.getUser();
		List<String> depts = new ArrayList<>();
		List<SysDept> sysDepts = remoteDeptService.getUserParentDepts(user.getDeptId(), Boolean.TRUE).getData();
		if (ObjectUtil.isNotNull(sysDepts)) {
			depts = sysDepts.parallelStream().map(sysDept -> StrUtil.toString(sysDept.getDeptId())).collect(Collectors.toList());
		}
    	final List<BpmModelEntity> bpmModelList = bpmModelManager.findByBusinessCode(tenantId, businessCode);
		final List<BpmModelPurview> bpmModelPurviewList = bpmModelPurviewManager.findByBusinessCode(tenantId, businessCode);
		String purview = processPurview(bpmModelPurviewList, user.getId().toString(), depts, bpmModelList);
		//查找当前用户被授予了哪个权限的流程
		List<BpmModelEntity> list = bpmModelList.stream().filter(e -> e.getCode().equals(purview)).collect(Collectors.toList());
		String confBaseModelId = list.get(0).getConfBaseModelId();
		return list.get(0).getBpmConfBases().stream()
				.filter(e -> e.getModelId().equals(confBaseModelId))
				.findFirst().get().getProcessDefinitionId();
	}

    // 表达式所需要的预设参数
    private void processFormulaMap(Map<String, Object> paramMap, String bpmModelCode, String tenantId, String userId,
        String businessKey) {
        if (!paramMap.containsKey("formulaMap")) {
            paramMap.put("formulaMap", new HashMap<String, Object>(8));
        }
        final Map<String, Object> formulaMap = (Map<String, Object>) paramMap.get("formulaMap");
        if (!formulaMap.containsKey("bpmModelCode")) {
            formulaMap.put("bpmModelCode", bpmModelCode);
        }
        if (!formulaMap.containsKey("userId")) {
            formulaMap.put("userId", userId);
        }
        if (!formulaMap.containsKey("businessKey")) {
            formulaMap.put("businessKey", businessKey);
        }
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService
     * #completeTask(java.lang.String,java.lang.String, boolean, java.lang.String, java.util.Map)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, String userId, Boolean agreement, String comment, String assignees,
        Map<String, Object> paramMap) {
        if (null == paramMap) {
            paramMap = new HashMap<String, Object>(8);
        }
        //添加同意不同意数据，可以提供给节点工具使用和条件
        if (null != agreement) {
            paramMap.put("agreement", agreement ? "1" : "0");
        }
        //判断是否已签收
        if (!isCheckOut(taskId, userId)) {
            workflowAPI.claimTask(taskId, userId, userId);
        }
        // 检查任务是否是并行会签任务
        if (workflowAPI.checkIsCountersignTask(taskId)) {
            //并行会签，设置投票同意票数、不同意票数及总票数参数名称
            paramMap.put("workflow_agreeParamName", "agreement,num_yes,num_no,num_total");
        }
		//放入指定的候选人
		if (StrUtil.isNotEmpty(assignees)) {
			paramMap.put("codeAssigneeList", assignees);
			paramMap.put("assigneeList", assignees);
		}
        // 提交
        try {
            workflowAPI.completeTask(taskId, userId, WorkflowConstants.HumanTaskConstants.ACTION_COMPLETE,
                agreement, comment, paramMap, userId);
        } catch (final Exception e) {
			log.error("提交失败，出错信息：{}", e.getMessage());
            throw new ArchiveRuntimeException(e.getMessage(), e);
        }finally {
			Authentication.setAuthenticatedUserId(null);
		}
    }

	/**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService
     * #completeProcess(java.lang.String,java.lang.String, boolean, java.lang.String, java.util.Map)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeProcess(String processInstanceId, String userId, Boolean agreement, String comment, String assignees,
        Map<String, Object> paramMap) {
        final List<TaskInfo> activeTaskList = taskInfoManager
            .findActiveTaskListByProcessInstanceIdAndUserId(processInstanceId, userId);
        if (null == activeTaskList || activeTaskList.isEmpty()) {
            throw new ArchiveRuntimeException("用户没有可提交的任务");
        }
        for (final TaskInfo taskInfo : activeTaskList) {
            completeTask(taskInfo.getId().toString(), userId, agreement, comment, assignees, paramMap);
        }
    }

	/**
	 * 判断是否是人工选择
	 * @param processInstanceId
	 * @param taskId
	 */
	@Override
	public JudgePersonResponseDTO judgeSubmitTaskWithPersonExclusive(String businessCode, String tenantId, String userId, List<String> deptIdList,
																	 String businessKey, String activityId, String taskId, Map<String, Object> paramMap) {
		if (null == paramMap) {
			paramMap = new HashMap<String, Object>(8);
		}
		// 工作流表达式用到，如果不设置，则审批人为表式时会报错
		if (!paramMap.containsKey("roleId")) {
			paramMap.put("roleId", null);
		}
		if (!paramMap.containsKey(FilingFieldConstants.ARCHIVE_DEPT_NAME)) {
			String deptIds = Optional.ofNullable((String)paramMap.get(FilingFieldConstants.ARCHIVE_DEPT)).orElse("");
			if (StrUtil.isNotBlank(deptIds)) {
				StringBuilder deptName = new StringBuilder();
				List<Long> deptList = Arrays.stream(deptIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
				deptList.forEach(e -> {
					SysDept dept = remoteDeptService.getDeptById(e).getData();
					deptName.append(dept.getName()).append(",");
				});
				paramMap.put(FilingFieldConstants.ARCHIVE_DEPT_NAME, deptName.substring(0, deptName.length()-1));
			} else {
				paramMap.put(FilingFieldConstants.ARCHIVE_DEPT_NAME, "");
			}
		}
		final List<String> bpmModelCodeList = getBpmModelCodeListByBusinessCode(businessCode,
				tenantId, userId, deptIdList);

		if (bpmModelCodeList.isEmpty()) {
			throw new ArchiveRuntimeException(String.format("业务编码[%s]未找到与当前用户绑定的流程", businessCode));
		}
		String processDefinitionId = getProcessDefinitionId(tenantId, businessCode);
		final BpmConfBase bpmConfBase = getActiveProcessDefinitionKeyByBpmModelCode(tenantId, bpmModelCodeList.get(0), processDefinitionId);
		processFormulaMap(paramMap, bpmModelCodeList.get(0), tenantId, userId, businessKey);
		//是否需要弹出人员选择框
		Boolean showPerson = workflowAPI.findActivityImplSubmitTaskWithPerson(taskId,bpmConfBase.getModelId());
		JudgePersonResponseDTO judgePersonResponseDTO = new JudgePersonResponseDTO();
		//显示人员列表
		if (showPerson){
			if ("__0__".equals(activityId)) {
				activityId = workflowAPI.getStartBehindActivityIds(bpmConfBase.getModelId()).get(0);
				paramMap.put("initiator",userId);
			}else {
				paramMap.put("initiator",taskInfoManager.get(Long.parseLong(taskId)).getProcessStarter());
			}
			List<NodeConfInfo> list = workflowAPI.getFakeNodeConfInfoByProcessDefinitionIdAndMap(tenantId, processDefinitionId,activityId, paramMap);
			if (list.size() > 0) {
				Set<UserDTO> users = list.get(0).getUsers();
				if (CollUtil.isEmpty(users)) {
                    judgePersonResponseDTO.setShowPerson(Boolean.FALSE);
                    return judgePersonResponseDTO;
                } else {
                    users.stream().forEach(userDTO -> {
                        //保留当前用户
                        //if (!userId.equals(userDTO.getId())) {
                        SysUser sysUser = new SysUser();
                        sysUser.setUserId(Long.valueOf(userDTO.getId()));
                        sysUser.setChineseName(userDTO.getDisplayName());
                        sysUser.setUsername(userDTO.getUsername());
                        judgePersonResponseDTO.getPersonList().add(sysUser);
                        //}
                    });
                }
			}
			judgePersonResponseDTO.setShowPerson(Boolean.TRUE);
		}else{
			judgePersonResponseDTO.setShowPerson(Boolean.FALSE);
		}
		return judgePersonResponseDTO;
	}

	@Override
	public Boolean checkIsCountersignTask(String taskId) {
		return workflowAPI.checkIsCountersignTask(taskId);
	}

	@Override
	public VoteDTO getVote(String processInstanceId, String activityId) {
		return workflowAPI.getVote(processInstanceId,activityId);
	}

	private boolean isCheckOut(String taskId, String userId) {
        final TaskInfo taskInfoHis = taskInfoManager.findUniqueBy("id", Long.parseLong(taskId));
        if (StrUtil.isBlank(taskInfoHis.getAssignee())) {
            return false;
        } else {
            if (!taskInfoHis.getAssignee().equals(userId)) {
                throw new ArchiveRuntimeException("流程已被他人审批了，请刷新后再操作");
            }
        }
        return true;
    }

	/**
	 * 终止用判断是否被审批
	 * @param taskId
	 * @param userId
	 * @return
	 */
	private boolean isCheckOutStop(String taskId, String userId) {
		final TaskInfo taskInfoHis = taskInfoManager.findUniqueBy("id", Long.parseLong(taskId));
		if (StrUtil.isBlank(taskInfoHis.getAssignee())) {
			return false;
		}
		return true;
	}
    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#withdrawSponsorTaskByProcessInstanceId(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawSponsorTaskByProcessInstanceId(String tenantId, String userId, String processInstanceId) {
        final TaskInfo withdrawTask = taskInfoManager
            .findWithdrawSponsorTaskListByProcessInstanceId(processInstanceId, userId);
        if (null == withdrawTask) {
            throw new ArchiveRuntimeException("该任务不能撤回");
        }
        if (!WorkflowConstants.HumanTaskConstants.STATUS_COMPLETE.equals(withdrawTask.getStatus())) {
            throw new ArchiveRuntimeException("该任务未提交，无需撤回");
        }
        //判断任务是否挂起或者存在
        final String taskId = withdrawTask.getId().toString();
        boolean flag = false;
        try {
            flag = workflowAPI.withdrawCheck2(taskId);
        } catch (final Exception e) {
            throw new ArchiveRuntimeException("该任务不能撤回");
        }
        if (flag) {
        	try {
				workflowAPI.withdraw(taskId, "任务被撤回", userId);
			} catch (Exception e) {
        		throw new ArchiveRuntimeException("该任务不能撤回",e);
			}finally {
				Authentication.setAuthenticatedUserId(null);
			}
        } else {
            throw new ArchiveRuntimeException("该任务不能撤回");
        }
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#withdrawTaskByProcessInstanceId(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawTaskByProcessInstanceId(String tenantId, String userId, String processInstanceId) {
        final List<TaskInfo> withdrawTaskInfoList = taskInfoManager
            .findWithdrawTaskListByProcessInstanceId(processInstanceId, userId);
        if (null == withdrawTaskInfoList || withdrawTaskInfoList.isEmpty()) {
            throw new ArchiveRuntimeException("该流程没有可以撤回的节点");
        }
        if (withdrawTaskInfoList.size() > 1) {
            throw new ArchiveRuntimeException("多条任务不允许撤回");
        }
        //判断任务是否挂起或者存在
        final String taskId = withdrawTaskInfoList.get(0).getId().toString();
        boolean flag = false;
        try {
            flag = workflowAPI.withdrawCheck2(taskId);
        } catch (final Exception e) {
            throw new ArchiveRuntimeException("该任务不能撤回");
        }
        if (flag) {
			try {
				workflowAPI.withdraw(taskId, "任务被撤回", userId);
			} catch (Exception e) {
				throw new ArchiveRuntimeException("该任务不能撤回",e);
			}finally {
				Authentication.setAuthenticatedUserId(null);
			}
        } else {
            throw new ArchiveRuntimeException("该任务不能撤回");
        }
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#withdrawTaskByTaskId(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawTaskByTaskId(String tenantId, String userId, String taskId) {
        boolean flag = false;
        try {
            flag = workflowAPI.withdrawCheck2(taskId);
        } catch (final Exception e) {
            throw new ArchiveRuntimeException("该任务不能撤回");
        }
        if (flag) {
			try {
				workflowAPI.withdraw(taskId, null, userId);
			} catch (Exception e) {
				throw new ArchiveRuntimeException("该任务不能撤回",e);
			}finally {
				Authentication.setAuthenticatedUserId(null);
			}
        } else {
            throw new ArchiveRuntimeException("该任务不能撤回");
        }
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#rollbackPrevious(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackPrevious(String taskId, String userId, String comment, Map<String, Object> paramMap) {
        //判断是否已签收
        if (!isCheckOut(taskId, userId)) {
            workflowAPI.claimTask(taskId, userId, userId);
        }
        try{
			workflowAPI.rollbackPrevious(taskId, comment, paramMap, userId);
		} catch (Exception e) {
			throw new ArchiveRuntimeException("该任务不能退回",e);
		}finally {
			Authentication.setAuthenticatedUserId(null);
		}
    }

    private BpmConfBase getActiveProcessDefinitionKeyByBpmModelCode(String tenantId, String bpmModelCode, String processDefinitionId) {
        final BpmModelEntity bpmModelEntity = bpmModelManager.findUnique(
            " from BpmModelEntity b where b.code = ?0 and b.tenantId = ?1", bpmModelCode, tenantId);
        if (null == bpmModelEntity) {
            throw new ArchiveRuntimeException(String.format("流程编码[%s]对应的流程未定义或不存在", bpmModelCode));
        }
        if (1 != bpmModelEntity.getStatus()) {
            throw new ArchiveRuntimeException(String.format("流程[%s]对应的流程未启用", bpmModelEntity.getName()));
        }
        //GDDA8-2203 已停用的流程版本还要可以继续走
        return bpmConfBaseManager.findUnique(" from BpmConfBase b where b.bpmModel = ?0 and b.processDefinitionId=?1", bpmModelEntity, processDefinitionId);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#rollbackActivity(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackActivity(String taskId, String activityId, String userId, String comment,
        Map<String, Object> paramMap) {
        //判断是否已签收
        if (!isCheckOut(taskId, userId)) {
            workflowAPI.claimTask(taskId, userId, userId);
        }
        try{
        	workflowAPI.rollbackActivity(taskId, activityId, comment, paramMap, userId);
		} catch (Exception e) {
			throw new ArchiveRuntimeException("该任务不能退回",e);
		}finally {
			Authentication.setAuthenticatedUserId(null);
		}
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#rollbackAssignee(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackAssignee(String taskId, String userId, String comment,
        Map<String, Object> paramMap) {
        //判断是否已签收
        if (!isCheckOut(taskId, userId)) {
            workflowAPI.claimTask(taskId, userId, userId);
        }
        try{
        	workflowAPI.rollbackAssignee(taskId, comment, paramMap, userId);
		} catch (Exception e) {
			throw new ArchiveRuntimeException("该任务不能退回",e);
		}finally {
			Authentication.setAuthenticatedUserId(null);
		}
    }

	/**
	 *
	 * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#terminateTask(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void terminateTask(String taskId, String userId, String userName, String agreement, String comment) {
		//判断是否已签收
		//if (!isCheckOut(taskId, userId)) {
		//    workflowAPI.claimTask(taskId, userId, userId);
		//}
		workflowAPI.completeTaskByHumanTaskIds(taskId, userId, userName, agreement,  comment);
	}

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#terminateProcessInstance(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminateProcessInstance(String processInstanceId, String userId,String userName, String agreement, String comment) {
        final List<TaskInfoHis> taskList = taskInfoManager.find(
            "from TaskInfoHis t where t.processInstanceId=?0 and t.completeStatus=?1 and (t.action is null or t.action=?2)", processInstanceId,
            WorkflowConstants.HumanTaskConstants.STATUS_ACTIVE, WorkflowConstants.HumanTaskConstants.ACTION_CLAIM);
        if (ObjectUtil.isEmpty(taskList)) {
            throw new ArchiveRuntimeException("流程不存在或已经结束!");
        }
        //记录日志详情
		if (WorkflowConstants.TERMINATE.equals(agreement)) {
			List<String> collect = taskList.stream().map(TaskInfoHis::getPresentationSubject).collect(Collectors.toList());
			try {
				SysLogContextHolder.setLogTitle(String.format("终止流程-实例名称 %s", collect.toString()));
			} catch (Exception e) {
				log.error("记录日志详情失败：", e);
			}
		}
        for (final TaskInfoHis task : taskList) {
            terminateTask(task.getId().toString(), userId, userName, agreement, comment);
        }
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#deleteProcessInstance(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessInstance(String tenantId, String userId, String processInstanceIds) {
        processConnector.modifyInstanceStatusByProcessInstanceId(tenantId, processInstanceIds,
            WorkflowConstants.ProcessConstants.END, userId);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#deliverTask(java.lang.String,
     *      java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public void deliverTask(String taskId, String userId, String[] toUserIds, String comment) {
        //判断是否已签收
        if (!isCheckOut(taskId, userId)) {
            workflowAPI.claimTask(taskId, userId, userId);
        }
        workflowAPI.communicate(taskId, Sets.newHashSet(toUserIds), comment, userId);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#reactTask(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void reactTask(String taskId, String comment) {
        workflowAPI.react(taskId, comment);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#reassignTask(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void reassignTask(String tenantId, String userId, String taskId, String toUserId, String comment) {
        //判断是否已签收
        if (!isCheckOut(taskId, userId)) {
            workflowAPI.claimTask(taskId, userId, userId);
        }
        workflowAPI.transfer(taskId, toUserId, comment, userId, tenantId);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#isProcessFinished(java.lang.String)
     */
    @Override
    public boolean isProcessFinished(String processInstanceId) {
        return workflowAPI.judgeProcessEnd(processInstanceId);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService#
     * businessSync(com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowBusinessModelSyncDTO)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean businessSync(WorkflowBusinessModelSyncDTO businessModelSyncDTO) {
        // 同步业务模型
        final WorkflowBusinessModelDTO businessModelDTO = businessModelSyncDTO.getBusinessModel();
        syncModel(businessModelDTO);
        // 同步业务字段
        final List<WorkflowBusinessMetadataDTO> businessMetadataDTOList = businessModelSyncDTO.getBusinessMetadataList();
        syncMetadata(businessModelSyncDTO.getTenantId(), businessModelDTO.getCode(), businessMetadataDTOList);
        return true;
    }

    // 同步业务模型
    private void syncModel(WorkflowBusinessModelDTO businessModelDTO) {
        BusinessModel businessModel = businessModelManager.findUnique(
            "from BusinessModel t where t.tenantId=?0 and t.code=?1", businessModelDTO.getTenantId(),
            businessModelDTO.getCode());
        if (null == businessModel) {
            businessModel = WorkflowUtil.convert(new BusinessModel(), businessModelDTO);
        } else {
            businessModel.setName(businessModelDTO.getName());
            businessModel.setDescription(businessModelDTO.getDescription());
        }
        if (StrUtil.isBlank(businessModel.getTenantId())) {
            businessModel.setTenantId("-1");
            businessModel.setCommon(true);
        }
        businessModelManager.save(businessModel);
    }

    // 同步业务字段
    private void syncMetadata(String tenantId, String businessCode,
        List<WorkflowBusinessMetadataDTO> businessMetadataDTOList) {
        // 删除公共的字段
        businessMetadataManager.batchUpdate("delete BusinessMetadata where tenantId=?0 and businessCode=?1", "-1", businessCode);
        // 删除租户的字段
        businessMetadataManager.batchUpdate("delete BusinessMetadata where tenantId=?0 and businessCode=?1", tenantId, businessCode);
        // 保存所有字段
        Integer sortNo = 1;
        BusinessMetadata businessMetadata = null;
        for (final WorkflowBusinessMetadataDTO businessMetadataDTO : businessMetadataDTOList) {
            businessMetadata = WorkflowUtil.convert(new BusinessMetadata(), businessMetadataDTO);
            businessMetadata.setBusinessCode(businessCode);
            businessMetadata.setBusinessType(WorkflowConstants.BusinessType.MATSER);
            businessMetadata.setSortNo(sortNo++);
            businessMetadataManager.save(businessMetadata);
        }
        businessMetadataManager.flush();
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowBusinessService
     * #getBpmModelCodeListByBusinessCode(java.lang.String,java.lang.String, java.lang.String, List)
     */
    @Override
    public List<String> getBpmModelCodeListByBusinessCode(String businessCode, String tenantId, String userId,
        List<String> deptIdList) {

        final List<BpmModelEntity> bpmModelList = bpmModelManager.findByBusinessCode(tenantId, businessCode);
        if (null == bpmModelList || bpmModelList.isEmpty()) {
            /*if (log.isWarnEnabled()) {
                log.warn("业务编码[{}]未关联流程或流程未启用", businessCode);
            }
            return Lists.newArrayList();*/
            throw new ArchiveRuntimeException(String.format(
                com.cescloud.saas.archive.api.modular.workflow.constant.WorkflowConstants.ErrorMsg.PROCESS_DISABLE_MSG,
                businessCode));
        }

        final List<String> codeList = Lists.newArrayList();

        final List<BpmModelPurview> bpmModelPurviewList = bpmModelPurviewManager.findByBusinessCode(tenantId,
            businessCode);
        if (null == bpmModelPurviewList || bpmModelPurviewList.isEmpty()) {
            for (final BpmModelEntity entity : bpmModelList) {
                if (1 != entity.getStatus()) {
                    if (log.isInfoEnabled()) {
                        log.info("流程[{}]未启用", entity.getName());
                    }
                } else {
                    codeList.add(entity.getCode());
                }
            }
        } else {
            final String bpmModelCode = processPurview(bpmModelPurviewList, userId, deptIdList, bpmModelList);
            if (null != bpmModelCode) {
                codeList.add(bpmModelCode);
            }
        }
        return codeList;
    }

    private Map<String, Boolean> processBpmModelStatusMap(List<BpmModelEntity> bpmModelList) {
        final Map<String, Boolean> bpmModelStatusMap = Maps.newHashMap();

        for (final BpmModelEntity entity : bpmModelList) {
            bpmModelStatusMap.put(entity.getCode(), 1 == entity.getStatus());
        }

        return bpmModelStatusMap;
    }

    private String processPurview(List<BpmModelPurview> bpmModelPurviewList, String userId, List<String> deptIdList,
        List<BpmModelEntity> bpmModelList) {
        final Map<String, String> purviewToMap = modelPurviewToMap(bpmModelPurviewList);
        final Map<String, Boolean> bpmModelStatusMap = processBpmModelStatusMap(bpmModelList);
        // 查看用户是否有绑定流程
        String objectKey = getObjectKey(WorkflowConstants.ObjectType.USER, userId);
        String bpmModelCode = purviewToMap.get(objectKey);
        if (null != bpmModelCode) {
            if (checkBpmModelCode(objectKey, bpmModelCode, bpmModelStatusMap)) {
                return bpmModelCode;
            }
        }
        // 查看部门是否有绑定流程
        for (final String deptId : deptIdList) {
            objectKey = getObjectKey(WorkflowConstants.ObjectType.DEPT, deptId);
            bpmModelCode = purviewToMap.get(objectKey);
            if (null != bpmModelCode) {
                if (checkBpmModelCode(objectKey, bpmModelCode, bpmModelStatusMap)) {
                    return bpmModelCode;
                }
            }
        }
        // 查看是否有全部可见的流程
        final Set<String> purviewBpmModelCodeSet = new HashSet<String>(purviewToMap.values());
        for (final BpmModelEntity entity : bpmModelList) {
            if (purviewBpmModelCodeSet.contains(entity.getCode())) {
                continue;
            }
            if (1 != entity.getStatus()) {
                if (log.isInfoEnabled()) {
                    log.info("流程[{}]未启用", entity.getName());
                }
            } else {
                return entity.getCode();
            }
        }
        return null;
    }

    private boolean checkBpmModelCode(String objectKey, String bpmModelCode, Map<String, Boolean> bpmModelStatusMap) {
        if (!bpmModelStatusMap.containsKey(bpmModelCode)) {
            if (log.isWarnEnabled()) {
                log.info("用户[{}]绑定的流程[{}]不存在或已被删除", objectKey, bpmModelCode);
            }
            return false;
        }
        if (!bpmModelStatusMap.get(bpmModelCode)) {
            if (log.isWarnEnabled()) {
                log.info("用户[{}]绑定的流程[{}]未启用", objectKey, bpmModelCode);
            }
            return false;
        }
        return true;
    }

    private Map<String, String> modelPurviewToMap(List<BpmModelPurview> bpmModelPurviewList) {
        final Map<String, String> purviewToMap = Maps.newHashMap();

        for (final BpmModelPurview entity : bpmModelPurviewList) {
            purviewToMap.put(getObjectKey(entity.getObjectType(), entity.getObjectId()), entity.getBpmModelCode());
        }

        return purviewToMap;
    }

    private String getObjectKey(String objectType, Object objectId) {
        return objectType + ":" + objectId;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getUncompletedTaskList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Page<?> getUncompletedTaskList(Page<?> page, String bpmModelCode, String tenantId, String userId,
        String tableName, String fields, String filter, String orders) {
        final com.cesgroup.core.page.Page wfPage = taskInfoManager.findUncompletedTaskList(tenantId, userId,
            bpmModelCode,
            (int) page.getCurrent(),
            (int) page.getSize(),
            tableName, "id",
            null == fields ? null : fields.split(","),
            filter, orders);

        page.setRecords((List) wfPage.getResult());
        page.setTotal(wfPage.getTotalCount());

        return page;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getCompletedTaskList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Page<?> getCompletedTaskList(Page<?> page, String bpmModelCode, String tenantId, String userId,
        String tableName, String fields, String filter, String orders) {
        final com.cesgroup.core.page.Page wfPage = taskInfoManager.findCompletedTaskList(tenantId, userId,
            bpmModelCode,
            (int) page.getCurrent(),
            (int) page.getSize(),
            tableName, "id",
            null == fields ? null : fields.split(","),
            filter, orders);

        page.setRecords((List) wfPage.getResult());
        page.setTotal(wfPage.getTotalCount());

        return page;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getFinishedProcessList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Page<?> getFinishedProcessList(Page<?> page, String bpmModelCode, String tenantId, String userId,
        String tableName, String fields, String filter, String orders) {
        final com.cesgroup.core.page.Page wfPage = taskInfoManager.findFinishedProcessList(tenantId, userId,
            bpmModelCode,
            (int) page.getCurrent(),
            (int) page.getSize(),
            tableName, "id",
            null == fields ? null : fields.split(","),
            filter, orders);

        page.setRecords((List) wfPage.getResult());
        page.setTotal(wfPage.getTotalCount());

        return page;
    }

    private Page<?> convertPage(Page<?> page, com.cesgroup.core.page.Page wfPage) {
        page.setRecords((List) wfPage.getResult());
        page.setTotal(wfPage.getTotalCount());
        return page;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#countStartProcess(java.lang.String,
     *      java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Map<String, Integer> countStartProcess(String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return taskInfoManager.countStartProcess(tenantId, userId, searchDTO.getBusinessCode(),
            searchDTO.getStartTime(), searchDTO.getEndTime());
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getStartProcessList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String, WorkflowSearchDTO)
     */
    @Override
    public Page<?> getStartProcessList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        final com.cesgroup.core.page.Page wfPage = taskInfoManager.findStartProcessList(tenantId, userId,
            (int) page.getCurrent(), (int) page.getSize(), searchDTO.getStatus(), searchDTO.getBusinessCode(),
            searchDTO.getStartTime(),
            searchDTO.getEndTime());
        //我的协同-我发起的，是否显示撤回按钮
		((List<Map<String, Object>>) wfPage.getResult()).forEach(e -> {
			List<Map<String, Object>> logs = (List<Map<String, Object>>) getTaskLogListByProcessInstanceId(String.valueOf(e.get("process_instance_id")));
			//流程刚提交且下一节点没有人操作，显示撤回按钮
			if (logs.size() >= 1 && ObjectUtil.isNull(logs.get(1).get("ACTION"))) {
				e.put("showRevokeButton", true);
			} else {
				e.put("showRevokeButton", false);
			}
			//如果是直接终止流程，流程状态前端显示为流程终止 而非 审批不通过
			if (logs.stream().anyMatch(attr5 -> ObjectUtil.isNotNull(attr5.get("attr5")))) {
				e.put("terminateProcess", true);
			} else {
				e.put("terminateProcess", false);
			}
		});
        return convertPage(page, wfPage);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#countUnsponsorTask(java.lang.String,
     *      java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Map<String, Integer> countUnsponsorTask(String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return taskInfoManager.countUnsponsorTask(tenantId, userId, searchDTO.getBusinessCode(),
            searchDTO.getStartTime(), searchDTO.getEndTime());
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getUnsponsorTaskList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String,
     *      com.cescloud.saas.archive.api.modular.workflow.dto.WorkflowSearchDTO)
     */
    @Override
    public Page<?> getUnsponsorTaskList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        final com.cesgroup.core.page.Page wfPage = taskInfoManager.findUnsponsorTaskList(tenantId, userId,
            (int) page.getCurrent(), (int) page.getSize(), searchDTO.getStatus(), searchDTO.getBusinessCode(),
            searchDTO.getStartTime(),
            searchDTO.getEndTime());
        return convertPage(page, wfPage);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#countApproveTask(java.lang.String,
     *      java.lang.String, WorkflowSearchDTO)
     */
    @Override
    public Map<String, Integer> countApproveTask(String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return taskInfoManager.countApproveTask(tenantId, userId, searchDTO.getBusinessCode(),
            searchDTO.getStartTime(), searchDTO.getEndTime());
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getApproveTaskList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String, WorkflowSearchDTO)
     */
    @Override
    public Page<?> getApproveTaskList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        final com.cesgroup.core.page.Page wfPage = taskInfoManager.findApproveTaskList(tenantId, userId,
            (int) page.getCurrent(), (int) page.getSize(), searchDTO.getStatus(), searchDTO.getBusinessCode(),
            searchDTO.getStartTime(),
            searchDTO.getEndTime());

		//complete-> 我审批的-已审批, total-> 我审批的-全部
		if (WorkflowConstants.HumanTaskConstants.STATUS_COMPLETE.equals(searchDTO.getStatus())
				|| "total".equals(searchDTO.getStatus())) {
			((List<Map<String, Object>>) wfPage.getResult()).forEach(e -> {
				String processInstanceId = e.get("process_instance_id").toString();
				Boolean processFinished = remoteWorkflowApiService.isProcessFinished(processInstanceId).getData();
				e.put("isProcessFinished", processFinished);
				//如果流程已完成，还要将总的流程结果显示出来，之前 我审批的->已审批 显示的本人审批结果
				if (processFinished) {
					//用这个流程的结果，覆盖掉自己的审批结果
					Map<String, Object> oneProcessResult = getOneProcessResult(processInstanceId);
					e.put("status", oneProcessResult.get("status").toString());
				}
			});
		}
        return convertPage(page, wfPage);
    }

	@Override
	public List<Map<String, Object>> getApproveTaskList(String tenantId, String processInstanceId) {
		final List<Map<String, Object>> list = taskInfoManager.findApproveTaskList(tenantId,
				WorkflowConstants.HumanTaskConstants.STATUS_ACTIVE,processInstanceId);
		return list;
	}

	/**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#countCopyTask(java.lang.String,
     *      java.lang.String, WorkflowSearchDTO)
     */
    @Override
    public Map<String, Integer> countCopyTask(String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        return taskInfoManager.countCopyTask(tenantId, userId, searchDTO.getBusinessCode(), searchDTO.getStartTime(),
            searchDTO.getEndTime());
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getCopyTaskList(com.cescloud.saas.archive.common.search.Page,
     *      java.lang.String, java.lang.String, WorkflowSearchDTO)
     */
    @Override
    public Page<?> getCopyTaskList(Page<?> page, String tenantId, String userId, WorkflowSearchDTO searchDTO) {
        final com.cesgroup.core.page.Page wfPage = taskInfoManager.findCopyTaskList(tenantId, userId,
            (int) page.getCurrent(), (int) page.getSize(), searchDTO.getStatus(), searchDTO.getBusinessCode(),
            searchDTO.getStartTime(),
            searchDTO.getEndTime());
        return convertPage(page, wfPage);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getGraphImageByProcessInstanceId(java.lang.String)
     */
    @Override
    public String getGraphImageByProcessInstanceId(String processInstanceId) {
        try {
            return workflowAPI.getGraphImageByProcessInstanceId(processInstanceId);
        } catch (final IOException e) {
            log.error("获取流程跟踪图出错", e);
            throw new ArchiveRuntimeException("获取流程跟踪图出错", e);
        }
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getTaskLogListByProcessInstanceId(java.lang.String)
     */
    @Override
    public List<?> getTaskLogListByProcessInstanceId(String processInstanceId) {
    	return humanTaskConnector.queryHistoryTaskWithCommentByInstanceId(processInstanceId, "T_WF_TASK_COMMENT", "TASK_INFO_ID", new String[]{"SUB_COMMENT"});
        //return taskInfoManager.findTaskLogListByProcessInstanceId(processInstanceId);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService#getRollbackActivityListByProcessInstanceIdAndActivityId(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public List<?> getRollbackActivityListByProcessInstanceIdAndActivityId(String processInstanceId,
        String activityId) {
        return workflowAPI.getRollbackActivityListByProcessInstanceIdAndActivityId(processInstanceId, activityId);
    }

    @Override
    public Boolean clearWorkflowTenantInfo(Long tenantId) {
        final List<String> sqlList = CollUtil.newArrayList();
        final List<SysUser> usersByTenantId = remoteUserService.getUsersByTenantId(tenantId).getData();
        if (CollUtil.isNotEmpty(usersByTenantId)) {
            final StringBuilder sql = new StringBuilder();
            sql.append(" delete FROM t_wf_workflow_log where user_id in ( ");
            final String collect = usersByTenantId.stream().map(sysUser -> String.valueOf(sysUser.getUserId()))
                .collect(Collectors.joining(" , "));
            sql.append(collect).append(" ) ");
            sqlList.add(sql.toString());
        }
        sqlList.add(String.format(" delete FROM t_wf_bpm_category where tenant_id = %s", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_ge_bytearray where deployment_id_ in (select id_ FROM t_wf_act_re_deployment where tenant_id_ = %s)", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_hi_actinst where tenant_id_ = %s ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_hi_comment where task_id_ in (SELECT id_ FROM t_wf_act_hi_taskinst where tenant_id_ = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_hi_identitylink where proc_inst_id_ in (SELECT id_ FROM t_wf_act_hi_procinst where tenant_id_ = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_hi_varinst where proc_inst_id_ in (SELECT id_ FROM t_wf_act_hi_procinst where tenant_id_ = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_ru_identitylink where proc_inst_id_ in (SELECT id_ FROM t_wf_act_hi_procinst where tenant_id_ = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_ru_identitylink where proc_inst_id_ in (SELECT id_ FROM t_wf_act_hi_procinst where tenant_id_ = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_ru_identitylink where task_id_ in (SELECT id_ FROM t_wf_act_ru_task where tenant_id_ = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_ru_variable where proc_inst_id_ in (SELECT id_ FROM t_wf_act_hi_procinst where tenant_id_ = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_task_comment where process_instance_id in (select id_ FROM t_wf_act_hi_procinst where tenant_id_ = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_hi_procinst where tenant_id_ = %s  ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_hi_taskinst where tenant_id_ = %s  ", tenantId));

        sqlList.add(String.format(
            " delete FROM t_wf_bpm_conf_countersign WHERE node_id IN (SELECT id FROM t_wf_bpm_conf_node WHERE conf_base_id IN ( SELECT id FROM t_wf_bpm_conf_base WHERE model_id IN ( SELECT id_ FROM t_wf_act_re_model WHERE tenant_id_ = %s ))) ",
            tenantId));
        sqlList.add(String.format(
            " delete FROM t_wf_bpm_conf_listener WHERE node_id IN (SELECT id FROM t_wf_bpm_conf_node WHERE conf_base_id IN ( SELECT id FROM t_wf_bpm_conf_base WHERE model_id IN ( SELECT id_ FROM t_wf_act_re_model WHERE tenant_id_ = %s ))) ",
            tenantId));
        sqlList.add(String.format(
            " delete FROM t_wf_bpm_conf_user where node_id in (select id FROM t_wf_bpm_conf_node WHERE conf_base_id IN (SELECT id FROM t_wf_bpm_conf_base WHERE model_id IN (SELECT id_ FROM t_wf_act_re_model WHERE tenant_id_= %s ))) ",
            tenantId));
        sqlList.add(String.format(
            " delete FROM t_wf_bpm_conf_operation where node_id in (select id FROM t_wf_bpm_conf_node WHERE conf_base_id IN (SELECT id FROM t_wf_bpm_conf_base WHERE model_id IN (SELECT id_ FROM t_wf_act_re_model WHERE tenant_id_= %s ))) ",
            tenantId));
        sqlList.add(String.format(
            " delete FROM t_wf_bpm_conf_node WHERE conf_base_id IN (SELECT id FROM t_wf_bpm_conf_base WHERE model_id IN (SELECT id_ FROM t_wf_act_re_model WHERE tenant_id_= %s )) ",
            tenantId));
        sqlList.add(String.format(" delete FROM t_wf_bpm_conf_base where model_id in (SELECT id_ FROM t_wf_act_re_model where tenant_id_ = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_re_model where tenant_id_ = %s  ", tenantId));
	    sqlList.add(String.format(" delete FROM t_wf_act_re_deployment where tenant_id_ = %s  ", tenantId));
	    sqlList.add(String.format(" delete FROM t_wf_act_ru_task where tenant_id_ = %s  ", tenantId));
	    sqlList.add(String.format(" delete FROM t_wf_act_ru_execution where tenant_id_ = %s  ", tenantId));
        sqlList.add(String.format(
            " delete FROM t_wf_task_def_user where base_id in (select id FROM t_wf_task_def_base where process_definition_id in (select id_ FROM t_wf_act_re_procdef where tenant_id_ = %s )) ",
            tenantId));
        sqlList.add(String.format(
            " delete FROM t_wf_task_def_operation where base_id in (select id FROM t_wf_task_def_base where process_definition_id in (select id_ FROM t_wf_act_re_procdef where tenant_id_ = %s )) ",
            tenantId));
        sqlList.add(String.format(
            " delete FROM t_wf_task_def_base where process_definition_id in (select id_ FROM t_wf_act_re_procdef where tenant_id_ = %s ) ",
            tenantId));
        sqlList.add(String.format(" delete FROM t_wf_act_re_procdef where tenant_id_ = %s  ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_bpm_model where tenant_id = %s   ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_bpm_model_purview where tenant_id = %s  ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_business_metadata where tenant_id = %s ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_business_model where tenant_id = %s ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_task_participant where task_id in (select id FROM t_wf_task_info where tenant_id = %s ) ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_task_info where tenant_id = %s ", tenantId));
        sqlList.add(String.format(" delete FROM t_wf_task_info_run where tenant_id = %s ", tenantId));
        final String[] strings = new String[sqlList.size()];
        sqlList.toArray(strings);
        jdbcTemplate.batchUpdate(strings);
        return true;
    }

	@Override
	public void updateParallelStatus(String processInstanceId, String id) {
		workflowMapper.updateParallelStatus(processInstanceId, id, SecurityUtils.getUser().getTenantId());
	}

	@Override
	public Map<String, Object> getOneProcessResult(String processInstanceId) {
		return taskInfoManager.getOneProcessResult(processInstanceId);
	}

	@Override
	public boolean getProcessWasUsed(String processDefinitionId) {
		List<Map<String, Object>> list = taskInfoManager.findList("SELECT id FROM T_WF_TASK_INFO WHERE process_definition_id=?0", CollUtil.newArrayList(processDefinitionId));
		return CollUtil.isNotEmpty(list);
	}

    public String getProcessById(String tenantId, String businessCode) {
        final List<BpmModelEntity> bpmModelList = bpmModelManager.findByBusinessCode(tenantId, businessCode);
        return bpmModelList.get(0).getName();
    }
}
