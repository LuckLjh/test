package com.cesgroup.bpm.cmd;

import com.cesgroup.bpm.graph.ActivitiGraphBuilder;
import com.cesgroup.bpm.graph.Graph;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * 查找对应的流程图
 * 
 * @author 国栋
 *
 */
public class FindGraphCmd implements Command<Graph> {

    private String processDefinitionId;

    public FindGraphCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public Graph execute(CommandContext commandContext) {
        return new ActivitiGraphBuilder(processDefinitionId).build();
    }
}
