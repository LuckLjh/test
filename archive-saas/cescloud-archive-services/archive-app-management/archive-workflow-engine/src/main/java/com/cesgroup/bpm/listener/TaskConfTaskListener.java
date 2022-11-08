package com.cesgroup.bpm.listener;

import com.cesgroup.bpm.expr.Expr;
import com.cesgroup.bpm.expr.ExprProcessor;
import com.cesgroup.bpm.support.DefaultTaskListener;
import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.workflow.expression.ExpressionManagerUtil;

import org.activiti.engine.delegate.DelegateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 国栋
 *
 */
public class TaskConfTaskListener extends DefaultTaskListener implements ExprProcessor {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(TaskConfTaskListener.class);

    @Override
    public void onCreate(DelegateTask delegateTask) throws Exception {
        String businessKey = delegateTask.getExecution().getProcessBusinessKey();
        String taskDefinitionKey = delegateTask.getTaskDefinitionKey();
        JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        try {
            String sql = "select ASSIGNEE from T_WF_BPM_TASK_CONF where BUSINESS_KEY=? "
                + " and TASK_DEFINITION_KEY=?";
            String assignee = jdbcTemplate.queryForObject(sql, String.class, businessKey,
                taskDefinitionKey);

            if ((assignee == null) || "".equals(assignee)) {
                return;
            }

            if ((assignee.indexOf("&&") != -1) || (assignee.indexOf("||") != -1)) {
                logger.info("办理人 : {}", assignee);

                List<String> candidateUsers = new Expr().evaluate(assignee, this);
                logger.info("候选人 : {}", candidateUsers);
                delegateTask.addCandidateUsers(candidateUsers);
            } else {
                String value = ExpressionManagerUtil.getInstance()
                    .executeExpressionByVariableScope(delegateTask, assignee).toString();
                delegateTask.setAssignee(value);
            }
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
        }
    }

    @Override
    public List<String> process(List<String> left, List<String> right, String operation) {
        if ("||".equals(operation)) {
            Set<String> set = new HashSet<String>();
            set.addAll(left);
            set.addAll(right);

            return new ArrayList<String>(set);
        } else if ("&&".equals(operation)) {
            List<String> list = new ArrayList<String>();

            for (String username : left) {
                if (right.contains(username)) {
                    list.add(username);
                }
            }

            return list;
        } else {
            throw new UnsupportedOperationException(operation);
        }
    }

    @Override
    public List<String> process(String text) {
        JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        String sql = "select child.NAME from T_WF_PARTY_ENTITY parent,T_WF_PARTY_STRUCT ps,"
            + "T_WF_PARTY_ENTITY child,T_WF_PARTY_TYPE child_type"
            + " where parent.ID=ps.PARENT_ENTITY_ID and ps.CHILD_ENTITY_ID=child.ID "
            + " and child.TYPE_ID=child_type.ID"
            + " and child_type.PERSON=1 and parent.NAME=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, text);
        List<String> usernames = new ArrayList<String>();

        for (Map<String, Object> map : list) {
            usernames.add(map.get("name").toString().toLowerCase());
        }

        logger.info("用户名 : {}", usernames);

        return usernames;
    }

}
