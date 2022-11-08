package com.cesgroup.api.user;

/**
 * 应用表名转换接口默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockApplicationAliasConverter implements ApplicationAliasConverter {

    @Override
    public String convertAlias(String type, String ip) {
        return type;
    }
}
