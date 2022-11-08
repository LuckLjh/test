package com.cesgroup.bpm.cmd;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

/**
 * 撤销任务命令
 *
 * @author 国栋
 *
 */
public class WithdrawTaskCmd implements Command<Integer> {

    private static Logger logger = LoggerFactory.getLogger(WithdrawTaskCmd.class);

    private final String historyTaskId;

    private final String humanTaskId;

    private Map<String, String> taskParameters;

    /**
     * 这个historyTaskId是已经完成的一个任务的id.
     */
    public WithdrawTaskCmd(String historyTaskId, String humanTaskId,
        Map<String, String> taskParameters) {
        this.historyTaskId = historyTaskId;
        this.humanTaskId = humanTaskId;
        this.taskParameters = taskParameters;
    }

    /**
     * 撤销流程.
     *
     * @return 0-撤销成功 1-流程结束 2-下一结点已经通过,不能撤销
     */
    @Override
    public Integer execute(CommandContext commandContext) {
        //根据taskInfo中attr4字段进行判断，只有1为可以撤销
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final String attr4 = (String) jdbcTemplate.queryForObject(
            "select attr4 from T_WF_TASK_INFO where id=?", String.class, humanTaskId);
        if (null == attr4 || !WorkflowConstants.YES.equals(attr4)) {
            logger.info("无法撤销 {}", historyTaskId);
            throw new IllegalStateException("该节点无法进行撤回");
        } else {
            //还原当前节点可撤销状态
            final TaskInfoManager taskInfoManager = ApplicationContextHelper
                .getBean(TaskInfoManager.class);
            taskInfoManager.updateTaskInfoWithdrawStatusByHumanTaskId(humanTaskId, WorkflowConstants.NOT);
            final TaskInfoRunManager taskInfoRunManager = ApplicationContextHelper
                .getBean(TaskInfoRunManager.class);
            taskInfoRunManager.updateTaskInfoRunWithdrawStatusByHumanTaskId(humanTaskId, WorkflowConstants.NOT);
        }
        // 获得历史任务
        final HistoricTaskInstanceEntity historicTaskInstanceEntity = Context.getCommandContext()
            .getHistoricTaskInstanceEntityManager().findHistoricTaskInstanceById(historyTaskId);

        // 获得历史节点
        final HistoricActivityInstanceEntity historicActivityInstanceEntity = getHistoricActivityInstanceEntity(
            historyTaskId);

        final Graph graph = new ActivitiGraphBuilder(historicTaskInstanceEntity.getProcessDefinitionId())
            .build();

        final Node node = graph.findById(historicActivityInstanceEntity.getActivityId());

        final List<String> historyNodeIds = new ArrayList<String>();
        if (null != node) {
            // 获得期望撤销的节点后面的所有节点历史
            try {
                final String resultList = "";
                final String nodeList = this.findIncomingNodeList(graph, node, resultList);
                if (StringUtils.isNotBlank(nodeList)) {
                    final String[] nodes = nodeList.split(",");
                    for (final String nodeId : nodes) {
                        historyNodeIds.add(nodeId);
                    }
                }

            } catch (final ParallelOrInclusiveException e) {
                e.printStackTrace();
            }
        }
        //此方式是为了根据Code和流程实例id 查询出当前正在运行的节点Node
        String resultCode = null;
        final String procInstanceId = historicTaskInstanceEntity.getProcessInstanceId();
        if (historyNodeIds.size() > 0) {
            for (final String codeId : historyNodeIds) {
                final List<Map<String, Object>> li = jdbcTemplate.queryForList(
                    "select t.status,t.is_countersign from t_wf_task_info t where "
                        + "t.process_instance_id=? and  t.code=? and t.status='active'",
                    new Object[] { procInstanceId, codeId, WorkflowConstants.HumanTaskConstants.STATUS_ACTIVE });
                if (li.size() > 0) {
                    //判断当前节点是不是会签
                    if (WorkflowConstants.YES
                        .equals(li.get(0).get("IS_COUNTERSIGN"))) {
                        throw new RuntimeException("下一节点是会签节点不能撤回");
                    }
                    resultCode = codeId;
                }
            }
        }

        //如果是跳转的节点，那么历史节点就是跳转后的节点
        final Map<String, Object> map = jdbcTemplate
            .queryForMap("select attr4,code from T_WF_TASK_INFO where id=?", humanTaskId);
        final String code = (String) map.get("code");
        String jumpEndNodeCode = null;
        String jumpStartNodeCode = null;
        if (null == taskParameters) {
            taskParameters = new HashMap<String, String>();
        } else {
            jumpEndNodeCode = taskParameters.get("_jumpEndNodeCode");
            jumpStartNodeCode = taskParameters.get("_jumpStartNodeCode");
        }

        //说明该节点是跳转过来的，回撤需要回撤到原来跳转前的节点
        if (null != jumpStartNodeCode && code.equals(jumpStartNodeCode)) {
            historyNodeIds.clear();
            historyNodeIds.add(jumpEndNodeCode);
            // 恢复期望撤销的任务和历史
            this.processHistoryTask(historicTaskInstanceEntity, historicActivityInstanceEntity);
            // 删除所有活动中的task
            this.deleteActiveTasks(historicTaskInstanceEntity.getProcessInstanceId(),
                historyNodeIds);

            this.deleteHistoryActivities(historyNodeIds,
                historicTaskInstanceEntity.getProcessInstanceId());

        } else {
            // 恢复期望撤销的任务和历史
            this.processHistoryTask(historicTaskInstanceEntity, historicActivityInstanceEntity);
            // 删除所有活动中的task
            this.deleteActiveTasks(historicTaskInstanceEntity.getProcessInstanceId(), resultCode);

            this.deleteHistoryActivities(resultCode,
                historicTaskInstanceEntity.getProcessInstanceId());

        }
        logger.info("撤销节点 {}", historicTaskInstanceEntity.getName());

        return 0;

    }

