package com.cesgroup.bpm.cmd;

import com.cesgroup.api.humantask.HumanTaskDTO;
import com.cesgroup.bpm.cmd.support.CounterSignOperationType;
import com.cesgroup.core.util.WorkflowConstants;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 会签（加签，减签）命令
 * <br>
 * 操作类型 add or remove
 * {@link com.cesgroup.bpm.cmd.support.CounterSignOperationType}
 * 
 * @author 国栋
 *
 */
public class CounterSignCmd implements Command<Object> {

    private static Logger log = LoggerFactory.getLogger(CounterSignCmd.class);

    private String operateType;

    private String activityId;

    private String assignee;

    private String processInstanceId;

    private String collectionVariableName;

    private String collectionElementVariableName;

    private CommandContext commandContext;

    private String taskId;

    private HumanTaskDTO humanTaskDto;

    /** 实例总数 **/
    private static final String NR_OF_INSTANCES = "nrOfInstances";

    /** 当前活动的，比如，还没完成的，实例数量。 对于顺序执行的多实例，值一直为1。 **/
    private static final String NR_OF_ACTIVEINSTANCES = "nrOfActiveInstances";
    /** 已经完成实例的数目。 **/
    // private static final String nrOfCompletedInstances =
    // "nrOfCompletedInstances";

    /**
     * 会签命令
     * 
     * @param operateType
     *            操作类型 add or remove
     * @param assignee
     *            处理人
     * @param taskId
     *            任务id
     */
    public CounterSignCmd(String operateType, String assignee, String taskId,
        HumanTaskDTO humanTaskDto) {
        this.operateType = operateType;
        this.assignee = assignee;
        this.taskId = taskId;
        this.humanTaskDto = humanTaskDto;
    }

    /**
     * @param operateType
     *            操作类型 add or remove
     *            {@link com.cesgroup.bpm.cmd.support.CounterSignOperationType}
     * @param activityId
     *            节点ID
     * @param assignee
     *            人员代码
     * @param processInstanceId
     *            流程实例ID
     * @param collectionVariableName
     *            collection 设置的变量名
     * @param collectionElementVariableName
     *            collection 的每个元素变量名
     */
    public CounterSignCmd(final String operateType, final String activityId, final String assignee,
        final String processInstanceId, final String collectionVariableName,
        final String collectionElementVariableName) {
        this.operateType = operateType;
        this.activityId = activityId;
        this.assignee = assignee;
        this.processInstanceId = processInstanceId;
        this.collectionVariableName = collectionVariableName;
        this.collectionElementVariableName = collectionElementVariableName;
    }

    @Override
    public Object execute(CommandContext commandContext) {
        this.commandContext = commandContext;

        if (this.taskId != null) {
            TaskEntity taskEntity = commandContext.getTaskEntityManager().findTaskById(taskId);
            activityId = taskEntity.getExecution().getActivityId();
            processInstanceId = taskEntity.getProcessInstanceId();
            this.collectionVariableName = "assigneeList";
            this.collectionElementVariableName = "assignee";
        }

        String executionId = null;
        if (operateType.equalsIgnoreCase(CounterSignOperationType.ADD)) {
            executionId = addInstance();
        } else if (operateType.equalsIgnoreCase(CounterSignOperationType.REMOVE)) {
            executionId = removeInstance();
        }

        return executionId;
    }

    /**
     * <li>加签
     */
    public String addInstance() {
        if (isParallel()) {
            return addParallelInstance();
        } else {
            addSequentialInstance();
            return null;
        }
    }

    /**
     * <li>减签
     */
    public String removeInstance() {
        if (isParallel()) {
            removeParallelInstance();
        } else {
            removeSequentialInstance();
        }
        return null;
    }

    /**
     * <li>添加一条并行实例
     */
    private String addParallelInstance() {
        ExecutionEntity parentExecutionEntity = commandContext.getExecutionEntityManager()
            .findExecutionById(processInstanceId).findExecution(activityId);
        checkLoopAssignExist(parentExecutionEntity);

        ExecutionEntity execution = parentExecutionEntity.createExecution();
        execution.setActive(true);
        execution.setConcurrent(true);
        execution.setScope(false);

        if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
            .equals(getActivity().getProperty("type"))) {
            ExecutionEntity extraScopedExecution = execution.createExecution();
            extraScopedExecution.setActive(true);
            extraScopedExecution.setConcurrent(false);
            extraScopedExecution.setScope(true);
            execution = extraScopedExecution;
        }

