package com.cesgroup.bpm.listener;

import com.cesgroup.bpm.expr.Expr;
import com.cesgroup.bpm.expr.ExprProcessor;
import com.cesgroup.bpm.support.DefaultTaskListener;
import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.spi.process.InternalProcessConnector;
import com.cesgroup.spi.process.ParticipantDefinition;
import com.cesgroup.spi.process.ProcessTaskDefinition;
import com.cesgroup.workflow.expression.ExpressionManagerUtil;

import org.activiti.engine.delegate.DelegateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 人工任务 任务监听器
 * 
 * @author 国栋
 *
 */
public class HumanTaskUserTaskListener extends DefaultTaskListener implements ExprProcessor {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(HumanTaskUserTaskListener.class);

    private InternalProcessConnector internalProcessConnector;
    // private BeanMapper beanMapper = new BeanMapper();

    @Override
    public void onCreate(DelegateTask delegateTask) throws Exception {
        String processDefinitionId = delegateTask.getProcessDefinitionId();
        String businessKey = delegateTask.getExecution().getProcessBusinessKey();
        String taskDefinitionKey = delegateTask.getExecution().getCurrentActivityId();
        ProcessTaskDefinition processTaskDefinition = internalProcessConnector
            .findTaskDefinition(processDefinitionId, businessKey, taskDefinitionKey);
        for (ParticipantDefinition participantDefinition : processTaskDefinition
            .getParticipantDefinitions()) {
            if ("user".equals(participantDefinition.getType())) {
                if ("add".equals(participantDefinition.getStatus())) {
                    delegateTask.addCandidateUser(participantDefinition.getValue());
                } else {
                    delegateTask.deleteCandidateUser(participantDefinition.getValue());
                }
            } else {
                if ("add".equals(participantDefinition.getStatus())) {
                    delegateTask.addCandidateGroup(participantDefinition.getValue());
                } else {
                    delegateTask.deleteCandidateGroup(participantDefinition.getValue());
                }
            }
        }

        String assignee = null;

        if (processTaskDefinition.getAssignee() != null) {
            assignee = ExpressionManagerUtil.getInstance()
                .executeExpressionByVariableScope(delegateTask, processTaskDefinition.getAssignee())
                .toString();
        }

        if (assignee == null) {
            delegateTask.setAssignee(null);
        } else if ((assignee.indexOf("&&") != -1) || (assignee.indexOf("||") != -1)) {
            logger.debug("处理人： {}", assignee);

            List<String> candidateUsers = new Expr().evaluate(assignee, this);
            logger.debug("候选人： {}", candidateUsers);
            delegateTask.addCandidateUsers(candidateUsers);
        } else {
            delegateTask.setAssignee(assignee);
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

        logger.info("用户名： {}", usernames);

        return usernames;
    }

    @Autowired
    public void setInternalProcessConnector(InternalProcessConnector internalProcessConnector) {
        this.internalProcessConnector = internalProcessConnector;
    }
}