    /**
     * 获取历史节点实体
     *
     * @param historyTaskId
     *            历史任务ID
     * @return HistoricActivityInstanceEntity
     */
    public HistoricActivityInstanceEntity getHistoricActivityInstanceEntity(String historyTaskId) {
        logger.info("历史任务id : {}", historyTaskId);

        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        final String historicActivityInstanceId = jdbcTemplate.queryForObject(
            "select id_ from T_WF_ACT_HI_ACTINST where task_id_=?", String.class, historyTaskId);
        logger.info("历史节点实例id : {}", historicActivityInstanceId);

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
     * 检测节点是否可以撤回
     *
     * @param node
     *            节点信息
     * @return boolean
     */
    public boolean checkCouldWithdraw(Node node) {
        // TODO: 如果是catchEvent，也应该可以撤销，到时候再说
        for (final Edge edge : node.getOutgoingEdges()) {
            final Node dest = edge.getDest();
            final String type = dest.getType();

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(type)) {
                if (!dest.isActive()) {
                    final boolean isSkip = isSkipActivity(dest.getId());

                    if (isSkip) {
                        return checkCouldWithdraw(dest);
                    } else {
                        logger.info("无法撤销， " + type + "(" + dest.getName() + ") 已经完成");
                        throw new IllegalStateException("无法撤销， 此任务后有节点已经完成");
                        //return false;
                    }
                }
            } else if (type.endsWith(WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS)) {
                logger.info("无法撤销， " + type + "(" + dest.getName() + ") 已经完成");
                throw new IllegalStateException("无法撤销， 此任务后有子流程在进行");
            } else {
                logger.info("无法撤销， " + type + "(" + dest.getName() + ") 已经完成");
                throw new IllegalStateException("无法撤销， 此任务后有节点已经完成");
                //return false;
            }
        }

        return true;
    }

