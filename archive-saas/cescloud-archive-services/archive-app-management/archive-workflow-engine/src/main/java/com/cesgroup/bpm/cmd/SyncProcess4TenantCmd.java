package com.cesgroup.bpm.cmd;

import com.cesgroup.bpm.graph.Edge;
import com.cesgroup.bpm.graph.Graph;
import com.cesgroup.bpm.graph.Node;
import com.cesgroup.bpm.persistence.domain.*;
import com.cesgroup.bpm.persistence.manager.*;
import com.cesgroup.bpm.support.TaskDefinitionBuilder;
import com.cesgroup.button.ButtonHelper;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.StringUtils;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.TaskDefBase;
import com.cesgroup.humantask.persistence.manager.TaskDefBaseManager;
import com.cesgroup.humantask.support.TaskDefinitionConnectorImpl;
import com.cesgroup.spi.humantask.TaskDefinitionConnector;
import com.cesgroup.spi.humantask.TaskDefinitionDTO;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.impl.cmd.GetBpmnModelCmd;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

import java.util.*;
import java.util.Map.Entry;

/**
 * 同步流程
 *
 * @author chenyao
 *
 */
public class SyncProcess4TenantCmd implements Command<Void> {

    /** 流程定义id. */
    private final String processDefinitionId;

    /** 租户ID */
    private final String tenantId;

    /**
     * 构造方法.
     */
    public SyncProcess4TenantCmd(String processDefinitionId, String tenantId) {
        this.processDefinitionId = processDefinitionId;
        this.tenantId = tenantId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        final ProcessDefinitionEntity processDefinitionEntity = new GetDeploymentProcessDefinitionCmd(
            processDefinitionId)
                .execute(commandContext);
        final String processDefinitionKey = processDefinitionEntity.getKey();
        final int processDefinitionVersion = processDefinitionEntity.getVersion();
        final BpmConfBaseManager bpmConfBaseManager = getBpmConfBaseManager();
        BpmConfBase bpmConfBase = bpmConfBaseManager
            .findUnique("from BpmConfBase where processDefinitionKey=?0 "
                + "and processDefinitionVersion=?1 "
                + "and bpmModel.tenantId=?2", processDefinitionKey,
                processDefinitionVersion, tenantId);

        if (bpmConfBase == null) {
            bpmConfBase = new BpmConfBase();
            bpmConfBase.setProcessDefinitionId(processDefinitionId);
            bpmConfBase.setProcessDefinitionKey(processDefinitionKey);
            bpmConfBase.setProcessDefinitionVersion(processDefinitionVersion);
            bpmConfBaseManager.save(bpmConfBase);
        } else if (bpmConfBase.getProcessDefinitionId() == null) {
            bpmConfBase.setProcessDefinitionId(processDefinitionId);
            bpmConfBaseManager.save(bpmConfBase);
        }

        final BpmnModel bpmnModel = new GetBpmnModelCmd(processDefinitionId).execute(commandContext);
        final Graph graph = new FindGraphCmd(processDefinitionId).execute(commandContext);
        this.processGlobal(bpmnModel, 1, bpmConfBase);

        int priority = 2;

        for (final Node node : graph.getNodes()) {
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY
                .equals(node.getType())) {
                continue;
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(node.getType())) {
                this.processUserTask(node, bpmnModel, priority++, bpmConfBase);
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_STARTEVENT
                .equals(node.getType())) {
                this.processStartEvent(node, bpmnModel, priority++, bpmConfBase);
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_ENDEVENT
                .equals(node.getType())) {
                this.processEndEvent(node, bpmnModel, priority++, bpmConfBase);
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                .equals(node.getType())) {
                this.processSubProcess(node, bpmnModel, priority++, bpmConfBase, commandContext);
            }
        }

