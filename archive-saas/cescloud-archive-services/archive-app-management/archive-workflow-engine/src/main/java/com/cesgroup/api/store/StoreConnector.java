package com.cesgroup.api.store;

import javax.activation.DataSource;

/**
 * 存储连接器
 * 
 * @author 国栋
 *
 */
public interface StoreConnector {

    /**
     * 保存
     * 
     * @param model
     *            模型
     * @param dataSource
     *            数据源
     * @param tenantId
     *            租户
     * @return 保存对象
     * @throws Exception
     *             执行失败抛出异常
     */
    StoreDTO saveStore(String model, DataSource dataSource, String tenantId) throws Exception;

    StoreDTO saveStore(String model, String key, DataSource dataSource, String tenantId)
        throws Exception;

    StoreDTO getStore(String model, String key, String tenantId) throws Exception;

    void removeStore(String model, String key, String tenantId) throws Exception;
}
