package com.cesgroup.bpm.cmd;

import com.cesgroup.core.jdbc.JdbcDao;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;

import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 提交完成到指定节点命令
 * 
 * @author 陆开奇
 *
 */
public class CompleteToActivityCmd implements Command<Object> {

    private String executionId;

    private List<String> activityIds = new ArrayList<String>();

    /**
     * 提交完成到指定节点命令
     * 
     * @param executionId 执行ID
     */
    public CompleteToActivityCmd(String executionId, List<String> activityIds) {
        this.activityIds = activityIds;
        this.executionId = executionId;
    }

    @Override
    public Object execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        // 获取当前流程的executionId，因为在并发的情况下executionId是唯一的。
        ExecutionEntity executionEntity = executionEntityManager.findExecutionById(executionId);
        ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
        // 根据executionId 获取Task
        Iterator<TaskEntity> localIterator = Context.getCommandContext().getTaskEntityManager()
            .findTasksByExecutionId(this.executionId).iterator();
        while (localIterator.hasNext()) {
            TaskEntity taskEntity = (TaskEntity) localIterator.next();
            // 触发任务监听
            taskEntity.fireEvent(TaskListener.EVENTNAME_COMPLETE);
            // 删除任务的原因
            commandContext.getTaskEntityManager().deleteTask(taskEntity,
                TaskEntity.DELETE_REASON_COMPLETED, false);
            //更新T_WF_ACT_HI_ACTINST
            JdbcTemplate jdbcTemplate = ApplicationContextHelper.getBean(JdbcDao.class)
                .getJdbcTemplate();
            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT * FROM T_WF_ACT_HI_ACTINST WHERE TASK_ID_=? AND END_TIME_ IS NULL",
                taskEntity.getId());
            Date now = new Date();
            for (Map<String, Object> map : list) {
                Date startTime = (Date) map.get("START_TIME_");
                long duration = now.getTime() - startTime.getTime();
                jdbcTemplate.update(
                    "UPDATE T_WF_ACT_HI_ACTINST SET END_TIME_=?,DURATION_=? WHERE ID_=?", now,
                    duration, map.get("ID_"));
            }

        }
        for (String activityId : activityIds) {
            ActivityImpl activity = processDefinition.findActivity(activityId);
            //判断被调用活动是否激活
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_CALL_ACTIVITY
                .equals(activity.getProperty("type"))) {
                ActivityBehavior activityBehavior = activity.getActivityBehavior();
                //调用活动。不知为何，只能判断当前流程调用的流程是否激活或者挂起
                if (activityBehavior instanceof CallActivityBehavior) { 
                    String processDefinitonKey = ((CallActivityBehavior) activityBehavior)
                        .getProcessDefinitonKey();
                    ProcessDefinitionQuery definitionQuery = Context.getCommandContext()
                        .getProcessEngineConfiguration().getRepositoryService()
                        .createProcessDefinitionQuery();
                    //取激活的流程模型版本
                    ProcessDefinition definitionEntity = definitionQuery
                        .processDefinitionKey(processDefinitonKey).active().singleResult();
                    if (definitionEntity == null) {
                        throw new RuntimeException("被调用的流程不存在，请检查流程配置是否有误");
                    }
                    if (definitionEntity.isSuspended()) {
                        throw new RuntimeException("被调用的流程被挂起，请检查流程配置是否有误");
                    }
                }
            }
            if (activity.isScope()) {
                ExecutionEntity parentExecution = executionEntity.createExecution();
                parentExecution.setActivity(activity);
                parentExecution.executeActivity(activity);
            } else {
                executionEntity.executeActivity(activity);
            }
        }

        return null;
    }
}
