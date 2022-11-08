/**
 * <p>Copyright:Copyright(c) 2018</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.bpm.listener</p>
 * <p>文件名:VoteTaskListener.java</p>
 * <p>创建时间:2018-01-26 17:28</p>
 * <p>作者:chen.liang1</p>
 */

package com.cesgroup.bpm.listener;

import com.cesgroup.bpm.support.DefaultExecutionListener;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.workflow.dto.VoteDTO;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 会签任务监听器，重写onStart方法，增加会签结果对象到流程中
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-26
 */
public class VoteTaskSetTaskParameterEventListener extends DefaultExecutionListener {

    private static final long serialVersionUID = -5311873698286616922L;

    @Autowired
    private ProcessEngine processEngine;
    
    @Override
    public void onStart(DelegateExecution delegateExecution) throws Exception {
        if (delegateExecution instanceof ExecutionEntity) {
            ExecutionEntity executionEntity = (ExecutionEntity) delegateExecution;
            //只在节点第一次启动时初始化参数
            if (executionEntity.isScope()) {
                String activityId = executionEntity.getActivityId();
                String processInstanceId = executionEntity.getProcessInstanceId();
                ActivityImpl activity = executionEntity.getActivity();
                System.out.println("增加参数到流程中");
                VoteDTO voteDTO = initVoteDTO(activity);
                processEngine.getRuntimeService().setVariable(processInstanceId,
                    "T_WF_VOTE_" + activityId, voteDTO);
                executionEntity.getProcessInstance().setVariable("T_WF_VOTE_" + activityId,
                    voteDTO);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private VoteDTO initVoteDTO(ActivityImpl activity) {
        VoteDTO voteDTO = new VoteDTO();
        Map<String, Object> properties = activity.getProperties();
        Map<String, String> vote = (Map<String, String>) properties
            .get(WorkflowConstants.ExtensionProperty.VOTE);
        Object voteType = vote.get(WorkflowConstants.ExtensionProperty.VOTE_VOTE_TYPE);
        if (voteType != null) {
            voteDTO.setVoteType(voteType.toString());
        }
        
        Object num = vote.get(WorkflowConstants.ExtensionProperty.VOTE_NUM);
        if (num != null) {
            voteDTO.setNum(num.toString());
        }
        
        Object votingSystem = vote.get(WorkflowConstants.ExtensionProperty.VOTING_SYSTEM);
        if (votingSystem != null) {
            voteDTO.setVotingSystem(votingSystem.toString());
        }
        
        Object endVote = vote.get(WorkflowConstants.ExtensionProperty.END_VOTE);
        if (endVote != null) {
            voteDTO.setEndVote(endVote.toString());
        }
        
        Object integratedTicketing = vote
            .get(WorkflowConstants.ExtensionProperty.INTEGRATED_TICKETING);
        if (voteType != null) {
            voteDTO.setIntegratedTicketing(integratedTicketing.toString());
        }
        return voteDTO;
        
    }

}
