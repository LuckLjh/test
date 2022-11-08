package com.cesgroup.bpm.cmd;

import com.cesgroup.core.util.WorkflowConstants;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright:Copyright(c) 2017
 * Company:上海中信信息发展股份有限公司
 * 包名:com.cesgroup.bpm.cmd
 * 文件名:FindActivityAndAssigneeCmd
 * 获取流程所有节点及其处理人(办理人+候选人+候选人组)
 * 
 * @author xuhw3
 * @date 2017/5/2
 * @todo
 */
public class FindActivityAndAssigneeCmd implements Command<List<Map<String, Object>>> {

    private static Logger logger = LoggerFactory.getLogger(FindActivityAndAssigneeCmd.class);

    private String processDefinitionId;

    public FindActivityAndAssigneeCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public List<Map<String, Object>> execute(CommandContext commandContext) {
        List<Map<String, Object>> activityAssigneeList = new ArrayList<Map<String, Object>>();
        Map<String, Object> activityMap = null;
        ProcessDefinitionEntity definitionEntity = new GetDeploymentProcessDefinitionCmd(
            processDefinitionId).execute(commandContext);
        List<ActivityImpl> activities = definitionEntity.getActivities();
        for (ActivityImpl activity : activities) {
            logger.info("{}", activity.getProperties());
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(activity.getProperty("type"))) {
                activityMap = new HashMap<String, Object>();
                findAssigneeByUserTask(activity, activityMap);
                activityAssigneeList.add(activityMap);
            }
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_CALL_ACTIVITY
                .equals(activity.getProperty("type"))) { //调用活动
                //
                logger.info("--------------FindActivityAndAssigneeCmd.callActivity()");
            }
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                .equals(activity.getProperty("type"))) { //若是子流程，拿到初始化节点
                ActivityImpl initial = (ActivityImpl) activity.getProperty("initial");
                findSubProcessAssignee(initial.getOutgoingTransitions(), activityAssigneeList);
            }
        }
        return activityAssigneeList;
    }

    private void findSubProcessAssignee(List<PvmTransition> outgoingTransitions,
        List<Map<String, Object>> activityAssigneeList) {
        PvmActivity destination = null;
        Map<String, Object> map = null;
        for (PvmTransition pvm : outgoingTransitions) {
            destination = pvm.getDestination();
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(destination.getProperty("type"))) {
                map = new HashMap<String, Object>();
                findAssigneeByUserTask((ActivityImpl) destination, map);
                activityAssigneeList.add(map);
                findSubProcessAssignee(destination.getOutgoingTransitions(), activityAssigneeList);
            }
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                .equals(destination.getProperty("type"))) {
                //暂不考虑子流程嵌套的问题
            }
        }
    }

    /**
     * 查询用户任务节点对应的处理人
     * 
     * @param activity 节点对象
     * @param map 参数
     * @return void
     */
    private Void findAssigneeByUserTask(ActivityImpl activity, Map<String, Object> map) {
        TaskDefinition taskDefinition = (TaskDefinition) activity.getProperty("taskDefinition");
        Expression assigneeExpression = taskDefinition.getAssigneeExpression();
        Set<Expression> userIdExpressions = taskDefinition.getCandidateUserIdExpressions();
        Set<Expression> groupIdExpressions = taskDefinition.getCandidateGroupIdExpressions();
        StringBuilder userIdBuilder = new StringBuilder();
        StringBuilder groupIdBuilder = new StringBuilder();
        if (assigneeExpression != null) {
            String expressionText = assigneeExpression.getExpressionText();
            if (expressionText != null && !"".equals(expressionText)
                && !expressionText.contains("$")) {
                map.put("activityId", activity.getId());
                map.put("activityAssigneeType", "user"); //可能会出现'流程发起人'
                map.put("assigneeExpression", expressionText);
            }
        }

        if (userIdExpressions != null && userIdExpressions.size() >= 1) {
            String expressionText = null;
            map.put("activityId", activity.getId());
            map.put("activityAssigneeType", "candidateUser"); //可能会出现'流程发起人'
            for (Expression express : userIdExpressions) {
                expressionText = express.getExpressionText();
                if (expressionText != null && !"".equals(expressionText)) {
                    userIdBuilder.append(expressionText).append(";");
                }
            }
            map.put("assigneeExpression", userIdBuilder.substring(0, userIdBuilder.length() - 1));
        }

        if (groupIdExpressions != null && groupIdExpressions.size() >= 1) {
            String expressionText = null;
            map.put("activityId", activity.getId());
            map.put("activityAssigneeType", "candidateGroup"); //可能会出现'流程发起人'
            for (Expression express : groupIdExpressions) {
                expressionText = express.getExpressionText();
                if (expressionText != null && !"".equals(expressionText)) {
                    groupIdBuilder.append(expressionText).append(";");
                }
            }
            map.put("assigneeExpression", groupIdBuilder.substring(0, groupIdBuilder.length() - 1));
        }
        return null;
    }
}
