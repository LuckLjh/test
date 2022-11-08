package com.cesgroup.bpm.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cesgroup.api.process.ProcessConnector;
import com.cesgroup.bpm.persistence.domain.BpmConfUser;
import com.cesgroup.bpm.persistence.manager.BpmConfUserManager;
import com.cesgroup.bpm.support.DefaultTaskListener;
import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.TaskInfoRun;
import com.cesgroup.humantask.persistence.domain.TaskParticipant;
import com.cesgroup.humantask.persistence.manager.TaskInfoRunManager;
import com.cesgroup.humantask.persistence.manager.TaskParticipantManager;
import com.cesgroup.workflow.expression.ExpressionManagerUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 根据配置表中的信息给任务赋相应的配置值
 *
 * @author 王国栋
 *
 */
public class ConfUserTaskListener extends DefaultTaskListener {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(ConfUserTaskListener.class);

    private BpmConfUserManager bpmConfUserManager;

    @Autowired
    private TaskInfoRunManager taskInfoRunManager;

    @Autowired
    private TaskParticipantManager taskParticipantManager;

    @Autowired
    private ProcessConnector processConnector;

    /** 实例总数 **/
    private static final String NR_OF_INSTANCES = "nrOfInstances";

    /** 当前活动的，比如，还没完成的，实例数量。 对于顺序执行的多实例，值一直为1。 **/
    private static final String NR_OF_ACTIVEINSTANCES = "nrOfActiveInstances";

