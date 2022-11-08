package com.cesgroup.bpm.cmd;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cesgroup.api.humantask.HumanTaskConnector;
import com.cesgroup.api.humantask.HumanTaskDTO;
import com.cesgroup.bpm.graph.ActivitiGraphBuilder;
import com.cesgroup.bpm.graph.Edge;
import com.cesgroup.bpm.graph.Graph;
import com.cesgroup.bpm.graph.Node;
import com.cesgroup.bpm.support.HumanTaskBuilder;
import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.spring.ApplicationContextHolder;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.manager.TaskInfoManager;
import com.cesgroup.humantask.persistence.manager.TaskInfoRunManager;
import com.cesgroup.spi.humantask.TaskDefinitionConnector;
import com.cesgroup.workflow.exception.ParallelOrInclusiveException;
import com.cesgroup.workflow.exception.ResultNullException;

/**
 * 退回任务.
 *
 * @author 国栋
 *
 */
public class RollbackTaskCmd implements Command<Object> {

    /** logger. */
    private static Logger logger = LoggerFactory.getLogger(RollbackTaskCmd.class);

    /** task id. */
    private final String taskId;

    /** activity id. */
    private String activityId;

    /** user id. */
    private String userId;

    /** use last assignee. */
    private boolean useLastAssignee = false;

    /** 需要处理的多实例节点. */
    private final Set<String> multiInstanceExecutionIds = new HashSet<String>();

    /**
     * 任务实体
     */
    private TaskEntity taskEntity;

    /**
     * 指定taskId和跳转到的activityId，自动使用最后的assignee.
     */
    public RollbackTaskCmd(String taskId, String activityId) {
        this.taskId = taskId;
        this.activityId = activityId;
        this.useLastAssignee = true;
    }

    /**
     * 指定taskId和跳转到的activityId, userId.
     */
    public RollbackTaskCmd(String taskId, String activityId, String userId) {
        this.taskId = taskId;
        this.activityId = activityId;
        if (userId == null || "".equals(userId)) {
            this.useLastAssignee = true;
        } else {
            this.userId = userId;
        }
    }

