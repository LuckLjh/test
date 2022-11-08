package com.cesgroup.bpm.cmd;

import cn.hutool.core.util.StrUtil;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import java.util.Map;


/**
 * 检查任务是否是并行会签任务
 * 
 * @author chenyao
 *
 */
public class CheckIsCountersignTaskCmd implements Command<Boolean> {

    private String taskId;

    public CheckIsCountersignTaskCmd(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public Boolean execute(CommandContext commandContext) {
        if (this.taskId != null) {
            TaskEntity taskEntity = commandContext.getTaskEntityManager().findTaskById(taskId);
            if (taskEntity == null){
            	return false;
			}
            ActivityImpl activity = taskEntity.getExecution().getActivity();
			Map<String, Object> properties = activity.getProperties();
            String countersignType = null;
            if (properties.get("multiInstance") != null) {
                countersignType = (String) properties.get("multiInstance");
            }
            try {
				//网关类型
				Object gateWayType = activity.getOutgoingTransitions().get(0).getDestination().getProperty("type");
				return "parallel".equals(countersignType)
						|| "parallelGateway".equals(StrUtil.toString(gateWayType))
						|| "inclusiveGateway".equals(StrUtil.toString(gateWayType));
			} catch (Exception e) {
				return "parallel".equals(countersignType);
			}

        }
        return false;
    }

}
