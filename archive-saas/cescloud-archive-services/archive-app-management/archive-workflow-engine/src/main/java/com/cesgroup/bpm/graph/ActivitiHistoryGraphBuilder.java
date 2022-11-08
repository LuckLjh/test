package com.cesgroup.bpm.graph;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据历史，生成实时运行阶段的子图.
 * 
 * @author 国栋
 *
 */
public class ActivitiHistoryGraphBuilder {

    /**
     * logger.
     */
    private static Logger logger = LoggerFactory.getLogger(ActivitiHistoryGraphBuilder.class);

    private String processInstanceId;

    private ProcessDefinitionEntity processDefinitionEntity;

    private List<HistoricActivityInstance> historicActivityInstances;

    private List<HistoricActivityInstance> visitedHistoricActivityInstances = 
        new ArrayList<HistoricActivityInstance>();

    private Map<String, Node> nodeMap = new HashMap<String, Node>();

    public ActivitiHistoryGraphBuilder(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    /**
     * 构建流程跟踪图
     * 
     * @return Graph
     */
    public Graph build() {
        fetchProcessDefinitionEntity();
        fetchHistoricActivityInstances();

        Graph graph = new Graph();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            Node currentNode = new Node();
            currentNode.setId(historicActivityInstance.getId());
            currentNode.setName(historicActivityInstance.getActivityId());
            currentNode.setType(historicActivityInstance.getActivityType());
            currentNode.setActive(historicActivityInstance.getEndTime() == null);
            logger.debug("当前节点： {}:{}", currentNode.getName(), currentNode.getId());

            Edge previousEdge = findPreviousEdge(currentNode,
                historicActivityInstance.getStartTime().getTime());

            if (previousEdge == null) {
                if (graph.getInitial() != null) {
                    nodeMap.put(currentNode.getId(), currentNode);
                    visitedHistoricActivityInstances.add(historicActivityInstance);
                    continue;
                }

                graph.setInitial(currentNode);
            } else {
                logger.debug("前置迁移线： {}", previousEdge.getName());
            }

            nodeMap.put(currentNode.getId(), currentNode);
            visitedHistoricActivityInstances.add(historicActivityInstance);
        }

        if (graph.getInitial() == null) {
            throw new IllegalStateException("无法找到初始");
        }

        return graph;
    }

    /**
     * 根据流程实例id获取对应的流程定义.
     */
    public void fetchProcessDefinitionEntity() {
        String processDefinitionId = Context.getCommandContext()
            .getHistoricProcessInstanceEntityManager()
            .findHistoricProcessInstance(processInstanceId).getProcessDefinitionId();
        GetDeploymentProcessDefinitionCmd cmd = new GetDeploymentProcessDefinitionCmd(
            processDefinitionId);
        processDefinitionEntity = cmd.execute(Context.getCommandContext());
    }

    /**
     * 获取历史节点实例信息
     */
    public void fetchHistoricActivityInstances() {
        HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = 
            new HistoricActivityInstanceQueryImpl();
        historicActivityInstanceQueryImpl.processInstanceId(processInstanceId)
            .orderByHistoricActivityInstanceStartTime().asc();
        // TODO: 如果用了uuid会造成这样排序出问题
        // 但是如果用startTime，可能出现因为处理速度太快，时间一样，导致次序颠倒的问题
        historicActivityInstanceQueryImpl.processInstanceId(processInstanceId)
            .orderByHistoricActivityInstanceId().asc();

        Page page = new Page(0, 100);
        historicActivityInstances = Context.getCommandContext()
            .getHistoricActivityInstanceEntityManager()
            .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl, page);
    }

    /**
     * 找到这个节点前面的连线.
     */
    public Edge findPreviousEdge(Node currentNode, long currentStartTime) {
        String activityId = currentNode.getName();
        ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
        HistoricActivityInstance nestestHistoricActivityInstance = null;
        String temporaryPvmTransitionId = null;

        // 遍历进入当前节点的所有连线
        for (PvmTransition pvmTransition : activityImpl.getIncomingTransitions()) {
            PvmActivity source = pvmTransition.getSource();

            String previousActivityId = source.getId();

            HistoricActivityInstance visitiedHistoryActivityInstance = 
                findVisitedHistoricActivityInstance(previousActivityId);

            if (visitiedHistoryActivityInstance == null) {
                continue;
            }

            // 如果上一个节点还未完成，说明不可能是从这个节点过来的，跳过
            if (visitiedHistoryActivityInstance.getEndTime() == null) {
                continue;
            }

            logger.debug("当前节点开始时间： {}", new Date(currentStartTime));
            logger.debug("最近节点结束时间： {}", visitiedHistoryActivityInstance.getEndTime());

            // 如果当前节点的开始时间，比上一个节点的结束时间要早，跳过
            if (currentStartTime < visitiedHistoryActivityInstance.getEndTime().getTime()) {
                continue;
            }

            if (nestestHistoricActivityInstance == null) {
                nestestHistoricActivityInstance = visitiedHistoryActivityInstance;
                temporaryPvmTransitionId = pvmTransition.getId();
            } else if ((currentStartTime
                - nestestHistoricActivityInstance.getEndTime().getTime()) > (currentStartTime
                    - visitiedHistoryActivityInstance.getEndTime().getTime())) {
                // 寻找离当前节点最近的上一个节点
                // 比较上一个节点的endTime与当前节点startTime的差
                nestestHistoricActivityInstance = visitiedHistoryActivityInstance;
                temporaryPvmTransitionId = pvmTransition.getId();
            }
        }

        // 没找到上一个节点，就返回null
        if (nestestHistoricActivityInstance == null) {
            return null;
        }

        Node previousNode = nodeMap.get(nestestHistoricActivityInstance.getId());

        if (previousNode == null) {
            return null;
        }

        logger.debug("前置节点： {}:{}", previousNode.getName(), previousNode.getId());

        Edge edge = new Edge();
        edge.setName(temporaryPvmTransitionId);
        previousNode.getOutgoingEdges().add(edge);
        edge.setSrc(previousNode);
        edge.setDest(currentNode);

        return edge;
    }

    /**
     * 
     * @param activityId 节点ID
     * @return HistoricActivityInstance
     */
    public HistoricActivityInstance findVisitedHistoricActivityInstance(String activityId) {
        for (int i = visitedHistoricActivityInstances.size() - 1; i >= 0; i--) {
            HistoricActivityInstance historicActivityInstance = visitedHistoricActivityInstances
                .get(i);

            if (activityId.equals(historicActivityInstance.getActivityId())) {
                return historicActivityInstance;
            }
        }

        return null;
    }
}
