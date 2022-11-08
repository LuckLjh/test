package com.cesgroup.bpm.listener;

import com.cesgroup.bpm.rule.AssigneeRule;
import com.cesgroup.bpm.rule.PositionAssigneeRule;
import com.cesgroup.bpm.rule.RuleMatcher;
import com.cesgroup.bpm.rule.SuperiorAssigneeRule;
import com.cesgroup.bpm.support.DefaultTaskListener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理人别名任务监听器
 * 
 * @author 国栋
 *
 */
public class AssigneeAliasTaskListener extends DefaultTaskListener {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(AssigneeAliasTaskListener.class);

    // private JdbcTemplate jdbcTemplate;
    private Map<RuleMatcher, AssigneeRule> assigneeRuleMap = 
        new HashMap<RuleMatcher, AssigneeRule>();

    /**
     * constructor
     */
    public AssigneeAliasTaskListener() {
        SuperiorAssigneeRule superiorAssigneeRule = new SuperiorAssigneeRule();
        PositionAssigneeRule positionAssigneeRule = new PositionAssigneeRule();
        assigneeRuleMap.put(new RuleMatcher("常用语"), superiorAssigneeRule);
        assigneeRuleMap.put(new RuleMatcher("岗位"), positionAssigneeRule);
    }

    @Override
    public void onCreate(DelegateTask delegateTask) throws Exception {
        String assignee = delegateTask.getAssignee();
        logger.debug("任务监听器(onCreate) 任务处理人： {}", assignee);

        if (assignee == null) {
            return;
        }

        for (Map.Entry<RuleMatcher, AssigneeRule> entry : assigneeRuleMap.entrySet()) {
            RuleMatcher ruleMatcher = entry.getKey();

            if (!ruleMatcher.matches(assignee)) {
                continue;
            }

            String value = ruleMatcher.getValue(assignee);
            AssigneeRule assigneeRule = entry.getValue();
            logger.debug("值： {}", value);
            logger.debug("处理人规则： {}", assigneeRule);

            if (assigneeRule instanceof SuperiorAssigneeRule) {
                this.processSuperior(delegateTask, assigneeRule, value);
            } else if (assigneeRule instanceof PositionAssigneeRule) {
                this.processPosition(delegateTask, assigneeRule, value);
            }
        }
    }

    /**
     * 根据规则设置任务办理人
     */
    public void processSuperior(DelegateTask delegateTask, AssigneeRule assigneeRule,
        String value) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        String startUserId = Context.getCommandContext().getHistoricProcessInstanceEntityManager()
            .findHistoricProcessInstance(processInstanceId).getStartUserId();
        String userId = assigneeRule.process(startUserId);
        logger.debug("用户id： {}", userId);
        delegateTask.setAssignee(userId);
    }

    /**
     * 根据规则设置任务办理人
     */
    public void processPosition(DelegateTask delegateTask, AssigneeRule assigneeRule,
        String value) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        String startUserId = Context.getCommandContext().getHistoricProcessInstanceEntityManager()
            .findHistoricProcessInstance(processInstanceId).getStartUserId();
        List<String> userIds = assigneeRule.process(value, startUserId);
        logger.debug("用户ids： {}", userIds);

        if (!userIds.isEmpty()) {
            delegateTask.setAssignee(userIds.get(0));
        }
    }

}
