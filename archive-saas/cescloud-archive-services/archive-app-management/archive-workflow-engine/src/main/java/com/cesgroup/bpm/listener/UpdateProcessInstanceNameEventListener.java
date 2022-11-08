package com.cesgroup.bpm.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.BaseEntityEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.cesgroup.api.user.UserConnector;
import com.cesgroup.core.util.StringUtils;

/**
 * 更新流程实例名字事件监听器
 * <p/>
 * {流程标题:title}：{发起人:startUser}在{发起时间:startTime}发起流程
 *
 * @author 国栋
 *
 */
public class UpdateProcessInstanceNameEventListener extends BaseEntityEventListener {

    @Autowired
    private UserConnector userConnector;

    @Autowired
    @Lazy
    private ProcessEngine processEngine;

    @Override
    protected void onInitialized(ActivitiEvent event) {
        if (!(event instanceof ActivitiEntityEventImpl)) {
            return;
        }

        final ActivitiEntityEventImpl activitiEntityEventImpl = (ActivitiEntityEventImpl) event;
        final Object entity = activitiEntityEventImpl.getEntity();

        if (!(entity instanceof ExecutionEntity)) {
            return;
        }

        final ExecutionEntity executionEntity = (ExecutionEntity) entity;

        if (!executionEntity.isProcessInstanceType()) {
            return;
        }

        final String processInstanceId = executionEntity.getId();
        final String processDefinitionId = executionEntity.getProcessDefinitionId();
        final CommandContext commandContext = Context.getCommandContext();
        final ProcessDefinitionEntity processDefinition = new GetDeploymentProcessDefinitionCmd(
            processDefinitionId).execute(commandContext);

        // {流程标题:title}: {发起人:startUser}在{发起时间:startTime}发起流程
        final String processDefinitionName = processDefinition.getName();
        final String userId = Authentication.getAuthenticatedUserId();
        String userName = "";
        if (StringUtils.isNotBlank(userId)) {
            userName = userConnector.findById(userId).getDisplayName();
        }
        String processInstanceName = null;
        final String processName = processEngine.getRuntimeService().getVariable(processInstanceId,
            "PROCESS_NAME", String.class);
        if (StringUtils.isNotBlank(processName)) {
            processInstanceName = processName;
        } else {
            //如果是子流程时，则userId为空
            if (userId == null) {
                final String initiator = (String) processEngine.getRuntimeService()
                    .getVariable(processInstanceId, "initiator");
                processInstanceName = processDefinitionName + "：" + initiator;
            } else {
                processInstanceName = processDefinitionName + "：" + userName;
            }
            processInstanceName += "在" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "发起流程";
        }

        // runtime
        executionEntity.setName(processInstanceName);

        // history
        final HistoricProcessInstanceEntity historicProcessInstanceEntity = commandContext
            .getHistoricProcessInstanceEntityManager()
            .findHistoricProcessInstance(processInstanceId);
        historicProcessInstanceEntity.setName(processInstanceName);
    }

}
