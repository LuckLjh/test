package com.cesgroup.bpm.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * 图像节点.
 * 
 * @author 国栋
 *
 */
public class Node extends GraphElement {

    /**
     * 类型，比如userTask，startEvent.
     */
    private String type;

    /**
     * 是否还未完成.
     */
    private boolean active;

    /**
     * 进入这个节点的所有连线.
     */
    private List<Edge> incomingEdges = new ArrayList<Edge>();

    /**
     * 外出这个节点的所有连线.
     */
    private List<Edge> outgoingEdges = new ArrayList<Edge>();

    /**
     * 如果当前为subProcess 那么此处需要存放subnode
     */
    private Node subNodes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Edge> getIncomingEdges() {
        return incomingEdges;
    }

    public void setIncomingEdges(List<Edge> incomingEdges) {
        this.incomingEdges = incomingEdges;
    }

    public List<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void setOutgoingEdges(List<Edge> outgoingEdges) {
        this.outgoingEdges = outgoingEdges;
    }

    public Node getSubNodes() {
        return subNodes;
    }

    public void setSubNodes(Node subNodes) {
        this.subNodes = subNodes;
    }

}
