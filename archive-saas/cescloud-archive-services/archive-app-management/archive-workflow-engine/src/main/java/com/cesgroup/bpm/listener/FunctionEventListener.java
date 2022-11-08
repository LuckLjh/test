package com.cesgroup.bpm.listener;

import java.util.List;

import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cesgroup.bpm.persistence.domain.BpmConfListener;
import com.cesgroup.bpm.persistence.manager.BpmConfListenerManager;
import com.cesgroup.workflow.expression.ExpressionManagerUtil;

/**
 * 节点方法监听器
 *
 * @author 国栋
 *
 */
public class FunctionEventListener implements ActivitiEventListener {

    private static Logger logger = LoggerFactory.getLogger(FunctionEventListener.class);

    private BpmConfListenerManager bpmConfListenerManager;

    @Override
    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
			case ACTIVITY_STARTED:
				onActivityStart((ActivitiActivityEvent) event);
				break;

			case ACTIVITY_COMPLETED:
				onActivityEnd((ActivitiActivityEvent) event);
				break;

			default:
				logger.debug("触发事件: {}", event.getType());
				break;
        }
    }

    /**
     * 节点开始执行表达式
     */
    public void onActivityStart(ActivitiActivityEvent event) {
        logger.debug("节点开始 {}", event);
        invokeExpression(event.getProcessDefinitionId(), event.getActivityId(), 0);
    }

    public void onActivityEnd(ActivitiActivityEvent event) {
        logger.debug("节点结束 {}", event);
        invokeExpression(event.getProcessDefinitionId(), event.getActivityId(), 1);
    }

    /**
     * 解析表达式
     */
    @SuppressWarnings("unchecked")
    public void invokeExpression(String processDefinitionId, String activityId, int type) {
        final String hql = "from BpmConfListener where bpmConfNode.bpmConfBase.processDefinitionId=?0 "
            + " and bpmConfNode.code=?1 and type=?2";
        final List<BpmConfListener> bpmConfListeners = bpmConfListenerManager.find(hql,
            processDefinitionId, activityId, type);

        for (final BpmConfListener bpmConfListener : bpmConfListeners) {
            final String expressionText = bpmConfListener.getValue();

            try {
                final Object result = ExpressionManagerUtil.getInstance()
                    .executeExpressionByVariableScope(Context.getExecutionContext().getExecution(),
                        expressionText);
                logger.info("执行表达式结果： {}", result);
            } catch (final Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Autowired
    public void setBpmConfListenerManager(BpmConfListenerManager bpmConfListenerManager) {
        this.bpmConfListenerManager = bpmConfListenerManager;
    }
}
