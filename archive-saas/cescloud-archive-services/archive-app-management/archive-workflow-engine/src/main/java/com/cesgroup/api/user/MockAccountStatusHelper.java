package com.cesgroup.api.user;

/**
 * 账户状态Helper默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockAccountStatusHelper implements AccountStatusHelper {

    @Override
    public boolean isLocked(String username, String application) {
        return false;
    }

    @Override
    public String getAccountStatus(String username, String application) {
        return AccountStatus.ENABLED;
    }
}
