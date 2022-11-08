package com.cesgroup.bpm.cmd;

import com.cesgroup.core.util.WorkflowConstants;

import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列举主流程节点及对应的子流程节点
 * 
 * @owner xuhw3
 * @date 2017/4/18.
 */
public class ListTaskCmd implements Command<Map<String, String>> {

    private static Logger logger = LoggerFactory.getLogger(ListTaskCmd.class);

    private String processDefinitionId;

    public ListTaskCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public Map<String, String> execute(CommandContext commandContext) {
        Map<String, String> resultProcessActList = new HashMap<String, String>();
        StringBuilder parentProcessActList = new StringBuilder(); //主流程任务节点
        StringBuffer subProcessActList = new StringBuffer(); //子流程任务节点
        ProcessDefinitionEntity definitionEntity =
            new GetDeploymentProcessDefinitionCmd(processDefinitionId).execute(commandContext);
        List<ActivityImpl> activities = definitionEntity.getActivities();
        for (ActivityImpl activity : activities) {
            logger.info("{}", activity.getProperties());
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(activity.getProperty("type"))) {
                parentProcessActList.append(activity.getId()).append(";");
            }
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_CALL_ACTIVITY
                .equals(activity.getProperty("type"))) {
                parentProcessActList.append(activity.getId()).append(";");
            }
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                .equals(activity.getProperty("type"))) { //若是子流程，拿到初始化节点
                ActivityImpl initial = (ActivityImpl) activity.getProperty("initial");
                parentProcessActList.append(activity.getId()).append(";");
                findSubProcessActivity(initial.getOutgoingTransitions(), subProcessActList);
            }
        }
        resultProcessActList.put("parentProcessActList", parentProcessActList.toString());
        resultProcessActList.put("subProcessActList", subProcessActList.toString());
        return resultProcessActList;
    }

    //查询子流程内部的人工任务节点，暂不考虑子流程嵌套的问题
    private void findSubProcessActivity(List<PvmTransition> outgoingTransitions,
        StringBuffer idList) {
        PvmActivity destination = null;
        for (PvmTransition transition : outgoingTransitions) {

            destination = transition.getDestination();
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_USERTASK
                .equals(destination.getProperty("type"))) {
                idList.append(destination.getId()).append(";");
                findSubProcessActivity(destination.getOutgoingTransitions(), idList);
            }

            if (WorkflowConstants.NodeTypeConstants.NODETYPE_ENDEVENT
                .equals(destination.getProperty("type"))) {
                return;
            }

            //暂不考虑子流程嵌套的问题
            if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                .equals(destination.getProperty("type"))) {
                //不做处理
            }
        }
    }
}
