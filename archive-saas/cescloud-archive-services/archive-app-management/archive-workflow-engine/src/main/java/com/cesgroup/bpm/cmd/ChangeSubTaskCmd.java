package com.cesgroup.bpm.cmd;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

import java.util.Date;

/**
 * 改变子任务命令
 * 
 * @author 国栋
 *
 */
public class ChangeSubTaskCmd implements Command<Object> {

    private String taskId;

    private String userId;

    public ChangeSubTaskCmd(String taskId, String userId) {
        this.taskId = taskId;
        this.userId = userId;
    }

    @Override
    public Object execute(CommandContext commandContext) {
        TaskEntity parentTask = commandContext.getTaskEntityManager().findTaskById(taskId);

        this.createSubTask(parentTask, parentTask.getAssignee());
        this.createSubTask(parentTask, userId);
        parentTask.setAssigneeWithoutCascade(null);

        return null;
    }

    /**
     * 创建子任务
     * 
     * @param parentTask
     *            父任务
     * @param assignee
     *            办理人
     */
    public void createSubTask(TaskEntity parentTask, String assignee) {
        TaskEntity task = TaskEntity.create(new Date());
        task.setProcessDefinitionId(parentTask.getProcessDefinitionId());
        // task.setId(historicTaskInstanceEntity.getId());
        task.setAssigneeWithoutCascade(assignee);
        task.setParentTaskIdWithoutCascade(parentTask.getId());
        task.setNameWithoutCascade(parentTask.getName());
        task.setTaskDefinitionKey(parentTask.getTaskDefinitionKey());
        task.setExecutionId(parentTask.getExecutionId());
        task.setPriority(parentTask.getPriority());
        task.setProcessInstanceId(parentTask.getProcessInstanceId());
        task.setDescriptionWithoutCascade(parentTask.getDescription());
        task.setCategory("subtask");

        Context.getCommandContext().getTaskEntityManager().insert(task);
    }
}
