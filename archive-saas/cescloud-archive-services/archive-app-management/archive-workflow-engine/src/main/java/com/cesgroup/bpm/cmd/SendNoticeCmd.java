package com.cesgroup.bpm.cmd;

import com.cesgroup.bpm.notice.TimeoutNotice;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**
 * 发送通知命令
 * 
 * @author 国栋
 *
 */
public class SendNoticeCmd implements Command<Void> {

    private String taskId;

    public SendNoticeCmd(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        TaskEntity delegateTask = commandContext.getTaskEntityManager().findTaskById(taskId);
        new TimeoutNotice().process(delegateTask);

        return null;
    }
}
