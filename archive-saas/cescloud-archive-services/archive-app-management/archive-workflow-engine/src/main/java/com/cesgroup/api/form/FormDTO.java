package com.cesgroup.api.form;

import java.util.ArrayList;
import java.util.List;

/**
 * 表单dto
 * 
 * @author 国栋
 *
 */
public class FormDTO {

    private String id;

    /** 编码 **/
    private String code;

    /** 表单名字 **/
    private String name;

    /** 表单内容 **/
    private String content;

    /** 是否重定向 **/
    private boolean redirect;

    /** url **/
    private String url;

    /** 流程定义id **/
    private String processDefinitionId;

    /** 任务id **/
    private String taskId;

    private List<String> buttons = new ArrayList<String>();

    private String activityId;

    private boolean autoCompleteFirstTask;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<String> getButtons() {
        return buttons;
    }

    public void setButtons(List<String> buttons) {
        this.buttons = buttons;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public boolean isAutoCompleteFirstTask() {
        return autoCompleteFirstTask;
    }

    public void setAutoCompleteFirstTask(boolean autoCompleteFirstTask) {
        this.autoCompleteFirstTask = autoCompleteFirstTask;
    }

    /**
     * 如果是开始表单，获取流程定义ID，不是的话，获取任务ID
     * @return String
     */
    public String getRelatedId() {
        if (isStartForm()) {
            return processDefinitionId;
        } else {
            return taskId;
        }
    }

    public boolean isTaskForm() {
        return taskId != null;
    }

    public boolean isStartForm() {
        return taskId == null;
    }

    public boolean isExists() {
        return code != null;
    }
}
