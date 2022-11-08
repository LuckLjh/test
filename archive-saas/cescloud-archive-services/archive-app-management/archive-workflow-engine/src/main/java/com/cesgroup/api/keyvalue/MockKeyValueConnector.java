package com.cesgroup.api.keyvalue;

import com.cesgroup.core.page.Page;

import java.util.List;
import java.util.Map;

/**
 * KeyValueConnector默认实现类
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockKeyValueConnector implements KeyValueConnector {

    @Override
    public Record findByCode(String code) {
        return null;
    }

    @Override
    public Record findByRef(String ref) {
        return null;
    }

    @Override
    public void save(Record record) {
    }

    @Override
    public void removeByCode(String code) {
    }

    @Override
    public List<Record> findByStatus(int status, String userId, String tenantId) {
        return null;
    }

    @Override
    public Page pagedQuery(Page page, int status, String userId, String tenantId) {
        return null;
    }

    @Override
    public Page pagedQuery(Page page, int status, String userId, String tenantId, String category) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long findTotalCount(String category, String q, String tenantId) {
        return 0L;
    }

    @Override
    public List<Map<String, Object>> findResult(Page page, String category, String tenantId,
                                                Map<String, String> headers, String q) {
        return null;
    }

    @Override
    public Record copyRecord(Record original, List<String> fields) {
        return null;
    }

    @Override
    public long findToalCountByUserId(String userId, int status, String tenantId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Page pageQueryWithBusinessData(Page page, int status, String userId, String tenantId,
        String category) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeByCodes(List<String> codes) {
        // TODO Auto-generated method stub

    }

    @Override
    public Page draftPagedQuery(Page page, int status, String userId, String tenantId) {
        // TODO Auto-generated method stub
        return null;
    }
}
