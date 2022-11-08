package com.cescloud.saas.archive.api.modular.workflow.constant;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum WorkflowStatusEnum {

    STATUS_WITHDRAW("withdraw", "撤回"),

    STATUS_ROLLBACK("rollback", "退回"),

    STATUS_TERMINATE("terminate", "终止"),

    STATUS_DELETE("delete", "删除"),

    ACTION_AUTO("auto", "自动提交"),

    ACTION_COMPLETE("complete", "完成任务"),

    ACTION_COPY("copy", "抄送"),

    ACTION_CLAIM("claim", "签收"),

    ACTION_COMMUNICATE("communicate", "沟通"),

    ACTION_TRANSFER("transfer", "转办"),

    ACTION_SKIP("skip", "跳过"),

    ACTION_JUMP("jump", "跳转"),

    ACTION_REMOVE_VOTE("removeVote", "减签");

    private final String name;
    private final String describe;

    private WorkflowStatusEnum(String name, String describe) {
        this.name = name;
        this.describe = describe;
    }

    public static WorkflowStatusEnum getEnum(String name) {
        if (ObjectUtil.isEmpty(name)) {
            return null;
        }
        WorkflowStatusEnum[] values = WorkflowStatusEnum.values();
        for (WorkflowStatusEnum workflowStatusEnum : values) {
            if (workflowStatusEnum.getName().equals(name)) {
                return workflowStatusEnum;
            }
        }
        return null;
    }
}
