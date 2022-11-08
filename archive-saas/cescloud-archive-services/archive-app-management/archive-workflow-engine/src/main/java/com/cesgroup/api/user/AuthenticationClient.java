package com.cesgroup.api.user;

/**
 * 认证客户端
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public interface AuthenticationClient {

    String doAuthenticate(String username, String password, String type, String application);
}