    /**
     * 退回流程.
     *
     * @return 0-退回成功 1-流程结束 2-下一结点已经通过,不能退回
     */
    @Override
    public Integer execute(CommandContext commandContext) {
        // 获得任务
        init(commandContext);
        // 找到想要回退到的节点
        final ActivityImpl targetActivity = findTargetActivity(commandContext);
        logger.info("回退到 {}", this.activityId);
        logger.info("{}", targetActivity.getProperties());
        //获取回退的节点的类型
        final String type = (String) targetActivity.getProperty("type");
        if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(type)) {
            logger.info("回退到用户任务");
            rollbackUserTask(commandContext);
        } else {
            throw new IllegalStateException("无法回退 " + type);
        }
        return 0;
    }

    private void init(CommandContext commandContext) {
        taskEntity = commandContext.getTaskEntityManager().findTaskById(taskId);
    }

    /**
     * 查找回退的目的节点.
     */
    public ActivityImpl findTargetActivity(CommandContext commandContext) {
        if (activityId == null) {
            final String historyActivityId = findNearestUserTask(commandContext);
            if (StringUtils.isBlank(historyActivityId)) {
                logger.error("上一个人工任务节点ID:{}", historyActivityId);
                throw new RuntimeException("上一个人工任务节点不存在，无法进行回退");
            }
            this.activityId = historyActivityId;
        }
        final String processDefinitionId = taskEntity.getProcessDefinitionId();
        final ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
            processDefinitionId).execute(commandContext);
        return processDefinitionEntity.findActivity(activityId);
    }

    /**
     * 判断想要回退的目标节点和当前节点是否在一个分支上.
     */
    public boolean isSameBranch(HistoricTaskInstanceEntity historicTaskInstanceEntity) {
        return taskEntity.getExecutionId().equals(historicTaskInstanceEntity.getExecutionId());
    }

    /**
     * 回退到userTask.
     */
    public Integer rollbackUserTask(CommandContext commandContext) {
        //是否退回指定节点
        boolean hasActivityId = false;
        if (StringUtils.isNotBlank(activityId)) {
            hasActivityId = true;
        }
        // 找到想要回退到的节点
        final ActivityImpl targetActivity = findTargetActivity(commandContext);
        // 找到想要回退对应的节点历史
        final HistoricActivityInstanceEntity historicActivityInstanceEntity = findTargetHistoricActivity(
            commandContext, targetActivity);
        logger.info("历史节点实例 : {}", historicActivityInstanceEntity.getId());
        final Graph graph = new ActivitiGraphBuilder(taskEntity.getProcessDefinitionId()).build();
        final Node node = graph.findById(targetActivity.getId());
        //自由跳过来的回退不需要根据线去判断是否可以回退
        if (hasActivityId) {
            //不做操作
        } else {
            if (!checkCouldRollback(node)) {
                logger.info("不能回退 {}", taskId);
                return 2;
            }
        }
        //跳转后不可撤回
        final TaskInfoManager taskInfoManager = ApplicationContextHelper.getBean(TaskInfoManager.class);
        taskInfoManager.updateTaskInfoWithdrawStatusByExecutionId(taskEntity.getExecutionId(),
            WorkflowConstants.NOT);
        final TaskInfoRunManager taskInfoRunManager = ApplicationContextHelper
            .getBean(TaskInfoRunManager.class);
        taskInfoRunManager.updateTaskInfoRunWithdrawStatusByExecutionId(taskEntity.getExecutionId(),
            WorkflowConstants.NOT);
        // 找到想要回退对应的任务历史
        final HistoricTaskInstanceEntity historicTaskInstanceEntity = findTargetHistoricTask(commandContext,
            targetActivity);
        if (this.isSameBranch(historicTaskInstanceEntity)) {
            // 如果退回的目标节点的executionId与当前task的executionId一样，说明是同一个分支
            // 只删除当前分支的task
            this.deleteActiveTask(taskEntity);
        } else {
            // 否则认为是从分支跳回主干
            // 删除所有活动中的task
            this.deleteActiveTasks(historicTaskInstanceEntity.getProcessInstanceId());
            // 获得期望退回的节点后面的所有节点历史
            final List<String> historyNodeIds = new ArrayList<String>();
            collectNodes(node, historyNodeIds);
        }

        // 处理多实例
        this.processMultiInstance();
        // 恢复期望退回的任务和历史 multiInstance=parallel
        final Map<String, Object> preNode = targetActivity.getProperties();

        if ("parallel".equals(preNode.get("multiInstance"))) {
            final ExecutionEntityManager executionEntityManager = commandContext
                .getExecutionEntityManager();
            // 获取当前流程的executionId，因为在并发的情况下executionId是唯一的。
            final ExecutionEntity executionEntity = executionEntityManager
                .findExecutionById(taskEntity.getExecutionId());
            final ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
            // 根据executionId 获取Task
            final Iterator<TaskEntity> localIterator = Context.getCommandContext().getTaskEntityManager()
                .findTasksByExecutionId(taskEntity.getExecutionId()).iterator();
            while (localIterator.hasNext()) {
                //TaskEntity taskEntity2 = (TaskEntity) localIterator.next();
                // 触发任务监听
                taskEntity.fireEvent("complete");
                // 删除任务的原因
                commandContext.getTaskEntityManager().deleteTask(taskEntity, "completed", false);
                //更新T_WF_ACT_HI_ACTINST
                final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
                    .getJdbcTemplate();
                final List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "SELECT * FROM T_WF_ACT_HI_ACTINST WHERE TASK_ID_=? AND END_TIME_ IS NULL",
                    taskEntity.getId());
                final Date now = new Date();
                for (final Map<String, Object> map : list) {
                    final Date startTime = (Date) map.get("START_TIME_");
                    final long duration = now.getTime() - startTime.getTime();
                    jdbcTemplate.update(
                        "UPDATE T_WF_ACT_HI_ACTINST SET END_TIME_=?,DURATION_=? WHERE ID_=?", now,
                        duration, map.get("ID_"));
                }

            }
            final ActivityImpl activity = processDefinition.findActivity(activityId);

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_CALL_ACTIVITY
                .equals(activity.getProperty("type"))) { //判断被调用活动是否激活
                final ActivityBehavior activityBehavior = activity.getActivityBehavior();
                //调用活动。不知为何，只能判断当前流程调用的流程是否激活或者挂起
                if (activityBehavior instanceof CallActivityBehavior) {
                    final String processDefinitonKey = ((CallActivityBehavior) activityBehavior)
                        .getProcessDefinitonKey();
                    final ProcessDefinitionQuery definitionQuery = Context.getCommandContext()
                        .getProcessEngineConfiguration().getRepositoryService()
                        .createProcessDefinitionQuery();
                    //取激活的流程模型版本
                    final ProcessDefinition definitionEntity = definitionQuery
                        .processDefinitionKey(processDefinitonKey).active().singleResult();
                    if (definitionEntity == null) {
                        throw new RuntimeException("被调用的流程不存在，请检查流程配置是否有误");
                    }
                    if (definitionEntity.isSuspended()) {
                        throw new RuntimeException("被调用的流程被挂起，请检查流程配置是否有误");
                    }
                }
            }
            if (activity.isScope()) {
                final ExecutionEntity parentExecution = executionEntity.createExecution();
                parentExecution.setActivity(activity);
                parentExecution.executeActivity(activity);
            } else {
                executionEntity.executeActivity(activity);
            }
        } else {
            this.processHistoryTask(commandContext, historicTaskInstanceEntity,
                historicActivityInstanceEntity);

        }

        logger.info("回退  {}", historicTaskInstanceEntity.getName());

        return 0;
    }

    /**
     * 找到想要回退对应的节点历史.
     */
    public HistoricActivityInstanceEntity findTargetHistoricActivity(CommandContext commandContext,
        ActivityImpl activityImpl) {
        final HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
        historicActivityInstanceQueryImpl.activityId(activityImpl.getId());
        historicActivityInstanceQueryImpl.processInstanceId(taskEntity.getProcessInstanceId());
        historicActivityInstanceQueryImpl.orderByHistoricActivityInstanceEndTime().desc();
        final HistoricActivityInstanceEntity historicActivityInstanceEntity = (HistoricActivityInstanceEntity) commandContext
            .getHistoricActivityInstanceEntityManager()
            .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl,
                new Page(0, 1))
            .get(0);
        return historicActivityInstanceEntity;
    }

    /**
     * 找到想要回退对应的节点历史.
     */
    public List<HistoricActivityInstanceEntity> findTargetHistoricActivityList(
        CommandContext commandContext, TaskEntity taskEntity, ActivityImpl activityImpl) {
        final HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
        historicActivityInstanceQueryImpl.activityId(activityImpl.getId());
        historicActivityInstanceQueryImpl.processInstanceId(taskEntity.getProcessInstanceId());
        historicActivityInstanceQueryImpl.orderByHistoricActivityInstanceEndTime().desc();

        final List<HistoricActivityInstance> historicActivityInstanceEntity = (List<HistoricActivityInstance>) commandContext
            .getHistoricActivityInstanceEntityManager()
            .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl,
                new Page(0, 100));
        final List<HistoricActivityInstanceEntity> resultList = new ArrayList<HistoricActivityInstanceEntity>();
        if (historicActivityInstanceEntity.size() > 0) {
            for (final HistoricActivityInstance historicActivityInstance : historicActivityInstanceEntity) {
                resultList.add((HistoricActivityInstanceEntity) historicActivityInstance);
            }
        }
        return resultList;
    }

    /**
     * 找到想要回退对应的任务历史.
     */
    public HistoricTaskInstanceEntity findTargetHistoricTask(CommandContext commandContext,
        ActivityImpl activityImpl) {
        final HistoricTaskInstanceQueryImpl historicTaskInstanceQueryImpl = new HistoricTaskInstanceQueryImpl();
        historicTaskInstanceQueryImpl.taskDefinitionKey(activityImpl.getId());
        historicTaskInstanceQueryImpl.processInstanceId(taskEntity.getProcessInstanceId());
        historicTaskInstanceQueryImpl.setFirstResult(0);
        historicTaskInstanceQueryImpl.setMaxResults(1);
        historicTaskInstanceQueryImpl.orderByHistoricTaskInstanceEndTime().desc();
        final HistoricTaskInstanceEntity historicTaskInstanceEntity = (HistoricTaskInstanceEntity) commandContext
            .getHistoricTaskInstanceEntityManager()
            .findHistoricTaskInstancesByQueryCriteria(historicTaskInstanceQueryImpl).get(0);
        return historicTaskInstanceEntity;
    }

    /**
     * 找到想要回退对应的任务历史.
     */
    public List<HistoricTaskInstanceEntity> findTargetHistoricTaskList(
        CommandContext commandContext, TaskEntity taskEntity, ActivityImpl activityImpl) {
        final HistoricTaskInstanceQueryImpl historicTaskInstanceQueryImpl = new HistoricTaskInstanceQueryImpl();
        historicTaskInstanceQueryImpl.taskDefinitionKey(activityImpl.getId());
        historicTaskInstanceQueryImpl.processInstanceId(taskEntity.getProcessInstanceId());
        historicTaskInstanceQueryImpl.setFirstResult(0);
        historicTaskInstanceQueryImpl.setMaxResults(100);
        historicTaskInstanceQueryImpl.orderByTaskCreateTime().desc();

        final List<HistoricTaskInstance> list = (List<HistoricTaskInstance>) commandContext
            .getHistoricTaskInstanceEntityManager()
            .findHistoricTaskInstancesByQueryCriteria(historicTaskInstanceQueryImpl);
        final List<HistoricTaskInstanceEntity> resultList = new ArrayList<HistoricTaskInstanceEntity>();
        if (list.size() > 0) {
            for (final HistoricTaskInstance historicTaskInstance : list) {
                resultList.add((HistoricTaskInstanceEntity) historicTaskInstance);
            }
        }
        return resultList;
    }

    /**
     * 查找离当前节点最近的上一个走过的userTask的code.
     *
     */
    public String findNearestUserTask(CommandContext commandContext) {
        if (taskEntity == null) {
            logger.debug("无法找到任务 : {}", taskId);
            return null;
        }
        final Graph graph = new ActivitiGraphBuilder(taskEntity.getProcessDefinitionId()).build();
        final Node node = graph.findById(taskEntity.getTaskDefinitionKey());
        Set<String> previousHistoricActivityInstanceIdSet = new HashSet<String>();
        //根据流程图去当前节点的所有上一个人工节点，如果是排他网关的情况下，递归向上查找
        previousHistoricActivityInstanceIdSet = findIncomingNodeList(graph, node,
            previousHistoricActivityInstanceIdSet);
        if (previousHistoricActivityInstanceIdSet.size() == 0) {
            throw new ResultNullException("回退上一步失败");
        }
        String historyActivityId = "";
        //拿到获取到的activityId和流程实例id去查询历史表 看看哪个节点走过
        //如果都走过的情况下 去根据时间去判断 完成时间非空且最新的是我们想要的结果
        final StringBuffer sb = new StringBuffer();
        sb.append("SELECT ACT_ID_ FROM T_WF_ACT_HI_ACTINST WHERE ACT_ID_ in (");
        for (final String previousHistoricActivityInstanceId : previousHistoricActivityInstanceIdSet) {
            sb.append("'" + previousHistoricActivityInstanceId + "',");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append(")");
        sb.append(" and PROC_INST_ID_ = " + taskEntity.getProcessInstanceId()
            + " order by END_TIME_ desc");
        final List<HistoricActivityInstance> list = commandContext.getProcessEngineConfiguration()
            .getHistoryService().createNativeHistoricActivityInstanceQuery().sql(sb.toString())
            .list();
        if (list.get(0) != null) {
            historyActivityId = list.get(0).getActivityId();
        }
        return historyActivityId;
    }

    /**
     * 查找进入的连线有几个 返回list
     */
    public Set<String> findIncomingNodeList(Graph graph, Node node, Set<String> previousList) {
        for (final Edge edge : graph.getEdges()) {
            final Node src = edge.getSrc();
            final Node dest = edge.getDest();
            final String srcType = src.getType();
            if (!dest.getId().equals(node.getId())) {
                continue;
            }
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(srcType)) {
                previousList.add(src.getId());
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY
                .equals(srcType)) {
                previousList = findIncomingNodeList(graph, src, previousList);
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(srcType)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY.equals(srcType)) {
                throw new ParallelOrInclusiveException("上一节点是并行网关或者包含网关，不能退回!");
            } else {
                logger.info(
                    "无法回退，前序节点不是用户任务 : " + src.getId() + " " + srcType + "(" + src.getName() + ")");
                return null;
            }
        }
        if (previousList.size() > 0) {
            return previousList;
        } else {
            logger.info(
                "无法回退，节点 : " + node.getId() + " " + node.getType() + "(" + node.getName() + ")");
            return null;
        }

    }

    /**
     * 查找进入的连线.
     */
    public String findIncomingNode(Graph graph, Node node) {
        for (final Edge edge : graph.getEdges()) {
            final Node src = edge.getSrc();
            final Node dest = edge.getDest();
            final String srcType = src.getType();

            if (!dest.getId().equals(node.getId())) {
                continue;
            }

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(srcType)) {
                return src.getId();
            } else if (srcType.endsWith("Gateway")) {
                return this.findIncomingNode(graph, src);
            } else {
                logger.info(
                    "无法回退，前序节点不是用户任务 : " + src.getId() + " " + srcType + "(" + src.getName() + ")");

                return null;
            }
        }

        logger
            .info("无法回退，节点 : " + node.getId() + " " + node.getType() + "(" + node.getName() + ")");

        return null;
    }

    /**
     * 查询历史节点.
     */
    public HistoricActivityInstanceEntity getHistoricActivityInstanceEntity(String historyTaskId) {
        logger.info("历史任务id : {}", historyTaskId);

        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final String historicActivityInstanceId = jdbcTemplate.queryForObject(
            "SELECT ID_ FROM T_WF_ACT_HI_ACTINST WHERE TASK_ID_=?", String.class, historyTaskId);
        logger.info("historicActivityInstanceId : {}", historicActivityInstanceId);

        final HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
        historicActivityInstanceQueryImpl.activityInstanceId(historicActivityInstanceId);

        final HistoricActivityInstanceEntity historicActivityInstanceEntity = (HistoricActivityInstanceEntity) Context
            .getCommandContext().getHistoricActivityInstanceEntityManager()
            .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl,
                new Page(0, 1))
            .get(0);

        return historicActivityInstanceEntity;
    }

    /**
     * 判断是否可回退.
     */
    public boolean checkCouldRollback(Node node) {
        // TODO: 如果是catchEvent，也应该可以退回，到时候再说
        for (final Edge edge : node.getOutgoingEdges()) {
            final Node dest = edge.getDest();
            final String type = dest.getType();

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(type)) {
                if (!dest.isActive()) {
                    return true;
                }
            } else if (type.endsWith("Gateway")) {
                return checkCouldRollback(dest);
            } else {
                logger.info("无法回退， " + type + "(" + dest.getName() + ") 已经完成");

                return false;
            }
        }

        return true;
    }

    /**
     * 删除活动状态任务.
     */
    public void deleteActiveTasks(String processInstanceId) {
        final List<TaskEntity> taskEntities = Context.getCommandContext().getTaskEntityManager()
            .findTasksByProcessInstanceId(processInstanceId);

        for (final TaskEntity taskEntity : taskEntities) {
            this.deleteActiveTask(taskEntity);
        }
    }

    /**
     * 遍历节点.
     */
    public void collectNodes(Node node, List<String> historyNodeIds) {
        logger.info("节点 : {}, {}, {}", node.getId(), node.getType(), node.getName());

        for (final Edge edge : node.getOutgoingEdges()) {
            logger.info("迁移线 : {}", edge.getName());

            final Node dest = edge.getDest();
            historyNodeIds.add(dest.getId());
            collectNodes(dest, historyNodeIds);
        }
    }

    /**
     * 根据任务历史，创建待办任务.
     */
    public void processHistoryTask(CommandContext commandContext,
        HistoricTaskInstanceEntity historicTaskInstanceEntity,
        HistoricActivityInstanceEntity historicActivityInstanceEntity) {
        if (this.userId == null) {
            if (this.useLastAssignee) {
                this.userId = historicTaskInstanceEntity.getAssignee();

            } else {
                final String processDefinitionId = taskEntity.getProcessDefinitionId();
                final ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
                    processDefinitionId)
                        .execute(commandContext);
                final TaskDefinition taskDefinition = processDefinitionEntity.getTaskDefinitions()
                    .get(historicTaskInstanceEntity.getTaskDefinitionKey());
                if (taskDefinition == null) {
                    final String message = "无法找到任务定义 : "
                        + historicTaskInstanceEntity.getTaskDefinitionKey();
                    logger.info(message);
                    throw new IllegalStateException(message);
                }

                if (taskDefinition.getAssigneeExpression() != null) {
                    logger.info("处理人表达式为null : {}", taskDefinition.getKey());
                    this.userId = (String) taskDefinition.getAssigneeExpression()
                        .getValue(taskEntity);
                }
            }
        }
        // 创建新任务
        final TaskEntity task = TaskEntity.create(new Date());
        final TaskDefinitionConnector taskDefinitionConnector = ApplicationContextHolder.getInstance()
            .getApplicationContext().getBean(TaskDefinitionConnector.class);
        final com.cesgroup.spi.humantask.FormDTO taskFormDto = taskDefinitionConnector.findForm(
            historicTaskInstanceEntity.getTaskDefinitionKey(),
            historicTaskInstanceEntity.getProcessDefinitionId());
        if (taskFormDto != null) {
            task.setFormKey(taskFormDto.getKey());
        }
        task.setProcessDefinitionId(historicTaskInstanceEntity.getProcessDefinitionId());
        // task.setId(historicTaskInstanceEntity.getId());
        task.setAssigneeWithoutCascade(userId);
        task.setParentTaskIdWithoutCascade(historicTaskInstanceEntity.getParentTaskId());
        task.setNameWithoutCascade(historicTaskInstanceEntity.getName());
        task.setTaskDefinitionKey(historicTaskInstanceEntity.getTaskDefinitionKey());
        task.setExecutionId(historicTaskInstanceEntity.getExecutionId());
        task.setPriority(historicTaskInstanceEntity.getPriority());
        task.setProcessInstanceId(historicTaskInstanceEntity.getProcessInstanceId());
        task.setExecutionId(historicTaskInstanceEntity.getExecutionId());
        task.setDescriptionWithoutCascade(historicTaskInstanceEntity.getDescription());
        task.setTenantId(historicTaskInstanceEntity.getTenantId());

        Context.getCommandContext().getTaskEntityManager().insert(task);

        // 把流程指向任务对应的节点
        final ExecutionEntity executionEntity = Context.getCommandContext().getExecutionEntityManager()
            .findExecutionById(historicTaskInstanceEntity.getExecutionId());
        //todo:当上一节点为并行会签时，无法根据executionId查询到ExecutionEntity
        if (executionEntity == null) {
            //不做操作
        }
        executionEntity.setActivity(getActivity(historicActivityInstanceEntity));

        // 创建HistoricActivityInstance
        Context.getCommandContext().getHistoryManager().recordActivityStart(executionEntity);

        // 创建HistoricTaskInstance
        Context.getCommandContext().getHistoryManager().recordTaskCreated(task, executionEntity);
        Context.getCommandContext().getHistoryManager().recordTaskId(task);
        // 更新ACT_HI_ACTIVITY里的assignee字段
        Context.getCommandContext().getHistoryManager().recordTaskAssignment(task);

        try {
            // humanTask
            this.createHumanTask(task, historicTaskInstanceEntity);
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * 获得历史节点对应的节点信息.
     */
    public ActivityImpl getActivity(HistoricActivityInstanceEntity historicActivityInstanceEntity) {
        final ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
            historicActivityInstanceEntity.getProcessDefinitionId())
                .execute(Context.getCommandContext());

        return processDefinitionEntity.findActivity(historicActivityInstanceEntity.getActivityId());
    }

    /**
     * 删除未完成任务.
     */
    public void deleteActiveTask(TaskEntity taskEntity) {
        final ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
            taskEntity.getProcessDefinitionId()).execute(Context.getCommandContext());
        final ActivityImpl activityImpl = processDefinitionEntity
            .findActivity(taskEntity.getTaskDefinitionKey());
        if (this.isMultiInstance(activityImpl)) {
            logger.info("{} 是多实例任务", taskEntity.getId());
            this.multiInstanceExecutionIds.add(taskEntity.getExecution().getParent().getId());
            logger.info("追加 : {}", taskEntity.getExecution().getParent().getId());
        }
        Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity, "rollback",
            false);
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final List<Map<String, Object>> list = jdbcTemplate.queryForList(
            "SELECT * FROM T_WF_ACT_HI_ACTINST WHERE TASK_ID_=? AND END_TIME_ IS NULL", taskId);
        final Date now = new Date();
        for (final Map<String, Object> map : list) {
            final Date startTime = (Date) map.get("START_TIME_");
            final long duration = now.getTime() - startTime.getTime();
            jdbcTemplate.update(
                "UPDATE T_WF_ACT_HI_ACTINST SET END_TIME_=?,DURATION_=? WHERE ID_=?", now, duration,
                map.get("ID_"));
        }
        jdbcTemplate.update("UPDATE t_wf_task_info SET COMPLETE_TIME=?,STATUS=? WHERE TASK_ID=?",
            now, "rollback", this.taskId);
    }

    /**
     * 判断跳过节点.
     */
    public boolean isSkipActivity(String historyActivityId) {
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final String historyTaskId = jdbcTemplate.queryForObject(
            "SELECT TASK_ID_ FROM T_WF_ACT_HI_ACTINST WHERE ID_=?", String.class,
            historyActivityId);

        final HistoricTaskInstanceEntity historicTaskInstanceEntity = Context.getCommandContext()
            .getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(historyTaskId);
        final String deleteReason = historicTaskInstanceEntity.getDeleteReason();

        return "skip".equals(deleteReason);
    }

    /**
     * 创建humanTask.
     */
    public HumanTaskDTO createHumanTask(DelegateTask delegateTask,
        HistoricTaskInstanceEntity historicTaskInstanceEntity) throws Exception {
        final HumanTaskConnector humanTaskConnector = ApplicationContextHelper
            .getBean(HumanTaskConnector.class);
        HumanTaskDTO humanTaskDto = new HumanTaskBuilder().setDelegateTask(delegateTask).build();

        /*if ("发起流程".equals(historicTaskInstanceEntity.getDeleteReason())) {
            humanTaskDto.setCatalog(WorkflowConstants.HumanTaskConstants.CATALOG_START);
        }*/

        final HistoricProcessInstance historicProcessInstance = Context.getCommandContext()
            .getHistoricProcessInstanceEntityManager()
            .findHistoricProcessInstance(delegateTask.getProcessInstanceId());
        humanTaskDto.setProcessStarter(historicProcessInstance.getStartUserId());
        humanTaskDto = humanTaskConnector.saveHumanTask(humanTaskDto, false);

        return humanTaskDto;
    }

    /**
     * 判断是否会签.
     */
    public boolean isMultiInstance(PvmActivity pvmActivity) {
        return pvmActivity.getProperty("multiInstance") != null;
    }

    /**
     * 处理多实例.
     */
    public void processMultiInstance() {
        logger.info("multiInstanceExecutionIds : {}", multiInstanceExecutionIds);

        for (final String executionId : multiInstanceExecutionIds) {
            final ExecutionEntity parent = Context.getCommandContext().getExecutionEntityManager()
                .findExecutionById(executionId);
            final List<ExecutionEntity> children = Context.getCommandContext().getExecutionEntityManager()
                .findChildExecutionsByParentExecutionId(parent.getId());
            for (final ExecutionEntity executionEntity : children) {
                executionEntity.remove();
            }

            parent.remove();
        }
    }
}
