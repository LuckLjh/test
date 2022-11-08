/**
 * <p>Copyright:Copyright(c) 2017</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.humantask.support</p>
 * <p>文件名:GetMultiInstanceTaskMainExecutionIdCmd.java</p>
 * <p>创建时间:2017-12-11 16:22</p>
 * <p>作者:chen.liang1</p>
 */

package com.cesgroup.bpm.cmd;

import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

/**
 * 获取流程所属执行线程的ID（线的ExecutionId）
 * 
 * @author chen.liang1
 * @version 1.0.0 2017-12-11
 */
public class GetActivityImplByTaskIdCmd implements Command<ActivityImpl> {

    private String taskId;

    public GetActivityImplByTaskIdCmd(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public ActivityImpl execute(CommandContext commandContext) {
        TaskEntity taskEntity = commandContext.getTaskEntityManager().findTaskById(taskId);
        ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
            taskEntity.getProcessDefinitionId()).execute(commandContext);
        return processDefinitionEntity
            .findActivity(taskEntity.getTaskDefinitionKey());
    }

}
