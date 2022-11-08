package com.cesgroup.api.tenant;

import java.util.List;

/**
 * 租户连接器
 * 
 * @author 国栋
 *
 */
public interface TenantConnector {

    /**
     * 根据id查找租户对象
     * 
     * @param id
     *            id
     * @return 租户对象
     */
    TenantDTO findById(String id);

    TenantDTO findByRef(String ref);

    TenantDTO findByCode(String code);

    List<TenantDTO> findAll();

    List<TenantDTO> findSharedTenants();
}
