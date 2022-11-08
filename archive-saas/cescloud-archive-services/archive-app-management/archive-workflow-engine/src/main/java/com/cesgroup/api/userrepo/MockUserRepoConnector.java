package com.cesgroup.api.userrepo;

import java.util.List;

/**
 * 用户仓库连接器默认实现
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockUserRepoConnector implements UserRepoConnector {

    @Override
    public UserRepoDTO findById(String id) {
        return null;
    }

    @Override
    public UserRepoDTO findByCode(String code) {
        return null;
    }

    @Override
    public List<UserRepoDTO> findAll() {
        return null;
    }
}
