package com.cesgroup.api.userrepo;

/**
 * 用户仓库缓存
 * 
 * @author 国栋
 *
 */
public interface UserRepoCache {

    UserRepoDTO findById(String id);

    UserRepoDTO findByCode(String code);

    void updateUserRepo(UserRepoDTO userRepoDto);

    void removeUserRepo(UserRepoDTO userRepoDto);
}