        return null;
    }

    private void processSubProcess(Node subNode, BpmnModel bpmnModel, int i,
        BpmConfBase bpmConfBase, CommandContext commandContext) {
        final List<Node> nodes = new ArrayList<Node>();
        getNodes(subNode.getSubNodes(), nodes);
        for (final Node node : nodes) {
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY
                .equals(node.getType())) {
                continue;
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(node.getType())) {
                this.processUserTask(node, bpmnModel, i++, bpmConfBase);
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_STARTEVENT
                .equals(node.getType())) {
                this.processStartEvent(node, bpmnModel, i++, bpmConfBase);
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_ENDEVENT
                .equals(node.getType())) {
                this.processEndEvent(node, bpmnModel, i++, bpmConfBase);
            } else if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                .equals(node.getType())) {
                processSubProcess(node, bpmnModel, i++, bpmConfBase, commandContext);
            }
        }

    }

    private void getNodes(Node subNode, List<Node> nodes) {
        nodes.add(subNode);
        if (subNode.getOutgoingEdges() != null) {
            final List<Edge> edges = subNode.getOutgoingEdges();
            for (final Edge edge : edges) {
                getNodes(edge.getDest(), nodes);
            }
        }
    }

    /**
     * 全局配置.
     */
    public void processGlobal(BpmnModel bpmnModel, int priority, BpmConfBase bpmConfBase) {
        final Process process = bpmnModel.getMainProcess();
        final BpmConfNodeManager bpmConfNodeManager = getBpmConfNodeManager();
        BpmConfNode bpmConfNode = bpmConfNodeManager
            .findUnique("from BpmConfNode where code=?0 and bpmConfBase=?1",
                process.getId(), bpmConfBase);

        if (bpmConfNode == null) {
            bpmConfNode = new BpmConfNode();
            bpmConfNode.setCode(process.getId());
            bpmConfNode.setName("全局");
            bpmConfNode.setType("process");
            bpmConfNode.setConfUser(2);
            bpmConfNode.setConfListener(0);
            bpmConfNode.setConfRule(2);
            bpmConfNode.setConfForm(0);
            bpmConfNode.setConfOperation(2);
            bpmConfNode.setConfNotice(2);
            bpmConfNode.setPriority(priority);
            bpmConfNode.setBpmConfBase(bpmConfBase);
            bpmConfNodeManager.save(bpmConfNode);
        }

        // 配置监听器
        processListener(process.getExecutionListeners(), bpmConfNode);
    }

    /**
     * 配置用户任务.
     */
    public void processUserTask(Node node, BpmnModel bpmnModel, int priority,
        BpmConfBase bpmConfBase) {
        final BpmConfNodeManager bpmConfNodeManager = getBpmConfNodeManager();
        BpmConfNode bpmConfNode = bpmConfNodeManager
            .findUnique("from BpmConfNode where code=?0 and bpmConfBase=?1",
                node.getId(), bpmConfBase);

        if (bpmConfNode == null) {
            bpmConfNode = new BpmConfNode();
            bpmConfNode.setCode(node.getId());
            bpmConfNode.setName(node.getName());
            bpmConfNode.setType(node.getType());
            bpmConfNode.setConfUser(0);
            bpmConfNode.setConfListener(0);
            bpmConfNode.setConfRule(0);
            bpmConfNode.setConfForm(0);
            bpmConfNode.setConfOperation(0);
            bpmConfNode.setConfNotice(0);
            bpmConfNode.setPriority(priority);
            bpmConfNode.setBpmConfBase(bpmConfBase);
            bpmConfNodeManager.save(bpmConfNode);
        }

        // 配置参与者
        final UserTask userTask = (UserTask) bpmnModel.getFlowElement(node.getId());

        //删除旧用户配置
		BpmConfUserManager bpmConfUserManager = getBpmConfUserManager();
		bpmConfUserManager.removeBpmConfUsersByNodeId(bpmConfNode.getId());

        int index = 1;
        index = processUserTaskConf(bpmConfNode, userTask.getAssignee(), 0, index);

        for (final String candidateUser : userTask.getCandidateUsers()) {
            index = processUserTaskConf(bpmConfNode, candidateUser, 1, index);
        }

        for (final String candidateGroup : userTask.getCandidateGroups()) {
            processUserTaskConf(bpmConfNode, candidateGroup, 2, index);
        }

        if (userTask.getLoopCharacteristics() != null) {
            final BpmConfCountersign bpmConfCountersign = new BpmConfCountersign();
            bpmConfCountersign.setType(0);
            bpmConfCountersign.setRate(100);
            bpmConfCountersign.setBpmConfNode(bpmConfNode);
            bpmConfCountersign.setSequential(
                userTask.getLoopCharacteristics().isSequential() ? 1 : 0);
            getBpmConfCountersignManager().save(bpmConfCountersign);
        }

        // 配置监听器
        processListener(userTask.getExecutionListeners(), bpmConfNode);
        processListener(userTask.getTaskListeners(), bpmConfNode);
        // 配置表单
        processForm(userTask, bpmConfNode);

        // 配置自定义的元素，比如operation 啊。 isshownnextnode 啊。。乱七八糟的东西。草。
        processOperation(node, userTask.getExtensionElements(), bpmConfNode);

        // 更新TaskDefinition
        final TaskDefinitionConnector taskDefinitionConnector = getTaskDefinitionConnector();
        final TaskDefinitionDTO taskDefinitionDto = new TaskDefinitionBuilder().setUserTask(userTask)
            .setProcessDefinitionId(bpmConfBase.getProcessDefinitionId()).build();
        taskDefinitionConnector.create(taskDefinitionDto);
    }

    private void processOperation(Node node, Map<String, List<ExtensionElement>> extensionElements,
        BpmConfNode bpmConfNode) {
        if (extensionElements != null) {
            Entry<String, List<ExtensionElement>> en;
            String key;
            List<ExtensionElement> value;
            Set<BpmConfOperation> set;
            final Iterator<Entry<String, List<ExtensionElement>>> it = extensionElements.entrySet()
                .iterator();
            String[] operationArray;
            final BpmConfOperationManager operationManager = ApplicationContextHelper
                .getBean(BpmConfOperationManager.class);
            final TaskDefinitionConnector taskDefinitionConnector = ApplicationContextHelper
                .getBean(TaskDefinitionConnectorImpl.class);
            final TaskDefBaseManager taskDefBaseManager = ApplicationContextHelper
                .getBean(TaskDefBaseManager.class);
            // BpmConfFormManager bpmConfFormManager =
            // ApplicationContextHelper.getBean(BpmConfFormManager.class);
            BpmConfOperation bpmConfOperation;
            // BpmConfForm bpmConfForm;
            // Long bpmConfNodeId = null;
            final ButtonHelper buttonHelper = new ButtonHelper();
            // boolean hasOperation = false;
            while (it.hasNext()) {
                en = it.next();
                key = en.getKey();
                value = en.getValue();
                if (StringUtils.isNotBlank(key)) {
                    if (key.equals(WorkflowConstants.ExtensionProperty.OPERATION)) {
                        // 操作
                        final String operations = value.get(0).getAttributes().get("value")
                            .get(0).getValue();
                        operationArray = operations.split(",");
                        for (final String operation : operationArray) {
                            if (bpmConfNode != null) {
                                set = bpmConfNode.getBpmConfOperations();
                                bpmConfNode.setConfOperation(0);
                                for (final BpmConfOperation confOperation : set) {
                                    operationManager.remove(confOperation);
                                }
                            }
                            bpmConfOperation = new BpmConfOperation();
                            bpmConfOperation.setValue(operation);
                            if (operation.contains("[")) {
                                bpmConfOperation.setName(buttonHelper.findButton(operation
                                    .substring(0, operation.indexOf("["))).getLabel());
                            } else {
                                bpmConfOperation.setName(buttonHelper.findButton(operation)
                                    .getLabel());
                            }

                            if (bpmConfNode != null) {
                                bpmConfOperation.setBpmConfNode(bpmConfNode);
                                bpmConfOperation.setPriority(bpmConfNode.getPriority());
                            }
                            bpmConfOperation.setBpmConfNode(bpmConfNode);
                            operationManager.save(bpmConfOperation);

                            // BpmConfOperation dest = bpmConfOperation;
                            String taskDefinitionKey = null;
                            String processDefinitionId = null;
                            int processDefinitionVersion = 0;
                            String processDefinitionKey = null;
                            if (bpmConfNode != null) {
                                taskDefinitionKey = bpmConfNode.getCode();
                                processDefinitionId = bpmConfNode.getBpmConfBase()
                                    .getProcessDefinitionId();
                                processDefinitionVersion = bpmConfNode.getBpmConfBase()
                                    .getProcessDefinitionVersion();
                                processDefinitionKey = bpmConfNode.getBpmConfBase()
                                    .getProcessDefinitionKey();
                            }

                            TaskDefBase taskDefBase = taskDefBaseManager
                                .getTaskDefBaseByCodeAndProcDefId(taskDefinitionKey,
                                    processDefinitionId);
                            if (taskDefBase == null) {
                                taskDefBase = new TaskDefBase();
                                taskDefBase.setCode(taskDefinitionKey);
                                taskDefBase.setProcessDefinitionId(processDefinitionId);
                                taskDefBase.setName(node.getName());
                                taskDefBase.setProcessDefinitionVersion(processDefinitionVersion);
                                taskDefBase.setProcessDefinitionKey(processDefinitionKey);
                                if (bpmConfNode.getBpmConfForms() != null
                                    && bpmConfNode.getBpmConfForms().size() > 0) {
                                    taskDefBase.setFormKey(((BpmConfForm) bpmConfNode
                                        .getBpmConfForms().toArray()[0]).getOriginValue());
                                }
                                taskDefBaseManager.save(taskDefBase);
                            }

                            taskDefinitionConnector.addOperation(taskDefinitionKey,
                                processDefinitionId, bpmConfOperation.getValue());
                        }

                    } else if (key.equals(WorkflowConstants.ExtensionProperty.SHOWNNEXTDIALOG)) {
                        // 是否显示下一步的页面
                        // String isshownext =
                        // value.get(0).getAttributes().get("value").get(0).getValue();
                    }
                }
            }
            // 将原来在循环中的创建方法移到循环外，解决生出操作按钮个数条taskDefBase的问题

        }

        //
        // operationManager.find("from BpmConfOperation where value=? and type=?
        // and priority=? and bpmConfNode=?", value, type, priority,
        // bpmConfNode)
    }

    /**
     * 配置开始事件.
     */
    public void processStartEvent(Node node, BpmnModel bpmnModel, int priority,
        BpmConfBase bpmConfBase) {
        final BpmConfNodeManager bpmConfNodeManager = getBpmConfNodeManager();
        BpmConfNode bpmConfNode = bpmConfNodeManager
            .findUnique("from BpmConfNode where code=?0 and bpmConfBase=?1",
                node.getId(), bpmConfBase);

        if (bpmConfNode == null) {
            bpmConfNode = new BpmConfNode();
            bpmConfNode.setCode(node.getId());
            bpmConfNode.setName(node.getName());
            bpmConfNode.setType(node.getType());
            bpmConfNode.setConfUser(2);
            bpmConfNode.setConfListener(0);
            bpmConfNode.setConfRule(2);
            bpmConfNode.setConfForm(0);
            bpmConfNode.setConfOperation(2);
            bpmConfNode.setConfNotice(0);
            bpmConfNode.setPriority(priority);
            bpmConfNode.setBpmConfBase(bpmConfBase);
            bpmConfNodeManager.save(bpmConfNode);
        }

        final FlowElement flowElement = bpmnModel.getFlowElement(node.getId());
        // 配置监听器
        this.processListener(flowElement.getExecutionListeners(), bpmConfNode);

        final StartEvent startEvent = (StartEvent) flowElement;
        // 配置表单
        this.processForm(startEvent, bpmConfNode);
    }

    /**
     * 配置参与者.
     */
    public int processUserTaskConf(BpmConfNode bpmConfNode, String value, int type, int priority) {
        if (value == null) {
            return priority;
        }

        // String valueCopy = null;
        // if (StringUtils.isNotBlank(value))
        // {
        // if ("流程发起人".equals(value))
        // {
        // valueCopy = "${initiator}";
        // }
        // else if("上一节点处理人".equals(value))
        // {
        // valueCopy = "${}";
        // }
        // else
        // {
        // valueCopy = value;
        // }
        // }

        final BpmConfUserManager bpmConfUserManager = getBpmConfUserManager();
        // 去掉排序过滤条件，防止一个人多条记录。
        // BpmConfUser bpmConfUser = bpmConfUserManager.findUnique("from
        // BpmConfUser where value=? and type=? and priority=? and
        // bpmConfNode=?", valueCopy, type, priority, bpmConfNode);
        BpmConfUser bpmConfUser = bpmConfUserManager
            .findUnique("from BpmConfUser where value=?0 and type=?1 and  bpmConfNode=?2",
                value, type, bpmConfNode);

        if (bpmConfUser == null) {
            bpmConfUser = new BpmConfUser();
            bpmConfUser.setValue(value);
            bpmConfUser.setType(type);
            bpmConfUser.setStatus(0);
            bpmConfUser.setPriority(priority);
            bpmConfUser.setBpmConfNode(bpmConfNode);
            bpmConfUserManager.save(bpmConfUser);
        }

        return priority + 1;
    }

    /**
     * 配置结束事件.
     */
    public void processEndEvent(Node node, BpmnModel bpmnModel, int priority,
        BpmConfBase bpmConfBase) {
        final BpmConfNodeManager bpmConfNodeManager = getBpmConfNodeManager();
        BpmConfNode bpmConfNode = bpmConfNodeManager
            .findUnique("from BpmConfNode where code=?0 and bpmConfBase=?1",
                node.getId(), bpmConfBase);

        if (bpmConfNode == null) {
            bpmConfNode = new BpmConfNode();
            bpmConfNode.setCode(node.getId());
            bpmConfNode.setName(node.getName());
            bpmConfNode.setType(node.getType());
            bpmConfNode.setConfUser(2);
            bpmConfNode.setConfListener(0);
            bpmConfNode.setConfRule(2);
            bpmConfNode.setConfForm(2);
            bpmConfNode.setConfOperation(2);
            bpmConfNode.setConfNotice(0);
            bpmConfNode.setPriority(priority);
            bpmConfNode.setBpmConfBase(bpmConfBase);
            bpmConfNodeManager.save(bpmConfNode);
        }

        final FlowElement flowElement = bpmnModel.getFlowElement(node.getId());
        // 配置监听器
        this.processListener(flowElement.getExecutionListeners(), bpmConfNode);
    }

    /**
     * 配置监听器.
     */
    public void processListener(List<ActivitiListener> activitiListeners, BpmConfNode bpmConfNode) {
        final Map<String, Integer> eventTypeMap = new HashMap<String, Integer>();
        eventTypeMap.put("start", 0);
        eventTypeMap.put("end", 1);
        eventTypeMap.put("take", 2);
        eventTypeMap.put("create", 3);
        eventTypeMap.put("assignment", 4);
        eventTypeMap.put("complete", 5);
        eventTypeMap.put("delete", 6);

        final BpmConfListenerManager bpmConfListenerManager = getBpmConfListenerManager();

        for (final ActivitiListener activitiListener : activitiListeners) {
            final String value = activitiListener.getImplementation();
            final int type = eventTypeMap.get(activitiListener.getEvent());
            BpmConfListener bpmConfListener = bpmConfListenerManager.findUnique(
                "from BpmConfListener where value=?0 and type=?1 and status=0 "
                    + "and bpmConfNode=?2",
                value, type,
                bpmConfNode);

            if (bpmConfListener == null) {
                bpmConfListener = new BpmConfListener();
                bpmConfListener.setValue(value);
                bpmConfListener.setType(type);
                bpmConfListenerManager.save(bpmConfListener);
            }
        }
    }

    /**
     * 配置表单，userTask.
     */
    public void processForm(UserTask userTask, BpmConfNode bpmConfNode) {
        if (userTask.getFormKey() == null) {
            return;
        }

        final BpmConfFormManager bpmConfFormManager = getBpmConfFormManager();
        BpmConfForm bpmConfForm = bpmConfFormManager
            .findUnique("from BpmConfForm where bpmConfNode=?0", bpmConfNode);

        if (bpmConfForm == null) {
            bpmConfForm = new BpmConfForm();
            bpmConfForm.setValue(userTask.getFormKey());
            bpmConfForm.setType(0);
            bpmConfForm.setOriginValue(userTask.getFormKey());
            bpmConfForm.setOriginType(0);
            bpmConfForm.setStatus(0);
            bpmConfForm.setBpmConfNode(bpmConfNode);
            bpmConfFormManager.save(bpmConfForm);
        }
    }

    /**
     * 配置表单，startEvent.
     */
    public void processForm(StartEvent startEvent, BpmConfNode bpmConfNode) {
        if (startEvent.getFormKey() == null) {
            return;
        }

        final BpmConfFormManager bpmConfFormManager = getBpmConfFormManager();
        BpmConfForm bpmConfForm = bpmConfFormManager
            .findUnique("from BpmConfForm where bpmConfNode=?0", bpmConfNode);

        if (bpmConfForm == null) {
            bpmConfForm = new BpmConfForm();
            bpmConfForm.setValue(startEvent.getFormKey());
            bpmConfForm.setType(0);
            bpmConfForm.setOriginValue(startEvent.getFormKey());
            bpmConfForm.setOriginType(0);
            bpmConfForm.setStatus(0);
            bpmConfForm.setBpmConfNode(bpmConfNode);
            bpmConfFormManager.save(bpmConfForm);
        }
    }

    // ~ ======================================================================
    public BpmConfBaseManager getBpmConfBaseManager() {
        return ApplicationContextHelper.getBean(BpmConfBaseManager.class);
    }

    public BpmConfNodeManager getBpmConfNodeManager() {
        return ApplicationContextHelper.getBean(BpmConfNodeManager.class);
    }

    public BpmConfUserManager getBpmConfUserManager() {
        return ApplicationContextHelper.getBean(BpmConfUserManager.class);
    }

    public BpmConfListenerManager getBpmConfListenerManager() {
        return ApplicationContextHelper.getBean(BpmConfListenerManager.class);
    }

    public BpmConfFormManager getBpmConfFormManager() {
        return ApplicationContextHelper.getBean(BpmConfFormManager.class);
    }

    public BpmConfCountersignManager getBpmConfCountersignManager() {
        return ApplicationContextHelper.getBean(BpmConfCountersignManager.class);
    }

    public TaskDefinitionConnector getTaskDefinitionConnector() {
        return ApplicationContextHelper.getBean(TaskDefinitionConnector.class);
    }

}
