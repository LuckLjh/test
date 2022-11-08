package com.cesgroup.api.user;

/**
 * 用户同步连接器接口
 * 
 * @author 国栋
 *
 */
public interface UserSyncConnector {

    /**
     * 跟新用户
     * 
     * @param userDto 用户DTO
     */
    void updateUser(UserDTO userDto);
}
