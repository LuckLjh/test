package com.cesgroup.api.tenant;

/**
 * 租户缓冲接口
 * 
 * @author 国栋
 *
 */
public interface TenantCache {

    TenantDTO findById(String id);

    TenantDTO findByRef(String ref);

    TenantDTO findByCode(String code);

    void updateTenant(TenantDTO tenantDto);

    void removeTenant(TenantDTO tenantDto);
}
