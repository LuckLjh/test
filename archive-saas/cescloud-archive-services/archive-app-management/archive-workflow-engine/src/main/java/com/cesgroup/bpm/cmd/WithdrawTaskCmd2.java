package com.cesgroup.bpm.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.cmd.CompleteTaskCmd;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cesgroup.core.util.WorkflowConstants;

/**
 * 撤回（改）
 *
 * @author chen.liang1
 * @version 1.0.0 2017-11-28
 */
public class WithdrawTaskCmd2 implements Command<String> {

    /** logger. */
    private static Logger logger = LoggerFactory.getLogger(WithdrawTaskCmd2.class);

    /** 当前任务ID（进行撤回操作的任务） */
    private final String curTaskId;

    /** 流程参数 */
    private final Map<String, Object> variables;

    /** 当前任务实体 */
    private HistoricTaskInstanceEntity curTaskEntity;

    /** 流程定义实体 */
    private ProcessDefinitionEntity processDefinitionEntity;

    private CommandContext commandContext;

    public WithdrawTaskCmd2(String taskId) {
        this(taskId, new HashMap<String, Object>());
    }

    public WithdrawTaskCmd2(String taskId, Map<String, Object> variables) {
        this.curTaskId = taskId;
        this.variables = variables;
    }

    @Override
    public String execute(CommandContext commandContext) {
        init(commandContext);
        //获取需要撤回的目标节点ID
        final String targetActivityId = findTargetActivityId();
        if (StringUtils.isBlank(targetActivityId)) {
            logger.debug("无法找到目标节点,任务ID：{}", curTaskId);
            throw new RuntimeException("撤回失败，无法找到目标节点");
        }
        //撤回任务
        return withDrawTask(targetActivityId);
    }

    /**
     * 初始化
     *
     * @param commandContext
     *            上下文
     */
    private void init(CommandContext commandContext) {
        this.commandContext = commandContext;
        //初始化当前任务
        initCurrentTaskEntity();
        //初始化流程定义
        initProcessDefinitionEntity();
    }

    /**
     * 初始化流程定义
     */
    private void initProcessDefinitionEntity() {
        final String processDefinitionId = curTaskEntity.getProcessDefinitionId();
        this.processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(processDefinitionId)
            .execute(commandContext);
        if (processDefinitionEntity == null) {
            logger.debug("无法找到流程定义,流程定义ID：{}", processDefinitionId);
            throw new RuntimeException("撤回失败，无法找到流程定义");
        }
    }

    /**
     * 初始化当前任务
     */
    private void initCurrentTaskEntity() {
        this.curTaskEntity = commandContext.getHistoricTaskInstanceEntityManager()
            .findHistoricTaskInstanceById(curTaskId);
        if (curTaskEntity == null) {
            logger.debug("无法找到任务,任务ID：{}", curTaskId);
            throw new RuntimeException("撤回失败，无法找到任务");
        }
    }

    /**
     * 根据目标节点ID撤回任务
     *
     * @param targetActivityId
     *            目标节点ID
     * @return String 被撤回的任务ID
     */
    private String withDrawTask(String targetActivityId) {
        //根据节点ID，找到撤回目标任务
        final Task task = getTargetTaskByActivityId(targetActivityId);
        //提交任务到目标节点
        completeTaskToTargetTask(targetActivityId, task);
        //删除历史记录
        deleteHistory(task.getId());
        return task.getId();
    }

