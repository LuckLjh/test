package com.cesgroup.bpm.behavior.usertask;

import com.cesgroup.bpm.behavior.ProcessEngineBeanFactory;

import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 自定义用户任务节点行为<br>
 * 就是个例子，说明这里可以对用户节点的行为进行控制
 * 
 * @author 国栋
 *
 */
public class CustomUserTaskActivityBehavior extends UserTaskActivityBehavior {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CustomUserTaskActivityBehavior.class);

    private JdbcTemplate jdbcTemplate; // 只是一个通过activiti引擎获取spring对象的例子

    public CustomUserTaskActivityBehavior(String userTaskId, TaskDefinition taskDefinition) {
        super(userTaskId, taskDefinition);
        jdbcTemplate = ProcessEngineBeanFactory.getBean("jdbcTemplate");
    }

    @Override
    public void setMultiInstanceActivityBehavior(
        MultiInstanceActivityBehavior multiInstanceActivityBehavior) {
        log.info("多实例用户任务  -- {}", multiInstanceActivityBehavior);
        super.setMultiInstanceActivityBehavior(multiInstanceActivityBehavior);
        log.info("jdbcTemplate : {}", jdbcTemplate);
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        log.info("{}:{} 开始执行", execution.getCurrentActivityId(),
            execution.getCurrentActivityName());
        super.execute(execution);
        log.info("{}:{} 结束执行", execution.getCurrentActivityId(),
            execution.getCurrentActivityName());
    }

    // 自定义行为可以监控到各个元素的各种行为, 只需要覆盖相应方法即可
}
