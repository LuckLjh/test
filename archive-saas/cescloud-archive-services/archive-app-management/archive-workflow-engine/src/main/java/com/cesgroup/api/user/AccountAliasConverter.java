package com.cesgroup.api.user;

/**
 * 账户别名转换器
 * 
 * @author 国栋
 *
 */
public interface AccountAliasConverter {

    /**
     * 转换别名
     * 
     * @param alias 别名
     * @return String
     */
    String convertAlias(String alias);
}
