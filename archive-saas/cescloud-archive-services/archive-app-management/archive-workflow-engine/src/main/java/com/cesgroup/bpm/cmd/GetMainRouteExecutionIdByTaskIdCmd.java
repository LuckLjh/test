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
 * 会签情况：并行 父->父EXECUTION， 串行 父EXECUTION
 * 普通任务：当前EXECUTION
 * 
 * @author chen.liang1
 * @version 1.0.0 2017-12-11
 */
public class GetMainRouteExecutionIdByTaskIdCmd implements Command<String> {

    private String taskId;

    public GetMainRouteExecutionIdByTaskIdCmd(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String execute(CommandContext commandContext) {
        TaskEntity taskEntity = commandContext.getTaskEntityManager().findTaskById(taskId);
        String executionId = taskEntity.getExecutionId();
        ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
            taskEntity.getProcessDefinitionId()).execute(commandContext);
        ActivityImpl activityImpl = processDefinitionEntity
            .findActivity(taskEntity.getTaskDefinitionKey());
        Object multiInstance = activityImpl.getProperty("multiInstance");
        //并行会签    
        if ("parallel".equals(multiInstance)) {
            executionId = taskEntity.getExecution().getParent().getParentId();
        }
        //串行会签
        if ("sequential".equals(multiInstance)) {
            executionId = taskEntity.getExecution().getParentId();
        }
        return executionId;
    }

}
