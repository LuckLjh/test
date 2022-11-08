/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.bpm.cmd</p>
 * <p>文件名:FindRollbackActivitiesCmd.java</p>
 * <p>创建时间:2020年1月2日 下午1:04:23</p>
 * <p>作者:qiucs</p>
 */

package com.cesgroup.bpm.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import com.cesgroup.api.humantask.RollbackActivityDTO;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.TaskInfoHis;
import com.cesgroup.humantask.persistence.manager.TaskInfoManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 退回节点
 *
 * @author qiucs
 * @version 1.0.0 2020年1月2日
 */
public class FindRollbackActivitiesCmd implements Command<List<RollbackActivityDTO>> {

    private final String processInstanceId;

    private final String activityId;

    public FindRollbackActivitiesCmd(String processInstanceId, String activityId) {
        this.processInstanceId = processInstanceId;
        this.activityId = activityId;
    }

    @Override
    public List<RollbackActivityDTO> execute(CommandContext commandContext) {
        final HistoricProcessInstance processInstance = Context.getProcessEngineConfiguration().getHistoryService()
            .createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            throw new IllegalArgumentException("无法找到流程实例： " + processInstanceId);
        }
        final Map<String, RollbackActivityDTO> rollbackActivityMap = getHistoricActivityMap();

        if (rollbackActivityMap.isEmpty()) {
            throw new IllegalArgumentException("当前流程没有可退回节点： " + processInstanceId);
        }

        final String processDefinitionId = processInstance.getProcessDefinitionId();
        final ProcessDefinitionEntity processDefinitionEntity = Context.getProcessEngineConfiguration()
            .getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);

        if (processDefinitionEntity == null) {
            throw new IllegalArgumentException("无法找到流程定义： " + processDefinitionId);
        }

        final ActivityImpl activity = processDefinitionEntity.findActivity(activityId);

        return this.getPreviousActivities(activity, rollbackActivityMap);
    }

    /**
     * 已提交过的历史节点集合
     *
     * @return
     */
    private Map<String, RollbackActivityDTO> getHistoricActivityMap() {
        final Map<String, RollbackActivityDTO> historicActivityMap = Maps.newHashMap();
        final TaskInfoManager manager = ApplicationContextHelper.getBean(TaskInfoManager.class);
        final List<TaskInfoHis> taskList = manager.findBy("processInstanceId", processInstanceId);

        if (null == taskList || taskList.isEmpty()) {
            return historicActivityMap;
        }

        taskList.forEach(task -> {
            if (!WorkflowConstants.HumanTaskConstants.ACTION_COMPLETE.equals(task.getAction())) {
                return;
            }
            if (!historicActivityMap.containsKey(task.getCode())) {
                historicActivityMap.put(task.getCode(), toRollbackActivityDTO(task));
            } else {
                List<String> assigneeNameList;
                final RollbackActivityDTO rollbackActivityDTO = historicActivityMap.get(task.getCode());
                assigneeNameList = rollbackActivityDTO.getAssigneeNameList();
                if (assigneeNameList.stream().anyMatch(assigneeName -> assigneeName.equals(task.getAssigneeName()))) {
                    return;
                }
                assigneeNameList.add(task.getAssigneeName());
            }
        });

        return historicActivityMap;
    }

    private RollbackActivityDTO toRollbackActivityDTO(TaskInfoHis task) {
        return RollbackActivityDTO.builder().id(task.getCode()).name(task.getName())
            .catalog(task.getCatalog()).assigneeNameList(Lists.newArrayList(task.getAssigneeName())).build();
    }

    /**
     * 获取当前节点之前的节点信息
     *
     * @param pvmActivity
     *            当前节点信息
     * @return list
     */
    private List<RollbackActivityDTO> getPreviousActivities(PvmActivity pvmActivity,
        Map<String, RollbackActivityDTO> rollbackActivityMap) {
        final List<RollbackActivityDTO> pvmActivities = new ArrayList<RollbackActivityDTO>();

        final List<PvmTransition> incomingTransitions = pvmActivity.getIncomingTransitions();

        for (final PvmTransition pvmTransition : incomingTransitions) {
            final PvmActivity targetActivity = pvmTransition.getSource();

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_STARTEVENT.equals(targetActivity.getProperty("type"))) {
                break;
            }

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(targetActivity.getProperty("type"))) {
                if (rollbackActivityMap.keySet().contains(targetActivity.getId())) {
                    pvmActivities.add(rollbackActivityMap.get(targetActivity.getId()));
                } else {
                    continue;
                }
            }
            pvmActivities.addAll(this.getPreviousActivities(targetActivity, rollbackActivityMap));
        }

        return pvmActivities;
    }
}
