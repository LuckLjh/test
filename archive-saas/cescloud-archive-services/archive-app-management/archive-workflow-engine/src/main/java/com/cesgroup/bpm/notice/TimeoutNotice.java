package com.cesgroup.bpm.notice;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cesgroup.api.notification.NotificationConnector;
import com.cesgroup.api.notification.NotificationDTO;
import com.cesgroup.api.user.UserConnector;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.bpm.persistence.domain.BpmConfNotice;
import com.cesgroup.bpm.persistence.manager.BpmConfNoticeManager;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;

/**
 * 超时通知
 *
 * @author 国栋
 *
 */
public class TimeoutNotice {

    private static Logger logger = LoggerFactory.getLogger(TimeoutNotice.class);

    /** TYPE_ARRIVAL */
    public static final int TYPE_ARRIVAL = 0;

    /** TYPE_COMPLETE */
    public static final int TYPE_COMPLETE = 1;

    /** TYPE_TIMEOUT */
    public static final int TYPE_TIMEOUT = 2;

    /**
     * 处理任务的超时事件
     */
    @SuppressWarnings("unchecked")
    public void process(DelegateTask delegateTask) {
        final String taskDefinitionKey = delegateTask.getTaskDefinitionKey();
        final String processDefinitionId = delegateTask.getProcessDefinitionId();

        final List<BpmConfNotice> bpmConfNotices = ApplicationContextHelper
            .getBean(BpmConfNoticeManager.class).find(
                "from BpmConfNotice where bpmConfNode.bpmConfBase.processDefinitionId=?0 "
                    + " and bpmConfNode.code=?1",
                processDefinitionId, taskDefinitionKey);

        for (final BpmConfNotice bpmConfNotice : bpmConfNotices) {
            if (TYPE_TIMEOUT == bpmConfNotice.getType()) {
                processTimeout(delegateTask, bpmConfNotice);
            }
        }
    }

    /**
     * 处理超时
     *
     * @param delegateTask
     *            代理的任务对象
     * @param bpmConfNotice
     *            配置提醒信息
     */
    public void processTimeout(DelegateTask delegateTask, BpmConfNotice bpmConfNotice) {
        try {
            final Date dueDate = delegateTask.getDueDate();
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(dueDate);

            final DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            final Duration duration = datatypeFactory.newDuration("-" + bpmConfNotice.getDueDate());
            duration.addTo(calendar);

            final Date noticeDate = calendar.getTime();
            final Date now = new Date();

            if ((now.getTime() < noticeDate.getTime())
                && ((noticeDate.getTime() - now.getTime()) < (60 * 1000))) {
                final UserConnector userConnector = ApplicationContextHelper.getBean(UserConnector.class);
                final TaskEntity taskEntity = new TaskEntity();
                taskEntity.setId(delegateTask.getId());
                taskEntity.setName(delegateTask.getName());
                taskEntity.setAssigneeWithoutCascade(
                    userConnector.findById(delegateTask.getAssignee()).getDisplayName());
                taskEntity.setVariableLocal(WorkflowConstants.HumanTaskConstants.TYPE_INITIATOR,
                    getInitiator(userConnector, delegateTask));
                //
                final Map<String, Object> data = new HashMap<String, Object>();
                data.put("task", taskEntity);
                data.put(WorkflowConstants.HumanTaskConstants.TYPE_INITIATOR,
                    this.getInitiator(userConnector, delegateTask));

                final String receiver = bpmConfNotice.getReceiver();

                /*
                 * BpmMailTemplate bpmMailTemplate = bpmConfNotice
                 * .getBpmMailTemplate(); ExpressionManager
                 * expressionManager = Context
                 * .getProcessEngineConfiguration().getExpressionManager();
                 */
                UserDTO userDto = null;

                if ("任务接收人".equals(receiver)) {
                    userDto = userConnector.findById(delegateTask.getAssignee());
                } else if ("流程发起人".equals(receiver)) {
                    userDto = userConnector.findById((String) delegateTask.getVariables()
                        .get(WorkflowConstants.HumanTaskConstants.TYPE_INITIATOR));
                } else {
                    final HistoricProcessInstanceEntity historicProcessInstanceEntity = Context
                        .getCommandContext().getHistoricProcessInstanceEntityManager()
                        .findHistoricProcessInstance(delegateTask.getProcessInstanceId());
                    userDto = userConnector
                        .findById(historicProcessInstanceEntity.getStartUserId());
                }

                /*
                 * String subject = expressionManager
                 * .createExpression(bpmMailTemplate.getSubject())
                 * .getValue(taskEntity).toString();
                 * String content = expressionManager
                 * .createExpression(bpmMailTemplate.getContent())
                 * .getValue(taskEntity).toString();
                 * this.sendMail(userDto, subject, content);
                 * this.sendSiteMessage(userDto, subject, content);
                 */
                final NotificationDTO notificationDto = new NotificationDTO();
                notificationDto.setReceiver(userDto.getId());
                notificationDto.setReceiverType("userid");
                notificationDto
                    .setTypes(Arrays.asList(bpmConfNotice.getNotificationType().split(",")));
                notificationDto.setData(data);
                notificationDto.setTemplate(bpmConfNotice.getTemplateCode());
                final NotificationConnector notificationConnector = ApplicationContextHelper
                    .getBean(NotificationConnector.class);

                notificationConnector.send(notificationDto, delegateTask.getTenantId());
            }
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public String getInitiator(UserConnector userConnector, DelegateTask delegateTask) {
        return userConnector.findById((String) delegateTask.getVariables()
            .get(WorkflowConstants.HumanTaskConstants.TYPE_INITIATOR)).getDisplayName();
    }
}
