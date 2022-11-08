/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.bpm.cmd</p>
 * <p>文件名:WithdrawTaskForceCmd.java</p>
 * <p>创建时间:2020年1月14日 下午1:39:40</p>
 * <p>作者:qiucs</p>
 */

package com.cesgroup.bpm.cmd;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import com.cesgroup.core.util.WorkflowConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 撤回（实现已签收可撤回、排他网关可撤回）
 *
 * @author qiucs
 * @version 1.0.0 2020年1月14日
 */
public class WithdrawTaskForceCmd implements Command<List<String>> {

    /** 当前任务ID（进行撤回操作的任务） */
    private final String curTaskId;

    private HistoryService historyService;

    private RuntimeService runtimeService;

    private RepositoryService repositoryService;

    private TaskService taskService;

    private HistoricTaskInstance historicTaskInstance;

    private ProcessInstance processInstance;

    private Map<String, Object> variables;

    private ProcessDefinitionEntity definitionEntity;

    private ActivityImpl hisActivity;

    final List<String> taskIdList = Lists.newArrayList();

    public WithdrawTaskForceCmd(String curTaskId) {
        this.curTaskId = curTaskId;
    }

    /**
     *
     * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
     */
    @Override
    public List<String> execute(CommandContext commandContext) {
        // 初始化
        init(commandContext);

        // 撤回
        processWithdraw();

        if (taskIdList.isEmpty()) {
            throw new RuntimeException("该任务不能撤回");
        }
        // 删除历史记录
        deleteHistoric();

        return taskIdList;
    }

    /*private void print(ActivityImpl activity) {
        System.out.println("activity: " + activity + ", id=" + activity.getId() + ", type="
            + activity.getProperty("type") + ", name=" + activity.getProperty("name"));
        System.out.println("getIncomingTransitions.size: " + activity.getIncomingTransitions().size());
        System.out.println("getOutgoingTransitions.size: " + activity.getOutgoingTransitions().size());
        final List<PvmTransition> outgoingTransitions = activity.getOutgoingTransitions();
        for (final PvmTransition transition : outgoingTransitions) {
            final PvmActivity destination = transition.getDestination();
            print((ActivityImpl) destination);
        }
    }*/

    private void init(CommandContext commandContext) {
        historyService = commandContext.getProcessEngineConfiguration().getHistoryService();
        runtimeService = commandContext.getProcessEngineConfiguration().getRuntimeService();
        repositoryService = commandContext.getProcessEngineConfiguration()
            .getRepositoryService();
        taskService = commandContext.getProcessEngineConfiguration().getTaskService();
        historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
            .taskId(curTaskId).singleResult();
        processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(historicTaskInstance.getProcessInstanceId()).singleResult();
        variables = runtimeService.getVariables(historicTaskInstance.getExecutionId());
        // 标记用于创建节点使用
        variables.put("_historicaAssignee", historicTaskInstance.getAssignee());
        variables.put("_historicaAction", WorkflowConstants.HumanTaskConstants.ACTION_WITHDRAW);
        definitionEntity = (ProcessDefinitionEntity) repositoryService
            .getProcessDefinition(historicTaskInstance.getProcessDefinitionId());
        hisActivity = definitionEntity.findActivity(historicTaskInstance.getTaskDefinitionKey());
    }

    /**
     * 删除流程历史记录
     */
    private void deleteHistoric() {

        final Optional<Map<String, Object>> mapOptional = Optional.ofNullable(variables);

        mapOptional.ifPresent(var -> {
            final Optional<Object> taskOptional = Optional
                .ofNullable(variables.get(WorkflowConstants.DefaultVariable.TEMP_LAST_COMPLETE_TASK_QUEUE));
            taskOptional.ifPresent(tq -> {
                @SuppressWarnings("unchecked")
                final List<String> taskQueue = (List<String>) tq;
                taskQueue.removeIf(str -> str.startsWith(historicTaskInstance.getId()));
            });
        });

    }

