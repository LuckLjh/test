package com.cesgroup.bpm.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cesgroup.bpm.graph.ActivitiGraphBuilder;
import com.cesgroup.bpm.graph.Edge;
import com.cesgroup.bpm.graph.Graph;
import com.cesgroup.bpm.graph.Node;
import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.workflow.exception.ParallelOrInclusiveException;

/**
 * 撤销任务命令
 *
 * @author 国栋
 *
 */
public class WithdrawTaskCheckCmd implements Command<Boolean> {

    private static Logger logger = LoggerFactory
        .getLogger(WithdrawTaskCheckCmd.class);

    private final String historyTaskId;

    private final String humanTaskId;

    @SuppressWarnings("unused")
    private final Map<String, String> taskParameters;

    /**
     * 这个historyTaskId是已经完成的一个任务的id.
     */
    public WithdrawTaskCheckCmd(String historyTaskId, String humanTaskId,
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
    public Boolean execute(CommandContext commandContext) {
        //根据taskInfo中attr4字段进行判断，只有1为可以撤销
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper
            .getBean(JdbcDao.class).getJdbcTemplate();
        final String attr4 = (String) jdbcTemplate.queryForObject(
            "select attr4 from T_WF_TASK_INFO where id=?", String.class,
            humanTaskId);
        if (null == attr4 || !"1".equals(attr4)) {
            logger.info("无法撤销 {}", historyTaskId);
            return false;
        }
        // 获得历史任务
        final HistoricTaskInstanceEntity historicTaskInstanceEntity = Context
            .getCommandContext().getHistoricTaskInstanceEntityManager()
            .findHistoricTaskInstanceById(historyTaskId);

        // 获得历史节点
        final HistoricActivityInstanceEntity historicActivityInstanceEntity = getHistoricActivityInstanceEntity(
            historyTaskId);

        //使用历史的在有子流程的流程中会出现问题，子流程里的节点无法被加入，故现在替换为ActivitiGraphBuilder构建

        final Graph graph = new ActivitiGraphBuilder(
            historicTaskInstanceEntity.getProcessDefinitionId()).build();

        final Node node = graph
            .findById(historicActivityInstanceEntity.getActivityId());

        final List<String> historyNodeIds = new ArrayList<String>();
        if (null != node) {
            // 获得期望撤销的节点后面的所有节点历史
            try {
                final String resultList = "";
                final String nodeList = this.findIncomingNodeList(graph, node,
                    resultList);
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
        //String resultCode = null;
        final String procInstanceId = historicTaskInstanceEntity
            .getProcessInstanceId();
        if (historyNodeIds.size() > 0) {
            for (final String codeId : historyNodeIds) {
                final List<Map<String, Object>> li = jdbcTemplate.queryForList(
                    "select t.status,t.is_countersign from t_wf_task_info t where t.code=? "
                        + " and t.process_instance_id=? and t.status=?",
                    new Object[] { codeId, procInstanceId, WorkflowConstants.HumanTaskConstants.STATUS_ACTIVE });
                if (li.size() > 0) {
                    //判断当前节点是不是会签
                    if (WorkflowConstants.YES
                        .equals(li.get(0).get("IS_COUNTERSIGN"))) {
                        return false;
                    }
                }
            }
        }

        return true;

    }

    /**
     * 获取历史节点实体
     *
     * @param historyTaskId
     *            历史任务ID
     * @return HistoricActivityInstanceEntity
     */
    public HistoricActivityInstanceEntity getHistoricActivityInstanceEntity(
        String historyTaskId) {
        logger.info("历史任务id : {}", historyTaskId);

        final JdbcTemplate jdbcTemplate = ApplicationContextHelper
            .getBean(JdbcDao.class).getJdbcTemplate();
        final List<String> historicActivityInstanceList = jdbcTemplate.queryForList(
            "select id_ from T_WF_ACT_HI_ACTINST where task_id_=?",
            String.class, historyTaskId);
        final String historicActivityInstanceId = historicActivityInstanceList.get(0);
        logger.info("历史节点实例id : {}", historicActivityInstanceId);

        final HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
        historicActivityInstanceQueryImpl
            .activityInstanceId(historicActivityInstanceId);

        final HistoricActivityInstanceEntity historicActivityInstanceEntity = (HistoricActivityInstanceEntity) Context
            .getCommandContext()
            .getHistoricActivityInstanceEntityManager()
            .findHistoricActivityInstancesByQueryCriteria(
                historicActivityInstanceQueryImpl, new Page(0, 1))
            .get(0);

        return historicActivityInstanceEntity;
    }

    /**
     * 查找进入的连线有几个 返回list
     */
    public String findIncomingNodeList(Graph graph, Node node,
        String previousList) {
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

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(srcType)) {
                previousList += dest.getId() + ",";
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY
                .equals(srcType)) {
                return this.findIncomingNodeList(graph, dest, previousList);
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY
                .equals(srcType)
                || WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY
                    .equals(srcType)) {
                throw new ParallelOrInclusiveException("上一节点是并行网关或者包含网关，不能退回!");
            } else {
                logger.info("无法回退，前序节点不是用户任务 : " + src.getId() + " " + srcType
                    + "(" + src.getName() + ")");

                return null;
            }
        }
        if (StringUtils.isNotBlank(previousList)) {
            return previousList;
        } else {
            logger.info("无法回退，节点 : " + node.getId() + " " + node.getType() + "("
                + node.getName() + ")");

            return null;
        }

    }

}
