package com.cesgroup.bpm.cmd;

import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.spi.process.FirstTaskForm;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.form.DefaultFormHandler;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 查找首节点表单
 * 
 * @author 国栋
 *
 */
public class FindFirstTaskFormCmd implements Command<FirstTaskForm> {

    private static Logger logger = LoggerFactory.getLogger(FindFirstTaskFormCmd.class);

    private String processDefinitionId;

    public FindFirstTaskFormCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public FirstTaskForm execute(CommandContext commandContext) {
        ProcessDefinitionEntity processDefinitionEntity = Context.getProcessEngineConfiguration()
            .getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);

        if (processDefinitionEntity == null) {
            throw new IllegalArgumentException("无法找到流程定义： " + processDefinitionId);
        }

        if (processDefinitionEntity.hasStartFormKey()) {
            return this.findStartEventForm(processDefinitionEntity);
        }

        ActivityImpl startActivity = processDefinitionEntity.getInitial();

        if (startActivity.getOutgoingTransitions().size() != 1) {
            throw new IllegalStateException(
                "开始节点迁出险不能多于1条，当前为： " + startActivity.getOutgoingTransitions().size());
        }

        PvmTransition pvmTransition = startActivity.getOutgoingTransitions().get(0);
        PvmActivity targetActivity = pvmTransition.getDestination();

        if (!WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
            .equals(targetActivity.getProperty("type"))) {
            logger.info("第一个节点不是人工节点，跳过");

            return new FirstTaskForm();
        }

        FirstTaskForm firstTaskForm = new FirstTaskForm();
        firstTaskForm.setProcessDefinitionId(processDefinitionId);
        firstTaskForm.setExists(true);
        firstTaskForm.setTaskForm(true);

        String taskDefinitionKey = targetActivity.getId();
        logger.debug("activityId : {}", targetActivity.getId());
        firstTaskForm.setActivityId(taskDefinitionKey);

        TaskDefinition taskDefinition = processDefinitionEntity.getTaskDefinitions()
            .get(taskDefinitionKey);

        Expression expression = taskDefinition.getAssigneeExpression();

        if (expression != null) {
            String expressionText = expression.getExpressionText();
            logger.debug("{}", expressionText);
            logger.debug("{}", startActivity.getProperties());
            logger.debug("{}", processDefinitionEntity.getProperties());
            firstTaskForm.setAssignee(expressionText);
        } else {
            logger.info("无法找到表达式： {}, {}", processDefinitionId, taskDefinitionKey);
        }

        String initiatorVariableName = (String) processDefinitionEntity
            .getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME);
        firstTaskForm.setInitiatorName(initiatorVariableName);

        DefaultFormHandler formHandler = (DefaultFormHandler) taskDefinition.getTaskFormHandler();

        if (formHandler.getFormKey() != null) {
            String formKey = formHandler.getFormKey().getExpressionText();
            firstTaskForm.setFormKey(formKey);
        } else {
            logger.info("无法找到formKey： {}, {}", processDefinitionId, taskDefinitionKey);
        }

        return firstTaskForm;
    }

    /**
     * 获取开始表单
     * 
     * @param processDefinitionEntity
     *            流程定义实体对象
     * @return 开始表单
     */
    public FirstTaskForm findStartEventForm(ProcessDefinitionEntity processDefinitionEntity) {
        FirstTaskForm firstTaskForm = new FirstTaskForm();
        firstTaskForm.setExists(true);
        firstTaskForm.setProcessDefinitionId(processDefinitionId);
        firstTaskForm.setTaskForm(false);

        DefaultFormHandler formHandler = (DefaultFormHandler) processDefinitionEntity
            .getStartFormHandler();

        if (formHandler.getFormKey() != null) {
            String formKey = formHandler.getFormKey().getExpressionText();
            firstTaskForm.setFormKey(formKey);
            firstTaskForm.setActivityId(processDefinitionEntity.getInitial().getId());
        }

        return firstTaskForm;
    }
}
