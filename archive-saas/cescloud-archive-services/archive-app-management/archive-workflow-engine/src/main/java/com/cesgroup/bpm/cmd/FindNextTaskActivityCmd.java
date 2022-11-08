package com.cesgroup.bpm.cmd;

import com.cesgroup.core.util.WorkflowConstants;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 查找下一步人工任务节点及其处理人
 * 
 * @owner xuhw3
 * @date 2017/4/11.
 * @description
 */
public class FindNextTaskActivityCmd implements Command<List<Map<String, String>>> {

    private String executionId;

    public FindNextTaskActivityCmd(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public List<Map<String, String>> execute(CommandContext commandContext) {
        ExecutionEntity executionEntity = Context.getCommandContext().getExecutionEntityManager()
            .findExecutionById(executionId);
        ActivityImpl currentActivity = executionEntity.getActivity();
        List<PvmTransition> outgoingTransitions = currentActivity.getOutgoingTransitions();
        PvmActivity destination;
        List<Map<String, String>> destTaskActivityList = new ArrayList<Map<String, String>>();
        Map<String, String> destTaskActivityMap;
        TaskDefinition taskDefinition;
        Expression assigneeExpression;
        Set<Expression> candidateUserIdExpressions; //候选人
        Set<Expression> candidateGroupIdExpressions; //候选组
        String expressionText = null;
        StringBuilder userIdBuilder = null;
        String[] expressionAssigneeArray = null;
        String nextNodeType = null;
        for (PvmTransition pvmTransition : outgoingTransitions) {
            destination = pvmTransition.getDestination();
            nextNodeType = (String) destination.getProperty("type");

            //人工任务节点(包含会签)
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK.equals(nextNodeType)) {
                taskDefinition = (TaskDefinition) destination.getProperty("taskDefinition");
                assigneeExpression = taskDefinition.getAssigneeExpression(); //节点处理人表达式

                candidateUserIdExpressions = taskDefinition.getCandidateUserIdExpressions();
                candidateGroupIdExpressions = taskDefinition.getCandidateGroupIdExpressions();

                destTaskActivityMap = new HashMap<String, String>();
                destTaskActivityMap.put("id", destination.getId());
                destTaskActivityMap.put("name", (String) destination.getProperty("name")); //节点名称
                destTaskActivityMap.put("comment",
                    (String) destination.getProperty("documentation")); //节点描述信息

                if (assigneeExpression != null && assigneeExpression.getExpressionText() != null) {
                    expressionText = assigneeExpression.getExpressionText();
                    if (!"流程发起人".equals(expressionText) && !expressionText.contains("$")
                        && expressionText.indexOf(":") > 0) {
                        //默认节点处理人
                        destTaskActivityMap.put("defaultAssignee", expressionText.split(":")[0]); 
                    } else {
                        destTaskActivityMap.put("defaultAssignee", "processStarter");
                    }
                    destTaskActivityMap.put("type", "user");
                }

                //一个节点可以同时配置候选人与候选组两种情况
                userIdBuilder = new StringBuilder();
                if (candidateUserIdExpressions.size() >= 1) {
                    for (Expression userIdExpression : candidateUserIdExpressions) {
                        if (userIdExpression != null) {
                            expressionText = userIdExpression.getExpressionText();
                            if (expressionText != null && (!"".equals(expressionText))
                                && expressionText.contains(":")) {
                                expressionAssigneeArray = expressionText.split(":");
                                userIdBuilder.append(expressionAssigneeArray[0]).append(",");
                                destTaskActivityMap.put("type", "user");
                            }
                        }
                    }
                }

                if (candidateGroupIdExpressions.size() >= 1) {
                    for (Expression groupIdExpression : candidateGroupIdExpressions) {
                        if (groupIdExpression != null) {
                            expressionText = groupIdExpression.getExpressionText();
                            if (expressionText != null && (!"".equals(expressionText))
                                && expressionText.contains(":")) {
                                expressionAssigneeArray = expressionText.split(":");
                                if (expressionAssigneeArray[0].startsWith("(")
                                    && expressionAssigneeArray[0].contains("角色")) { //针对：(角色)研发工程师
                                    userIdBuilder.append(expressionAssigneeArray[0].substring(4,
                                        expressionAssigneeArray[0].length())).append(",");
                                    destTaskActivityMap.put("type", "role");
                                } else {
                                    userIdBuilder.append(expressionAssigneeArray[0]).append(",");
                                    destTaskActivityMap.put("type", "org");
                                }

                            }
                        }
                    }
                }

                if (null != userIdBuilder && !"".equals(userIdBuilder.toString())) {
                    destTaskActivityMap.put("defaultAssignee",
                        userIdBuilder.substring(0, userIdBuilder.length() - 1));
                }

                destTaskActivityList.add(destTaskActivityMap);
            }
        }
        return destTaskActivityList;
    }
}
