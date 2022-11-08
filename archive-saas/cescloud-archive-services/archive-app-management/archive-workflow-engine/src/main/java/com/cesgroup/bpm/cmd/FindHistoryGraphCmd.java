package com.cesgroup.bpm.cmd;

import com.cesgroup.bpm.graph.ActivitiHistoryGraphBuilder;
import com.cesgroup.bpm.graph.Graph;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * 查找历史流程图
 * 
 * @author 国栋
 *
 */
public class FindHistoryGraphCmd implements Command<Graph> {

    private String processInstanceId;

    public FindHistoryGraphCmd(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public Graph execute(CommandContext commandContext) {
        return new ActivitiHistoryGraphBuilder(processInstanceId).build();
    }
}
