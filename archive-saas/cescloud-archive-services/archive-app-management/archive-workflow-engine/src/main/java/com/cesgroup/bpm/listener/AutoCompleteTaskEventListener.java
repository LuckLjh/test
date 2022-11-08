/**
 * <p>Copyright:Copyright(c) 2018</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.bpm.listener</p>
 * <p>文件名:AutoCompleteTaskEventListener.java</p>
 * <p>创建时间:2018-02-02 14:05</p>
 * <p>作者:chen.liang1</p>
 */

package com.cesgroup.bpm.listener;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cesgroup.api.humantask.HumanTaskConnector;
import com.cesgroup.api.humantask.HumanTaskDTO;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.bpm.cmd.GetActivityImplByTaskIdCmd;
import com.cesgroup.bpm.support.DefaultTaskListener;
import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.TaskInfoRun;
import com.cesgroup.humantask.persistence.manager.TaskInfoRunManager;

/**
 * 自动提交任务 事件监听器
 *
 * @author chen.liang1
 * @version 1.0.0 2018-02-02
 */
public class AutoCompleteTaskEventListener extends DefaultTaskListener {

    private static final long serialVersionUID = 1L;

    @Autowired
    private HumanTaskConnector humanTaskConnector;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private TaskInfoRunManager taskInfoRunManager;

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(DelegateTask delegateTask) throws Exception {
        final String taskId = delegateTask.getId();
        final HumanTaskDTO humanTaskDTO = humanTaskConnector.findHumanTaskByTaskId(taskId);
        if (humanTaskDTO == null) {
            return;
        }
        final ActivityImpl activity = processEngine.getManagementService()
            .executeCommand(new GetActivityImplByTaskIdCmd(taskId));
        final List<String> nodetactics = (List<String>) activity.getProperty(
            WorkflowConstants.ExtensionProperty.NODETACTICS);

        if (nodetactics != null) {
            // 开关，用于判断是否已经执行过提交操作，执行过之后关闭开关，不在进行提交操作
            boolean flag = false;
            for (final String nodetactic : nodetactics) {
                // 如果已经执行过提交操作，跳出循环
                if (flag) {
                    break;
                }
                // 无办理人时，流程自动提交
                final String humanTaskId = humanTaskDTO.getId();
                if (WorkflowConstants.ExtensionProperty.NODETACTICS_AUTO_COMMIT_IF_NULL_ASSIGNEEMENT
                    .equals(nodetactic)) {
                    // 获取节点办理人，判断当前流程办理人是否为空
                    flag = completeTaskIfNullAssigneement(humanTaskId);
                }
                // 相同办理人时，流程自动提交
                if (WorkflowConstants.ExtensionProperty.NODETACTICS_AUTO_COMMIT_IF_SAME_ASSIGNEEMENT
                    .equals(nodetactic)) {
                    // 如果下一节点需要人员选择，则不自动提交
                    if (humanTaskConnector.findActivityImplSubmitTaskWithPerson(humanTaskId, null)) {
                        break;
                    }
                    flag = completeTaskIfSameAssigneement(delegateTask, humanTaskId);
                }
            }
        }
    }

    /** 尝试执行: 无节点办理人时自动提交 */
    private boolean completeTaskIfNullAssigneement(String humanTaskId) throws Exception {
        if (humanTaskConnector.findUsersByTaskInfoId(humanTaskId).isEmpty()) {

            // 提交任务
            humanTaskConnector.claimTask(humanTaskId,
                WorkflowConstants.HumanTaskConstants.USER_AUTO_COMMIT,
                WorkflowConstants.HumanTaskConstants.USER_AUTO_COMMIT);
            autoCompleteTask(humanTaskId, WorkflowConstants.HumanTaskConstants.USER_AUTO_COMMIT);
            return true;
        }
        return false;
    }

    /** 尝试执行: 相同办理人时自动提交 */
    private boolean completeTaskIfSameAssigneement(DelegateTask delegateTask, String humanTaskId)
        throws Exception {
        final JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
            .getJdbcTemplate();
        // 查询出该节点的attr5的状态 如果不是自动提交就停在该节点
        final List<Map<String, Object>> taskInfoList = jdbcTemplate.queryForList(
            "select * from t_wf_task_info t where t.PROCESS_INSTANCE_ID=? and t.CODE=?",
            delegateTask.getProcessInstanceId(), delegateTask.getTaskDefinitionKey());
        if ("complete".equals(taskInfoList.get(0).get("STATUS"))
            && !WorkflowConstants.HumanTaskConstants.WORKFLOW_AUTOCOMMIT
                .equals(taskInfoList.get(0).get("ATTR5"))) {
            return false;
        }
        // 获取上一个办理人, 判断流程办理人是否与上一个办理人时相同的办理人
        final String preTaskAssignee = (String) delegateTask.getVariable(
            WorkflowConstants.DefaultVariable.PRE_TASK_ASSIGNEE);
        final TaskInfoRun taskInfo = taskInfoRunManager.get(Long.parseLong(humanTaskId));
        // 获取任务中是否有处理人还是未签收的任务
        final String assignee = taskInfo.getAssignee();
        // 如果当前任务的处理人是空,则表示该节点是抢占节点
        if (StringUtils.isBlank(assignee)) {

            final Set<UserDTO> userDTOs = humanTaskConnector.findUsersByTaskInfoId(humanTaskId);
            if (userDTOs.isEmpty()) {
                return false;
            } else if (userDTOs.size() > 1) {
                return false;
            }
            // 获取自动提交的点的处理人 如果是一个人且是上一个节点处理人 那么做自动提交(抢占节点)
            if (StringUtils.isNotBlank(preTaskAssignee)
                && (preTaskAssignee.equals(userDTOs.iterator().next().getId()))) {
                // 提交任务
                humanTaskConnector.claimTask(humanTaskId, preTaskAssignee,
                    WorkflowConstants.HumanTaskConstants.USER_AUTO_COMMIT);
                autoCompleteTask(humanTaskId, preTaskAssignee);
                return true;
            }

        }

        // 处理人是一个并且不是抢占节点
        if ((StringUtils.isNotBlank(preTaskAssignee) && StringUtils.isNotBlank(assignee))
            && (preTaskAssignee.equals(assignee))) {
            autoCompleteTask(humanTaskId, preTaskAssignee);
            return true;
        }
        return false;
    }

    /** 提交任务 */
    private void autoCompleteTask(String humanTaskId, String preTaskAssignee) throws Exception {
        // 避免任务顺序混乱，设置1秒的间隔时间
        Thread.sleep(1000);

        humanTaskConnector.completeTask(humanTaskId, preTaskAssignee,
            WorkflowConstants.HumanTaskConstants.ACTION_AUTO, true, "自动提交", null,
            WorkflowConstants.HumanTaskConstants.USER_AUTO_COMMIT);
    }
}
