package com.cesgroup.bpm.listener;

import com.cesgroup.api.humantask.HumanTaskConnector;
import com.cesgroup.api.humantask.HumanTaskDTO;
import com.cesgroup.bpm.persistence.domain.BpmConfUser;
import com.cesgroup.bpm.persistence.manager.BpmConfUserManager;
import com.cesgroup.bpm.support.DefaultTaskListener;
import com.cesgroup.bpm.support.DelegateTaskHolder;
import com.cesgroup.bpm.support.HumanTaskBuilder;
import com.cesgroup.core.mapper.BeanMapper;
import com.cesgroup.core.util.StringUtils;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.humantask.persistence.domain.TaskInfo;
import com.cesgroup.humantask.persistence.domain.TaskInfoHis;
import com.cesgroup.humantask.persistence.manager.TaskInfoManager;
import com.cesgroup.workflow.expression.ExpressionManagerUtil;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 人工任务任务监听器。可以在执行任务的时候做一些控制
 *
 * @author 国栋
 *
 */
public class HumanTaskTaskListener extends DefaultTaskListener {

    private static final long serialVersionUID = 1L;

    /** type_copy */
    public static final int TYPE_COPY = 3;

    private static Logger logger = LoggerFactory.getLogger(HumanTaskTaskListener.class);

    private HumanTaskConnector humanTaskConnector;

    private BpmConfUserManager bpmConfUserManager;

    @Autowired
    private TaskInfoManager taskInfoManager;

    private final BeanMapper beanMapper = new BeanMapper();

    @Override
    public void onCreate(DelegateTask delegateTask) throws Exception {
        HumanTaskDTO humanTaskDto = null;

        // 根据delegateTask创建HumanTaskDTO
        try {
            DelegateTaskHolder.setDelegateTask(delegateTask);

            humanTaskDto = this.createHumanTask(delegateTask);

            // 任务抄送
            checkCopyHumanTask(delegateTask, humanTaskDto);
        } finally {
            DelegateTaskHolder.clear();
        }

        if (humanTaskDto != null) {
            delegateTask.setAssignee(humanTaskDto.getAssignee());
            delegateTask.setOwner(humanTaskDto.getOwner());
        }
    }

