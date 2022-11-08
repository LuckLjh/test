package com.cesgroup.api.user;

/**
 * 账户别名转换器默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockAccountAliasConverter implements AccountAliasConverter {

    @Override
    public String convertAlias(String alias) {
        return alias;
    }
}
