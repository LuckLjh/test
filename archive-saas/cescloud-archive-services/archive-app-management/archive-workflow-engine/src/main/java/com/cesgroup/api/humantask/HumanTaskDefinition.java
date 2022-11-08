package com.cesgroup.api.humantask;

/**
 * 人工任务定义
 * 
 * @author 国栋
 *
 */
public class HumanTaskDefinition {

    /** 任务key **/
    private String key;

    /** 任务名字 **/
    private String name;

    /** 处理人 **/
    private String assignee;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
