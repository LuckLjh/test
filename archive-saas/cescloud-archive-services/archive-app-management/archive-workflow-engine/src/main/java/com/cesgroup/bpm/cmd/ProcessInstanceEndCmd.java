package com.cesgroup.bpm.cmd;

import java.util.List;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import com.cesgroup.core.util.WorkflowConstants;

/**
 * 流程实例终止命令
 *
 * @author 国栋
 */
public class ProcessInstanceEndCmd implements Command<Void> {

    private String processInstanceId = null;

    public ProcessInstanceEndCmd(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public Void execute(CommandContext cmdContext) {
        final ExecutionEntity executionEntity = cmdContext.getExecutionEntityManager()
            .findExecutionById(this.processInstanceId);

        final ExecutionEntity parentEnt = getTopExecution(executionEntity);

        parentEnt.setVariable("_historicaAction", WorkflowConstants.HumanTaskConstants.ACTION_TERMINATE);

        //流程终止（若包含子流程，必须先终止子流程）
        // 1、子流程终止，主流程同时也终止
        // 2、子流程终止，子流程继续往后走
        final List<ExecutionEntity> childEntities = cmdContext.getExecutionEntityManager()
            .findChildExecutionsByParentExecutionId(parentEnt.getId());

        for (final ExecutionEntity child : childEntities) {
            child.end();
        }

        parentEnt
            .setDeleteReason(WorkflowConstants.HumanTaskConstants.DELETE_REASON_TERMINATE);
        parentEnt.end();

        return null;
    }

    private ExecutionEntity getTopExecution(ExecutionEntity executionEntity) {
        final ExecutionEntity parentEnt = executionEntity.getParent();

        if (parentEnt == null) {
            return executionEntity;
        }
        return getTopExecution(parentEnt);
    }
}