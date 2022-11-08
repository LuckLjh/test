package com.cesgroup.api.userrepo;

import java.util.List;

/**
 * 用户仓库连接器
 * 
 * @author 国栋
 *
 */
public interface UserRepoConnector {

    /**
     * 根据ID获取用户仓库
     * @param id id
     * @return 用户仓库
     */
    UserRepoDTO findById(String id);

    /**
     * 根据CODE获取用户仓库
     * @param code code
     * @return 用户仓库
     */
    UserRepoDTO findByCode(String code);

    /**
     * 获取所有用户仓库
     * @return list
     */
    List<UserRepoDTO> findAll();
}