    /**
     * 如果直接完成了activiti的task，要同步完成HumanTask.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onComplete(DelegateTask delegateTask) throws Exception {
        //HumanTaskDTO humanTaskDto = humanTaskConnector
        //.findHumanTaskByTaskId(delegateTask.getId());
        //流程转发时会有两条相同taskId的任务，需要根据taskId和assign去判断
        /*final List<TaskInfo> taskList = taskInfoManager.find("from TaskInfoHis where taskId = ?0"
            + " and assignee = ?1", delegateTask.getId(), delegateTask.getAssignee());
        if (taskList.size() > 0) {
            final TaskInfoHis taskInfoHis = (TaskInfoHis) taskList.get(0);
            final HumanTaskDTO humanTaskDto = new HumanTaskDTO();
            beanMapper.copy(taskInfoHis, humanTaskDto);

            humanTaskDto.setStatus(WorkflowConstants.HumanTaskConstants.STATUS_COMPLETE);
            humanTaskDto.setCompleteTime(new Date());
            humanTaskDto.setDuration(String.valueOf(
                humanTaskDto.getCompleteTime().getTime() - humanTaskDto
                    .getCreateTime().getTime()));
            if (StringUtils.isBlank(humanTaskDto.getAction())) {
                humanTaskDto.setAction(WorkflowConstants.HumanTaskConstants.ACTION_COMPLETE_TASK);
            }

            humanTaskConnector.saveHumanTask(humanTaskDto, false);
        }*/
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onDelete(DelegateTask delegateTask) throws Exception {
        // TODO: 2017/6/ 减签时有可能导致父任务被删除
        //HumanTaskDTO humanTaskDto = humanTaskConnector
        //    .findHumanTaskByTaskId(delegateTask.getId());
        //流程转发时会有两条相同taskId的任务，需要根据taskId和assign去判断
        final List<TaskInfo> taskList = taskInfoManager
            .find("from TaskInfoHis where taskId = ?0 and assignee = ?1", delegateTask.getId(),
                delegateTask.getAssignee());
        final HumanTaskDTO humanTaskDto = new HumanTaskDTO();
        TaskInfoHis taskInfoHis = null;
        if (taskList.size() > 0) {
            taskInfoHis = (TaskInfoHis) taskList.get(0);
            beanMapper.copy(taskInfoHis, humanTaskDto);
        }

        if (taskInfoHis == null) {
            return;
        }

		if (!WorkflowConstants.HumanTaskConstants.STATUS_COMPLETE.equals(humanTaskDto.getStatus())
				&& !humanTaskDto.getAction().equals(WorkflowConstants.HumanTaskConstants.ACTION_ROLLBACK)) {
            humanTaskDto.setStatus(WorkflowConstants.HumanTaskConstants.STATUS_DELETE);
            humanTaskDto.setCompleteStatus(WorkflowConstants.HumanTaskConstants.STATUS_COMPLETE);
            humanTaskDto.setCompleteTime(new Date());
            humanTaskDto.setDuration(String.valueOf(
                humanTaskDto.getCompleteTime().getTime() - humanTaskDto.getCreateTime().getTime()));
            if (humanTaskDto.getAction() == null) {
                humanTaskDto
                    .setAction(WorkflowConstants.HumanTaskConstants.ACTION_TERMINATE);
            }
            humanTaskConnector.saveHumanTask(humanTaskDto, false);
        }
    }

    /**
     * 是否会签任务.
     */
    public boolean isVote(DelegateTask delegateTask) {
        final ExecutionEntity executionEntity = (ExecutionEntity) delegateTask.getExecution();
        final ActivityImpl activityImpl = executionEntity.getActivity();

        return activityImpl.getProperty("multiInstance") != null;
    }

    /**
     * 根据task对象创建humanTask
     *
     * @param delegateTask
     *            节点相关信息
     * @return HumanTaskDTO
     * @throws Exception
     *             执行失败抛出异常
     */
    private HumanTaskDTO createHumanTask(DelegateTask delegateTask) throws Exception {
        HumanTaskDTO humanTaskDto = new HumanTaskBuilder().setDelegateTask(delegateTask).build();

        if (delegateTask.getDueDate() != null) {
            humanTaskDto.setExpirationTime(delegateTask.getDueDate());
        }

        setHistoricaAssignee(delegateTask, humanTaskDto);
        humanTaskDto = humanTaskConnector.saveHumanTask(humanTaskDto);
        logger.debug("候选人： {}", delegateTask.getCandidates());

        return humanTaskDto;
    }

    /**
     * 设置历史记录中的办理人，撤回、退回使用
     *
     * @param delegateTask
     *            activiti引擎当前task对象
     * @param humanTaskDto
     *            ces引擎humanTask对象
     */
    private void setHistoricaAssignee(DelegateTask delegateTask, HumanTaskDTO humanTaskDto) {
        final String assignee = (String) delegateTask.getVariable("_historicaAssignee");
        if (StringUtils.isNotBlank(assignee)) {
            humanTaskDto.setAssignee(assignee);
        }
        delegateTask.removeVariable("_historicaAssignee");
        final String action = (String) delegateTask.getVariable("_historicaAction");
        if (StringUtils.isNotBlank(action)) {
            //humanTaskDto.setAction(action);
            if (WorkflowConstants.HumanTaskConstants.ACTION_ROLLBACK.equals(action)) {
                humanTaskDto.setStatus(WorkflowConstants.HumanTaskConstants.STATUS_ROLLBACK);
            } else if (WorkflowConstants.HumanTaskConstants.ACTION_WITHDRAW.equals(action)) {
                humanTaskDto.setStatus(WorkflowConstants.HumanTaskConstants.STATUS_WITHDRAW);
            }
        }
        delegateTask.removeVariable("_historicaAction");
    }

