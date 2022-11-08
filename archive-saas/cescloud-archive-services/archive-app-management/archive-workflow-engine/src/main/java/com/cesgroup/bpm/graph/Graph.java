package com.cesgroup.bpm.graph;

import com.cesgroup.core.util.WorkflowConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * 图像类
 * 
 * @author 国栋
 *
 */
public class Graph {

    /**
     * 初始化图像节点
     */
    private Node initial;

    /**
     * 返回起始节点
     * 
     * @return Node
     */
    public Node getInitial() {
        return initial;
    }

    /**
     * 起始节点
     * 
     * @param initial 初始节点
     */
    public void setInitial(Node initial) {
        this.initial = initial;
    }

    /**
     * 返回所有节点
     * 
     * @return list
     */
    public List<Node> getNodes() {
        List<Node> nodes = new ArrayList<Node>();
        visitNode(initial, nodes);

        return nodes;
    }

    /**
     * 递归
     * 
     * @param node 节点信息
     * @param nodes 结果集
     */
    public void visitNode(Node node, List<Node> nodes) {
        nodes.add(node);
        if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS.equals(node.getType())) {
            for (Edge edge : node.getSubNodes().getOutgoingEdges()) {
                Node nextNode = edge.getDest();
                visitNode(nextNode, nodes);
            }
        }
        for (Edge edge : node.getOutgoingEdges()) {
            Node nextNode = edge.getDest();
            visitNode(nextNode, nodes);
        }
    }

    /**
     * 返回边界线数组
     * 
     * @return list
     */
    public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<Edge>();
        visitEdge(initial, edges);

        return edges;
    }

    /**
     * 递归构造迁移线
     * 
     * @param node 节点
     * @param edges 迁移线集合
     */
    public void visitEdge(Node node, List<Edge> edges) {
        for (Edge edge : node.getOutgoingEdges()) {
            edges.add(edge);

            Node nextNode = edge.getDest();
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                .equals(nextNode.getType())) {
                visitEdge(nextNode.getSubNodes(), edges);
            }
            visitEdge(nextNode, edges);
        }
    }

    /**
     * 根据id获取节点
     * 
     * @param id 节点ID
     * @return Node
     */
    public Node findById(String id) {
        for (Node node : this.getNodes()) {
            if (id.equals(node.getId())) {
                return node;
            }
        }

        return null;
    }
}
