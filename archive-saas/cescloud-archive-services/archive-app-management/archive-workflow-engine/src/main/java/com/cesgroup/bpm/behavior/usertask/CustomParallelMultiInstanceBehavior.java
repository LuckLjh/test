package com.cesgroup.bpm.behavior.usertask;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 并行多实例自定义行为动作<br>
 * 这个类什么也没做。就是说明这里可以对并行多实例会签进行扩展控制
 * 
 * @author 国栋
 *
 */
public class CustomParallelMultiInstanceBehavior extends ParallelMultiInstanceBehavior {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CustomParallelMultiInstanceBehavior.class);

    /**
     * constructor
     * 
     * @param activity 节点信息
     * @param originalActivityBehavior 原始节点执行器
     */
    public CustomParallelMultiInstanceBehavior(ActivityImpl activity,
        AbstractBpmnActivityBehavior originalActivityBehavior) {
        super(activity, originalActivityBehavior);
    }

    @Override
    protected void createInstances(ActivityExecution execution) throws Exception {
        log.info("创建多实例: {}  ", execution);
        super.createInstances(execution);
    }

    @Override
    public void setCompletionConditionExpression(Expression completionConditionExpression) {
        log.info("你要表达式做什么用?: {}  ", completionConditionExpression.getExpressionText());
        super.setCompletionConditionExpression(completionConditionExpression);
    }
}