    /**
     * 检测复制的humanTask对象
     */
    @SuppressWarnings("unchecked")
    public void checkCopyHumanTask(DelegateTask delegateTask, HumanTaskDTO humanTaskDto)
        throws Exception {
        final List<BpmConfUser> bpmConfUsers = bpmConfUserManager.find(
            "from BpmConfUser where bpmConfNode.bpmConfBase.processDefinitionId=?0 "
                + " and bpmConfNode.code=?1",
            delegateTask.getProcessDefinitionId(),
            delegateTask.getExecution().getCurrentActivityId());
        logger.debug("{}", bpmConfUsers);

        try {
            for (final BpmConfUser bpmConfUser : bpmConfUsers) {
                logger.debug("状态： {}, 类型： {}", bpmConfUser.getStatus(), bpmConfUser.getType());
                logger.debug("值： {}", bpmConfUser.getValue());

                final Object v = ExpressionManagerUtil.getInstance()
                    .executeExpressionByVariableScope(delegateTask, bpmConfUser.getValue());
                String value = null;
                if (!(v instanceof Collection || v instanceof String[])) {
                    value = v.toString();
                }

                if (bpmConfUser.getStatus() == 1) {
                    if (bpmConfUser.getType() == TYPE_COPY) {
                        logger.info("拷贝人工任务： {}, {}", humanTaskDto.getId(), value);
                        this.copyHumanTask(humanTaskDto, value);
                    }
                }
            }
        } catch (final Exception ex) {
            logger.debug(ex.getMessage(), ex);
        }
    }

    /**
     * 获得第一个节点.
     */
    /*private PvmActivity findFirstActivity(String processDefinitionId) {
        final ProcessDefinitionEntity processDefinitionEntity = Context.getProcessEngineConfiguration()
            .getProcessDefinitionCache().get(processDefinitionId);
    
        final ActivityImpl startActivity = processDefinitionEntity.getInitial();
    
        if (startActivity.getOutgoingTransitions().size() != 1) {
            throw new IllegalStateException(
                "start activity outgoing transitions cannot more than 1, now is : "
                    + startActivity.getOutgoingTransitions().size());
        }
    
        final PvmTransition pvmTransition = startActivity.getOutgoingTransitions().get(0);
        final PvmActivity targetActivity = pvmTransition.getDestination();
    
        if (!"userTask".equals(targetActivity.getProperty("type"))) {
            logger.debug("首节点不是用户任务，跳过");
    
            return null;
        }
    
        return targetActivity;
    }*/

    /**
     * 判断是否流程结束
     *
     * @param processInstanceId
     * @return
     */
    /*private boolean isEnded(String processInstanceId) {
        final RuntimeService runtimeService = Context.getProcessEngineConfiguration().getRuntimeService();
        final ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId).singleResult();
        return processInstance.isEnded();
    }*/

    /**
     * 创建humanTask
     */
    public void copyHumanTask(HumanTaskDTO humanTaskDto, String userId) {
        // 创建新任务
        final HumanTaskDTO target = new HumanTaskDTO();
        beanMapper.copy(humanTaskDto, target);
        target.setId(null);
        target.setCategory(WorkflowConstants.HumanTaskConstants.CATALOG_COPY);
        target.setAssignee(userId);
        target.setCatalog(WorkflowConstants.HumanTaskConstants.CATALOG_COPY);

        humanTaskConnector.saveHumanTask(target);
    }

    @Autowired
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    @Autowired
    public void setBpmConfUserManager(BpmConfUserManager bpmConfUserManager) {
        this.bpmConfUserManager = bpmConfUserManager;
    }
}
