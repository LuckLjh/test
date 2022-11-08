package com.cesgroup.api.tenant;

import java.util.Collections;
import java.util.List;

/**
 * 模拟租户连接器
 * 
 * @author 国栋
 *
 */
public class MockTenantConnector implements TenantConnector {

    private TenantDTO tenantDto;

    /**
     * 默认构造
     */
    public MockTenantConnector() {
        tenantDto = new TenantDTO();
        tenantDto.setId("1");
        tenantDto.setRef("1");
        tenantDto.setCode("default");
        tenantDto.setUserRepoRef("1");
    }

    @Override
    public TenantDTO findById(String id) {
        return tenantDto;
    }

    @Override
    public TenantDTO findByRef(String ref) {
        return tenantDto;
    }

    @Override
    public TenantDTO findByCode(String code) {
        return tenantDto;
    }

    @Override
    public List<TenantDTO> findAll() {
        return Collections.singletonList(tenantDto);
    }

    @Override
    public List<TenantDTO> findSharedTenants() {
        return Collections.singletonList(tenantDto);
    }
}
