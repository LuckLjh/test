package com.cesgroup.bpm.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.hutool.core.collection.CollectionUtil;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.workflow.exception.ResultNullException;

/**
 * 退回任务.
 *
 * @author chen.liang1
 *
 */
public class RollbackTaskCmd2 implements Command<Object> {

    /** logger. */
    /*
     * private static Logger logger =
     * LoggerFactory.getLogger(RollbackTaskCmd2.class);
     */

    /** task id. */
    private final String taskId;

    /** activity id. */
    private final String activityId;

    /** user id. */
    private String userId;

    private TaskEntity taskEntity;

    private ProcessDefinitionEntity processDefinitionEntity;

    private final boolean useLastAssignee;

    /**
     * 指定taskId和跳转到的activityId，自动使用最后的assignee.
     */
    public RollbackTaskCmd2(String taskId, String activityId) {
        this.taskId = taskId;
        this.activityId = activityId;
        this.useLastAssignee = true;
    }

    /**
     * 指定taskId和跳转到的activityId, userId.
     */
    public RollbackTaskCmd2(String taskId, String activityId, String userId) {
        this.taskId = taskId;
        this.activityId = activityId;
        this.userId = userId;
        this.useLastAssignee = false;
    }

    /**
     * 退回流程.
     *
     * @return 0-退回成功 1-流程结束 2-下一结点已经通过,不能退回
     */
    @Override
    public Integer execute(CommandContext commandContext) {
        init(commandContext);
        // 获取当前活动的节点信息
        final ActivityImpl activitySrc = taskEntity.getExecution().getActivity();
        // 记录节点所属执行线程中的上一个提交节点的ID+aciton
        // List<String> taskIdList =
        // (List<String>)taskEntity.getExecution().getVariable(
        // WorkflowConstants.DefaultVariable.TEMP_LAST_COMPLETE_TASK_QUEUE);
        // 获取当前任务下一个流向
        List<PvmTransition> oriPvmTransitionList = null;
        // 接受所有新的流向
        List<PvmTransition> newPvmTransitionList = null;
        try {
            oriPvmTransitionList = clearTransition(activitySrc);
            newPvmTransitionList = new ArrayList<PvmTransition>();
            ActivityImpl targetActivity = null;
            // 获取跳转的节点的信息
            if (StringUtils.isBlank(activityId)) {
                // boolean flag = true;
                targetActivity = new FindPreviousActivityImplCmd(taskId, null)
                    .execute(commandContext);
                // 利用递归去查找上一个节点
                targetActivity = findActivityByTargetActivity(targetActivity, commandContext);
            } else {
                targetActivity = findActivity(activityId);
            }

            if (this.useLastAssignee) {
                this.userId = getLastAssignee(commandContext, targetActivity);
            }
            // 设置参数，保证流程办理人是历史办理人
            final Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("_historicaAssignee", userId);
            variables.put("_historicaAction", WorkflowConstants.HumanTaskConstants.ACTION_ROLLBACK);
            // 退回时，指定办理人
            variables.put("_codeList", "defaultRunNodeWorkflowNode");
            variables.put("_codeAssigneeList", userId);

            // 获取原本的跳转流向
            final TransitionImpl createOutgoingTransition = activitySrc.createOutgoingTransition();
            createOutgoingTransition.setDestination(targetActivity);
            newPvmTransitionList.add(createOutgoingTransition);
            // 完成当前的任务
            new CompleteTaskWithCommentCmd(taskId, variables,
                WorkflowConstants.HumanTaskConstants.DELETE_REASON_COMPLETED)
                    .execute(commandContext);
        } finally {
            if (newPvmTransitionList != null) {
                // 删除现有的自定义的流向
                for (final PvmTransition pvmTransition : newPvmTransitionList) {
                    pvmTransition.getDestination().getIncomingTransitions().remove(pvmTransition);
                }
            }
            // 还原最初的节点流向
            restoreTransition(activitySrc, oriPvmTransitionList);
        }
        Context.getProcessEngineConfiguration().getProcessDefinitionCache().remove(
            taskEntity.getProcessDefinitionId());
        return 0;

    }

