package com.cesgroup.bpm.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.Collections;
import java.util.List;

/**
 * 代理任务监听器
 * 
 * @author 国栋
 *
 */
public class ProxyTaskListener implements TaskListener {

    private static final long serialVersionUID = 1L;

    private List<TaskListener> taskListeners = Collections.emptyList();

    @Override
    public void notify(DelegateTask delegateTask) {
        for (TaskListener taskListener : taskListeners) {
            taskListener.notify(delegateTask);
        }
    }

    public void setTaskListeners(List<TaskListener> taskListeners) {
        this.taskListeners = taskListeners;
    }
}
