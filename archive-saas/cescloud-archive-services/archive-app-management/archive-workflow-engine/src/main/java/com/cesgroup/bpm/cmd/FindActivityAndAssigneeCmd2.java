package com.cesgroup.bpm.cmd;

import com.cesgroup.api.user.UserConnector;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.NodeConfInfo;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 获取节点和节点的办理人
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class FindActivityAndAssigneeCmd2 implements Command<Map<String, String>> {

    private static Logger logger = LoggerFactory.getLogger(FindActivityAndAssigneeCmd2.class);

    private String processDefinitionId;

    private UserConnector userConnector;

    public FindActivityAndAssigneeCmd2(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        this.userConnector = ApplicationContextHelper.getBean(UserConnector.class);
    }

    @Override
    public Map<String, String> execute(CommandContext commandContext) {
        if (StringUtils.isBlank(processDefinitionId)) {
            logger.info("流程定义为空");
            return null;
        }
        ProcessDefinitionEntity definitionEntity = new GetDeploymentProcessDefinitionCmd(
            processDefinitionId).execute(commandContext);
        List<ActivityImpl> activityImplList = definitionEntity.getActivities();
        List<NodeConfInfo> nodeConfInfosByActivities = getNodeConfInfosByActivities(
            activityImplList);
        StringBuilder activityIds = new StringBuilder(); //节点ID集合
        StringBuilder activityAssignees = new StringBuilder(); //节点ID对应处理人集合
        for (NodeConfInfo nodeConfInfo : nodeConfInfosByActivities) {
            activityIds.append(nodeConfInfo.getId()).append(";");
            for (UserDTO user : nodeConfInfo.getUsers()) {
                activityAssignees.append(user.getDisplayName()).append(",");
            }
            activityAssignees.deleteCharAt(activityAssignees.length() - 1).append(";");
        }
        activityIds.deleteCharAt(activityIds.length() - 1);
        activityAssignees.deleteCharAt(activityAssignees.length() - 1);
        Map<String, String> result = new HashMap<String, String>();
        result.put("activityIds", new String(activityIds));
        result.put("activityAssignees", new String(activityAssignees));
        return result;
    }

    /**
     * @param activityImplList 节点信息集合
     */
    private List<NodeConfInfo> getNodeConfInfosByActivities(List<ActivityImpl> activityImplList) {
        List<NodeConfInfo> result = new ArrayList<NodeConfInfo>();
        for (ActivityImpl activityImpl : activityImplList) {
            logger.info("{}", activityImpl.getProperties());
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(activityImpl.getProperty("type"))) {
                NodeConfInfo nodeConfInfo = getNodeConfInfoByActivityImpl(activityImpl);
                result.add(nodeConfInfo);
            }
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_CALL_ACTIVITY
                .equals(activityImpl.getProperty("type"))) { //调用活动
                //
                logger.info("--------------FindActivityAndAssigneeCmd.callActivity()");
            }
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                .equals(activityImpl.getProperty("type"))) { //若是子流程，拿到初始化节点
                List<ActivityImpl> activities = activityImpl.getActivities();
                result.addAll(getNodeConfInfosByActivities(activities));
            }
        }
        return result;
    }

    /**
     * @param activityImpl 节点实体
     */
    private NodeConfInfo getNodeConfInfoByActivityImpl(ActivityImpl activityImpl) {
        TaskDefinition taskDefinition = (TaskDefinition) activityImpl.getProperty("taskDefinition");
        //定义一个节点的实体
        NodeConfInfo nodeConfInfo = new NodeConfInfo();
        nodeConfInfo.setId(activityImpl.getId());
        nodeConfInfo.setName(activityImpl.getProperty("name") == null ? ""
            : (String) activityImpl.getProperty("name"));
        nodeConfInfo.setUsers(getUsersByTaskDefinition(taskDefinition));
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
        Set<UserDTO> resultList = new HashSet<UserDTO>();
        //获取节点配置的办理人
        Expression assigneeExpression = taskDefinition.getAssigneeExpression();
        //获取节点配置的候选人组
        Set<Expression> candidateGroupIdExpressions = taskDefinition
            .getCandidateGroupIdExpressions();
        //获取节点配置的候选人
        Set<Expression> candidateUserIdExpressions = taskDefinition.getCandidateUserIdExpressions();

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
        String candidateGroupType = checkCandidateGroupType(candidateGroupIdExpressions);

        Set<UserDTO> resultList = new HashSet<UserDTO>();

        //配置候选人组
        for (Expression expression : candidateGroupIdExpressions) {
            String expressionText = expression.getExpressionText();
            //候选人组 配置表达式的情况下
            if (StringUtils.isNotBlank(candidateGroupType)
                && "expression".equals(candidateGroupType)) {
                resultList.add(UserDTO.expressionUserDto(expressionText));
            } else if (StringUtils.isNotBlank(candidateGroupType)
                && "group".equals(candidateGroupType)) {
                String candidateGroupExpression = expressionText.split(":")[0];
                resultList.add(UserDTO.expressionUserDto(candidateGroupExpression));
            } else if (StringUtils.isNotBlank(candidateGroupType)
                && "role".equals(candidateGroupType)) {
                String candidateGroupExpression = expressionText.split(":")[0];
                resultList.add(UserDTO.expressionUserDto(candidateGroupExpression));
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
        Set<UserDTO> resultList = new HashSet<UserDTO>();
        //配置候选人
        for (Expression expression : candidateUserIdExpressions) {
            //获取表达式
            String expressionText = expression.getExpressionText();
            if ("流程发起人".equals(expressionText)) { //如果配置的是流程发起人的情况下
                resultList.add(UserDTO.expressionUserDto(expressionText));
            } else if (expressionText.startsWith("表达式") 
                || expressionText.indexOf("${") >= 0) { //表达式:${test}
                resultList.add(UserDTO.expressionUserDto(expressionText));
            } else { //配置了用户的情况下
                String userId = expressionText.split(":")[1];
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
        Set<UserDTO> resultList = new HashSet<UserDTO>();
        if (assigneeExpression != null) {
            //获取表达式
            String expressionText = assigneeExpression.getExpressionText();
            if ("流程发起人".equals(expressionText)) { //如果配置的是流程发起人的情况下
                resultList.add(UserDTO.expressionUserDto(expressionText));
            } else if ("${assignee}".equals(expressionText)) {
                //不做处理
            } else if (expressionText.startsWith("表达式")) { //表达式:${test} -> ${test}
                resultList.add(UserDTO.expressionUserDto(expressionText));
            } else { //配置了一个用户的情况下 userName:userId -> userId
                String userId = expressionText.split(":")[1];
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
        for (Expression expression : candidateGroupIdExpressions) {
            String expressionText = expression.getExpressionText();
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
}
