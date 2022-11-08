package com.cesgroup.bpm.cmd;

import com.cesgroup.core.util.WorkflowConstants;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * 查找前置节点命令
 * 
 * @author 国栋
 *
 */
public class FindPreviousActivitiesCmd implements Command<List<PvmActivity>> {

    private String processDefinitionId;

    private String activityId;

    public FindPreviousActivitiesCmd(String processDefinitionId, String activityId) {
        this.processDefinitionId = processDefinitionId;
        this.activityId = activityId;
    }

    @Override
    public List<PvmActivity> execute(CommandContext commandContext) {
        ProcessDefinitionEntity processDefinitionEntity = Context.getProcessEngineConfiguration()
            .getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);

        if (processDefinitionEntity == null) {
            throw new IllegalArgumentException("无法找到流程定义： " + processDefinitionId);
        }

        ActivityImpl activity = processDefinitionEntity.findActivity(activityId);

        return this.getPreviousActivities(activity);
    }

    /**
     * 获取当前节点之前的节点信息
     * 
     * @param pvmActivity
     *            当前节点信息
     * @return list
     */
    public List<PvmActivity> getPreviousActivities(PvmActivity pvmActivity) {
        List<PvmActivity> pvmActivities = new ArrayList<PvmActivity>();

        for (PvmTransition pvmTransition : pvmActivity.getIncomingTransitions()) {
            PvmActivity targetActivity = pvmTransition.getDestination();

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(targetActivity.getProperty("type"))) {
                pvmActivities.add(targetActivity);
            } else {
                pvmActivities.addAll(this.getPreviousActivities(targetActivity));
            }
        }

        return pvmActivities;
    }
}
