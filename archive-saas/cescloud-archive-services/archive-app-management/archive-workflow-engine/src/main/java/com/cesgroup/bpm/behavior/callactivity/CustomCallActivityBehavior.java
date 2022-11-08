
package com.cesgroup.bpm.behavior.callactivity;

import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SubProcessActivityBehavior;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义调用子流程的行为，支持自动异步跳过执行。
 * 
 * @author 王国栋
 *
 */
public class CustomCallActivityBehavior extends CallActivityBehavior
    implements SubProcessActivityBehavior {

    private static final long serialVersionUID = 1L;

    private List<AbstractDataAssociation> dataInputAssociations
        = new ArrayList<AbstractDataAssociation>();

    private Expression processDefinitionExpression;

    public CustomCallActivityBehavior(String processDefinitionKey,
        List<MapExceptionEntry> mapExceptions) {
        super(processDefinitionKey, mapExceptions);
    }

    public CustomCallActivityBehavior(Expression processDefinitionExpression,
        List<MapExceptionEntry> mapExceptions) {
        super(processDefinitionExpression, mapExceptions);
        this.processDefinitionExpression = processDefinitionExpression;
    }

    @Override
    public void addDataInputAssociation(AbstractDataAssociation dataInputAssociation) {
        this.dataInputAssociations.add(dataInputAssociation);
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        String processDefinitonKey = this.processDefinitonKey;
        if (processDefinitionExpression != null) {
            processDefinitonKey = (String) processDefinitionExpression.getValue(execution);
        }

        ProcessDefinitionEntity processDefinition = null;
        if (execution.getTenantId() == null
            || ProcessEngineConfiguration.NO_TENANT_ID.equals(execution.getTenantId())) {
            processDefinition = Context.getProcessEngineConfiguration().getDeploymentManager()
                .findDeployedLatestProcessDefinitionByKey(processDefinitonKey);
        } else {
            processDefinition = Context.getProcessEngineConfiguration().getDeploymentManager()
                .findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitonKey,
                    execution.getTenantId());
        }

        // Do not start a process instance if the process definition is
        // suspended
        if (processDefinition.isSuspended()) {
            throw new ActivitiException(
                "Cannot start process instance. Process definition " + processDefinition.getName()
                    + " (id = " + processDefinition.getId() + ") is suspended");
        }
        Authentication.setAuthenticatedUserId(String.valueOf(execution.getVariable("initiator")));
        PvmProcessInstance subProcessInstance = execution
            .createSubProcessInstance(processDefinition);

        // copy process variables
        for (AbstractDataAssociation dataInputAssociation : dataInputAssociations) {
            Object value = null;
            if (dataInputAssociation.getSourceExpression() != null) {
                value = dataInputAssociation.getSourceExpression().getValue(execution);
            } else {
                value = execution.getVariable(dataInputAssociation.getSource());
            }
            subProcessInstance.setVariable(dataInputAssociation.getTarget(), value);
        }

        try {
            if (execution.getVariable("initiator") != null) {
                ((ExecutionEntity) subProcessInstance).setVariable("initiator",
                    execution.getVariable("initiator"));
            }

            if (execution.getActivity().isAsync()) {
                // 打断与主流程的关联关系
                ((ExecutionEntity) subProcessInstance).setSuperExecution(null);
            }
            subProcessInstance.start();
            if (execution.getActivity().isAsync()) {
                completed(execution);
            }
        } catch (Exception e) {
            if (!ErrorPropagation.mapException(e, execution, mapExceptions, true)) {
                throw e;
            }

        }
    }

}
