package com.cesgroup.bpm.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;

/**
 * 跳转到指定节点命令
 * 
 * @author 国栋
 *
 */
public class JumpToActivityCmd implements Command<Object> {

    private String activityId;

    private String executionId;

    private String jumpOrigin;

    /**
     * 跳转到指定节点命令，跳转的说明信息为 jumpToActivity
     * 
     * @param executionId 执行线程ID
     * @param activityId 节点ID
     */
    public JumpToActivityCmd(String executionId, String activityId) {
        this(executionId, activityId, "jumpToActivity");
    }

    /**
     * 跳转到指定节点命令
     * 
     * @param executionId
     *            执行id
     * @param activityId
     *            节点id
     * @param jumpOrigin
     *            跳转原因
     */
    public JumpToActivityCmd(String executionId, String activityId, String jumpOrigin) {
        this.activityId = activityId;
        this.executionId = executionId;
        this.jumpOrigin = jumpOrigin;
    }

    @Override
    public Object execute(CommandContext commandContext) {
        for (TaskEntity taskEntity : commandContext.getTaskEntityManager()
            .findTasksByExecutionId(executionId)) {
            taskEntity.setVariableLocal("跳转原因", jumpOrigin);
            commandContext.getTaskEntityManager().deleteTask(taskEntity, jumpOrigin, false);
        }

        ExecutionEntity executionEntity = commandContext.getExecutionEntityManager()
            .findExecutionById(executionId);
        ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
        ActivityImpl activity = processDefinition.findActivity(activityId);

        executionEntity.executeActivity(activity);

        return null;
    }
}
