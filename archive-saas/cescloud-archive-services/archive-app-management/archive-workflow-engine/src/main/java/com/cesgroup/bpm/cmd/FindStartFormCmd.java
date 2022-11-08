package com.cesgroup.bpm.cmd;

import com.cesgroup.api.form.FormDTO;
import com.cesgroup.core.util.WorkflowConstants;

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
 * 查找发起流程表单的逻辑. <br>
 * 如果startEvent有formKey，直接返回。<br>
 * 如果startEvent后续的第一个节点是userTask，并且userTask的负责人是流程发起人，也返回它的formKey。
 * 
 * @author 国栋
 *
 */
public class FindStartFormCmd implements Command<FormDTO> {

    private static Logger logger = LoggerFactory.getLogger(FindStartFormCmd.class);

    private String processDefinitionId;

    public FindStartFormCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public FormDTO execute(CommandContext commandContext) {
        ProcessDefinitionEntity processDefinitionEntity = Context.getProcessEngineConfiguration()
            .getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);

        if (processDefinitionEntity == null) {
            throw new IllegalArgumentException("无法找到流程定义： " + processDefinitionId);
        }

        FormDTO formDto = new FormDTO();
        formDto.setProcessDefinitionId(processDefinitionId);

        // startEvent存在formKey的情况
        if (processDefinitionEntity.hasStartFormKey()) {
            formDto.setAutoCompleteFirstTask(false);

            DefaultFormHandler formHandler = (DefaultFormHandler) processDefinitionEntity
                .getStartFormHandler();

            if (formHandler.getFormKey() == null) {
                // 这个逻辑很古怪，上面判断了hasStartFormKey应该就避免这里为null的情况
                logger.info("weired start form key is null");
                return formDto;
            }

            String formKey = formHandler.getFormKey().getExpressionText();
            formDto.setCode(formKey);
            formDto.setActivityId(processDefinitionEntity.getInitial().getId());

            return formDto;
        }

        formDto.setAutoCompleteFirstTask(true);

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

            return formDto;
        }

        String taskDefinitionKey = targetActivity.getId();
        logger.debug("节点id : {}", targetActivity.getId());

        TaskDefinition taskDefinition = processDefinitionEntity.getTaskDefinitions()
            .get(taskDefinitionKey);

        Expression expression = taskDefinition.getAssigneeExpression();

        if (expression == null) {
            logger.info("处理人为空，跳过");

            return formDto;
        }

        String expressionText = expression.getExpressionText();
        logger.debug("{}", expressionText);
        logger.debug("{}", startActivity.getProperties());
        logger.debug("{}", processDefinitionEntity.getProperties());

        String initiatorVariableName = (String) processDefinitionEntity
            .getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME);

        if (!("${" + initiatorVariableName + "}").equals(expressionText)) {
            logger.info("办理人 {} 不是 {}，跳过", taskDefinitionKey, "${" + initiatorVariableName + "}");

            return formDto;
        }

        DefaultFormHandler formHandler = (DefaultFormHandler) taskDefinition.getTaskFormHandler();

        if (formHandler.getFormKey() == null) {
            // 满足一切要求，但是xml里没配置formKey，再给一次机会，去上层搜索一遍数据库配置里是不是配置了这个userTask的formKey
            logger.info("无法找到表单关键字 : {}, {}", processDefinitionId, taskDefinitionKey);
            formDto.setActivityId(taskDefinitionKey);

            return formDto;
        }

        String formKey = formHandler.getFormKey().getExpressionText();
        formDto.setCode(formKey);

        formDto.setActivityId(taskDefinitionKey);

        return formDto;
    }
}
