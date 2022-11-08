package com.cesgroup.bpm.cmd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cmd.GetBpmnModelCmd;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * 流程定义图
 *
 * @author 国栋
 *
 */
public class ProcessDefinitionDiagramCmd implements Command<InputStream> {

    protected String processDefinitionId;

    public ProcessDefinitionDiagramCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public InputStream execute(CommandContext commandContext) {
        final ProcessDefinitionEntity processDefinition = new GetDeploymentProcessDefinitionCmd(
            processDefinitionId).execute(commandContext);
        final String diagramResourceName = processDefinition.getDiagramResourceName();
        final String deploymentId = processDefinition.getDeploymentId();
        if (deploymentId != null && null != diagramResourceName) {
            final byte[] bytes = commandContext.getResourceEntityManager()
                .findResourceByDeploymentIdAndResourceName(deploymentId, diagramResourceName)
                .getBytes();
            final InputStream inputStream = new ByteArrayInputStream(bytes);

            return inputStream;
        }

        final GetBpmnModelCmd getBpmnModelCmd = new GetBpmnModelCmd(processDefinitionId);
        final BpmnModel bpmnModel = getBpmnModelCmd.execute(commandContext);
        final ProcessEngineConfiguration processEngineConfiguration = Context
            .getProcessEngineConfiguration();
        final InputStream is = processEngineConfiguration.getProcessDiagramGenerator().generateDiagram(bpmnModel, "png",
            processEngineConfiguration.getActivityFontName(),
            processEngineConfiguration.getLabelFontName(), null);
        return is;
    }
}
