package com.cesgroup.api.delegate;

import com.cesgroup.core.page.Page;
import com.cesgroup.internal.delegate.persistence.domain.DelegateInfo;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * 模拟代理连接器
 * 
 * @author 国栋
 *
 */
public class MockDelegateConnector implements DelegateConnector {

    @Override
    public String findAttorney(String userId, String processDefinitionId, String taskDefinitionKey,
                               String tenantId) {
        return null;
    }

    @Override
    public void recordDelegate(String userId, String attorney, String taskId, String tenantId) {
    }

    @Override
    public void cancel(String taskId, String userId, String tenantId) {
    }

    @Override
    public void complete(String taskId, String complete, String tenantId) {
    }

    @Override
    public List<Map<String, Object>> loadBpmProcess(final int status) {
        return null;
    }

    @Override
    public void removeAllDelegateInfoCascadeById(final String tenantId, final String userId,
        final String delegateInfoIds) {

    }

    @Override
    public Page queryAllDelegateInfo(final String tenantId, final String userId, final Page page) {
        return null;
    }

    @Override
    public List<DelegateInfo> queryAllDelegateInfo(String tenantId, String userId) {
        return null;
    }

    @Override
    public List<Map<String, Object>> loadModelBpmProcess(final int status) {
        return null;
    }

    @Override
    public Map<String, Object> loadBpmProcessByCode(final String code) {
        return null;
    }

    @Override
    public void saveDelegateInfo(final String name, final String startTime, final String endTime,
        final String[] processDefinitionIds, final String userId, final String tenantId)
        throws ParseException {

    }

    @Override
    public boolean isExistDelegetedStrategy(final String[] processDefinitionIds,
        final String userId, final String tenantId) {
        return false;
    }
}
