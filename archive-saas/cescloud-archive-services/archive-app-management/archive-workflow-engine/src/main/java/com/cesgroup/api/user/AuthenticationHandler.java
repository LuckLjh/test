package com.cesgroup.api.user;

/**
 * 认证处理
 * 
 * @author 国栋
 *
 */
public interface AuthenticationHandler {

    /**
     * 是否支持类型
     * 
     * @param type
     *            类型
     * @return boolean
     */
    boolean support(String type);

    /**
     * 鉴权
     * 
     * @param username
     *            用户名
     * @param password
     *            密码
     * @param application
     *            应用
     * @return String
     */
    String doAuthenticate(String username, String password, String application);
}
