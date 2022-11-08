package com.cesgroup.bpm.cmd;

import com.cesgroup.core.util.WorkflowConstants;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 列举节点命令
 * 
 * @author 国栋
 *
 */
public class ListActivityCmd implements Command<Map<String, String>> {

    private static Logger logger = LoggerFactory.getLogger(ListActivityCmd.class);

    private String executionId;

    public ListActivityCmd(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public Map<String, String> execute(CommandContext commandContext) {
        ExecutionEntity executionEntity = Context.getCommandContext().getExecutionEntityManager()
            .findExecutionById(executionId);
        ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
        Map<String, String> map = new HashMap<String, String>();

        for (ActivityImpl activity : processDefinition.getActivities()) {
            logger.info("{}", activity.getProperties());

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(activity.getProperty("type"))
                || WorkflowConstants.NodeTypeConstants.NODETYPE_STARTEVENT
                    .equals(activity.getProperty("type"))) {
                map.put(activity.getId(), (String) activity.getProperty("name"));
            }
        }

        return map;
    }
}
