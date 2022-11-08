package com.cesgroup.bpm.cmd;

import com.cesgroup.bpm.util.CustomProcessDiagramGenerator;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

import java.io.InputStream;

/**
 * 历史流程实例图
 * 
 * @author 国栋
 *
 */
public class HistoryProcessInstanceDiagramCmd implements Command<InputStream> {

    protected String historyProcessInstanceId;

    public HistoryProcessInstanceDiagramCmd(String historyProcessInstanceId) {
        this.historyProcessInstanceId = historyProcessInstanceId;
    }

    @Override
    public InputStream execute(CommandContext commandContext) {
        try {
            CustomProcessDiagramGenerator customProcessDiagramGenerator = 
                new CustomProcessDiagramGenerator();

            return customProcessDiagramGenerator.generateDiagram(historyProcessInstanceId);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