    /**
     * 提交任务到目标节点
     *
     * @param targetActivityId
     *            目标节点
     * @param task
     *            当前处于活动状态的任务
     */
    private void completeTaskToTargetTask(String targetActivityId, Task task) {
        final ActivityImpl activityImpl = findActivityImplByTaskDefinitionKey(targetActivityId);
        List<PvmTransition> oriPvmTransitionList = null;
        List<PvmTransition> newPvmTransitionList = null;
        try {
            oriPvmTransitionList = clearTransition(activityImpl);
            newPvmTransitionList = new ArrayList<PvmTransition>();
            final ActivityImpl curActivity = findActivityImplByTaskDefinitionKey(
                curTaskEntity.getTaskDefinitionKey());

            //设置参数，保证流程办理人是历史办理人
            //variables.put("_codeList", activityId);
            //variables.put("_codeAssigneeList", userIds);
            variables.put("_historicaAssignee", curTaskEntity.getAssignee());
            variables.put("_historicaAction", WorkflowConstants.HumanTaskConstants.ACTION_WITHDRAW);

            final TransitionImpl transitionImpl = activityImpl.createOutgoingTransition();
            transitionImpl.setDestination(curActivity);
            newPvmTransitionList.add(transitionImpl);
            new CompleteTaskCmd(task.getId(), variables).execute(commandContext);
        } catch (final RuntimeException e) {
            throw e;
        } finally {
            if (newPvmTransitionList != null) {
                //删除现有的自定义的流向
                for (final PvmTransition pvmTransition : newPvmTransitionList) {
                    pvmTransition.getDestination().getIncomingTransitions()
                        .remove(pvmTransition);
                }
                //还原最初的节点流向
                restoreTransition(activityImpl, oriPvmTransitionList);
            }
            Context.getProcessEngineConfiguration().getProcessDefinitionCache().remove(
                task.getProcessDefinitionId());
        }
    }

    /**
     * 根据目标节点ID，找到撤回目标任务
     *
     * @param targetActivityId
     *            目标节点ID
     * @return Task
     */
    private Task getTargetTaskByActivityId(String targetActivityId) {
        final List<Task> list = commandContext.getProcessEngineConfiguration().getTaskService()
            .createTaskQuery().taskDefinitionKey(targetActivityId)
            .executionId(curTaskEntity.getExecutionId()).active().list();
        if (list == null) {
            throw new RuntimeException("撤回失败，没有找到撤回目标");
        }
        if (list.size() != 1) {
            throw new RuntimeException("撤回失败，目标节点拥有多个活动实例");
        }
        final Task task = list.get(0);

        final ActivityImpl activityImpl = findActivityImplByTaskDefinitionKey(
            task.getTaskDefinitionKey());
        //并行会签任务
        if (isParallelVoteTask(activityImpl)) {
            throw new RuntimeException("撤回失败，撤回目标节点是并行会签节点");
        }
        //抢占节点，已经签收
        if (isPreemptionTask(activityImpl) && task.getAssignee() != null) {
            throw new RuntimeException("撤回失败，撤回目标已经被签收");
        }
        return task;
    }

    /**
     * 是否是抢占节点
     *
     * @param activityImpl
     *            节点信息
     * @return boolean
     */
    private boolean isPreemptionTask(ActivityImpl activityImpl) {
        final TaskDefinition taskDefinition = (TaskDefinition) activityImpl.getProperty("taskDefinition");
        return activityImpl.getProperty("multiInstance") == null //不是多实例节点
            && taskDefinition.getAssigneeExpression() == null
            && (taskDefinition.getCandidateUserIdExpressions() != null
                || taskDefinition.getCandidateGroupIdExpressions() != null);
    }

    /**
     * 任务是并行会签任务
     *
     * @param activityImpl
     *            任务节点信息
     * @return boolean
     */
    private boolean isParallelVoteTask(ActivityImpl activityImpl) {
        return "parallel".equals(activityImpl.getProperty("multiInstance"));
    }

    /**
     * 删除历史记录
     *
     * @param taskId
     *            任务ID
     */
    @SuppressWarnings("unchecked")
    private void deleteHistory(String taskId) {
        commandContext.getProcessEngineConfiguration().getHistoryService()
            .deleteHistoricTaskInstance(taskId);
        commandContext.getProcessEngineConfiguration().getHistoryService()
            .deleteHistoricTaskInstance(curTaskId);
        final List<HistoricActivityInstance> list = commandContext.getProcessEngineConfiguration()
            .getHistoryService().createHistoricActivityInstanceQuery()
            .executionId(curTaskEntity.getExecutionId()).orderByHistoricActivityInstanceStartTime()
            .asc().list();
        final List<HistoricActivityInstance> subList = new ArrayList<HistoricActivityInstance>();
        boolean flag = false;
        for (final HistoricActivityInstance hai : list) {
            if (curTaskId.equals(hai.getTaskId())) {
                flag = true;
            }
            if (flag) {
                subList.add(hai);
            }
            if (taskId.equals(hai.getTaskId())) {
                break;
            }
        }
        //更新流程运行时参数
        final RuntimeService runtimeService = commandContext.getProcessEngineConfiguration()
            .getRuntimeService();
        final List<String> taskQueue = (List<String>) runtimeService.getVariable(
            curTaskEntity.getExecutionId(),
            WorkflowConstants.DefaultVariable.TEMP_LAST_COMPLETE_TASK_QUEUE);
        taskQueue.remove(0);
        commandContext.getDbSqlSession().delete("deleteHistoricActivityInstancesByEntityList",
            subList);
    }