        setLoopVariable(parentExecutionEntity, NR_OF_INSTANCES,
            (Integer) parentExecutionEntity.getVariableLocal(NR_OF_INSTANCES) + 1);
        setLoopVariable(parentExecutionEntity, NR_OF_ACTIVEINSTANCES,
            (Integer) parentExecutionEntity.getVariableLocal(NR_OF_ACTIVEINSTANCES) + 1);
        setLoopVariable(execution, "loopCounter", parentExecutionEntity.getExecutions().size() + 1);
        setLoopVariable(execution, collectionElementVariableName, assignee);
        ActivityImpl activity = getActivity();
        execution.executeActivity(activity);
        return execution.getId();
    }

    private void checkLoopAssignExist(ExecutionEntity parentExecutionEntity) {
        Set<String> var = getLoopVariable(parentExecutionEntity, collectionElementVariableName);
        if (var != null) {
            if (var.contains(assignee)) {
                throw new RuntimeException("加签失败，处理人已经存在会签列表中：" + assignee);
            }
        }
    }

    /**
     * <li>给串行实例集合中添加一个审批人
     */
    @SuppressWarnings("unchecked")
    private void addSequentialInstance() {
        ExecutionEntity execution = getActivieExecutions().get(0);

        if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
            .equals(getActivity().getProperty("type"))) {
            if (!execution.isActive() && execution.isEnded() && ((execution.getExecutions() == null)
                || (execution.getExecutions().size() == 0))) {
                execution.setActive(true);
            }
        }

        Collection<String> col = (Collection<String>) execution.getVariable(collectionVariableName);
        col.add(assignee);
        execution.setVariable(collectionVariableName, col);
        setLoopVariable(execution, NR_OF_INSTANCES,
            (Integer) execution.getVariableLocal(NR_OF_INSTANCES) + 1);
    }

    /**
     * <li>移除一条并行实例
     */
    private void removeParallelInstance() {
        List<ExecutionEntity> executions = getActivieExecutions();

        for (ExecutionEntity executionEntity : executions) {
            if (humanTaskDto != null
                && executionEntity.getId().equals(humanTaskDto.getExecutionId())) {
                executionEntity.remove();

                ExecutionEntity parentConcurrentExecution = executionEntity.getParent();

                if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                    .equals(getActivity().getProperty("type"))) {
                    parentConcurrentExecution = parentConcurrentExecution.getParent();
                }

                setLoopVariable(parentConcurrentExecution, NR_OF_INSTANCES,
                    (Integer) parentConcurrentExecution.getVariableLocal(NR_OF_INSTANCES) - 1);
                setLoopVariable(parentConcurrentExecution, NR_OF_ACTIVEINSTANCES,
                    (Integer) parentConcurrentExecution.getVariableLocal(NR_OF_ACTIVEINSTANCES) - 1);

                break;
            }
        }
    }

    /**
     * <li>冲串行列表中移除未完成的用户(当前执行的用户无法移除)
     */
    @SuppressWarnings("unchecked")
    private void removeSequentialInstance() {
        ExecutionEntity executionEntity = getActivieExecutions().get(0);

        Collection<String> col = (Collection<String>) executionEntity
            .getVariable(collectionVariableName);
        log.info("移除前审批列表 : {}", col.toString());
        col.remove(assignee);
        executionEntity.setVariable(collectionVariableName, col);
        setLoopVariable(executionEntity, NR_OF_INSTANCES,
            (Integer) executionEntity.getVariableLocal(NR_OF_INSTANCES) - 1);

        // 如果串行要删除的人是当前active执行,
        if (executionEntity.getVariableLocal(collectionElementVariableName).equals(assignee)) {
            throw new ActivitiException("当前正在执行的实例,无法移除!");
        }

        log.info("移除后审批列表 : {}", col.toString());
    }

    /**
     * <li>获取活动的执行 , 子流程的活动执行是其孩子执行(并行多实例情况下)
     * <li>串行情况下获取的结果数量为1
     */
    protected List<ExecutionEntity> getActivieExecutions() {
        List<ExecutionEntity> activeExecutions = new ArrayList<ExecutionEntity>();
        ActivityImpl activity = getActivity();
        List<ExecutionEntity> executions = getChildExecutionByProcessInstanceId();

        for (ExecutionEntity execution : executions) {
            if (execution.isActive() && (execution.getActivityId().equals(activityId)
                || activity.contains(execution.getActivity()))) {
                activeExecutions.add(execution);
            }
        }

        return activeExecutions;
    }

    /**
     * <li>获取流程实例根的所有子执行
     */
    protected List<ExecutionEntity> getChildExecutionByProcessInstanceId() {
        return commandContext.getExecutionEntityManager()
            .findChildExecutionsByProcessInstanceId(processInstanceId);
    }

    /**
     * <li>返回当前节点对象
     */
    protected ActivityImpl getActivity() {
        return getProcessDefinition().findActivity(activityId);
    }

    /**
     * <li>判断节点多实例类型是否是并发
     */
    protected boolean isParallel() {
        return "parallel".equals(getActivity().getProperty("multiInstance"));
    }

    /**
     * <li>返回流程定义对象
     */
    protected ProcessDefinitionImpl getProcessDefinition() {
        return getProcessInstanceEntity().getProcessDefinition();
    }

    /**
     * <li>返回流程实例的根执行对象
     * 
     * @return 执行线程
     */
    protected ExecutionEntity getProcessInstanceEntity() {
        return commandContext.getExecutionEntityManager().findExecutionById(processInstanceId);
    }

    /**
     * <li>添加本地变量
     * 
     * @param execution
     *            当前执行
     * @param variableName
     *            变量名
     * @param value
     *            变量值
     */
    protected void setLoopVariable(ActivityExecution execution, String variableName, Object value) {
        execution.setVariableLocal(variableName, value);
    }

    /**
     * 返回会签中的变量
     * 
     * @param execution 执行线程
     * @param variableName 变量名
     * @return set
     */
    protected Set<String> getLoopVariable(ActivityExecution execution, String variableName) {
        Set<String> ass = new HashSet<String>();
        List<ExecutionEntity> exList = getChildExecutionByProcessInstanceId();
        if (exList != null) {
            for (ActivityExecution ae : exList) {
                if (ae.getVariable(variableName) != null) {
                    ass.add(String.valueOf(ae.getVariable(variableName)));
                }
            }
        }
        return ass;
    }
}