    private List<Task> getActivityTaskList(String activityId) {
        final long count = historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(processInstance.getId()).taskDefinitionKey(activityId).count();
        if (0 == count) {
            return Lists.newArrayList();
        }
        final List<Task> currTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId())
            .taskDefinitionKey(activityId).active().list();
        if (null == currTasks || currTasks.isEmpty() || count != currTasks.size()) {
            throw new RuntimeException("任务已审批，无法撤回");
        }
        return currTasks;
    }

    private void completeUserTasks(ActivityImpl nextActivity, List<Task> nextTaskList) {

        final List<PvmTransition> oriPvmTransitionList = clearOutgoingTransitions(nextActivity);
        // 建立新方向
        final TransitionImpl newTransition = nextActivity.createOutgoingTransition();
        newTransition.setDestination(hisActivity);

        try {
            for (final Task nextTask : nextTaskList) {
                taskService.claim(nextTask.getId(), null);
                taskService.complete(nextTask.getId(), variables);
                historyService.deleteHistoricTaskInstance(nextTask.getId());
                taskIdList.add(nextTask.getId());
            }
        } catch (final Exception e) {
            throw new RuntimeException("撤回失败", e);
        } finally {
            // 撤回节点恢复方向
            hisActivity.getIncomingTransitions().remove(newTransition);
            // 下一节点恢复方向
            restoreOutgoingTransitions(nextActivity, oriPvmTransitionList);
        }

    }

    private void completeUserTasks(ActivityImpl gatewayActivity, List<ActivityImpl> nextActivityList,
        List<Task> nextTaskList) {

        final Map<String, List<PvmTransition>> oriPvmTransitionMap = Maps.newHashMap();
        final List<PvmTransition> gatewayOriIncomingTransitionList = Lists.newArrayList();
        final List<PvmTransition> gatewayOriOutgoingTransitionList = Lists.newArrayList();
        final List<PvmTransition> incomingTransitionList = gatewayActivity.getIncomingTransitions();
        final List<PvmTransition> outgoingTransitionList = gatewayActivity.getOutgoingTransitions();
        incomingTransitionList.forEach(transition -> gatewayOriIncomingTransitionList.add(transition));
        outgoingTransitionList.forEach(transition -> gatewayOriOutgoingTransitionList.add(transition));
        incomingTransitionList.clear();
        outgoingTransitionList.clear();
        final TransitionImpl gatewayTransition = gatewayActivity.createOutgoingTransition();
        gatewayTransition.setDestination(hisActivity);
        for (final ActivityImpl nextActivity : nextActivityList) {
            oriPvmTransitionMap.put(nextActivity.getId(), clearOutgoingTransitions(nextActivity));
            // 建立新方向，清理网关进出路线，并重新关联
            nextActivity.createOutgoingTransition().setDestination(gatewayActivity);
        }

        try {
            for (final Task nextTask : nextTaskList) {
                taskService.claim(nextTask.getId(), null);
                taskService.complete(nextTask.getId(), variables);
                historyService.deleteHistoricTaskInstance(nextTask.getId());
                taskIdList.add(nextTask.getId());
            }
        } catch (final Exception e) {
            throw new RuntimeException("撤回失败", e);
        } finally {
            // 撤回节点恢复方向
            hisActivity.getIncomingTransitions().remove(gatewayTransition);
            // 网关恢复方向
            incomingTransitionList.clear();
            outgoingTransitionList.clear();
            incomingTransitionList.addAll(gatewayOriIncomingTransitionList);
            outgoingTransitionList.addAll(gatewayOriOutgoingTransitionList);
            // 下一节点恢复方向
            for (final ActivityImpl nextActivity : nextActivityList) {
                restoreOutgoingTransitions(nextActivity, oriPvmTransitionMap.get(nextActivity.getId()));
            }
        }

    }

    // 清除下一节点方向
    private List<PvmTransition> clearOutgoingTransitions(ActivityImpl nextActivity) {
        final List<PvmTransition> oriPvmTransitionList = Lists.newArrayList();
        final List<PvmTransition> pvmTransitionList = nextActivity.getOutgoingTransitions();
        for (final PvmTransition pvmTransition : pvmTransitionList) {
            oriPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();
        return oriPvmTransitionList;
    }

    private void restoreOutgoingTransitions(ActivityImpl nextActivity, final List<PvmTransition> oriPvmTransitionList) {
        // 清空现有流向
        final List<PvmTransition> pvmTransitionList = nextActivity.getOutgoingTransitions();
        pvmTransitionList.clear();
        // 还原以前流向
        pvmTransitionList.addAll(oriPvmTransitionList);
    }

    private void processWithdraw() {
        final List<PvmTransition> outgoingTransitions = hisActivity.getOutgoingTransitions();
        for (final PvmTransition pt : outgoingTransitions) {
            //获取目标节点
            final PvmActivity dest = pt.getDestination();
            final String type = dest.getProperty("type").toString();
            switch (type) {
            case WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK:
                withdrawUserTask(dest.getId());
                break;
            case WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY:
                withdrawExclusiveGateway(dest);
                break;
            case WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY:
                withdrawParallelGateway(dest);
                break;
            case WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY:
                withdrawInclusiveGateway(dest);
                break;
            case WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS:
                throw new RuntimeException("子流程不支持撤回");
            case WorkflowConstants.NodeTypeConstants.NODETYPE_CALL_ACTIVITY:
                throw new RuntimeException("调用活动不支持撤回");
            default:
                throw new RuntimeException("该任务不支持撤回");
            }
        }
    }

    private void withdrawUserTask(String activityId) {
        final List<Task> activityTaskList = getActivityTaskList(activityId);
        final ActivityImpl nextActivity = definitionEntity.findActivity(activityId);
        completeUserTasks(nextActivity, activityTaskList);
    }

    private void withdrawExclusiveGateway(PvmActivity gatewayActivity) {
        final List<PvmTransition> outgoingTransitions = gatewayActivity.getOutgoingTransitions();

        for (final PvmTransition pt : outgoingTransitions) {
            //获取目标节点
            final PvmActivity dest = pt.getDestination();
            final Object type = dest.getProperty("type");
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(type)) {
                withdrawUserTask(dest.getId());
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(type)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY.equals(type)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY.equals(type)) {
                throw new RuntimeException("连续网关节点不支持撤回");
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS.equals(type)) {
                throw new RuntimeException("子流程不支持撤回");
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_CALL_ACTIVITY.equals(type)) {
                throw new RuntimeException("调用活动不支持撤回");
            }
        }
    }

    private void withdrawParallelGateway(PvmActivity gatewayActivity) {
        final List<PvmTransition> outgoingTransitions = gatewayActivity.getOutgoingTransitions();
        if (1 == outgoingTransitions.size()) {
            withdrawUserTask(outgoingTransitions.get(0).getDestination().getId());
            return;
        }
        final List<ActivityImpl> nextActivityList = Lists.newArrayList();
        final List<Task> nextTaskList = Lists.newArrayList();
        for (final PvmTransition pt : outgoingTransitions) {
            //获取目标节点
            final PvmActivity dest = pt.getDestination();
            final Object type = dest.getProperty("type");
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(type)) {
                nextActivityList.add((ActivityImpl) dest);
                nextTaskList.addAll(getActivityTaskList(dest.getId()));
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(type)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY.equals(type)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY.equals(type)) {
                throw new RuntimeException("连续网关不支持撤回");
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS.equals(type)) {
                throw new RuntimeException("子流程不支持撤回");
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_CALL_ACTIVITY.equals(type)) {
                throw new RuntimeException("调用活动不支持撤回");
            }
        }
        completeUserTasks((ActivityImpl) gatewayActivity, nextActivityList, nextTaskList);
    }

    private void withdrawInclusiveGateway(PvmActivity gatewayActivity) {
        withdrawParallelGateway(gatewayActivity);
    }

}
