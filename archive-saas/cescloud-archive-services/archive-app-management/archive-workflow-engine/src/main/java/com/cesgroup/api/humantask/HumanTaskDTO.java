package com.cesgroup.api.humantask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 人工任务dto对象
 *
 * @author 国栋
 *
 */
public class HumanTaskDTO {

    private String id;

    /** 业务主键 **/
    private String businessKey;

    /** 任务环节 **/
    private String name;

    /** 任务标题 **/
    private String presentationSubject;

    /** 任务名称 **/
    private String presentationName;

    /** 描述 **/
    private String description;

    /** 处理人 **/
    private String assignee;

    /** 所有人 **/
    private String owner;

    /** 处理人名称 **/
    private String assigneeName;

    /** 所有人名称 **/
    private String ownerName;

    /** 到期时间（到期时间） **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expirationTime;

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    /** 代理状态 **/
    private String delegateStatus;

    /** 创建时间（到达时间） **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 完成时间 **/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;

    /** 挂起状态,默认是none，挂起为：2 **/
    private String suspendStatus;

    /** 代码 */
    private String code;

    /** 关联的任务ID */
    private String taskId;

    /** 关联的分支ID */
    private String executionId;

    /** 关联的流程实例ID */
    private String processInstanceId;

    /** 关联的流程定义ID */
    private String processDefinitionId;

    /** 关联的流程发起人 */
    private String processStarter;

    /** 关联的流程发起人名称 */
    private String processStarterName;

    @Column(name = "PROCESS_STARTER_NAME", length = 200)
    public String getProcessStarterName() {
        return processStarterName;
    }

    public void setProcessStarterName(String processStarterName) {
        this.processStarterName = processStarterName;
    }

    /**  **/
    private String taskDefinitionKey;

    /** 流程业务标识 */
    private String processBusinessKey;

    /** 优先级 */
    private int priority;

    /** 工期 **/
    private String duration;

    /** 租户id **/
    private String tenantId;

    /** 分类 **/
    private String category;

    private String form;

    /** 状态 */
    private String status;

    /** 状态 */
    private String completeStatus;

    /** 租户 */
    private String parentId;

    /** 任务类型 **/
    private String catalog;

    /** 动作 **/
    private String action;

    /** 是否同意:0-否，1-是 **/
    private String agreement;

    /** 意见 **/
    private String comment;

    /** 消息 **/
    private String message;

    /** 是否会签:0-否，1-是 **/
    private String isCountersign;

    /** 流程名称 */
    private String attr1;

    /** 催办标志：不为空即催 */
    private String attr2;

    /** 抄送人/沟通目标对象 */
    private String attr3;

    /** 扩展字段 */
    private String attr4;

    /** 扩展字段 */
    private String attr5;

    private List<HumanTaskDTO> children = new ArrayList<HumanTaskDTO>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPresentationSubject() {
        return presentationSubject;
    }

    public void setPresentationSubject(String presentationSubject) {
        this.presentationSubject = presentationSubject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDelegateStatus() {
        return delegateStatus;
    }

    public void setDelegateStatus(String delegateStatus) {
        this.delegateStatus = delegateStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSuspendStatus() {
        return suspendStatus;
    }

    public void setSuspendStatus(String suspendStatus) {
        this.suspendStatus = suspendStatus;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessStarter() {
        return processStarter;
    }

    public void setProcessStarter(String processStarter) {
        this.processStarter = processStarter;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the completeStatus
     */
    public String getCompleteStatus() {
        return completeStatus;
    }

    /**
     * @param completeStatus
     *            the completeStatus to set
     */
    public void setCompleteStatus(String completeStatus) {
        this.completeStatus = completeStatus;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    /**
     * 设置完成时间的同时设置任务完成耗时
     *
     * @param completeTime
     *            完成的时间
     */
    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
        if (completeTime != null && createTime != null) {
            duration = String.valueOf(completeTime.getTime() - createTime.getTime());
        }
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<HumanTaskDTO> getChildren() {
        return children;
    }

    public void setChildren(List<HumanTaskDTO> children) {
        this.children = children;
    }

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(String attr1) {
        this.attr1 = attr1;
    }

    public String getAttr2() {
        return attr2;
    }

    public void setAttr2(String attr2) {
        this.attr2 = attr2;
    }

    public String getAttr3() {
        return attr3;
    }

    public void setAttr3(String attr3) {
        this.attr3 = attr3;
    }

    public String getAttr4() {
        return attr4;
    }

    public void setAttr4(String attr4) {
        this.attr4 = attr4;
    }

    public String getAttr5() {
        return attr5;
    }

    public void setAttr5(String attr5) {
        this.attr5 = attr5;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    public String getProcessBusinessKey() {
        return processBusinessKey;
    }

    public void setProcessBusinessKey(String processBusinessKey) {
        this.processBusinessKey = processBusinessKey;
    }

    /**
     * @return the isCountersign
     */
    public String getIsCountersign() {
        return isCountersign;
    }

    /**
     * @param isCountersign
     *            the isCountersign to set
     */
    public void setIsCountersign(String isCountersign) {
        this.isCountersign = isCountersign;
    }
}