    /**
     * 删除未完成任务.
     */
    public void deleteActiveTasks(String processInstanceId, List<String> historyNodeIds) {
        // humantask
        final HumanTaskConnector humanTaskConnector = ApplicationContextHelper
            .getBean(HumanTaskConnector.class);
        for (final String s : historyNodeIds) {
            //多次撤回可能不止查出一条记录，所以要加上unfinished条件
            final HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
            historicActivityInstanceQueryImpl.activityId(s);
            historicActivityInstanceQueryImpl.processInstanceId(processInstanceId);
            historicActivityInstanceQueryImpl.unfinished();

            final HistoricActivityInstanceEntity haie = (HistoricActivityInstanceEntity) Context
                .getCommandContext().getHistoricActivityInstanceEntityManager()
                .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl,
                    new Page(0, 1))
                .get(0);

            if (haie != null) {
                if (haie.getTaskId() != null) {
                    humanTaskConnector.removeHumanTaskByTaskId(haie.getTaskId());
                }
                Context.getCommandContext().getTaskEntityManager()
                    .deleteTasksByProcessInstanceId(processInstanceId, "被撤回", true);
                //.deleteTasksByProcessInstanceId(processInstanceId, null, true);
                final TaskInfoRunManager taskInfoRunManager = ApplicationContextHelper
                    .getBean(TaskInfoRunManager.class);
                taskInfoRunManager.removeAll(taskInfoRunManager.findBy("taskId", haie.getTaskId()));
            }
        }
    }

    /**
     * 删除活动任务
     *
     * @param processInstanceId
     *            流程实例ID
     * @param historyNodeId
     *            历史节点ID
     */
    public void deleteActiveTasks(String processInstanceId, String historyNodeId) {
        // humantask
        final HumanTaskConnector humanTaskConnector = ApplicationContextHelper
            .getBean(HumanTaskConnector.class);

        //多次撤回可能不止查出一条记录，所以要加上unfinished条件
        final HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
        historicActivityInstanceQueryImpl.activityId(historyNodeId);
        historicActivityInstanceQueryImpl.processInstanceId(processInstanceId);
        historicActivityInstanceQueryImpl.unfinished();
        final HistoricActivityInstanceEntity haie = (HistoricActivityInstanceEntity) Context
            .getCommandContext().getHistoricActivityInstanceEntityManager()
            .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl,
                new Page(0, 1))
            .get(0);
        if (haie != null) {
            if (haie.getTaskId() != null) {
                humanTaskConnector.removeHumanTaskByTaskId(haie.getTaskId());
            }
            Context.getCommandContext().getTaskEntityManager()
                .deleteTasksByProcessInstanceId(processInstanceId, "被撤回", true);
            final TaskInfoRunManager taskInfoRunManager = ApplicationContextHelper
                .getBean(TaskInfoRunManager.class);
            taskInfoRunManager.removeAll(taskInfoRunManager.findBy("taskId", haie.getTaskId()));
        }
        //.deleteTasksByProcessInstanceId(processInstanceId, null, true);
    }

    /**
     * 查找进入的连线有几个 返回list
     */
    public String findIncomingNodeList(Graph graph, Node node, String previousList) {
        for (final Edge edge : graph.getEdges()) {
            //线的起点
            final Node src = edge.getSrc();
            //线的终点
            final Node dest = edge.getDest();
            //终点的类型
            final String srcType = dest.getType();
            //线的起点是当前节点
            if (!src.getId().equals(node.getId())) {
                continue;
            }

            if ("begin".equals(srcType)) {
                continue;
            }

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(srcType)) {
                previousList += dest.getId() + ",";
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY
                .equals(srcType)) {
                return this.findIncomingNodeList(graph, dest, previousList);
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(srcType)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY.equals(srcType)) {
                throw new ParallelOrInclusiveException("上一节点是并行网关或者包含网关，不能退回!");
            } else {
                logger.info(
                    "无法回退，前序节点不是用户任务 : " + src.getId() + " " + srcType + "(" + src.getName() + ")");

                return null;
            }
        }
        if (StringUtils.isNotBlank(previousList)) {
            return previousList;
        } else {
            logger.info(
                "无法回退，节点 : " + node.getId() + " " + node.getType() + "(" + node.getName() + ")");

            return null;
        }

    }

    /**
     * 删除历史节点.
     */
    public void deleteHistoryActivities(List<String> historyNodeIds, String processInstanceId) {
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        logger.info("历史节点id : {}", historyNodeIds);

        for (final String a : historyNodeIds) {
            //多次撤回可能不止查出一条记录，所以要加上unfinished条件
            final HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
            historicActivityInstanceQueryImpl.activityId(a);
            historicActivityInstanceQueryImpl.processInstanceId(processInstanceId);
            historicActivityInstanceQueryImpl.unfinished();

            final HistoricActivityInstanceEntity haie = (HistoricActivityInstanceEntity) Context
                .getCommandContext().getHistoricActivityInstanceEntityManager()
                .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl,
                    new Page(0, 1))
                .get(0);
            if (haie != null) {
                final String taskId = haie.getTaskId();
                if (taskId != null) {
                    Context.getCommandContext().getHistoricTaskInstanceEntityManager()
                        .deleteHistoricTaskInstanceById(taskId);
                }

                jdbcTemplate.update("delete from T_WF_ACT_HI_ACTINST where id_=?", haie.getId());
            }
        }
    }

    /**
     * 删除历史节点.
     */
    public void deleteHistoryActivities(String historyNodeId, String processInstanceId) {
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        logger.info("历史节点id : {}", historyNodeId);

        //多次撤回可能不止查出一条记录，所以要加上unfinished条件
        final HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
        historicActivityInstanceQueryImpl.activityId(historyNodeId);
        historicActivityInstanceQueryImpl.processInstanceId(processInstanceId);
        historicActivityInstanceQueryImpl.unfinished();

        final HistoricActivityInstanceEntity haie = (HistoricActivityInstanceEntity) Context
            .getCommandContext().getHistoricActivityInstanceEntityManager()
            .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl,
                new Page(0, 1))
            .get(0);
        if (haie != null) {
            final String taskId = haie.getTaskId();
            if (taskId != null) {
                Context.getCommandContext().getHistoricTaskInstanceEntityManager()
                    .deleteHistoricTaskInstanceById(taskId);
            }

            jdbcTemplate.update("delete from T_WF_ACT_HI_ACTINST where id_=?", haie.getId());
        }

    }

    /**
     *
     * @param historicTaskInstanceEntity
     *            历史任务
     * @param historicActivityInstanceEntity
     *            历史节点
     */
    public void processHistoryTask(HistoricTaskInstanceEntity historicTaskInstanceEntity,
        HistoricActivityInstanceEntity historicActivityInstanceEntity) {
        historicTaskInstanceEntity.setEndTime(null);
        historicTaskInstanceEntity.setDurationInMillis(null);
        historicActivityInstanceEntity.setEndTime(null);
        historicActivityInstanceEntity.setDurationInMillis(null);

        final TaskEntity task = TaskEntity.create(new Date());

        final TaskDefinitionConnector taskDefinitionConnector = ApplicationContextHolder.getInstance()
            .getApplicationContext().getBean(TaskDefinitionConnector.class);
        final com.cesgroup.spi.humantask.FormDTO taskFormDto = taskDefinitionConnector.findForm(
            historicTaskInstanceEntity.getTaskDefinitionKey(),
            historicTaskInstanceEntity.getProcessDefinitionId());

        task.setFormKey(taskFormDto.getKey());
        task.setProcessDefinitionId(historicTaskInstanceEntity.getProcessDefinitionId());
        task.setId(historicTaskInstanceEntity.getId());
        task.setAssigneeWithoutCascade(historicTaskInstanceEntity.getAssignee());
        task.setParentTaskIdWithoutCascade(historicTaskInstanceEntity.getParentTaskId());
        task.setNameWithoutCascade(historicTaskInstanceEntity.getName());
        task.setTaskDefinitionKey(historicTaskInstanceEntity.getTaskDefinitionKey());
        task.setExecutionId(historicTaskInstanceEntity.getExecutionId());
        task.setPriority(historicTaskInstanceEntity.getPriority());
        task.setProcessInstanceId(historicTaskInstanceEntity.getProcessInstanceId());
        task.setDescriptionWithoutCascade(historicTaskInstanceEntity.getDescription());
        task.setTenantId(historicTaskInstanceEntity.getTenantId());

        Context.getCommandContext().getTaskEntityManager().insert(task);
        this.createHumanTask(task, historicTaskInstanceEntity);
        final ExecutionEntity executionEntity = Context.getCommandContext().getExecutionEntityManager()
            .findExecutionById(historicTaskInstanceEntity.getExecutionId());
        if (executionEntity == null) {
            throw new RuntimeException("该节点无法进行撤回");
        } else {
            executionEntity.setActivity(getActivity(historicActivityInstanceEntity));
        }
    }

    /**
     * 获取节点
     *
     * @param historicActivityInstanceEntity
     *            历史节点
     * @return ActivityImpl
     */
    public ActivityImpl getActivity(HistoricActivityInstanceEntity historicActivityInstanceEntity) {
        final ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
            historicActivityInstanceEntity.getProcessDefinitionId())
                .execute(Context.getCommandContext());

        return processDefinitionEntity.findActivity(historicActivityInstanceEntity.getActivityId());
    }

    /**
     * 检测是否是跳过的任务
     *
     * @param historyActivityId
     *            历史节点ID
     * @return boolean
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
     */
    public HumanTaskDTO createHumanTask(DelegateTask delegateTask,
        HistoricTaskInstanceEntity historicTaskInstanceEntity) {
        HumanTaskDTO humanTaskDto = new HumanTaskBuilder().setDelegateTask(delegateTask).build();

        /*if ("发起流程".equals(historicTaskInstanceEntity.getDeleteReason())) {
            humanTaskDto.setCatalog(WorkflowConstants.HumanTaskConstants.CATALOG_START);
        }*/

        final HistoricProcessInstance historicProcessInstance = Context.getCommandContext()
            .getHistoricProcessInstanceEntityManager()
            .findHistoricProcessInstance(delegateTask.getProcessInstanceId());
        humanTaskDto.setProcessStarter(historicProcessInstance.getStartUserId());
        humanTaskDto.setComment("任务被撤回");
        if (historicTaskInstanceEntity.getDueDate() != null) {
            humanTaskDto.setDuration(null);
        }
        final HumanTaskConnector humanTaskConnector = ApplicationContextHelper
            .getBean(HumanTaskConnector.class);
        humanTaskDto = humanTaskConnector.saveHumanTask(humanTaskDto, false);

        return humanTaskDto;
    }

}
