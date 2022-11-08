package com.cesgroup.api.user;

/**
 * 应用表名转换接口
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public interface ApplicationAliasConverter {

    String convertAlias(String type, String ip);
}