    @Override
    @SuppressWarnings({ "unchecked" })
    public void onCreate(DelegateTask delegateTask) throws Exception {
        final List<BpmConfUser> bpmConfUsers = bpmConfUserManager.find(
            "from BpmConfUser where bpmConfNode.bpmConfBase.processDefinitionId=?0 "
                + " and bpmConfNode.code=?1",
            delegateTask.getProcessDefinitionId(),
            delegateTask.getExecution().getCurrentActivityId());
        logger.debug("{}", bpmConfUsers);
        if (null == bpmConfUsers || bpmConfUsers.isEmpty()) {
            return;
        }
        final Map<String, Object> variables = Context.getProcessEngineConfiguration()
            .getRuntimeService().getVariables(delegateTask.getExecutionId());
        final String codeList = (String) variables.get("_codeList");
        final String assigneeList = (String) variables.get("_codeAssigneeList");

        if (StringUtils.isNotEmpty(codeList) && StringUtils.isNotEmpty(assigneeList)
            && (delegateTask.getTaskDefinitionKey().equals(codeList) || "defaultRunNodeWorkflowNode".equals(codeList))
            && !"${assignee}".equals(bpmConfUsers.get(0).getValue())) {

            final ExecutionEntity executionEntity = (ExecutionEntity) delegateTask.getExecution();
            final List<IdentityLinkEntity> identityLinks = executionEntity.getIdentityLinks();
            identityLinks.removeAll(identityLinks);
            final Set<IdentityLink> candidates = delegateTask.getCandidates();
            for (final IdentityLink identityLink : candidates) {

                if (identityLink.getUserId() != null) {
                    delegateTask.deleteCandidateUser(identityLink.getUserId());
                }
                if (identityLink.getGroupId() != null) {
                    delegateTask.deleteCandidateGroup(identityLink.getGroupId());
                }
            }
            final String taskId = delegateTask.getId();
            final TaskInfoRun taskInfoRun = (TaskInfoRun) taskInfoRunManager.findByTaskId(taskId);
            //如果不是会签节点就是抢占节点 抢占的id存放在taskParticipantManager表中
            if (!WorkflowConstants.YES.equals(taskInfoRun.getIsCountersign())) {
                taskParticipantManager.removeByTaskInfoId(taskInfoRun.getId());
            }
            final String assignUser = (String) variables.get("_codeAssigneeList");
            if (StringUtils.isNotBlank(assignUser)) {
                final Set<String> userIdSet = new LinkedHashSet<String>(8);
                final String[] assignUserArr = assignUser.split(";");
                for (final String assArr : assignUserArr) {
                    final String[] s = ((String) assArr).split(":");
                    if (s.length == 2) {
                        userIdSet.add(s[1]);
                    } else {
                        userIdSet.add(s[0]);
                    }
                }
                saveTaskParticipants(delegateTask, taskInfoRun, userIdSet, "user");
            }
            return;
        }
        if (!bpmConfUsers.get(0).getValue().contains("表达式:${customRuleService")
            || (bpmConfUsers.size() >= 2
                && (!bpmConfUsers.get(1).getValue().contains("表达式:${customRuleService")))) {

            for (final BpmConfUser bpmConfUser : bpmConfUsers) {
                logger.debug("状态 : {}, 类型: {}", bpmConfUser.getStatus(), bpmConfUser.getType());
                logger.debug("值 : {}", bpmConfUser.getValue());
                final String value = ExpressionManagerUtil.getInstance()
                    .executeExpressionByVariableScope(delegateTask, bpmConfUser.getValue())
                    .toString();

                if (bpmConfUser.getStatus() == 1) {
                    if (bpmConfUser.getType() == 0) {
                        delegateTask.setAssignee(value);
                    } else if (bpmConfUser.getType() == 1) {
                        delegateTask.addCandidateUser(value);
                    } else if (bpmConfUser.getType() == 2) {
                        delegateTask.addCandidateGroup(value);
                    }
                } else if (bpmConfUser.getStatus() == 2) {
                    if (bpmConfUser.getType() == 0) {
                        if (delegateTask.getAssignee().equals(value)) {
                            delegateTask.setAssignee(null);
                        }
                    } else if (bpmConfUser.getType() == 1) {
                        delegateTask.deleteCandidateUser(value);
                    } else if (bpmConfUser.getType() == 2) {
                        delegateTask.deleteCandidateGroup(value);
                    }
                }
            }
        } else {
            // 获取表达式执行器，准备执行表达式
            try {
                List<String> firstList = null;
                List<String> newList = null;
                List<String> changeList = null;
                final Set<String> userIdSet = new LinkedHashSet<String>();
                final Set<String> tempSet = new HashSet<String>();
                int temp = 0;
                //删除delegateTask中配置的候选人或者候选人组
                final Set<IdentityLink> candidates = delegateTask.getCandidates();
                for (final IdentityLink identityLink : candidates) {

                    if (identityLink.getUserId() != null) {
                        delegateTask.deleteCandidateUser(identityLink.getUserId());
                        temp = 1;
                    }
                    if (identityLink.getGroupId() != null) {
                        delegateTask.deleteCandidateGroup(identityLink.getGroupId());
                        temp = 2;
                    }
                }
                //删除任务参与表中的相应任务的待办人
                final String taskId = delegateTask.getId();
                final TaskInfoRun taskInfoRun = (TaskInfoRun) taskInfoRunManager.findByTaskId(taskId);
                //如果不是会签节点就是抢占节点 抢占的id存放在taskParticipantManager表中
                if (!WorkflowConstants.YES.equals(taskInfoRun.getIsCountersign())) {
                    taskParticipantManager.removeByTaskInfoId(taskInfoRun.getId());
                }
                for (final BpmConfUser bpmConfUser : bpmConfUsers) {
                    logger.debug("状态 : {}, 类型: {}", bpmConfUser.getStatus(), bpmConfUser.getType());
                    logger.debug("值 : {}", bpmConfUser.getValue());
                    final String value = ExpressionManagerUtil.getInstance()
                        .executeExpressionByVariableScope(delegateTask, bpmConfUser.getValue())
                        .toString();

                    if (bpmConfUser.getStatus() == 1) {
                        if (bpmConfUser.getType() == 0) {
                            delegateTask.setAssignee(value);
                        } else if (bpmConfUser.getType() == 1) {
                            delegateTask.addCandidateUser(value);
                        } else if (bpmConfUser.getType() == 2) {
                            delegateTask.addCandidateGroup(value);
                        }
                    } else if (bpmConfUser.getStatus() == 2) {
                        if (bpmConfUser.getType() == 0) {
                            if (delegateTask.getAssignee().equals(value)) {
                                delegateTask.setAssignee(null);
                            }
                        } else if (bpmConfUser.getType() == 1) {
                            delegateTask.deleteCandidateUser(value);
                        } else if (bpmConfUser.getType() == 2) {
                            delegateTask.deleteCandidateGroup(value);
                        }
                    }
                    final int index = bpmConfUsers.indexOf(bpmConfUser);
                    if (index == 0) {
                        final String[] lastStr = value.replace("[", "").replace("]", "").split(",");
                        firstList = Arrays.asList(lastStr);
                        changeList = new ArrayList<String>(firstList);
                        if (changeList.size() > 0) {
                            for (final String str : changeList) {
                                userIdSet.add(str.trim());
                            }
                        }
                    } else {
                        final String[] newStr = value.replace("[", "").replace("]", "").split(",");
                        newList = Arrays.asList(newStr);
                    }
                    //自定义规则之间进行与操作(交集)
                    if (index >= 1 && bpmConfUsers.get(index - 1).getValue().contains("&&")) {
                        changeList.retainAll(newList);
                        userIdSet.clear();
                        if (changeList.size() > 0) {
                            for (final String str : changeList) {
                                userIdSet.add(str.trim());
                            }
                        }
                    }
                    //自定义规则之间进行或操作(并集)
                    if ((index >= 1 && bpmConfUsers.get(index - 1).getValue().contains("||"))
                        || (getHumanTaskVoteType(taskInfoRun)
                            .equals(WorkflowConstants.HumanTaskConstants.COUNTERSIGN_PARALLEL)
                            && index == 1)
                        || (getHumanTaskVoteType(taskInfoRun)
                            .equals(WorkflowConstants.HumanTaskConstants.COUNTERSIGN_SEQUENTIAL)
                            && index == 1)
                        || (index >= 1 && (!bpmConfUsers.get(index - 1)
                            .getValue().contains("||")
                            && !bpmConfUsers.get(index - 1).getValue().contains("&&")))) {
                        changeList.addAll(newList);
                        tempSet.addAll(changeList);
                        changeList.retainAll(tempSet);
                        userIdSet.clear();
                        if (changeList.size() > 0) {
                            for (final String str : changeList) {
                                userIdSet.add(str.trim());
                            }
                        }
                    }
                }
                //节点配置的是抢占节点,重新把人员信息添加到delegateTask和taskParticipant表中
                if (!WorkflowConstants.YES.equals(taskInfoRun.getIsCountersign())) {
                    //temp==1 说明这个节点配置的是候选人 把与或之后的结果添加会相应的任务和表中
                    //temp==2 说明这个节点配置的是候选组 把与或之后的结果添加会相应的任务和表中
                    saveTaskParticipants(delegateTask, taskInfoRun, userIdSet, temp == 1 ? "user" : "group");
                }
                if (temp == 1) {
                    delegateTask.addCandidateUsers(userIdSet);
                }
                if (temp == 2) {
                    delegateTask.addCandidateGroups(userIdSet);
                }
                //会签节点配置的是串行 根据与或之后的处理人,重新配置到串行节点中
                if (WorkflowConstants.YES.equals(taskInfoRun.getIsCountersign())
                    && getHumanTaskVoteType(taskInfoRun)
                        .equals(WorkflowConstants.HumanTaskConstants.COUNTERSIGN_SEQUENTIAL)
                    && bpmConfUsers.size() > 2) {
                    final ExecutionEntity executionEntity = Context.getExecutionContext()
                        .getExecution();
                    final Collection<String> col = (Collection<String>) executionEntity
                        .getVariable("assigneeList");
                    col.retainAll(userIdSet);
                    setLoopVariable(executionEntity, NR_OF_INSTANCES,
                        userIdSet.size());
                    setLoopVariable(executionEntity, NR_OF_ACTIVEINSTANCES,
                        userIdSet.size());
                }
                //会签节点配置的并行会签,根据与或之后的处理人,重新配置到并行节点中
                if (WorkflowConstants.YES.equals(taskInfoRun.getIsCountersign())
                    && getHumanTaskVoteType(taskInfoRun)
                        .equals(WorkflowConstants.HumanTaskConstants.COUNTERSIGN_PARALLEL)
                    && bpmConfUsers.size() > 2) {

                    final List<TaskInfoRun> taskList = taskInfoRunManager
                        .find("from TaskInfoRun where code = ?0 and  processInstanceId = ?1",
                            taskInfoRun.getCode(), taskInfoRun.getProcessInstanceId());
                    /*List<TaskInfoHis>   taskHisList = taskInfoManager
                            .find("from TaskInfoHis where code = ? and  processInstanceId = ?",
                                    taskInfoRun.getCode() , taskInfoRun.getProcessInstanceId());*/
                    for (int i = 0; i < taskList.size(); i++) {
                        int vote = 0;
                        final String assignee = taskList.get(i).getAssignee();
                        for (final String str : userIdSet) {
                            if (assignee.equals(str)) {
                                vote = 1;
                            }
                        }

                        if (vote == 0) {
                            final ExecutionEntity executionEntity = Context.getExecutionContext()
                                .getExecution();
                            executionEntity.remove();
                            final ExecutionEntity parentExecutionEntity = executionEntity.getParent();
                            setLoopVariable(parentExecutionEntity, NR_OF_INSTANCES,
                                (Integer) parentExecutionEntity
                                    .getVariableLocal(NR_OF_INSTANCES) - 1);
                            setLoopVariable(parentExecutionEntity, NR_OF_ACTIVEINSTANCES,
                                (Integer) parentExecutionEntity
                                    .getVariableLocal(NR_OF_ACTIVEINSTANCES) - 1);
                            final JdbcTemplate jdbcTemplate = ApplicationContextHelper
                                .getBean(JdbcDao.class).getJdbcTemplate();
                            final String sqlTaskInfoRun = "delete from t_wf_task_info_run where code = ? "
                                + "and process_instance_id = ? " + "and assignee = ?";
                            final String sqlTaskInfo = "delete from t_wf_task_info where code = ? "
                                + "and process_instance_id = ? " + "and assignee = ?";
                            final Object[] args = new Object[] { taskInfoRun.getCode(),
                                taskInfoRun.getProcessInstanceId(), taskList.get(i)
                                    .getAssignee() };
                            jdbcTemplate.update(sqlTaskInfoRun, args);
                            jdbcTemplate.update(sqlTaskInfo, args);
                        }
                    }
                }

            } catch (final Exception ex) {
                logger.debug(ex.getMessage(), ex);
            }
        }
    }

