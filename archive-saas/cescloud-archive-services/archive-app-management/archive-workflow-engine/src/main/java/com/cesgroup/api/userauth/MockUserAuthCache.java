package com.cesgroup.api.userauth;

/**
 * 用户auth缓存默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockUserAuthCache implements UserAuthCache {

    @Override
    public UserAuthDTO findByUsername(String username, String tenantId) {
        return null;
    }

    @Override
    public UserAuthDTO findByRef(String ref, String tenantId) {
        return null;
    }

    @Override
    public UserAuthDTO findById(String id, String tenantId) {
        return null;
    }

    @Override
    public void updateUserAuth(UserAuthDTO userAuthDto) {
    }

    @Override
    public void removeUserAuth(UserAuthDTO userAuthDto) {
    }
}
