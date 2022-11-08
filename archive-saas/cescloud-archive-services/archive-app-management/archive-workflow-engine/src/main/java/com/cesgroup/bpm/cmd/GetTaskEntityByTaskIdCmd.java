/**
 * <p>Copyright:Copyright(c) 2018</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.bpm.cmd</p>
 * <p>文件名:GetTaskEntityByTaskIdCmd.java</p>
 * <p>创建时间:2018-02-02 17:55</p>
 * <p>作者:chen.liang1</p>
 */

package com.cesgroup.bpm.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**
 * 根据taskId获取taskEntity对象
 * @author chen.liang1
 * @version 1.0.0 2018-02-02
 */
public class GetTaskEntityByTaskIdCmd implements Command<TaskEntity> {
    
    private String taskId;
    
    public GetTaskEntityByTaskIdCmd(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public TaskEntity execute(CommandContext commandContext) {
        return commandContext.getTaskEntityManager().findTaskById(taskId);
    }

}