    private void saveTaskParticipants(DelegateTask delegateTask, TaskInfoRun taskInfoRun, Set<String> userIdSet,
        String type) {
        boolean shouldFlush = false;
        for (final String userId : userIdSet) {
            if (StrUtil.isBlank(userId)) {
                continue;
            }
            TaskParticipant taskParticipant = taskParticipantManager
                .findUnique("from TaskParticipant t where t.taskInfoRun=?0 and t.ref=?1", taskInfoRun,
                    userId);
            if (null != taskParticipant) {
                continue;
            }
            taskParticipant = new TaskParticipant();
            taskParticipant.setCategory("candidate");
            taskParticipant.setRef(userId);
            taskParticipant.setType(type);
            taskParticipant.setTaskInfoRun(taskInfoRun);
            taskParticipant.setStatus(WorkflowConstants.HumanTaskConstants.STATUS_NO_SIGNED);
            taskParticipantManager.save(taskParticipant);
            shouldFlush = true;
        }
        if (shouldFlush) {
            taskParticipantManager.flush();
            taskParticipantManager.clear();
        }
        if (userIdSet.isEmpty()) {
            return;
        }
        if ("user".equals(type)) {
            delegateTask.addCandidateUsers(userIdSet);
        } else {
            delegateTask.addCandidateGroups(userIdSet);
        }
    }

    private String getHumanTaskVoteType(TaskInfoRun taskInfoRun) {
        final String processDefinitionId = taskInfoRun.getProcessDefinitionId();
        final ProcessDefinitionEntity processDefinitionEntity = processConnector
            .getProcessDefinitionEntityByProcessDefinitionId(processDefinitionId);
        final ActivityImpl activityImpl = processDefinitionEntity.findActivity(taskInfoRun.getCode());
        final String multiInstance = activityImpl.getProperty("multiInstance") == null ? ""
            : activityImpl.getProperty("multiInstance").toString();
        return multiInstance;
    }

    @Autowired
    public void setBpmConfUserManager(BpmConfUserManager bpmConfUserManager) {
        this.bpmConfUserManager = bpmConfUserManager;
    }

    /**
     * <li>添加本地变量
     *
     * @param execution
     *            当前执行
     * @param variableName
     *            变量名
     * @param value
     *            变量值
     */
    protected void setLoopVariable(ActivityExecution execution, String variableName, Object value) {
        execution.setVariableLocal(variableName, value);
    }

}