    /**
     * 获取撤回的目标节点
     *
     * @return String 目标节点ID
     */
    private String findTargetActivityId() {
        final ActivityImpl activityImpy = findActivityImplByTaskDefinitionKey(
            curTaskEntity.getTaskDefinitionKey());
        //所有可能到达的人工节点的ID
        final Set<String> possibleActivityIdSet = findAllPossibleDestinationActivity(activityImpy);
        //获取当前流程正在运行的任务
        final Map<String, String> activeActivityInfoMap = findActiveActivity();
        //根据当前任务的execution获取该线程上正在运行的节点ID
        final String activityId = activeActivityInfoMap.get(curTaskEntity.getExecutionId());
        //如果当前节点是自由跳结束的节点，直接返回activityId
        if (WorkflowConstants.HumanTaskConstants.DELETE_REASON_FREE_JUMP
            .equals(curTaskEntity.getDeleteReason())) {
            return activityId;
        }
        //如果不是自由跳结束的节点，目标节点必须是当前节点可以到达的节点
        if (possibleActivityIdSet.contains(activityId)) {
            return activityId;
        }
        return null;
    }

    /**
     * 根据节点ID获取节点实体对象
     *
     * @param taskDefinitionKey
     *            节点ID
     * @return ActivityImpl 封装节点信息的对象
     */
    private ActivityImpl findActivityImplByTaskDefinitionKey(String taskDefinitionKey) {
        return processDefinitionEntity.findActivity(taskDefinitionKey);
    }

    /**
     * 获取节点所有可能到达的下一个人工节点
     *
     * @param pvmActivity
     *            节点对象
     * @return Set 所有可能到达的人工节点的ID
     */
    private static Set<String> findAllPossibleDestinationActivity(PvmActivity pvmActivity) {
        final Set<String> result = new HashSet<String>();
        final List<PvmTransition> outgoingTransitions = pvmActivity.getOutgoingTransitions();
        for (final PvmTransition pt : outgoingTransitions) {
            //获取目标节点
            final PvmActivity dest = pt.getDestination();
            final Object type = dest.getProperty("type");
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(type)) {
                result.add(dest.getId());
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(type)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY.equals(type)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY.equals(type)) {
                result.addAll(findAllPossibleDestinationActivity(dest));
            }
        }
        return result;
    }

    /**
     * 获取当前流程正在运行的节点的executionId和taskDefinitionKey信息
     * 当正在运行的节点是并行会签时，获取该节点的父executionId作为key值
     *
     * @return Map{executionId: taskDefinitionKey}
     */
    private Map<String, String> findActiveActivity() {
        final Map<String, String> result = new HashMap<String, String>();
        //流程实例ID
        final String processInstanceId = curTaskEntity.getProcessInstanceId();
        final List<Task> list = commandContext.getProcessEngineConfiguration().getTaskService()
            .createTaskQuery().processInstanceId(processInstanceId).active().list();
        for (final Task task : list) {
            final String taskDefinitionKey = task.getTaskDefinitionKey();
            String executionId = task.getExecutionId();
            final ActivityImpl activityImpl = findActivityImplByTaskDefinitionKey(taskDefinitionKey);
            //如果是并行会签
            if (isParallelVoteTask(activityImpl)) {
                final TaskEntity taskEntity = (TaskEntity) task;
                executionId = taskEntity.getExecution().getParent().getParentId();
            }
            result.put(executionId, taskDefinitionKey);
        }
        return result;
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
}
