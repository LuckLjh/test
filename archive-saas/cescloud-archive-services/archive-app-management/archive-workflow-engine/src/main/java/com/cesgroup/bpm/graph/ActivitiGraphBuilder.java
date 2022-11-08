package com.cesgroup.bpm.graph;

import com.cesgroup.core.util.WorkflowConstants;

import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 根据流程定义，构建设计阶段的图.
 * 
 * @author 国栋
 *
 */
public class ActivitiGraphBuilder {

    /**
     * 流程定义id.
     */
    private String processDefinitionId;

    /**
     * 流程定义.
     */
    private ProcessDefinitionEntity processDefinitionEntity;

    /**
     * 已访问的节点id.
     */
    private Set<String> visitedNodeIds = new HashSet<String>();

    /**
     * 构造方法.
     */
    public ActivitiGraphBuilder(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    /**
     * 构建图.
     */
    public Graph build() {
        this.fetchProcessDefinitionEntity();

        Node initial = visitNode(processDefinitionEntity.getInitial());

        Graph graph = new Graph();
        graph.setInitial(initial);

        return graph;
    }

    /**
     * 获取流程定义.
     */
    public void fetchProcessDefinitionEntity() {
        GetDeploymentProcessDefinitionCmd cmd = new GetDeploymentProcessDefinitionCmd(
            processDefinitionId);
        processDefinitionEntity = cmd.execute(Context.getCommandContext());
    }

    /**
     * 遍历.
     */
    public Node visitNode(PvmActivity pvmActivity) {
        Node currentNode = new Node();
        currentNode.setId(pvmActivity.getId());
        currentNode.setName(this.getString(pvmActivity.getProperty("name")));
        currentNode.setType(this.getString(pvmActivity.getProperty("type")));

        List<? extends PvmActivity> activitys = pvmActivity.getActivities();
        if (activitys != null && activitys.size() > 0) {
            for (PvmActivity at : activitys) {
                if (WorkflowConstants.NodeTypeConstants.NODETYPE_STARTEVENT
                    .equals(at.getProperty("type"))) {
                    visitSubNode(currentNode, at);
                }
            }

        }
        if (!visitedNodeIds.contains(pvmActivity.getId())) {
            visitedNodeIds.add(pvmActivity.getId());
        } else {
            return currentNode;
        }

        for (PvmTransition pvmTransition : pvmActivity.getOutgoingTransitions()) {
            PvmActivity destination = pvmTransition.getDestination();
            Node targetNode = this.visitNode(destination);

            Edge edge = new Edge();
            edge.setId(pvmTransition.getId());
            edge.setSrc(currentNode);
            edge.setDest(targetNode);
            currentNode.getOutgoingEdges().add(edge);
        }

        return currentNode;
    }

    private Node visitSubNode(Node currentNode, PvmActivity pvmActivity) {
        Node subNode = visitNode(pvmActivity);
        currentNode.setSubNodes(subNode);
        return currentNode;
    }

    /**
     * 把object转换为string.
     */
    public String getString(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof String) {
            return (String) object;
        } else {
            return object.toString();
        }
    }
}
