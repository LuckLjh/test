/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.bpm.listener</p>
 * <p>文件名:ProcessEndEventListener.java</p>
 * <p>创建时间:2019年12月16日 下午2:04:40</p>
 * <p>作者:qiucs</p>
 */

package com.cesgroup.bpm.listener;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import com.cesgroup.api.process.ProcessConnector;
import com.cesgroup.api.user.UserConnector;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.TaskInfo;
import com.cesgroup.humantask.persistence.domain.TaskInfoHis;
import com.cesgroup.humantask.persistence.manager.TaskInfoManager;
import com.google.common.collect.Sets;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月16日
 */
public class ProcessCompletedEventListener implements ActivitiEventListener {

    /**
     *
     * @see org.activiti.engine.delegate.event.ActivitiEventListener#onEvent(org.activiti.engine.delegate.event.ActivitiEvent)
     */
    @Override
    public void onEvent(ActivitiEvent event) {
        if (ActivitiEventType.PROCESS_COMPLETED.equals(event.getType())) {
            final ActivitiEntityEventImpl activitiEntityEventImpl = (ActivitiEntityEventImpl) event;
            saveEndActivityTaskInfo(activitiEntityEventImpl);
        }
    }

    /**
     *
     * @see org.activiti.engine.delegate.event.ActivitiEventListener#isFailOnException()
     */
    @Override
    public boolean isFailOnException() {
        return false;
    }

    private String getEndActivityId(final ExecutionEntity entity) {

        final ActivityImpl executionActivity = entity.getActivity();

        final Set<String> activityIdSet = Sets.newHashSet(entity.getActivityId());

        return getEndActivityId(executionActivity, activityIdSet);
    }

    private String getEndActivityId(PvmActivity activity, Set<String> activityIdSet) {
        if (WorkflowConstants.NodeTypeConstants.NODETYPE_ENDEVENT.equals(activity.getProperty("type"))) {
            return activity.getId();
        }
        final List<PvmTransition> outgoingTransitions = activity.getOutgoingTransitions();
        if (null == outgoingTransitions) {
            return null;
        }
        for (final PvmTransition pvmTransition : outgoingTransitions) {
            final PvmActivity destination = pvmTransition.getDestination();
            if (activityIdSet.contains(destination.getId())) {
                continue;
            }
            activityIdSet.add(destination.getId());
            final String endActivityId = getEndActivityId(destination, activityIdSet);
            if (null != endActivityId) {
                return endActivityId;
            }
        }

        return null;
    }

    private void saveEndActivityTaskInfo(final ActivitiEntityEventImpl activitiEntityEvent) {
        final ExecutionEntity entity = (ExecutionEntity) activitiEntityEvent.getEntity();
        if (null != entity.getParent()) {
            // 子流程不自动添加结束节点
            return;
        }
        final String processInstanceId = activitiEntityEvent.getProcessInstanceId();
        final String executionId = activitiEntityEvent.getExecutionId();
        final String endActivityId = getEndActivityId(entity);
        final HistoricProcessInstance historiprocessInstance = activitiEntityEvent.getEngineServices()
            .getHistoryService()
            .createHistoricProcessInstanceQuery().processInstanceId(processInstanceId)
            .singleResult();
        final Object historicaAction = entity.getVariable("_historicaAction");
        final String action = null != historicaAction ? historicaAction.toString()
            : WorkflowConstants.HumanTaskConstants.ACTION_AUTO;
        // 新建TaskInfo实体类，并设置开始节点相关参数，保存
        final TaskInfo taskInfo = new TaskInfoHis();
        taskInfo.setBusinessKey(entity.getBusinessKey());
        taskInfo.setCode(endActivityId);
        taskInfo.setName("结束");
        taskInfo.setTenantId(entity.getTenantId());
        if (WorkflowConstants.HumanTaskConstants.ACTION_TERMINATE.equals(action)) {
            taskInfo.setStatus(WorkflowConstants.HumanTaskConstants.STATUS_TERMINATE);
            taskInfo.setComment("终止");
        } else {
            taskInfo.setStatus(WorkflowConstants.HumanTaskConstants.STATUS_COMPLETE);
            taskInfo.setComment("结束");
        }
        taskInfo.setCompleteStatus(WorkflowConstants.HumanTaskConstants.STATUS_COMPLETE);
        taskInfo.setPresentationSubject(historiprocessInstance.getName());
        final Date now = new Date(System.currentTimeMillis() + 1000);// 延后1秒保证时间上排序
        taskInfo.setCreateTime(now);
        taskInfo.setCompleteTime(now);
        taskInfo.setProcessStarter(historiprocessInstance.getStartUserId());
        final UserDTO starter = ApplicationContextHelper.getBean(UserConnector.class)
            .findById(historiprocessInstance.getStartUserId());
        if (null != starter) {
            taskInfo.setProcessStarterName(starter.getDisplayName());
        }
        taskInfo.setProcessDefinitionId(entity.getProcessDefinitionId());
        taskInfo.setProcessInstanceId(processInstanceId);
        taskInfo.setCatalog(WorkflowConstants.HumanTaskConstants.CATALOG_END);
        taskInfo.setIsCountersign(WorkflowConstants.NOT);
        final ProcessConnector processConnector = ApplicationContextHelper
            .getBean(ProcessConnector.class);
        final String processDefinitionName = processConnector.queryProcessNameByDefinitionId(
            entity.getProcessDefinitionId(), entity.getTenantId());
        taskInfo.setAttr1(processDefinitionName);
        taskInfo.setSuspendStatus(WorkflowConstants.ProcessConstants.DEFAULT_ACTIVE);
        taskInfo.setAttr4(WorkflowConstants.NOT);
        taskInfo.setAction(action);
        taskInfo.setExecutionId(executionId);
        ApplicationContextHelper.getBean(TaskInfoManager.class).saveOne(taskInfo);
    }

}
