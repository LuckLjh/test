/**
 * <p>Copyright:Copyright(c) 2018</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.api.core.workflow</p>
 * <p>文件名:CheckGraphIsConfMajorLineCmd.java</p>
 * <p>创建时间:2018-05-08 18:22</p>
 * <p>作者:huz</p>
 */

package com.cesgroup.bpm.cmd;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;

import java.util.List;
import java.util.Set;




/**
 * 
 * @author huz
 * @version 1.0.0 2018-05-08
 */
public class CheckGraphIsConfMajorLineCmd implements Command<Boolean> {
    
    private String processDefinitionId;

    /**
     * @param processDefinitionId
     * 
     */
    public CheckGraphIsConfMajorLineCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    /** 
     * 
     * @see org.activiti.engine.impl.interceptor.Command#
     * execute(org.activiti.engine.impl.interceptor.CommandContext) 
     */
    @Override
    public Boolean execute(CommandContext commandContext) {
        ProcessDefinitionEntity processDefinitionEntity = findProcessDefinition(commandContext);
        List<ActivityImpl> activities = processDefinitionEntity.getActivities();
        for (ActivityImpl activityImpl : activities) {
            if ("userTask".equals(activityImpl.getProperty("type"))) {
                //获取当前节点配置的任务信息
                TaskDefinition taskDefinition = (TaskDefinition) activityImpl.getProperty(
                        "taskDefinition");
                //获取节点配置的办理人
                Expression assigneeExpression = taskDefinition.getAssigneeExpression();
                //获取节点配置的候选人组
                Set<Expression> candidateGroupIdExpressions = taskDefinition
                    .getCandidateGroupIdExpressions();
                //获取节点配置的候选人
                Set<Expression> candidateUserIdExpressions = taskDefinition
                        .getCandidateUserIdExpressions();
                if (assigneeExpression != null) {
                    String expressionText = assigneeExpression.getExpressionText();
                    if (expressionText.contains("\"checkMajorLine\":\"1\"")) {
                        return true;
                    }
                }
                
                for (Expression expression : candidateUserIdExpressions) {
                    String expressionText = expression.getExpressionText();
                    if (expressionText.contains("\"checkMajorLine\":\"1\"")) {
                        return true;
                    }
                }
                
                for (Expression expression : candidateGroupIdExpressions) {
                    String expressionText = expression.getExpressionText();
                    if (expressionText.contains("\"checkMajorLine\":\"1\"")) {
                        return true;
                    }
                }
            }
          
        }
        
        
        
        
        
        return false;
    }
    
    private ProcessDefinitionEntity findProcessDefinition(CommandContext commandContext) {
        return new GetDeploymentProcessDefinitionCmd(processDefinitionId).execute(commandContext);
    }

}
