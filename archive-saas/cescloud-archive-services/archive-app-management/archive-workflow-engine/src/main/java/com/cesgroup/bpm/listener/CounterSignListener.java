package com.cesgroup.bpm.listener;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import com.cesgroup.api.user.UserConnector;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import com.cesgroup.bpm.persistence.domain.BpmConfNode;
import com.cesgroup.bpm.persistence.domain.BpmConfUser;
import com.cesgroup.bpm.persistence.manager.BpmConfBaseManager;
import com.cesgroup.bpm.persistence.manager.BpmConfNodeManager;
import com.cesgroup.bpm.persistence.manager.BpmConfUserManager;
import com.cesgroup.core.spring.ApplicationContextHelper;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.workflow.expression.ExpressionManagerUtil;

/**
 * 替换办理人监听器
 *
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class CounterSignListener implements ExecutionListener {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    public void notify(final DelegateExecution execution) throws Exception {
        //当前节点是否配置了指定人员
        final Object codeList = execution.getVariable("_codeList");
        Object assignUsers = null;
        //先查看是否这个节点配置过
        if (null != codeList) {
            final String code = (String) codeList;
            //code相同或者是默认不选节点
            if (execution.getCurrentActivityId().equals(code)
                || "defaultRunNodeWorkflowNode".equals(code)) {
                assignUsers = execution.getVariable("_codeAssigneeList");
                if (assignUsers != null && !"".equals(assignUsers)) {
                    final String asUsers = (String) assignUsers;
                    final Set<String> users = new LinkedHashSet<String>();
                    for (final String u : asUsers.split(";")) {
                        final String[] s = u.split(":");
                        if (1 == s.length) {
                            users.add(s[0]);
                        } else {
                            users.add(s[1]);
                        }
                    }
                    execution.setVariableLocal("assigneeList", users);
                    return;
                }

            }
        }
        final BpmConfBaseManager confBaseManager = ApplicationContextHelper
            .getBean(BpmConfBaseManager.class);
        final List<BpmConfBase> confBases = confBaseManager.find(
            "from BpmConfBase t where t.processDefinitionId = ?0",
            execution.getProcessDefinitionId());
        if (confBases != null) {
            final BpmConfBase confBase = confBases.get(0);
            final BpmConfNodeManager confNodeManager = ApplicationContextHelper
                .getBean(BpmConfNodeManager.class);
            final List<BpmConfNode> nodes = confNodeManager.find(
                "from BpmConfNode t where t.bpmConfBase = ?0 and t.code = ?1", confBase,
                execution.getCurrentActivityId());
            if (nodes != null) {
                if (WorkflowConstants.NodeTypeConstants.NODETYPE_SUBPROCESS
                    .equals(execution.getCurrentActivityName())) {
                    //当节点是子流程且存在parallelProcessList变量的时候，改流程根据人数并发出相应的个数
                    final Set<String> users = new LinkedHashSet<String>();
                    String userStr = null;
                    if (null != execution.getVariable("parallelProcessList")) {
                        userStr = (String) execution.getVariable("parallelProcessList");
                        final String[] usersArr = userStr.split(",");
                        for (final String user : usersArr) {
                            users.add(user);
                        }
                        execution.setVariableLocal("assigneeList", users);
                    }
                } else {
                    if (nodes.size() == 0) {
                        return;
                    }
                    final BpmConfNode node = nodes.get(0);
                    final BpmConfUserManager confUserManager = ApplicationContextHelper
                        .getBean(BpmConfUserManager.class);
                    final List<BpmConfUser> confUsers = confUserManager
                        .find("from BpmConfUser t where t.bpmConfNode = ?0", node);
                    if (confUsers != null) {
                        // List<String> users = new ArrayList<String>();
                        final Set<String> users = new LinkedHashSet<String>();
                        for (final BpmConfUser confU : confUsers) {
                            if (confU.getType().intValue() == 1) {
                                // 参与人
                                final String temp = confU.getValue().replace("表达式:", "");

                                //users.add(u[1]);
                                final Object v = parseExpression(execution.getId(), temp);
                                if (v instanceof Collection) {
                                    final Collection<String> c = (Collection<String>) v;
                                    for (final String userId : c) {
                                        users.add(userId);
                                    }
                                } else if (v instanceof String[]) {
                                    final String[] arr = (String[]) v;
                                    for (final String userId : arr) {
                                        users.add(userId);
                                    }
                                } else {
                                    users.add(v.toString());
                                }
                            } else if (confU.getType().intValue() == 2) {
                                final UserConnector uc = ApplicationContextHelper
                                    .getBean(UserConnector.class);
                                // 参与人组
                                // TODO (部门)研发中心:id,
                                String temp = confU.getValue();
                                if (temp != null && !"".equals(temp)) {
                                    if (temp.startsWith("(部门)")) {
                                        // 拆部门
                                        temp = temp.replace("(部门)", "");
                                        final String[] orgs = temp.split(",");
                                        if (orgs != null && orgs.length > 0) {

                                            for (final String s : orgs) {
                                                final String[] ids = s.split(":");
                                                if (ids != null && ids.length > 0) {
                                                    final List<UserDTO> uds = uc.findByOrgId(ids[1]);
                                                    for (final UserDTO userDTO : uds) {
                                                        users.add(userDTO.getId());
                                                    }
                                                }
                                            }
                                        }
                                    } else if (temp.startsWith("(角色)")) {
                                        // 拆角色
                                        temp = temp.replace("(角色)", "");
                                        final String[] roles = temp.split(",");
                                        if (roles != null && roles.length > 0) {

                                            for (final String s : roles) {
                                                final String[] ids = s.split(":");
                                                if (ids != null && ids.length > 0) {
                                                    final List<UserDTO> uds = uc.findByRoleId(ids[1]);
                                                    for (final UserDTO userDTO : uds) {
                                                        users.add(userDTO.getId());
                                                    }
                                                }
                                            }
                                        }
                                    } else if (temp.startsWith("表达式:") || temp.startsWith("${")) {
                                        temp = temp.replace("表达式:", "");
                                        final Object v = parseExpression(execution.getId(), temp);
                                        if (v instanceof Collection) {
                                            final Collection<String> c = (Collection<String>) v;
                                            for (final String userId : c) {
                                                users.add(userId);
                                            }
                                        } else if (v instanceof String[]) {
                                            final String[] arr = (String[]) v;
                                            for (final String userId : arr) {
                                                users.add(userId);
                                            }
                                        } else {
                                            users.add(v.toString());
                                        }

                                    }
                                }
                            }
                        }

                        if (!execution.getVariablesLocal().containsKey("assigneeList")) {
                            execution.setVariableLocal("assigneeList", users);
                        }
                    }
                }

            }
        }

    }

    /**
     * 解析表达式
     *
     * @param executionId
     *            执行线程ID
     * @param expressionText
     *            表达式
     */
    private Object parseExpression(final String executionId, final String expressionText) {
        if (expressionText.startsWith("${")) {
            final ExecutionEntity taskEntity = Context.getCommandContext().getExecutionEntityManager()
                .findExecutionById(executionId);
            return ExpressionManagerUtil.getInstance().executeExpressionByVariableScope(taskEntity,
                expressionText);
        }
        return expressionText;
    }

}
