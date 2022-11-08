package com.cesgroup.bpm.cmd;

import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.workflow.dto.VoteDTO;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 完成任务并填充说明
 * 
 * @author 国栋
 *
 */
public class CompleteTaskWithCommentCmd implements Command<Object> {

    private String taskId;

    private String comment;

    private Map<String, Object> variables;

    /** constructor */
    public CompleteTaskWithCommentCmd(String taskId, Map<String, Object> variables,
        String comment) {
        this.taskId = taskId;
        this.variables = variables;
        this.comment = comment;
    }

    @Override
    public Object execute(CommandContext commandContext) {
        TaskEntity taskEntity = commandContext.getTaskEntityManager().findTaskById(taskId);
        if (StringUtils.isBlank(taskEntity.getAssignee())) {
            throw new RuntimeException("当前任务必须先签收");
        }
        if (variables != null) {
            taskEntity.setExecutionVariables(variables);
        }

        boolean localScope = false;

        if ((taskEntity.getDelegationState() != null)
            && taskEntity.getDelegationState().equals(DelegationState.PENDING)) {
            throw new ActivitiException("不能提交被委派的任务，应该使用'resolved'替代。");
        }

        taskEntity.fireEvent(TaskListener.EVENTNAME_COMPLETE);

        if ((Authentication.getAuthenticatedUserId() != null)
            && (taskEntity.getProcessInstanceId() != null)) {
            taskEntity.getProcessInstance().involveUser(Authentication.getAuthenticatedUserId(),
                IdentityLinkType.PARTICIPANT);
        }

        if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            Context.getProcessEngineConfiguration().getEventDispatcher()
                .dispatchEvent(ActivitiEventBuilder.createEntityWithVariablesEvent(
                    ActivitiEventType.TASK_COMPLETED, taskEntity, variables, localScope));
        }

        Context.getCommandContext().getTaskEntityManager().deleteTask(taskEntity, comment, false);

        if (taskEntity.getExecutionId() != null) {
            ExecutionEntity execution = taskEntity.getExecution();
			execution.removeTask(taskEntity);
			VoteDTO voteDTO = (VoteDTO)execution.getVariable("T_WF_VOTE_" + execution.getActivityId());
			if (voteDTO == null) {
				try {
					Context.setExecutionContext(execution);
					execution.signal(null, null);
				} finally {
					Context.removeExecutionContext();
				}
			}else{ //如果是会签节点，如果会签不通过，直接终止流程。
				if (voteDTO.getCompleted() == true && voteDTO.getResult() == false){
					final ExecutionEntity parentEnt = getTopExecution(execution);
					endchildEntities(commandContext,parentEnt);
					parentEnt.setDeleteReason(WorkflowConstants.HumanTaskConstants.DELETE_REASON_TERMINATE);
					parentEnt.end();
				}else{
					try {
						Context.setExecutionContext(execution);
						execution.signal(null, null);
					} finally {
						Context.removeExecutionContext();
					}
				}
			}
        }

        return null;
    }

    private void endchildEntities(CommandContext commandContext,ExecutionEntity parentEnt){
		final List<ExecutionEntity> childEntities = commandContext.getExecutionEntityManager()
				.findChildExecutionsByParentExecutionId(parentEnt.getId());
		for (final ExecutionEntity child : childEntities) {
			endchildEntities(commandContext,child);
			child.end();
		}
	}

	private ExecutionEntity getTopExecution(ExecutionEntity executionEntity) {
		final ExecutionEntity parentEnt = executionEntity.getParent();

		if (parentEnt == null) {
			return executionEntity;
		}
		return getTopExecution(parentEnt);
	}
}
