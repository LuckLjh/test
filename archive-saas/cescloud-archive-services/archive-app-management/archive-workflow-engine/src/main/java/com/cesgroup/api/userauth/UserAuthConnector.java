package com.cesgroup.api.userauth;

/**
 * 用户连接器
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public interface UserAuthConnector {

    UserAuthDTO findByUsername(String username, String tenantId);

    UserAuthDTO findByRef(String ref, String tenantId);

    UserAuthDTO findById(String id, String tenantId);
}
