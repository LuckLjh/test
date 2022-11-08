/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.listener.message</p>
 * <p>文件名:TaskApprovalMessageHumanTaskListener.java</p>
 * <p>创建时间:2019年12月25日 下午4:39:05</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.listener.message;

import java.util.List;

import com.cescloud.saas.archive.common.constants.InfoTypeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import com.cescloud.saas.archive.common.message.publisher.InfoPublisher;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.listener.HumanTaskListener;
import com.cesgroup.humantask.persistence.domain.TaskInfo;
import com.cesgroup.humantask.persistence.domain.TaskParticipant;
import com.cesgroup.humantask.persistence.manager.TaskParticipantManager;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 发送任务审批消息提醒
 *
 * @author qiucs
 * @version 1.0.0 2019年12月25日
 */
@Slf4j
public class TaskApprovalMessageHumanTaskListener implements HumanTaskListener {

    @Autowired(required = false)
    private InfoPublisher infoPublisher;

    @Autowired
    private TaskParticipantManager taskParticipantManager;

    /**
     *
     * @see com.cesgroup.humantask.listener.HumanTaskListener#onCreate(com.cesgroup.humantask.persistence.domain.TaskInfo)
     */
    @Override
    public void onCreate(TaskInfo taskInfo) throws Exception {
        try {
            if (WorkflowConstants.HumanTaskConstants.ACTION_WITHDRAW.equals(taskInfo.getAction())) {
                // 撤回任务不发送提醒
                return;
            }
            final String assignee = taskInfo.getAssignee();
            if (WorkflowConstants.HumanTaskConstants.USER_AUTO_COMMIT.equals(assignee)) {
                // 自动提交不发送提醒
                return;
            }
            if (null == infoPublisher) {
                if (log.isInfoEnabled()) {
                    log.info("消息MQ未配置，请检查");
                }
                return;
            }
            final Long tenantId = Long.valueOf(taskInfo.getTenantId());
            final String content = taskInfo.getPresentationSubject();
            final String infoArg = "";
            final String infoType = InfoTypeConstants.INFO;
            if (WorkflowConstants.HumanTaskConstants.CATALOG_SPONSOR.equals(taskInfo.getCatalog())) {
                // 发起节点不发送提醒
                if (WorkflowConstants.HumanTaskConstants.STATUS_ROLLBACK.equals(taskInfo.getStatus())) {
                    if (StrUtil.isNotBlank(assignee)) {
                        infoPublisher.send("流程退回", Long.valueOf(assignee), tenantId, content, infoType, infoArg,InfoTypeConstants.SUCCESS_ICON);
                    }
                }
                return;
            }

            final String title = "流程审批";
            if (StrUtil.isNotBlank(assignee)) {
                final Long userId = Long.valueOf(taskInfo.getAssignee());
                infoPublisher.send(title, userId, tenantId, content, infoType, infoArg,InfoTypeConstants.SUCCESS_ICON);
            } else {
                final List<TaskParticipant> taskParticipantList = taskParticipantManager.findByTaskId(taskInfo.getId());
                if (null == taskParticipantList || taskParticipantList.isEmpty()) {
                    if (log.isWarnEnabled()) {
                        log.warn("任务ID[{}]（{}）未获取到审批人", taskInfo.getId(), content);
                    }
                    return;
                }
                for (final TaskParticipant entity : taskParticipantList) {
                    infoPublisher.send(title, Long.valueOf(entity.getRef()), tenantId, content, infoType, infoArg,InfoTypeConstants.SUCCESS_ICON);
                }
            }
        } catch (final Exception e) {
            log.error("发送流程审批消息失败", e);
        }
    }

    /**
     *
     * @see com.cesgroup.humantask.listener.HumanTaskListener#onComplete(com.cesgroup.humantask.persistence.domain.TaskInfo)
     */
    @Override
    public void onComplete(TaskInfo taskInfo) throws Exception {

    }

}
