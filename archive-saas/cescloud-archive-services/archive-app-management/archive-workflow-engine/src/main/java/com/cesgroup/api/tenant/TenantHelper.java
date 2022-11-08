package com.cesgroup.api.tenant;

/**
 * 租户帮助类。
 * 
 * @author 国栋
 *
 */
public class TenantHelper {

    private static ThreadLocal<TenantDTO> tenantThreadLocal = new ThreadLocal<TenantDTO>();

    protected TenantHelper() {
    }

    public static String getTenantId() {
        return getTenantDto().getId();
    }

    public static String getTenantCode() {
        return getTenantDto().getCode();
    }

    public static String getUserRepoRef() {
        return getTenantDto().getUserRepoRef();
    }

    /**
     * 获取当前租户
     * 
     * @return 租户对象
     */
    public static TenantDTO getTenantDto() {
        TenantDTO tenantDto = tenantThreadLocal.get();

        if (tenantDto == null) {
            throw new IllegalStateException("无法找到租户");
        }

        return tenantDto;
    }

    public static void setTenantDto(TenantDTO tenantDto) {
        tenantThreadLocal.set(tenantDto);
    }

    public static void clear() {
        tenantThreadLocal.remove();
    }
}
