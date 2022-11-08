package com.cesgroup.bpm.behavior;

import com.cesgroup.bpm.behavior.callactivity.CustomCallActivityBehavior;
import com.cesgroup.bpm.behavior.usertask.CustomUserTaskActivityBehavior;

import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.IOParameter;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.InclusiveGatewayActivityBehaviorEx;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.InclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.data.SimpleDataInputAssociation;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.bpmn.webservice.MessageImplicitDataOutputAssociation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义节点动作工厂
 * 
 * @author 国栋
 *
 */
public class CustomActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    private static Logger log = LoggerFactory.getLogger(CustomUserTaskActivityBehavior.class);

    // 创建自定义的
    @Override
    public CallActivityBehavior createCallActivityBehavior(CallActivity callActivity) {
        log.info("改变'异步调用子流程'行为： {}  ", CustomCallActivityBehavior.class);

        String expressionRegex = "\\$+\\{+.+\\}";

        CustomCallActivityBehavior callActivityBehaviour = null;
        if (StringUtils.isNotEmpty(callActivity.getCalledElement())
            && callActivity.getCalledElement().matches(expressionRegex)) {
            callActivityBehaviour = new CustomCallActivityBehavior(
                expressionManager.createExpression(callActivity.getCalledElement()),
                callActivity.getMapExceptions());
        } else {
            callActivityBehaviour = new CustomCallActivityBehavior(callActivity.getCalledElement(),
                callActivity.getMapExceptions());
        }

        for (IOParameter ioParameter : callActivity.getInParameters()) {
            if (StringUtils.isNotEmpty(ioParameter.getSourceExpression())) {
                Expression expression = expressionManager
                    .createExpression(ioParameter.getSourceExpression().trim());
                callActivityBehaviour.addDataInputAssociation(
                    new SimpleDataInputAssociation(expression, ioParameter.getTarget()));
            } else {
                callActivityBehaviour.addDataInputAssociation(new SimpleDataInputAssociation(
                    ioParameter.getSource(), ioParameter.getTarget()));
            }
        }

        for (IOParameter ioParameter : callActivity.getOutParameters()) {
            if (StringUtils.isNotEmpty(ioParameter.getSourceExpression())) {
                Expression expression = expressionManager
                    .createExpression(ioParameter.getSourceExpression().trim());
                callActivityBehaviour.addDataOutputAssociation(
                    new MessageImplicitDataOutputAssociation(ioParameter.getTarget(), expression));
            } else {
                callActivityBehaviour.addDataOutputAssociation(
                    new MessageImplicitDataOutputAssociation(ioParameter.getTarget(),
                        ioParameter.getSource()));
            }
        }

        return callActivityBehaviour;

    }

    // 同样可以覆盖别的方法,加入其他元素的自定义行为,参考 @see ActivityBehaviorFactory
    // 该类控制执行到某一元素时触发

    //重写父类中包含网关中的流条件(条件中判断增加包含选项)
    @Override
    public InclusiveGatewayActivityBehavior createInclusiveGatewayActivityBehavior(InclusiveGateway inclusiveGateway) {
        return new InclusiveGatewayActivityBehaviorEx();
    }

}
