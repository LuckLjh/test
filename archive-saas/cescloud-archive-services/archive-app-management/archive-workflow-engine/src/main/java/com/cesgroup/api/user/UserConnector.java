package com.cesgroup.api.user;

import java.util.List;

/**
 * 用户连接器
 *
 * @author 国栋
 *
 */
public interface UserConnector {

    /**
     * 根据唯一标识获取用户信息.
     *
     * @param id
     *            用户的唯一标识，即便是不同用户库的用户id也是唯一的
     */
    UserDTO findById(String id);

    /**
     * 根据用户ID集合获取用户信息集合
     * 
     * @param idList
     * @return
     */
    List<UserDTO> findByIdList(List<String> idList);

    /** 组织及其自组织 */
    List<UserDTO> findByOrgId(String orgId);

    /**
     * 根据角色ID获取用户信息集合
     *
     * @param roleId
     * @return
     */
    List<UserDTO> findByRoleId(String roleId);

    /**
     * 获取用户任务中自定义规则配置用户ID
     *
     * @param userId
     *            用户ID
     * @param orgClass
     *            用户来自
     * @param mayorLineId
     *            专业线ID
     * @param roleIds
     *            角色IDs
     * @param isAdmin
     *            是否是管理员
     * @return
     *
     */
    List<String> getUserByUserIdsForUserTaskCustomRule(String userId, String orgClass,
        String mayorLineId, List<String> roleIds,
        String isAdmin);

    /**
     * @param orgId
     *
     * @param mayorLineId
     *
     * @param roleIds
     *
     * @param isAdmin
     *
     * @return
     *
     */
    List<String> getUserByOrgIdForUserTaskCustomRule(String orgId, String orgLevelld,
        String mayorLineId,
        List<String> roleIds, String isAdmin);

    List<UserDTO> findByRoleIdAndTenantId(String candidateGroupExpression, String tenantId);

}
