package com.cesgroup.api.userauth;

/**
 * 用户auth连接器默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockUserAuthConnector implements UserAuthConnector {

    @Override
    public UserAuthDTO findByUsername(String username, String tenantId) {
        UserAuthDTO userAuthDto = new UserAuthDTO();
        userAuthDto.setId(username);
        userAuthDto.setUsername(username);
        userAuthDto.setDisplayName(username);
        userAuthDto.setEnabled(true);

        return userAuthDto;
    }

    @Override
    public UserAuthDTO findByRef(String ref, String tenantId) {
        UserAuthDTO userAuthDto = new UserAuthDTO();
        userAuthDto.setId(ref);
        userAuthDto.setUsername(ref);
        userAuthDto.setDisplayName(ref);
        userAuthDto.setEnabled(true);

        return userAuthDto;
    }

    @Override
    public UserAuthDTO findById(String id, String tenantId) {
        UserAuthDTO userAuthDto = new UserAuthDTO();
        userAuthDto.setId(id);
        userAuthDto.setUsername(id);
        userAuthDto.setDisplayName(id);
        userAuthDto.setEnabled(true);

        return userAuthDto;
    }
}
