package com.cesgroup.bpm.cmd;

import cn.hutool.core.collection.CollectionUtil;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.workflow.exception.ResultNullException;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 退回任务.
 * 
 * @author chen.liang1
 *
 */
public class FindPreviousActivityImplCmd implements Command<ActivityImpl> {

    /** logger. */
    private static Logger logger = LoggerFactory.getLogger(FindPreviousActivityImplCmd.class);

    /** task id. */
    private String taskId;

    /** activity id. */
    private String activityId;

    /** user id. */
    private String userId;

    private TaskEntity taskEntity;

    private ProcessDefinitionEntity processDefinitionEntity;

    /**
     * 指定taskId和跳转到的activityId，自动使用最后的assignee.
     */
    public FindPreviousActivityImplCmd(String taskId, String activityId) {
        this.taskId = taskId;
        this.activityId = activityId;
    }

    /**
     * 指定taskId和跳转到的activityId, userId.
     */
    public FindPreviousActivityImplCmd(String taskId, String activityId, String userId) {
        this.taskId = taskId;
        this.activityId = activityId;
        this.userId = userId;
    }

    /**
     * 返回回退的目的节点.
     */
    @Override
    public ActivityImpl execute(CommandContext commandContext) {
        init(commandContext);
        ActivityImpl targetActivity = findTargetActivity(commandContext);
        return targetActivity;

    }

    /**
     * 初始化
     * 
     * @param commandContext 上下文
     */
    private void init(CommandContext commandContext) {
        //获取当前的任务
        this.taskEntity = commandContext.getTaskEntityManager().findTaskById(taskId);
        //获取流程定义
        this.processDefinitionEntity = (ProcessDefinitionEntity) taskEntity.getExecution()
            .getProcessDefinition();
    }

    /**
     * 查找回退的目的节点.
     */
    private ActivityImpl findTargetActivity(CommandContext commandContext) {
        if (StringUtils.isBlank(activityId)) {
            String historyActivityId = findNearestUserTask(commandContext);
            if (StringUtils.isBlank(historyActivityId)) {
                logger.error("上一个人工任务节点ID:{}", historyActivityId);
                throw new RuntimeException("上一个人工任务节点不存在，无法进行回退");
            }
            this.activityId = historyActivityId;
        }
        return findActivity(activityId);
    }

    /**
     * 根据节点ID 获取节点信息
     * 
     * @param activityId
     *            节点ID
     * @return ActivityImpl 封装节点信息的对象
     */
    private ActivityImpl findActivity(String activityId) {
        return processDefinitionEntity.findActivity(activityId);
    }

    /**
     * 查找离当前节点最近的上一个走过的userTask的code.
     * 
     */
    private String findNearestUserTask(CommandContext commandContext) {
        if (taskEntity == null) {
            logger.debug("无法找到任务 : {}", taskId);
            return null;
        }
        Set<String> previousHistoricActivityInstanceIdSet = new HashSet<String>();
        //根据流程图去当前节点的所有上一个人工节点，如果是排他网关的情况下，递归向上查找
        previousHistoricActivityInstanceIdSet = findIncomingNodeList(
            findActivity(taskEntity.getTaskDefinitionKey()));
        if (CollectionUtil.size(previousHistoricActivityInstanceIdSet) == 0) {
            throw new ResultNullException("回退上一步失败");
        }
        String historyActivityId = "";
        //拿到获取到的activityId和流程实例id去查询历史表 看看哪个节点走过 
        //如果都走过的情况下 去根据时间去判断 完成时间非空且最新的是我们想要的结果
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ACT_ID_, ASSIGNEE_ FROM T_WF_ACT_HI_ACTINST WHERE ACT_ID_ in (");
        for (String previousHistoricActivityInstanceId : previousHistoricActivityInstanceIdSet) {
            sb.append("'" + previousHistoricActivityInstanceId + "',");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append(")");
        sb.append(" and PROC_INST_ID_ = " + taskEntity.getProcessInstanceId()
            + " order by END_TIME_ desc");
        List<HistoricActivityInstance> list = commandContext.getProcessEngineConfiguration()
            .getHistoryService().createNativeHistoricActivityInstanceQuery().sql(sb.toString())
            .list();
        if (list.size() > 0) {
            HistoricActivityInstance historicActivityInstance = list.get(0);
            historyActivityId = historicActivityInstance.getActivityId();
            //指定退回的办理人为历史办理人(不覆盖原有值)
            this.userId = userId == null ? historicActivityInstance.getAssignee() : userId;
        }
        return historyActivityId;
    }

    private static Set<String> findIncomingNodeList(PvmActivity findActivity) {
        Set<String> result = new HashSet<String>();
        List<PvmTransition> incomingTransitions = findActivity.getIncomingTransitions();
        for (PvmTransition pt : incomingTransitions) {
            PvmActivity src = pt.getSource();
            Object srcType = src.getProperty("type");
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(srcType)) {
                result.add(src.getId());
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY
                .equals(srcType)) {
                result.addAll(findIncomingNodeList(src));
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(srcType)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY.equals(srcType)) {
                //不做操作
            } else {
                logger.info("无法回退，前序节点不是用户任务 : " + src.getId() + " " + srcType + "("
                    + src.getProperty("name") + ")");
                return null;
            }
        }
        return result;
    }

}
