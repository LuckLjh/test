package com.cesgroup.api.store;

import javax.activation.DataSource;

/**
 * 存储连接器默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockStoreConnector implements StoreConnector {

    @Override
    public StoreDTO saveStore(String model, DataSource dataSource, String tenantId)
        throws Exception {
        return null;
    }

    @Override
    public StoreDTO saveStore(String model, String key, DataSource dataSource, String tenantId)
        throws Exception {
        return null;
    }

    @Override
    public StoreDTO getStore(String model, String key, String tenantId) throws Exception {
        return null;
    }

    @Override
    public void removeStore(String model, String key, String tenantId) throws Exception {
    }
}
