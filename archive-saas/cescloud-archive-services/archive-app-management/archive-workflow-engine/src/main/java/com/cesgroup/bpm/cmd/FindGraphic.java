package com.cesgroup.bpm.cmd;

import com.cesgroup.api.humantask.HumanTaskDTO;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 获取流程图
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class FindGraphic implements Command<Integer> {

    private HumanTaskDTO humanTaskDto;

    @Autowired
    private ProcessEngine processEngine;

    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    @Autowired
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public FindGraphic(HumanTaskDTO humanTaskDto) {
        this.humanTaskDto = humanTaskDto;
    }

    @Override
    public Integer execute(CommandContext commandContext) {
        // TODO Auto-generated method stub

        String processDefinitionId = humanTaskDto.getProcessDefinitionId();
        String executionId = humanTaskDto.getExecutionId();

        RepositoryService rs = processEngine.getRepositoryService();

        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) rs)
            .getDeployedProcessDefinition(processDefinitionId);

        List<ActivityImpl> activitiList = def.getActivities(); //rs是指RepositoryService的实例
        //String excId = task.getExecutionId();
        ExecutionEntity execution = (ExecutionEntity) processEngine.getRuntimeService()
            .createExecutionQuery().executionId(executionId).singleResult();
        String activitiId = execution.getActivityId();

        for (ActivityImpl activityImpl : activitiList) {
            String id = activityImpl.getId();
            if (activitiId.equals(id)) {
                //输出某个节点的某种属性
                System.out.println("当前任务：" + activityImpl.getProperty("name"));
                //获取从某个节点出来的所有线路
                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
                for (PvmTransition tr : outTransitions) {
                    PvmActivity ac = tr.getDestination(); //获取线路的终点节点
                    System.out.println("下一步任务任务：" + ac.getProperty("name"));
                }
                break;
            }
        }

        return null;
    }

}
