package com.cesgroup.api.user;

import java.util.List;

/**
 * 用户连接器默认实现
 *
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class MockUserConnector implements UserConnector {

    /**
     * 根据唯一标识获取用户信息.
     *
     * @param id
     *            用户的唯一标识，即便是不同用户库的用户id也是唯一的
     */
    @Override
    public UserDTO findById(String id) {
        final UserDTO result = new UserDTO();
        result.setId("390f7faf6ec144c5a203b5196d867692");
        result.setDisplayName("workflow");
        result.setUsername("工作流测试1");
        return result;
    }

    @Override
    public List<UserDTO> findByIdList(List<String> idList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<UserDTO> findByOrgId(String orgId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<UserDTO> findByRoleId(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *
     * @see com.cesgroup.api.user.UserConnector#
     *      getUserByUserIdsForUserTaskCustomRule(
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.util.List, java.lang.String)
     */
    @Override
    public List<String> getUserByUserIdsForUserTaskCustomRule(String userId, String orgClass,
        String mayorLineId, List<String> roleIds, String isAdmin) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *
     * @see com.cesgroup.api.user.UserConnector#
     *      getUserByOrgIdForUserTaskCustomRule(
     *      java.lang.String, java.lang.String, java.util.List,
     *      java.lang.String)
     */
    @Override
    public List<String> getUserByOrgIdForUserTaskCustomRule(String orgId, String orgLevelId,
        String mayorLineId, List<String> roleIds, String isAdmin) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<UserDTO> findByRoleIdAndTenantId(String candidateGroupExpression, String tenantId) {
        // TODO Auto-generated method stub
        return null;
    }

}
