package com.cesgroup.bpm.listener;

import com.cesgroup.api.humantask.HumanTaskConnector;
import com.cesgroup.api.humantask.HumanTaskDTO;
import com.cesgroup.bpm.support.DefaultTaskListener;

import org.activiti.engine.delegate.DelegateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 人工任务同步
 * 
 * @author 国栋
 *
 */
public class HumanTaskSyncTaskListener extends DefaultTaskListener {

    private static final long serialVersionUID = 1L;

    /** type_copy */
    public static final int TYPE_COPY = 3;

    private static Logger logger = LoggerFactory.getLogger(HumanTaskSyncTaskListener.class);

    private HumanTaskConnector humanTaskConnector;

    @Override
    public void onCreate(DelegateTask delegateTask) throws Exception {
        HumanTaskDTO humanTaskDto = humanTaskConnector.findHumanTaskByTaskId(delegateTask.getId());
        logger.debug("人工任务同步监听器，任务所有人{}", humanTaskDto.getOwner());
        delegateTask.setOwner(humanTaskDto.getOwner());
        logger.debug("人工任务同步监听器，任务办理人{}", humanTaskDto.getAssignee());
        delegateTask.setAssignee(humanTaskDto.getAssignee());
    }

    @Autowired
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }
}
