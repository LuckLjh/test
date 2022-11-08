package com.cesgroup.bpm.cmd;

import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cesgroup.api.user.UserConnector;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.bpm.graph.ActivitiGraphBuilder;
import com.cesgroup.bpm.graph.Edge;
import com.cesgroup.bpm.graph.Graph;
import com.cesgroup.bpm.graph.Node;
import com.cesgroup.bpm.support.MapVariableScope;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.NodeConfInfo;
import com.cesgroup.humantask.persistence.domain.TaskInfoHis;
import com.cesgroup.humantask.persistence.manager.TaskInfoRunManager;
import com.cesgroup.workflow.expression.ExpressionManagerUtil;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 发现下一个节点信息
 *
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class FindNewNextActivitiesCmd implements Command<List<NodeConfInfo>> {

    /** activityId id. */
    private final String activityId;

    private final String tenantId;

    private Integer activeCount;

    /** 影响流程运转的参数 */
    private final MapVariableScope map;

    private ProcessDefinitionEntity processDefinitionEntity;

    //private TaskEntity taskEntity;

    private Graph graph;

    private UserConnector userConnector;

    private final String processDefinitionId;

    private TaskInfoRunManager taskInfoRunManager;

    /** constructor */
    public FindNewNextActivitiesCmd(String processDefinitionId, String activityId,
        Map<String, Object> map, String tenantId) {
        this.processDefinitionId = processDefinitionId;
        this.activityId = activityId;
        this.map = new MapVariableScope();
        this.map.setVariables(map);
        this.tenantId = tenantId;
    }

    @Override
    public List<NodeConfInfo> execute(CommandContext commandContext) {
        init(commandContext);

        //定义下一个运行的的节点集合的信息
        final List<NodeConfInfo> nextNodeInfos = new ArrayList<NodeConfInfo>();
        //获取流程图中当前节点
        Node node = null;
        if (StringUtils.isNotBlank(activityId)) {
            node = graph.findById(activityId);
        } else {
            node = graph.getInitial();
        }
        if (node != null) {
            //获取当前节点之后运行的所有节点
            final List<String> result = new ArrayList<String>();
            final List<String> nextActivityInstanceIds = findIncomingNodeList(node, result);
            //遍历所有节点
            for (final String id : nextActivityInstanceIds) {
                final NodeConfInfo nodeConfInfo = getNodeConfInfoByNodeId(commandContext, id);
                nextNodeInfos.add(nodeConfInfo);
            }
        }
        return nextNodeInfos;
    }

    /**
     * 根据节点ID生成节点信息
     *
     * @param commandContext
     *            上下文
     * @param id
     *            节点ID
     * @return NodeConfInfo 封装节点基本信息对象
     */
    private NodeConfInfo getNodeConfInfoByNodeId(CommandContext commandContext, String id) {
        //定义一个节点的实体
        final NodeConfInfo nodeConfInfo = new NodeConfInfo();
        nodeConfInfo.setId(id);
        //获取当前节点的基本信息
        final ActivityImpl activityImpl = processDefinitionEntity.findActivity(id);
        //name=部门助理登记, documentation=申请人发起费用报销申请, multiInstance=parallel, type=userTask
        nodeConfInfo.setName(activityImpl.getProperty("name") == null ? ""
            : (String) activityImpl.getProperty("name"));
        //获取当前节点配置的任务信息
        final TaskDefinition taskDefinition = (TaskDefinition) activityImpl.getProperty("taskDefinition");
        if (taskDefinition != null) {
            final Set<UserDTO> resultList = getUsersByTaskDefinition(taskDefinition);
            nodeConfInfo.setUsers(resultList);
        }
        return nodeConfInfo;
    }

    /**
     * 解析节点配置的人员
     *
     * @param resultList
     *            节点配置人员集合
     * @param assigneeExpression
     *            处理人配置
     * @param candidateGroupIdExpressions
     *            候选人组配置
     * @param candidateUserIdExpressions
     *            候选人配置
     * @return Set 封装用户信息的集合
     */
    private Set<UserDTO> getUsersByTaskDefinition(TaskDefinition taskDefinition) {
        //定义一个节点的处理人的集合（处理人的id以及表达式）
        final Set<UserDTO> resultList = new HashSet<UserDTO>();
        //获取节点配置的办理人
        final Expression assigneeExpression = taskDefinition.getAssigneeExpression();
        //获取节点配置的候选人组
        final Set<Expression> candidateGroupIdExpressions = taskDefinition
            .getCandidateGroupIdExpressions();
        //获取节点配置的候选人
        final Set<Expression> candidateUserIdExpressions = taskDefinition.getCandidateUserIdExpressions();

        if (assigneeExpression != null) {
            //办理人不为null的情况下，分成四种情形 第一种配置的流程发起人，第二种是默认的${assignee},第三种是表达式，第四种是配置普通用户
            resultList.addAll(getUserListByAssigneeExpression(assigneeExpression));
        }
        if (candidateUserIdExpressions != null && candidateUserIdExpressions.size() > 0) {
            resultList.addAll(getUserListByCandidateUserIdExpressions(candidateUserIdExpressions));
        }
        if (candidateGroupIdExpressions != null && candidateGroupIdExpressions.size() > 0) {
            resultList
                .addAll(getUserListByCandidateGroupIdExpressions(candidateGroupIdExpressions));
        }

        return resultList;
    }

    /**
     * 根据候选人组表达式获取候选人
     *
     * @param candidateGroupIdExpressions
     *            获选人组表达式
     * @return Set 封装用户信息的集合
     */
    private Set<UserDTO> getUserListByCandidateGroupIdExpressions(
        Set<Expression> candidateGroupIdExpressions) {
        //获取候选人组类型
        final String candidateGroupType = checkCandidateGroupType(candidateGroupIdExpressions);

        final Set<UserDTO> resultList = new HashSet<UserDTO>();

        //GDDA8-2087 流程设置了选择提交、下一节点自定义规则设置了上一节点办理人，设置变量上一节点办理人为本人
        if (!map.map.containsKey(WorkflowConstants.WORKFLOW_DEFAULTVARIABLE_PRETASKASSIGNEE)) {
			map.map.put(WorkflowConstants.WORKFLOW_DEFAULTVARIABLE_PRETASKASSIGNEE, SecurityUtils.getUser().getId());
		}
        //配置候选人组
        for (final Expression expression : candidateGroupIdExpressions) {
            final String expressionText = expression.getExpressionText();
            //候选人组 配置表达式的情况下
            if (StringUtils.isNotBlank(candidateGroupType)
                && "expression".equals(candidateGroupType)) {
                final String userExpression = expressionText.substring(expressionText.indexOf(":") + 1);
                final Object parseExpression = ExpressionManagerUtil.getInstance()
                    .executeExpressionByVariableScope(map, userExpression);
                resultList.addAll(addResultListByExpression(parseExpression));
            } else if (StringUtils.isNotBlank(candidateGroupType)
                && "group".equals(candidateGroupType)) {
                final String candidateGroupExpression = expressionText.split(":")[1];
                final List<UserDTO> list = userConnector.findByOrgId(candidateGroupExpression);
                resultList.addAll(list);
            } else if (StringUtils.isNotBlank(candidateGroupType)
                && "role".equals(candidateGroupType)) {
                final String candidateGroupExpression = expressionText.split(":")[1];
                final List<UserDTO> list = userConnector.findByRoleIdAndTenantId(candidateGroupExpression,
                    tenantId);
                resultList.addAll(list);
            }
        }

        return resultList;
    }

    /**
     * 根据候选人表达式获取用户列表
     *
     * @param candidateUserIdExpressions
     *            获选人表达式
     * @return Set 封装用户信息的集合
     */
    private Set<UserDTO> getUserListByCandidateUserIdExpressions(
        Set<Expression> candidateUserIdExpressions) {
        final Set<UserDTO> resultList = new HashSet<UserDTO>();

		//GDDA8-2087 流程设置了选择提交、下一节点自定义规则设置了上一节点办理人，设置变量上一节点办理人为本人
		if (!map.map.containsKey(WorkflowConstants.WORKFLOW_DEFAULTVARIABLE_PRETASKASSIGNEE)) {
			map.map.put(WorkflowConstants.WORKFLOW_DEFAULTVARIABLE_PRETASKASSIGNEE, SecurityUtils.getUser().getId());
		}

        //配置候选人
        for (final Expression expression : candidateUserIdExpressions) {
            //获取表达式
            final String expressionText = expression.getExpressionText();
            if ("流程发起人".equals(expressionText)) { //如果配置的是流程发起人的情况下
                final String startUserId = (String) map.getVariables().get("initiator");
                resultList.add(userConnector.findById(startUserId));
            } else if (expressionText.startsWith("表达式:")) { //表达式:${test}
                final String userExpression = expressionText.substring(expressionText.indexOf(":") + 1);
                final Object parseExpression = ExpressionManagerUtil.getInstance()
                    .executeExpressionByVariableScope(map, userExpression);
                resultList.addAll(addResultListByExpression(parseExpression));
            } else { //配置了用户的情况下
                final String userId = expressionText.split(":")[1];
                resultList.add(userConnector.findById(userId));
            }
        }
        return resultList;
    }

    /**
     * 根据节点办理人表达式获取节点用户信息
     *
     * @param assigneeExpression
     *            节点办理人表达式
     * @return Set 封装用户信息的集合
     */
    private Set<UserDTO> getUserListByAssigneeExpression(Expression assigneeExpression) {
        final Set<UserDTO> resultList = new HashSet<UserDTO>();
        if (assigneeExpression != null) {
            //获取表达式
            final String expressionText = assigneeExpression.getExpressionText();
            if ("流程发起人".equals(expressionText)) { //如果配置的是流程发起人的情况下
                final String userId = (String) map.getVariables().get("initiator");
                resultList.add(userConnector.findById(userId));
            } else if ("${assignee}".equals(expressionText)) {
                //不做处理
            } else if (expressionText.startsWith("表达式")) { //表达式:${test} -> ${test}
                final String expression = expressionText.split(":")[1];
                final Object parseExpression = ExpressionManagerUtil.getInstance()
                    .executeExpressionByVariableScope(map, expression);
                resultList.addAll(addResultListByExpression(parseExpression));
            } else { //配置了一个用户的情况下 userName:userId -> userId
                final String userId = expressionText.split(":")[1];
                resultList.add(userConnector.findById(userId));
            }
        }
        return resultList;
    }

    /**
     * 根据表达式解析的结果，增加结果集
     *
     * @param parseExpression
     *            表达式解析的结果
     * @return Set 封装用户信息的集合
     */
    @SuppressWarnings("unchecked")
    private Set<UserDTO> addResultListByExpression(Object parseExpression) {
        final Set<UserDTO> resultList = new HashSet<UserDTO>();
        //结果为数组
        if (parseExpression instanceof String[]) {
            final String[] userIds = (String[]) parseExpression;
            for (final String userId : userIds) {
                final UserDTO userDTO = userConnector.findById(userId);
                resultList.add(userDTO);
            }
            //结果为集合
        } else if (parseExpression instanceof Collection) {
            final Collection<String> userIds = (Collection<String>) parseExpression;
            for (final String userId : userIds) {
                final UserDTO userDTO = userConnector.findById(userId);
                resultList.add(userDTO);
            }
            //其他
        } else {
            if (parseExpression != null) {
                final String userId = parseExpression.toString();
                resultList.add(userConnector.findById(userId));
            }
        }
        return resultList;
    }

    /**
     * 判断候选人组类型
     *
     * @param candidateGroupIdExpressions
     *            节点上所有的表达式
     * @return String， expression：表达式， group：部门，role：角色
     */
    private String checkCandidateGroupType(Set<Expression> candidateGroupIdExpressions) {
        //配置候选人组(多加一个根据角色或者部门查询所有用户的实现)
        //首先判断是什么类型的（部门 /角色 /表达式）
        String candidateGroupType = "";
        for (final Expression expression : candidateGroupIdExpressions) {
            final String expressionText = expression.getExpressionText();
            if (expressionText.startsWith("表达式")) {
                candidateGroupType = "expression";
                break;
            }
            if (expressionText.startsWith("(部门)")) {
                candidateGroupType = "group";
                break;
            }
            if (expressionText.startsWith("(角色)")) {
                candidateGroupType = "role";
                break;
            }
        }
        return candidateGroupType;
    }

    /**
     * 初始化
     *
     * @param commandContext
     *            上下文
     */
    public void init(CommandContext commandContext) {
        //this.taskEntity = findTask(commandContext);
        this.processDefinitionEntity = findProcessDefinition(commandContext);
        this.graph = new ActivitiGraphBuilder(processDefinitionId).build();
        this.userConnector = ApplicationContextHelper.getBean(UserConnector.class);
        this.taskInfoRunManager = ApplicationContextHelper.getBean(TaskInfoRunManager.class);
    }

    /**
     * 获取ProcessDefinition
     *
     * @param commandContext
     *            上下文
     * @return 流程定义实体
     */
    private ProcessDefinitionEntity findProcessDefinition(CommandContext commandContext) {
        return new GetDeploymentProcessDefinitionCmd(processDefinitionId).execute(commandContext);
    }

    /**
     * 查找进入的连线所有的节点ID 返回list
     */
    public List<String> findIncomingNodeList(Node node, List<String> result) {
        for (final Edge edge : node.getOutgoingEdges()) {
            //获取边界线的终点
            final Node dest = edge.getDest();
            prepareActivityIdsListByNode(dest, result);
        }
        return result;
    }

    /**
     * 根据节点信息获取所有下一个人工节点的信息
     *
     * @param node
     *            作为判断的节点信息
     * @param nextActivityIdsList
     *            分装结果集的集合
     */
    private void prepareActivityIdsListByNode(Node node, List<String> nextActivityIdsList) {
        final String type = node.getType();
        //如果是人工节点
        if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(type)) {
            nextActivityIdsList.add(node.getId());
        }
        //如果是子流程的情况下
        if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS.equals(type)) {
            final Node subNodes = node.getSubNodes();
            //如果是开始节点
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_STARTEVENT
                .equals(subNodes.getType())) {
                findIncomingNodeList(subNodes, nextActivityIdsList);
            }
        }

        //获取排他网关的节点
        ActivityImpl activityNode = processDefinitionEntity.findActivity(node.getId());
        if (WorkflowConstants.NodeTypeConstants.NODETYPE_ENDEVENT.equals(type)) {
            final ScopeImpl parent = activityNode.getParent();
            if (parent instanceof ActivityImpl) {
                activityNode = (ActivityImpl) parent;
                final Node parentNode = graph.findById(activityNode.getId());
                findIncomingNodeList(parentNode, nextActivityIdsList);
            } else {
                nextActivityIdsList.add(activityNode.getId());
            }

        }

        //获取当前排他网关 发散出去的线的集合
        final List<PvmTransition> outgoingTransitions = activityNode.getOutgoingTransitions();
        //如果是排他网关
        if (WorkflowConstants.NodeTypeConstants.NODETYPE_EXCLUSIVEGATEWAY.equals(type)) {
            boolean flag = true;//开关，默认走默认流
            //获取默认流ID
            final String defaultFlowId = (String) activityNode.getProperty("default");
            Node defaultNode = null;
            //遍历排他网关发散出去的线
            for (final PvmTransition pvm : outgoingTransitions) {
                //获取线上配置的条件，根据条件，执行第一个符合条件的节点
                final String expressionStr = pvm.getProperty("conditionText") == null ? ""
                    : pvm.getProperty("conditionText").toString();
                if (checkExpression(expressionStr)) {
                    final PvmActivity destination = pvm.getDestination();
                    final Node dest = graph.findById(destination.getId());
                    if (pvm.getId().equals(defaultFlowId)) {
                        defaultNode = dest;
                        continue;
                    }
                    flag = false;//有符合的条件，不走默认流
                    if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(dest.getType())) {
                        activeCount = 0;
                        activeCount = getGatewayActiveCount((ActivityImpl) destination);
                        if (activeCount > 0) {
                            return;
                        }
                    }
                    prepareActivityIdsListByNode(dest, nextActivityIdsList);
                    break;
                }
            }
            //是否走默认流
            if (flag) {
                if (StringUtils.isNoneBlank(defaultFlowId)) {
                    prepareActivityIdsListByNode(defaultNode, nextActivityIdsList);
                }
            }
        }
        //如果是包含网关
        if (WorkflowConstants.NodeTypeConstants.NODETYPE_INCLUSIVEGATEWAY.equals(type)) {
            activeCount = 0;
            activeCount = getGatewayActiveCount(activityNode);
            if (activeCount > 0) {
                return;
            }
            //遍历排他网关发散出去的线，根据条件执行后续所有符合调节的节点
            for (final PvmTransition pvm : outgoingTransitions) {
                //获取线上配置的条件
                final String expressionStr = pvm.getProperty("conditionText") == null ? ""
                    : pvm.getProperty("conditionText").toString();
                if (checkExpression(expressionStr)) {
                    final PvmActivity destination = pvm.getDestination();
                    final Node dest = graph.findById(destination.getId());
                    prepareActivityIdsListByNode(dest, nextActivityIdsList);
                }
            }
        }
        //如果是并行网关
        if (WorkflowConstants.NodeTypeConstants.NODETYPE_PARALLELGATEWAY.equals(type)) {
            int count = 0;
            count = getGatewayActiveCount(activityNode);
            if (count > 0) {
                return;
            }
            //遍历排他网关发散出去的线，所有的后续节点都要执行
            for (final PvmTransition pvm : outgoingTransitions) {
                final PvmActivity destination = pvm.getDestination();
                final Node dest = graph.findById(destination.getId());
                prepareActivityIdsListByNode(dest, nextActivityIdsList);
            }
        }
    }

    private int getGatewayActiveCount(ActivityImpl activityNode) {
        final List<PvmTransition> incomingTransitions = activityNode.getIncomingTransitions();
        final String processInstanceId = (String) this.map.getVariable("processInstanceId");
        for (final PvmTransition incomingTransition : incomingTransitions) {
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(incomingTransition.getSource().getProperty("type"))) {
                if (StringUtils.isNotEmpty(processInstanceId)) {
                    final List<TaskInfoHis> taskInfoHisList = taskInfoRunManager.find("from TaskInfoHis t "
                        + "where t.code = ?0 and t.processInstanceId = ?1 order by t.completeTime DESC ",
                        incomingTransition.getSource().getId(), processInstanceId);
                    if (taskInfoHisList != null && taskInfoHisList.size() > 0) {
                        final TaskInfoHis taskInfoHis = taskInfoHisList.get(0);
                        if (taskInfoHis != null
                            && WorkflowConstants.HumanTaskConstants.STATUS_ACTIVE.equals(taskInfoHis.getStatus())) {
                            activeCount++;
                        }
                    }
                }
            } else {
                getGatewayActiveCount((ActivityImpl) incomingTransition.getSource());
            }
        }
        return activeCount;
    }

    /**
     * 判断表达式 ： 表达式为空或者表达式解析结果为true 返回true
     *
     * @param expressionStr
     *            表达式的字符串
     * @return boolean
     */
    private boolean checkExpression(String expressionStr) {
        if (StringUtils.isNotBlank(expressionStr)) {
            String[] expressionSplit;
            final Map<String, Object> variables = map.getVariables();
            final Set<String> variablesSet = variables.keySet();
            String substring1;
            final String expressionSub = expressionStr.substring(2, expressionStr.length() - 1).replace(" ", "");
            if (expressionStr.contains("&&")) {
                expressionSplit = expressionSub.split("\\&\\&");
            } else {
                expressionSplit = expressionSub.split("\\|\\|");
            }
            if (expressionSplit.length > 1) {
                for (final String s : expressionSplit) {
                    Boolean flagExpression = true;
                    if (s.contains("!")) {
                        substring1 = s.substring(0, s.indexOf("!"));
                    } else {
                        substring1 = s.substring(0, s.indexOf("=", 0));
                    }
                    for (final String string : variablesSet) {
                        if (StringUtils.isNotBlank(expressionStr) && substring1.equals(string)) {
                            flagExpression = false;
                        }
                    }
                    if (flagExpression) {
                        variables.put(substring1, "");
                    }
                }
                this.map.setVariables(variables);
            }
        }
        return StringUtils.isBlank(expressionStr) || Boolean.parseBoolean(ExpressionManagerUtil
            .getInstance().executeExpressionByVariableScope(map, expressionStr).toString());
    }
}
