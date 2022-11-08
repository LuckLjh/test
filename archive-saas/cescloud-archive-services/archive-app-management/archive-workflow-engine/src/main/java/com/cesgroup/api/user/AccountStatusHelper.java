package com.cesgroup.api.user;

/**
 * 账户状态Helper
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public interface AccountStatusHelper {

    boolean isLocked(String username, String application);

    String getAccountStatus(String username, String application);
}
