package com.cesgroup.api.delegate;

import com.cesgroup.core.page.Page;
import com.cesgroup.internal.delegate.persistence.domain.DelegateInfo;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * 代理连接器
 *
 * @author 国栋
 */
public interface DelegateConnector {

    /**
     * 返回办理人
     *
     * @param userId
     *            用户id
     * @param processDefinitionId
     *            流程定义id
     * @param taskDefinitionKey
     *            任务定义key
     * @param tenantId
     *            租户id
     * @return 办理人标识
     */
    String findAttorney(String userId, String processDefinitionId, String taskDefinitionKey,
        String tenantId);

    /**
     * 记录委托代理信息
     *
     * @param userId
     *            用户id
     * @param attorney
     *            办理人标识
     * @param taskId
     *            任务id
     * @param tenantId
     *            租户id
     */
    void recordDelegate(String userId, String attorney, String taskId, String tenantId);

    /**
     * 取消代理
     *
     * @param taskId
     *            任务id
     * @param userId
     *            用户id
     * @param tenantId
     *            租户id
     */
    void cancel(String taskId, String userId, String tenantId);

    /**
     * 代理完成任务
     *
     * @param taskId
     *            任务id
     * @param userId
     *            用户id
     * @param tenantId
     *            租户id
     */
    void complete(String taskId, String userId, String tenantId);

    List<Map<String, Object>> loadBpmProcess(int status);

    void removeAllDelegateInfoCascadeById(String tenantId, String userId, String delegateInfoIds);

    Page queryAllDelegateInfo(String tenantId, String userId, Page page);

    List<DelegateInfo> queryAllDelegateInfo(String tenantId, String userId);

    List<Map<String, Object>> loadModelBpmProcess(int status);

    Map<String, Object> loadBpmProcessByCode(String code);

    void saveDelegateInfo(String delegateUserId, String startTime, String endTime,
        String[] processDefinitionIds, String userId, String tenantId) throws ParseException;

    boolean isExistDelegetedStrategy(String[] processDefinitionIds, String userId, String tenantId);
}
