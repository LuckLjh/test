package com.cesgroup.api.user;

/**
 * 用户缓存
 * 
 * @author 国栋
 *
 */
public interface UserCache {

    /**
     * 根据ID获取用户信息
     */
    UserDTO findById(String id);

    /**
     * 根据用户名获取用户信息
     */
    UserDTO findByUsername(String username, String userRepoRef);

    /**
     * 根据REF获取用户信息
     */
    UserDTO findByRef(String ref, String userRepoRef);

    /**
     * 根据昵称获取用户信息
     */
    UserDTO findByNickName(String nickName);

    void updateUser(UserDTO userDto);

    void removeUser(UserDTO userDto);
}
