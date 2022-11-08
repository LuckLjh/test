package com.cesgroup.api.user;

/**
 * 认证客户端默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockAuthenticationClient implements AuthenticationClient {

    @Override
    public String doAuthenticate(String username, String password, String type,
                                 String application) {
        return AccountStatus.ENABLED;
    }
}
