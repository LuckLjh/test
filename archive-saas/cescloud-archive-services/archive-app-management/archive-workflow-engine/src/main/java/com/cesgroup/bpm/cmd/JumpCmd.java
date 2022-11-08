package com.cesgroup.bpm.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;

/**
 * 跳转命令
 * 
 * @author 国栋
 *
 */
public class JumpCmd implements Command<Object> {

    private String activityId;

    private String executionId;

    private String jumpOrigin;

    /**
     * 新建一个跳转命令，跳转说明为 jump
     * 
     * @param executionId 执行线程ID
     * @param activityId 节点ID
     */
    public JumpCmd(String executionId, String activityId) {
        this(executionId, activityId, "jump");
    }

    /**
     * 新建一个跳转命令
     * 
     * @param executionId
     *            执行id
     * @param activityId
     *            节点id
     * @param jumpOrigin
     *            跳转说明
     */
    public JumpCmd(String executionId, String activityId, String jumpOrigin) {
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
