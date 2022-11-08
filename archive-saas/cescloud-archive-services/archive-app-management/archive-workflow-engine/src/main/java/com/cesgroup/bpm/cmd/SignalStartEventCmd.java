package com.cesgroup.bpm.cmd;

import org.activiti.engine.impl.bpmn.behavior.FlowNodeActivityBehavior;
import org.activiti.engine.impl.cmd.NeedsActiveExecutionCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 信号开始事件命令
 * 
 * @author 国栋
 *
 */
public class SignalStartEventCmd extends NeedsActiveExecutionCmd<Object> {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(SignalStartEventCmd.class);

    public SignalStartEventCmd(String executionId) {
        super(executionId);
    }

    @Override
    protected Object execute(CommandContext commandContext, ExecutionEntity execution) {
        try {
            FlowNodeActivityBehavior activityBehavior = (FlowNodeActivityBehavior) execution
                .getActivity().getActivityBehavior();
            Method method = FlowNodeActivityBehavior.class.getDeclaredMethod("leave",
                ActivityExecution.class);
            method.setAccessible(true);
            method.invoke(activityBehavior, execution);
            method.setAccessible(false);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return null;
    }

    @Override
    protected String getSuspendedExceptionMessage() {
        return "被挂起的执行无法触发信号";
    }
}
