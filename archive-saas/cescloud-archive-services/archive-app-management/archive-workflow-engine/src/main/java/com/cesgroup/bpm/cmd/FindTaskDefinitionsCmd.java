package com.cesgroup.bpm.cmd;

import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.task.TaskDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * 查找任务定义
 * 
 * @author 国栋
 *
 */
public class FindTaskDefinitionsCmd implements Command<List<TaskDefinition>> {

    protected String processDefinitionId;

    public FindTaskDefinitionsCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public List<TaskDefinition> execute(CommandContext commandContext) {
        ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
            processDefinitionId).execute(commandContext);

        List<TaskDefinition> taskDefinitions = new ArrayList<TaskDefinition>();
        taskDefinitions.addAll(processDefinitionEntity.getTaskDefinitions().values());

        return taskDefinitions;
    }
}