    /**
     * 找到想要回退对应的任务历史.
     */
    private String getLastAssignee(CommandContext commandContext,
        ActivityImpl activityImpl) {
        final HistoricTaskInstanceQueryImpl historicTaskInstanceQueryImpl = new HistoricTaskInstanceQueryImpl();
        historicTaskInstanceQueryImpl.taskDefinitionKey(activityImpl.getId());
        historicTaskInstanceQueryImpl.processInstanceId(taskEntity.getProcessInstanceId());
        historicTaskInstanceQueryImpl.setFirstResult(0);
        historicTaskInstanceQueryImpl.setMaxResults(1);
        historicTaskInstanceQueryImpl.orderByHistoricTaskInstanceEndTime().desc();
        final List<HistoricTaskInstance> hiList = commandContext
            .getHistoricTaskInstanceEntityManager()
            .findHistoricTaskInstancesByQueryCriteria(historicTaskInstanceQueryImpl);
        if (null != hiList && hiList.size() > 0) {
            return hiList.get(0).getAssignee();
        }
        throw new ResultNullException("回退上一步失败，原因：上一节点没有审批人");
    }

    // 查找targetActivity节点的上一个节点
    private ActivityImpl findActivityByTargetActivity(ActivityImpl targetActivity,
        CommandContext commandContext) {
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        // 通过流程实例id 和 节点id 查找出 符合的taskInfo对象 可能存在多条 只要最新的一条的attr5的状态就可以
        final List<Map<String, Object>> attr5List = jdbcTemplate.queryForList(
            "select ATTR5 from t_wf_task_info t where t.PROCESS_INSTANCE_ID=? and t.CODE=?",
            taskEntity.getProcessInstanceId(), targetActivity.getId());
        if (attr5List.size() > 0) {
            final Map<String, Object> map = attr5List.get(0);
            final String attr5 = (String) map.get("ATTR5");
            if (StringUtils.isNotBlank(attr5)) {
                if (WorkflowConstants.HumanTaskConstants.WORKFLOW_AUTOCOMMIT.equals(attr5)) {
                    // 查询出走过当前节点最近的code
                    final String findNearestUserTask = findNearestUserTask(targetActivity,
                        commandContext);
                    final ProcessDefinitionImpl processDefinition = taskEntity.getExecution()
                        .getProcessDefinition();
                    final ActivityImpl findNearesActivity = processDefinition.findActivity(
                        findNearestUserTask);
                    return findActivityByTargetActivity(findNearesActivity, commandContext);

                } else {
                    return targetActivity;
                }
            } else {
                return targetActivity;
            }
        } else {
            return targetActivity;
        }
    }

    /**
     * 查找离当前节点最近的上一个走过的userTask的code.
     *
     */
    private String findNearestUserTask(ActivityImpl targetActivity, CommandContext commandContext) {

        Set<String> previousHistoricActivityInstanceIdSet = new HashSet<String>();
        // 根据流程图去当前节点的所有上一个人工节点，如果是排他网关的情况下，递归向上查找
        previousHistoricActivityInstanceIdSet = findIncomingNodeList(
            findActivity(targetActivity.getId()));
        if (CollectionUtil.size(previousHistoricActivityInstanceIdSet) == 0) {
            throw new ResultNullException("回退上一步失败");
        }
        String historyActivityId = "";
        // 拿到获取到的activityId和流程实例id去查询历史表 看看哪个节点走过
        // 如果都走过的情况下 去根据时间去判断 完成时间非空且最新的是我们想要的结果
        final StringBuffer sb = new StringBuffer();
        sb.append("SELECT ACT_ID_, ASSIGNEE_ FROM T_WF_ACT_HI_ACTINST WHERE ACT_ID_ in (");
        for (final String previousHistoricActivityInstanceId : previousHistoricActivityInstanceIdSet) {
            sb.append("'" + previousHistoricActivityInstanceId + "',");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append(")");
        sb.append(" and PROC_INST_ID_ = " + taskEntity.getProcessInstanceId()
            + " order by END_TIME_ desc");
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString());
        if (list.size() > 0) {
            final Map<String, Object> historicActivityInstance = list.get(0);
            historyActivityId = (String) historicActivityInstance.get("ACT_ID_");

        }
        return historyActivityId;
    }

