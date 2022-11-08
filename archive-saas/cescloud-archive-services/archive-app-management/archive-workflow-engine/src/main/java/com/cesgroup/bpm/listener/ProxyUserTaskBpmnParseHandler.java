package com.cesgroup.bpm.listener;

import com.cesgroup.bpm.parser.ExtensionUserTaskParseHandler;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.parse.BpmnParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 办理人工任务bpmn解析处理器
 * 
 * @author 国栋
 *
 */
public class ProxyUserTaskBpmnParseHandler implements BpmnParseHandler {

    private static Logger logger = LoggerFactory.getLogger(ProxyUserTaskBpmnParseHandler.class);

    private String taskListenerId;

    private boolean useDefaultUserTaskParser;

    @Override
    public void parse(BpmnParse bpmnParse, BaseElement baseElement) {
        if (!(baseElement instanceof UserTask)) {
            return;
        }

        if (useDefaultUserTaskParser) {
            new ExtensionUserTaskParseHandler().parse(bpmnParse, baseElement);
        }

        UserTask userTask = (UserTask) baseElement;
        logger.debug("bpmnParse : {}, userTask : {}", bpmnParse, userTask);

        TaskDefinition taskDefinition = (TaskDefinition) bpmnParse.getCurrentActivity()
            .getProperty(UserTaskParseHandler.PROPERTY_TASK_DEFINITION);

        configEvent(taskDefinition, bpmnParse, TaskListener.EVENTNAME_CREATE);
        configEvent(taskDefinition, bpmnParse, TaskListener.EVENTNAME_ASSIGNMENT);
        configEvent(taskDefinition, bpmnParse, TaskListener.EVENTNAME_COMPLETE);
        configEvent(taskDefinition, bpmnParse, TaskListener.EVENTNAME_DELETE);
    }

    /**
     * 配置事件
     * 
     * @param taskDefinition
     *            任务定义
     * @param bpmnParse
     *            流程解析
     * @param eventName
     *            事件名字
     */
    public void configEvent(TaskDefinition taskDefinition, BpmnParse bpmnParse, String eventName) {
        ActivitiListener activitiListener = new ActivitiListener();
        activitiListener.setEvent(eventName);
        activitiListener
            .setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        activitiListener.setImplementation("#{" + taskListenerId + "}");
        taskDefinition.addTaskListener(eventName,
            bpmnParse.getListenerFactory().createDelegateExpressionTaskListener(activitiListener));
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection<Class<? extends BaseElement>> getHandledTypes() {
        List types = Collections.singletonList(UserTask.class);
        return types;
    }

    public void setTaskListenerId(String taskListenerId) {
        this.taskListenerId = taskListenerId;
    }

    public void setUseDefaultUserTaskParser(boolean useDefaultUserTaskParser) {
        this.useDefaultUserTaskParser = useDefaultUserTaskParser;
    }
}
