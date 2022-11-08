package com.cesgroup.api.userauth;

/**
 * 用户auth缓存
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public interface UserAuthCache {

    /**
     * 根据用户名和租户ID获取用户
     * 
     * @param username
     *            用户名
     * @param tenantId
     *            租户ID
     * @return 用户UserAuthDTO
     */
    UserAuthDTO findByUsername(String username, String tenantId);

    /**
     * 根据ref和租户ID获取用户
     * 
     * @param ref
     *            ref
     * @param tenantId
     *            租户ID
     * @return 用户UserAuthDTO
     */
    UserAuthDTO findByRef(String ref, String tenantId);

    /**
     * 根据ID和租户ID获取用户
     * 
     * @param id
     *            用户ID
     * @param tenantId
     *            租户ID
     * @return 用户UserAuthDTO
     */
    UserAuthDTO findById(String id, String tenantId);

    void updateUserAuth(UserAuthDTO userAuthDto);

    void removeUserAuth(UserAuthDTO userAuthDto);
}