    private static Set<String> findIncomingNodeList(PvmActivity findActivity) {
        final Set<String> result = new HashSet<String>();
        final List<PvmTransition> incomingTransitions = findActivity.getIncomingTransitions();
        for (final PvmTransition pt : incomingTransitions) {
            final PvmActivity src = pt.getSource();
            final Object srcType = src.getProperty("type");
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(srcType)) {
                result.add(src.getId());
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY
                .equals(srcType)) {
                result.addAll(findIncomingNodeList(src));
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(srcType)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY
                    .equals(srcType)) {
                // 不做操作
            } else {
                return null;
            }
        }
        return result;
    }

    /**
     * 初始化
     *
     * @param commandContext
     *            上下文
     */
    private void init(CommandContext commandContext) {
        // 获取当前的任务
        this.taskEntity = commandContext.getTaskEntityManager().findTaskById(taskId);
        // 获取流程定义
        this.processDefinitionEntity = (ProcessDefinitionEntity) taskEntity.getExecution()
            .getProcessDefinition();
    }

    private ActivityImpl findActivity(String activityId) {
        return processDefinitionEntity.findActivity(activityId);
    }

    /**
     * 查找回退的目的节点.
     */
    /*
       * private ActivityImpl findTargetActivity(CommandContext commandContext)
       * { if (StringUtils.isBlank(activityId)) { String historyActivityId =
       * findNearestUserTask(commandContext); if
       * (StringUtils.isBlank(historyActivityId)) {
       * logger.error("上一个人工任务节点ID:{}", historyActivityId); throw new
       * RuntimeException("上一个人工任务节点不存在，无法进行回退"); } this.activityId =
       * historyActivityId; } return findActivity(activityId); }
       */

    /**
     * 根据节点ID 获取节点信息
     *
     * @param activityId
     *            节点ID
     * @return ActivityImpl 封装节点信息的对象
     */
    /*
     * private ActivityImpl findActivity(String activityId) { return
     * processDefinitionEntity.findActivity(activityId); }
     */

    /**
     * 查找离当前节点最近的上一个走过的userTask的code.
     *
     */
    /*
     * private String findNearestUserTask(CommandContext commandContext) { if
     * (taskEntity == null) { logger.debug("无法找到任务 : {}", taskId); return null;
     * } Set<String> previousHistoricActivityInstanceIdSet = new
     * HashSet<String>(); //根据流程图去当前节点的所有上一个人工节点，如果是排他网关的情况下，递归向上查找
     * previousHistoricActivityInstanceIdSet = findIncomingNodeList(
     * findActivity(taskEntity.getTaskDefinitionKey())); if
     * (previousHistoricActivityInstanceIdSet.size() == 0) { throw new
     * ResultNullException("回退上一步失败"); } String historyActivityId = "";
     * //拿到获取到的activityId和流程实例id去查询历史表 看看哪个节点走过 //如果都走过的情况下 去根据时间去判断
     * 完成时间非空且最新的是我们想要的结果 StringBuffer sb = new StringBuffer(); sb.append(
     * "SELECT ACT_ID_, ASSIGNEE_ FROM T_WF_ACT_HI_ACTINST WHERE ACT_ID_ in (");
     * for (String previousHistoricActivityInstanceId :
     * previousHistoricActivityInstanceIdSet) { sb.append("'" +
     * previousHistoricActivityInstanceId + "',"); } sb.delete(sb.length() - 1,
     * sb.length()); sb.append(")"); sb.append(" and PROC_INST_ID_ = " +
     * taskEntity.getProcessInstanceId() + " order by END_TIME_ desc");
     * List<HistoricActivityInstance> list =
     * commandContext.getProcessEngineConfiguration()
     * .getHistoryService().createNativeHistoricActivityInstanceQuery().sql(sb.
     * toString()) .list(); if (list.size() > 0) { HistoricActivityInstance
     * historicActivityInstance = list.get(0); historyActivityId =
     * historicActivityInstance.getActivityId(); //指定退回的办理人为历史办理人(不覆盖原有值)
     * this.userId = userId == null ? historicActivityInstance.getAssignee() :
     * userId; } return historyActivityId; }
     */

    /*
     * private static Set<String> findIncomingNodeList(PvmActivity findActivity)
     * { Set<String> result = new HashSet<String>(); List<PvmTransition>
     * incomingTransitions = findActivity.getIncomingTransitions(); for
     * (PvmTransition pt : incomingTransitions) { PvmActivity src =
     * pt.getSource(); Object srcType = src.getProperty("type"); if
     * (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(srcType)) {
     * result.add(src.getId()); } else if
     * (WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY
     * .equals(srcType)) { result.addAll(findIncomingNodeList(src)); } else if
     * (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(
     * srcType) ||
     * WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY.equals(
     * srcType)) { //不做操作 } else { logger.info("无法回退，前序节点不是用户任务 : " +
     * src.getId() + " " + srcType + "(" + src.getProperty("name") + ")");
     * return null; } } return result; }
     */

    /**
     * 还原指定活动节点流向
     *
     * @param activityImpl
     *            活动节点
     * @param oriPvmTransitionList
     *            原有节点流向集合
     */
    private void restoreTransition(ActivityImpl activityImpl,
        List<PvmTransition> oriPvmTransitionList) {
        // 清空现有流向
        final List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        pvmTransitionList.clear();
        // 还原以前流向
        pvmTransitionList.addAll(oriPvmTransitionList);
    }

    /**
     * 清空指定活动节点流向
     *
     * @param activityImpl
     *            活动节点
     * @return 节点流向集合
     */
    private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {
        // 存储当前节点所有流向临时变量
        final List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
        // 获取当前节点所有流向，存储到临时变量，然后清空
        final List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        for (final PvmTransition pvmTransition : pvmTransitionList) {
            oriPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();

        return oriPvmTransitionList;
    }
    /* */
    /**
     * 根据候选人组表达式获取候选人
     *
     * @param candidateGroupIdExpressions
     *            获选人组表达式
     * @return Set 封装用户信息的集合
     */

    /*
     * private Set<String> getUserListByCandidateGroupIdExpressions(
     * Set<Expression> candidateGroupIdExpressions) { //获取候选人组类型 String
     * candidateGroupType =
     * checkCandidateGroupType(candidateGroupIdExpressions);
     *
     * Set<String> resultList = new HashSet<String>(); UserDTO expressionUserDto
     * = null; //配置候选人组 for (Expression expression :
     * candidateGroupIdExpressions) { String expressionText =
     * expression.getExpressionText(); //候选人组 配置表达式的情况下 if
     * (StringUtils.isNotBlank(candidateGroupType) &&
     * "expression".equals(candidateGroupType)) { expressionUserDto =
     * UserDTO.expressionUserDto(expressionText);
     * resultList.add(expressionUserDto.getId()); } else if
     * (StringUtils.isNotBlank(candidateGroupType) &&
     * "group".equals(candidateGroupType)) { String candidateGroupExpression =
     * expressionText.split(":")[0]; expressionUserDto =
     * UserDTO.expressionUserDto(candidateGroupExpression);
     * resultList.add(expressionUserDto.getId()); } else if
     * (StringUtils.isNotBlank(candidateGroupType) &&
     * "role".equals(candidateGroupType)) { String candidateGroupExpression =
     * expressionText.split(":")[0]; expressionUserDto =
     * UserDTO.expressionUserDto(candidateGroupExpression);
     * resultList.add(expressionUserDto.getId()); } }
     *
     * return resultList; }
     */
    /**
     * 根据候选人表达式获取用户列表
     *
     * @param candidateUserIdExpressions
     *            获选人表达式
     * @return Set 封装用户信息的集合
     */
    /*
     * private Set<String> getUserListByCandidateUserIdExpressions(
     * Set<Expression> candidateUserIdExpressions) { Set<String> resultList =
     * new HashSet<String>(); //配置候选人 for (Expression expression :
     * candidateUserIdExpressions) { //获取表达式 String expressionText =
     * expression.getExpressionText(); if ("流程发起人".equals(expressionText)) {
     * //如果配置的是流程发起人的情况下 UserDTO expUserDto =
     * UserDTO.expressionUserDto(expressionText);
     * resultList.add(expUserDto.getId()); } else if
     * (expressionText.startsWith("表达式") || expressionText.indexOf("${") >= 0) {
     * //表达式:${test} UserDTO exprUserDto =
     * UserDTO.expressionUserDto(expressionText);
     * resultList.add(exprUserDto.getId()); } else { //配置了用户的情况下 String userId =
     * expressionText.split(":")[1]; resultList.add(userId); } } return
     * resultList; }
     */
    /* *//**
          * 判断候选人组类型
          *
          * @param candidateGroupIdExpressions
          *            节点上所有的表达式
          * @return String， expression：表达式， group：部门，role：角色
          *//*
            * private String checkCandidateGroupType(Set<Expression>
            * candidateGroupIdExpressions) { //配置候选人组(多加一个根据角色或者部门查询所有用户的实现)
            * //首先判断是什么类型的（部门 /角色 /表达式） String candidateGroupType = ""; for
            * (Expression expression : candidateGroupIdExpressions) { String
            * expressionText = expression.getExpressionText(); if
            * (expressionText.startsWith("表达式")) { candidateGroupType =
            * "expression"; break; } if (expressionText.startsWith("(部门)")) {
            * candidateGroupType = "group"; break; } if
            * (expressionText.startsWith("(角色)")) { candidateGroupType = "role";
            * break; } } return candidateGroupType; }
            *
            */
}
