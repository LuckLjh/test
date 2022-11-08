package com.cesgroup.bpm.cmd;

import com.cesgroup.core.util.WorkflowConstants;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 提交完成到指定节点命令（改） 不使用jdbcTemplete
 * 
 * @author chen.liang1
 *
 */
public class CompleteToActivityCmd2 implements Command<Boolean> {

    /** 任务ID */
    private String taskId;

    /** 跳转的节点ID集合 */
    private List<String> activityIds = new ArrayList<String>();

    /** 流程相关参数 */
    private Map<String, Object> variables;

    /** 是否是自由跳，影响任务的删除原因 */
    private boolean isJump;

    public CompleteToActivityCmd2(String taskId, List<String> activityIds) {
        this(taskId, activityIds, null, true);
    }

    public CompleteToActivityCmd2(String taskId, List<String> activityIds, boolean isJump) {
        this(taskId, activityIds, null, isJump);
    }

    public CompleteToActivityCmd2(String taskId, List<String> activityIds,
        Map<String, Object> variables) {
        this(taskId, activityIds, variables, true);
    }

    /** constructor */
    public CompleteToActivityCmd2(String taskId, List<String> activityIds,
        Map<String, Object> variables, boolean isJump) {
        this.activityIds = activityIds;
        this.taskId = taskId;
        this.variables = variables;
        this.isJump = isJump;
    }

    @Override
    public Boolean execute(CommandContext commandContext) {
        // 获取当前的任务
        TaskEntity taskEntity = Context.getCommandContext().getTaskEntityManager()
            .findTaskById(taskId);
        // 根据当前的任务 找到当前的流程定义
        ProcessDefinitionImpl processDefinition = taskEntity.getExecution().getProcessDefinition();
        // 根据任务的processDefinitionKey去获取当前活动的点
        ActivityImpl activitySrc = processDefinition
            .findActivity(taskEntity.getTaskDefinitionKey());
        List<PvmTransition> oriPvmTransitionList = null;
        List<PvmTransition> newPvmTransitionList = null;
        try {
            // 获取当前任务下一个流向
            oriPvmTransitionList = clearTransition(activitySrc);
            // 接受所有新的流向
            newPvmTransitionList = new ArrayList<PvmTransition>();
            for (String activityId : activityIds) {
                ActivityImpl activityTarget = processDefinition.findActivity(activityId);
                // 为当前节点动态创建新的流出项
                TransitionImpl transitionImpl = activitySrc.createOutgoingTransition();
                // 为当前活动节点新的流出目标指定流程目标
                transitionImpl.setDestination(activityTarget);
                newPvmTransitionList.add(transitionImpl);
            }
            // 完成当前的任务
            String reason = WorkflowConstants.HumanTaskConstants.DELETE_REASON_COMPLETED;
            if (isJump) {
                reason = WorkflowConstants.HumanTaskConstants.DELETE_REASON_FREE_JUMP;
            }
            new CompleteTaskWithCommentCmd(taskId, variables, reason).execute(commandContext);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            if  (newPvmTransitionList != null) {
                // 删除现有的自定义的流向
                for (PvmTransition pvmTransition : newPvmTransitionList) {
                    pvmTransition.getDestination().getIncomingTransitions().remove(pvmTransition);
                }
                // 还原最初的节点流向
                restoreTransition(activitySrc, oriPvmTransitionList);
            }
            Context.getProcessEngineConfiguration().getProcessDefinitionCache().remove(
                    taskEntity.getProcessDefinitionId());
        }
        return true;
    }

    /**
     * 清空指定活动节点流向
     * 
     * @param activityImpl
     *            活动节点
     * @return 节点流向集合
     */
    private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {
        // 存储当前节点所有流向临时变量
        List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
        // 获取当前节点所有流向，存储到临时变量，然后清空
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitionList) {
            oriPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();

        return oriPvmTransitionList;
    }

    /**
     * 还原指定活动节点流向
     * 
     * @param activityImpl
     *            活动节点
     * @param oriPvmTransitionList
     *            原有节点流向集合
     */
    private void restoreTransition(ActivityImpl activityImpl,
        List<PvmTransition> oriPvmTransitionList) {
        // 清空现有流向
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        pvmTransitionList.clear();
        // 还原以前流向
        pvmTransitionList.addAll(oriPvmTransitionList);
    }
}
