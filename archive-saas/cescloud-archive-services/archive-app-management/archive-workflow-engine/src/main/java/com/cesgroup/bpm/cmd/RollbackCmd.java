package com.cesgroup.bpm.cmd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cesgroup.api.humantask.HumanTaskConnector;
import com.cesgroup.api.humantask.HumanTaskDTO;
import com.cesgroup.bpm.graph.ActivitiHistoryGraphBuilder;
import com.cesgroup.bpm.graph.Edge;
import com.cesgroup.bpm.graph.Graph;
import com.cesgroup.bpm.graph.Node;
import com.cesgroup.bpm.support.HumanTaskBuilder;
import com.cesgroup.bpm.support.JumpInfo;
import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;

/**
 * 回退
 *
 * @author 国栋
 *
 */
public class RollbackCmd implements Command<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(RollbackCmd.class);

    /** ACTIVITY_PREVIOUS */
    public static final String ACTIVITY_PREVIOUS = "ACTIVITY_PREVIOUS";

    /** ASSIGNEE_LAST */
    public static final String ASSIGNEE_LAST = "ASSIGNEE_LAST";

    /** ASSIGNEE_AUTO */
    public static final String ASSIGNEE_AUTO = "ASSIGNEE_AUTO";

    private final String taskId;

    private final String activityId;

    private final String userId;

    private final JumpInfo jumpInfo = new JumpInfo();

    /**
     * 创建退回命令
     *
     * @param taskId
     *            这个taskId是运行阶段task的id.
     * @param activityId
     *            节点id
     * @param userId
     *            用户id
     */
    public RollbackCmd(String taskId, String activityId, String userId) {
        this.taskId = taskId;
        this.activityId = activityId;
        this.userId = userId;
    }

    /**
     * 初始化源
     */
    public void initSource() {
        // source task
        jumpInfo.setSourceTaskId(taskId);

        final TaskEntity sourceTask = Context.getCommandContext().getTaskEntityManager()
            .findTaskById(taskId);
        jumpInfo.setSourceTask(sourceTask);

        final ProcessDefinitionEntity processDefinitionEntity = Context.getProcessEngineConfiguration()
            .getDeploymentManager()
            .findDeployedProcessDefinitionById(sourceTask.getProcessDefinitionId());
        // source activity
        jumpInfo.setSourceActivityId(sourceTask.getTaskDefinitionKey());
        jumpInfo.setSourceActivity(
            processDefinitionEntity.findActivity(jumpInfo.getSourceActivityId()));

    }

    /**
     * 退回流程.
     *
     * @return true 成功, false 失败
     */
    @Override
    public Boolean execute(CommandContext commandContext) {
        // 初始化当前的起点信息
        initSource();

        // 找到回退的目标节点
        final String targetActivityId = findTargetActivityId();

        if (targetActivityId == null) {
            logger.info("无法找到跳转目标节点： {}", taskId);

            return Boolean.FALSE;
        }

        /*
         * // 尝试查找最近的上游userTask String historyTaskId =
         * this.findNearestUserTask();
         * logger.info("nearest history user task is : {}", historyTaskId);
         * if (historyTaskId == null) { logger.info("cannot rollback {}",
         * taskId);
         * return "activity"; }
         */

        // 校验这个节点是否可以回退
        final boolean isValid = this.validateTargetActivity(targetActivityId);

        if (!isValid) {
            logger.info("无法回退： {} 到 {}", taskId, targetActivityId);

            return Boolean.FALSE;
        }

        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final String historyTaskId = jdbcTemplate.queryForObject(
            "select id_ from T_WF_ACT_HI_TASKINST where act_id_=? order by END_TIME_ desc",
            String.class, targetActivityId);
        final HistoricTaskInstanceEntity historicTaskInstanceEntity = Context.getCommandContext()
            .getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(historyTaskId);
        final HistoricActivityInstanceEntity historicActivityInstanceEntity = getHistoricActivityInstanceEntity(
            historyTaskId);

        // 开始回退
        if (this.isSameBranch(historicTaskInstanceEntity)) {
            // 如果退回的目标节点的executionId与当前task的executionId一样，说明是同一个分支
            // 只删除当前分支的task
            this.deleteActiveTask();
        } else {
            // 否则认为是从分支跳回主干
            // 删除所有活动中的task
            this.deleteActiveTasks(historicTaskInstanceEntity.getProcessInstanceId());

            // 获得期望退回的节点后面的所有节点历史
            final List<String> historyNodeIds = new ArrayList<String>();
            final Graph graph = new ActivitiHistoryGraphBuilder(
                historicTaskInstanceEntity.getProcessInstanceId()).build();

            final Node node = graph.findById(historicActivityInstanceEntity.getId());
            this.collectNodes(node, historyNodeIds);
            this.deleteHistoryActivities(historyNodeIds);
        }

        // 恢复期望退回的任务和历史
        this.processHistoryTask(historicTaskInstanceEntity, historicActivityInstanceEntity);

        logger.info("activiti 回退 {}", historicTaskInstanceEntity.getName());

        return Boolean.TRUE;
    }

    /**
     * 找到目的地activityId.
     */
    public String findTargetActivityId() {
        if (ACTIVITY_PREVIOUS.equals(this.activityId)) {
            final String taskId = this.findNearestUserTask();
            final TaskEntity taskEntity = Context.getCommandContext().getTaskEntityManager()
                .findTaskById(taskId);

            return taskEntity.getTaskDefinitionKey();
        } else {
            return this.activityId;
        }
    }

    /**
     * 校验目标节点是否可以回退.
     *
     * @param targetActivityId
     *            目标节点id
     * @return boolean
     */
    public boolean validateTargetActivity(String targetActivityId) {
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final String historyTaskId = jdbcTemplate.queryForObject(
            "select id_ from T_WF_ACT_HI_TASKINST where act_id_=? order by END_TIME_ desc",
            String.class, targetActivityId);

        // 先找到历史任务
        final HistoricTaskInstanceEntity historicTaskInstanceEntity = Context.getCommandContext()
            .getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(historyTaskId);

        // 再反向查找历史任务对应的历史节点
        final HistoricActivityInstanceEntity historicActivityInstanceEntity = getHistoricActivityInstanceEntity(
            historyTaskId);

        logger.info("历史节点实例： {}", historicActivityInstanceEntity.getId());

        final Graph graph = new ActivitiHistoryGraphBuilder(
            historicTaskInstanceEntity.getProcessInstanceId()).build();

        final Node node = graph.findById(historicActivityInstanceEntity.getId());

        if (!this.checkCouldRollback(node)) {
            logger.info("无法回退： {}", taskId);

            return false;
        }

        return true;
    }

    /**
     * @param historicTaskInstanceEntity
     *            历史任务实例
     * @return boolean
     */
    public boolean isSameBranch(HistoricTaskInstanceEntity historicTaskInstanceEntity) {
        final TaskEntity taskEntity = Context.getCommandContext().getTaskEntityManager()
            .findTaskById(taskId);

        return taskEntity.getExecutionId().equals(historicTaskInstanceEntity.getExecutionId());
    }

    /**
     *
     * @return taskId
     */
    public String findNearestUserTask() {
        final TaskEntity taskEntity = Context.getCommandContext().getTaskEntityManager()
            .findTaskById(taskId);

        if (taskEntity == null) {
            logger.debug("无法找到任务： {}", taskId);

            return null;
        }

        final Graph graph = new ActivitiHistoryGraphBuilder(taskEntity.getProcessInstanceId()).build();
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final String historicActivityInstanceId = jdbcTemplate.queryForObject(
            "select id_ from T_WF_ACT_HI_ACTINST where task_id_=?", String.class, taskId);
        final Node node = graph.findById(historicActivityInstanceId);

        final String previousHistoricActivityInstanceId = this.findIncomingNode(graph, node);

        if (previousHistoricActivityInstanceId == null) {
            logger.debug("无法找到前置历史节点实例： {}", taskEntity);

            return null;
        }

        return jdbcTemplate.queryForObject("select task_id_ from T_WF_ACT_HI_ACTINST where id_=?",
            String.class, previousHistoricActivityInstanceId);
    }

    /**
     *
     * @param graph
     *            流程图
     * @param node
     *            节点信息
     * @return taskId
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
                final boolean isSkip = isSkipActivity(src.getId());

                if (isSkip) {
                    return this.findIncomingNode(graph, src);
                } else {
                    return src.getId();
                }
            } else if (srcType.endsWith("Gateway")) {
                return this.findIncomingNode(graph, src);
            } else {
                logger.info(
                    "无法回退，前置节点不是人工节点： " + src.getId() + " " + srcType + "(" + src.getName() + ")");

                return null;
            }
        }

        logger.info("无法回退： : " + node.getId() + " " + node.getType() + "(" + node.getName() + ")");

        return null;
    }

    /**
     *
     * @param historyTaskId
     *            历史任务ID
     * @return HistoricActivityInstanceEntity
     */
    public HistoricActivityInstanceEntity getHistoricActivityInstanceEntity(String historyTaskId) {
        logger.info("historyTaskId : {}", historyTaskId);

        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final String historicActivityInstanceId = jdbcTemplate.queryForObject(
            "select id_ from T_WF_ACT_HI_ACTINST where task_id_=?", String.class, historyTaskId);
        logger.info("historicActivityInstanceId : {}", historicActivityInstanceId);

        final HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
        historicActivityInstanceQueryImpl.activityInstanceId(historicActivityInstanceId);

        final HistoricActivityInstanceEntity historicActivityInstanceEntity = (HistoricActivityInstanceEntity) Context
            .getCommandContext()
            .getHistoricActivityInstanceEntityManager()
            .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl,
                new Page(0, 1))
            .get(0);

        return historicActivityInstanceEntity;
    }

    /**
     * 检查此节点是否可以回退
     *
     * @param node
     *            节点信息
     * @return boolean
     */
    public boolean checkCouldRollback(Node node) {
        // TODO: 如果是catchEvent，也应该可以退回，到时候再说
        for (final Edge edge : node.getOutgoingEdges()) {
            final Node dest = edge.getDest();
            final String type = dest.getType();

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(type)) {
                if (!dest.isActive()) {
                    final boolean isSkip = isSkipActivity(dest.getId());

                    if (isSkip) {
                        return checkCouldRollback(dest);
                    } else {
                        // logger.info("cannot rollback, " + type + "("
                        // + dest.getName() + ") is complete.");
                        // return false;
                        return true;
                    }
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
     * 删除活动节点
     *
     * @param processInstanceId
     *            流程实例ID
     */
    public void deleteActiveTasks(String processInstanceId) {
        Context.getCommandContext().getTaskEntityManager()
            .deleteTasksByProcessInstanceId(processInstanceId, "退回", false);

        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final List<Map<String, Object>> list = jdbcTemplate.queryForList(
            "select * from T_WF_ACT_HI_ACTINST where proc_inst_id_=? and end_time_ is null",
            processInstanceId);
        final Date now = new Date();

        for (final Map<String, Object> map : list) {
            final Date startTime = (Date) map.get("start_time_");
            final long duration = now.getTime() - startTime.getTime();
            jdbcTemplate.update(
                "update T_WF_ACT_HI_ACTINST set end_time_=?,duration_=? where id_=?", now, duration,
                map.get("id_"));
        }
    }

    /**
     *
     * @param node
     *            节点信息
     * @param historyNodeIds
     *            结果集
     */
    public void collectNodes(Node node, List<String> historyNodeIds) {
        logger.info("节点 : {}, {}, {}", node.getId(), node.getType(), node.getName());

        for (final Edge edge : node.getOutgoingEdges()) {
            logger.info("迁移线：: {}", edge.getName());

            final Node dest = edge.getDest();
            historyNodeIds.add(dest.getId());
            collectNodes(dest, historyNodeIds);
        }
    }

    /**
     *
     * @param historyNodeIds
     *            历史节点集合
     */
    public void deleteHistoryActivities(List<String> historyNodeIds) {
        /*
         * JdbcTemplate jdbcTemplate = ApplicationContextHelper
         * .getBean(JdbcTemplate.class);
         * logger.info("historyNodeIds : {}", historyNodeIds);
         * for (String id : historyNodeIds) {
         * jdbcTemplate.update("delete from ACT_HI_ACTINST where id_=?", id); }
         */
    }

    /**
     * 根据任务历史，创建待办任务.
     */
    public void processHistoryTask(HistoricTaskInstanceEntity historicTaskInstanceEntity,
        HistoricActivityInstanceEntity historicActivityInstanceEntity) {
        /*
         * historicTaskInstanceEntity.setEndTime(null);
         * historicTaskInstanceEntity.setDurationInMillis(null);
         * historicActivityInstanceEntity.setEndTime(null);
         * historicActivityInstanceEntity.setDurationInMillis(null);
         */

        // 创建新任务
        final TaskEntity task = TaskEntity.create(new Date());
        task.setProcessDefinitionId(historicTaskInstanceEntity.getProcessDefinitionId());
        // task.setId(historicTaskInstanceEntity.getId());
        // task.setAssigneeWithoutCascade(historicTaskInstanceEntity.getAssignee());
        task.setAssigneeWithoutCascade(this.userId);
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
     * 获取节点信息
     *
     * @param historicActivityInstanceEntity
     *            历史节点信息
     * @return ActivityImpl
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
    public void deleteActiveTask() {
        final TaskEntity taskEntity = Context.getCommandContext().getTaskEntityManager()
            .findTaskById(this.taskId);
        Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity, "回退", false);

        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final List<Map<String, Object>> list = jdbcTemplate.queryForList(
            "select * from T_WF_ACT_HI_ACTINST where task_id_=? and end_time_ is null", taskId);
        final Date now = new Date();

        for (final Map<String, Object> map : list) {
            final Date startTime = (Date) map.get("start_time_");
            final long duration = now.getTime() - startTime.getTime();
            jdbcTemplate.update(
                "update T_WF_ACT_HI_ACTINST set end_time_=?,duration_=? where id_=?", now, duration,
                map.get("id_"));
        }

        // 处理humanTask
        final HumanTaskConnector humanTaskConnector = ApplicationContextHelper
            .getBean(HumanTaskConnector.class);
        final HumanTaskDTO humanTaskDto = humanTaskConnector.findHumanTaskByTaskId(this.taskId);
        humanTaskDto.setCompleteTime(new Date());
        humanTaskDto.setDuration(String.valueOf(
            humanTaskDto.getCompleteTime().getTime() - humanTaskDto.getCreateTime().getTime()));
        humanTaskDto.setStatus("rollback");
        humanTaskConnector.saveHumanTask(humanTaskDto);
    }

    /**
     * 判断跳过节点.
     */
    public boolean isSkipActivity(String historyActivityId) {
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final String historyTaskId = jdbcTemplate.queryForObject(
            "select task_id_ from T_WF_ACT_HI_ACTINST where id_=?", String.class,
            historyActivityId);

        final HistoricTaskInstanceEntity historicTaskInstanceEntity = Context.getCommandContext()
            .getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(historyTaskId);
        final String deleteReason = historicTaskInstanceEntity.getDeleteReason();

        return "skip".equals(deleteReason);
    }

    /**
     * 创建任务
     *
     * @param delegateTask
     *            任务
     * @param historicTaskInstanceEntity
     *            历史任务
     * @return HumanTaskDTO
     * @throws Exception
     *             执行失败抛出异常
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
        humanTaskDto = humanTaskConnector.saveHumanTask(humanTaskDto);

        return humanTaskDto;
    }
}
