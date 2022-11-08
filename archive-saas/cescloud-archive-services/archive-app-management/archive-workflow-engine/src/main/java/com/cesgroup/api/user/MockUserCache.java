package com.cesgroup.api.user;

/**
 * 用户缓存默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockUserCache implements UserCache {

    @Override
    public UserDTO findById(String id) {
        UserDTO userDto = new UserDTO();
        userDto.setId(id);
        userDto.setUsername(id);
        userDto.setDisplayName(id);

        return userDto;
    }

    @Override
    public UserDTO findByUsername(String username, String userRepoRef) {
        UserDTO userDto = new UserDTO();
        userDto.setId(username);
        userDto.setUsername(username);
        userDto.setDisplayName(username);

        return userDto;
    }

    @Override
    public UserDTO findByRef(String ref, String userRepoRef) {
        UserDTO userDto = new UserDTO();
        userDto.setId(ref);
        userDto.setUsername(ref);
        userDto.setDisplayName(ref);

        return userDto;
    }

    @Override
    public UserDTO findByNickName(String nickName) {
        UserDTO userDto = new UserDTO();
        userDto.setId(nickName);
        userDto.setUsername(nickName);
        userDto.setDisplayName(nickName);

        return userDto;
    }

    @Override
    public void updateUser(UserDTO userDto) {
    }

    @Override
    public void removeUser(UserDTO userDto) {
    }
}
