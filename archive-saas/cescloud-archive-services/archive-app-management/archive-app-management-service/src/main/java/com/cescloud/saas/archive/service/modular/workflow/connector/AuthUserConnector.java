/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.connector</p>
 * <p>文件名:AuthUserConnector.java</p>
 * <p>创建时间:2019年10月14日 下午1:35:55</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.connector;

import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.api.modular.user.entity.SysUser;
import com.cescloud.saas.archive.api.modular.user.feign.RemoteUserService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cesgroup.api.user.UserConnector;
import com.cesgroup.api.user.UserDTO;
import com.cesgroup.core.util.WorkflowConstants;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月14日
 */
@Slf4j
public class AuthUserConnector implements UserConnector {

    @Autowired
    private RemoteUserService remoteUserService;

    private void checkRemoteData(R<?> remoteData, String name) {
        if (!CommonConstants.SUCCESS.equals(remoteData.getCode())) {
            log.error(remoteData.getMsg());
            throw new ArchiveRuntimeException("获取用户（" + name + "）失败", "获取办理用户失败");
        }
        if (null == remoteData.getData()) {
            throw new ArchiveRuntimeException("流程办理用户不存在（" + name + "）", "流程办理用户不存在");
        }
    }

    private UserDTO convert(SysUser sysUser) {
        final UserDTO userDTO = new UserDTO();
        userDTO.setId(sysUser.getUserId().toString());
        userDTO.setUsername(sysUser.getUsername());
        userDTO.setDisplayName(sysUser.getChineseName());
        userDTO.setMobile(sysUser.getPhone());
        return userDTO;
    }

    /**
     *
     * @see com.cesgroup.api.user.UserConnector#findById(java.lang.String)
     */
    @Override
    public UserDTO findById(String id) {
        if (log.isDebugEnabled()) {
            log.debug("AuthUserConnector.findById(String id=[{}])", id);
        }
        if (WorkflowConstants.HumanTaskConstants.USER_AUTO_COMMIT.equals(id)) {
            return autoCommitUserDto();
        }
        final R<SysUser> remoteData = remoteUserService.getUserById(Long.parseLong(id));
        checkRemoteData(remoteData, "findById");
        return convert(remoteData.getData());
    }

    @Override
    public List<UserDTO> findByIdList(List<String> idList) {
        if (null == idList || idList.isEmpty()) {
            return Lists.newArrayList();
        }

        final R<List<SysUser>> remoteData = remoteUserService.getUserListByIdList(convert2int(idList));
        checkRemoteData(remoteData, "findByIdList");
        final List<UserDTO> userList = Lists.newArrayList();
        final List<SysUser> data = remoteData.getData();
        data.forEach(sysUser -> {
            userList.add(convert(sysUser));
        });
        return userList;
    }

    private Long[] convert2int(List<String> idList) {
        final List<Long> intIdList = Lists.newArrayList();
        idList.forEach(id -> {
            if (StrUtil.isNotBlank(id)) {
                intIdList.add(Long.parseLong(id));
            }
        });
        return intIdList.toArray(new Long[] {});
    }

    /**
     *
     * @see com.cesgroup.api.user.UserConnector#findByOrgId(java.lang.String)
     */
    @Override
    public List<UserDTO> findByOrgId(String orgId) {
        //final R<List<SysUser>> remoteData = remoteUserService.getUsersIncludeSubByDeptId(Integer.parseInt(orgId));
        final R<List<SysUser>> remoteData = remoteUserService.getUsersByDeptId(Long.parseLong(orgId));
        checkRemoteData(remoteData, "findByOrgId");
        final List<UserDTO> userList = Lists.newArrayList();
        final List<SysUser> data = remoteData.getData();
        data.forEach(sysUser -> {
            userList.add(convert(sysUser));
        });
        return userList;
    }

    /**
     *
     * @see com.cesgroup.api.user.UserConnector#findByRoleId(java.lang.String)
     */
    @Override
    public List<UserDTO> findByRoleId(String roleId) {
        final R<List<SysUser>> remoteData = remoteUserService.getUsersByRoleId(Long.parseLong(roleId));
        checkRemoteData(remoteData, "findByRoleId");
        final List<UserDTO> userList = Lists.newArrayList();
        final List<SysUser> data = remoteData.getData();
        data.forEach(sysUser -> {
            userList.add(convert(sysUser));
        });
        return userList;
    }

    /**
     * 自动提交用户
     *
     * @return UserDTO
     */
    private UserDTO autoCommitUserDto() {
        final UserDTO userDTO = new UserDTO();
        userDTO.setId("-1");
        userDTO.setUsername(WorkflowConstants.HumanTaskConstants.USER_AUTO_COMMIT);
        userDTO.setDisplayName("自动提交");
        return userDTO;
    }

    /**
     * 横向、纵向、是否为管理员、专业线未实现（用户管理暂无此功能）
     *
     * @see com.cesgroup.api.user.UserConnector#getUserByUserIdsForUserTaskCustomRule(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.List,
     *      java.lang.String)
     */
    @Override
    public List<String> getUserByUserIdsForUserTaskCustomRule(String userId, String orgLevelld, String mayorLineId,
        List<String> roleIds, String isAdmin) {
        R<List<Long>> remoteData = null;

        Long[] roleIdArr = null;
        if (!isManager(isAdmin)) {
            roleIdArr = roleIds.stream().map(id -> new Long(id)).collect(Collectors.toList())
                .toArray(new Long[] {});
        }

        switch (orgLevelld) {

        case "currentDept": //当前层级
        case "currentSuperior": //兼容历史版本
            if (isManager(isAdmin)) {
                remoteData = remoteUserService.getAdminUserIdListByUserId(Long.parseLong(userId));
            } else {
                remoteData = remoteUserService.getUserIdListByUserIdAndRoleIds(Long.parseLong(userId), roleIdArr);
            }
            checkRemoteData(remoteData, "getUserByUserIdsForUserTaskCustomRule");
            return remoteData.getData().stream().map(id -> id.toString()).collect(Collectors.toList());

        case "currentDeptIncludeSub": //当前层级及其下属部门
        case "currentDepartments": //兼容历史版本
            remoteData = remoteUserService.getUserIdListIncludeSubByUserIdAndRoleIds(Long.parseLong(userId), roleIdArr, false);
            checkRemoteData(remoteData, "getUserByUserIdsForUserTaskCustomRule");
            return remoteData.getData().stream().map(id -> id.toString()).collect(Collectors.toList());

        case "upper_current_below": //单位及下属部门
			//获取当前层级，上属层级，下属层级
			remoteData = remoteUserService.getUserIdListIncludeSubByUserIdAndRoleIds(Long.parseLong(userId), roleIdArr, true);
			checkRemoteData(remoteData, "getUserIdListIncludeAllByUserIdAndRoleIds");
			return remoteData.getData().stream().map(id -> id.toString()).collect(Collectors.toList());

        /*case "horizontalSuperior": //上级层级(横向)
            break;*/
        case "verticalSuperior": //上级层级(纵向)
            if (isManager(isAdmin)) {
                remoteData = remoteUserService.getVerticalSuperiorAdminUserIdListByUserId(Long.parseLong(userId));
            } else {
                remoteData = remoteUserService.getVerticalSuperiorUserIdListByUserIdAndRoleIds(Long.parseLong(userId),
                    roleIdArr);
            }
            checkRemoteData(remoteData, "getUserByUserIdsForUserTaskCustomRule");
            return remoteData.getData().stream().map(id -> id.toString()).collect(Collectors.toList());
        default:
            break;
        }
        return null;
    }

    /**
     *
     * @see com.cesgroup.api.user.UserConnector#getUserByOrgIdForUserTaskCustomRule(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.List,
     *      java.lang.String)
     */
    @Override
    public List<String> getUserByOrgIdForUserTaskCustomRule(String orgIds, String orgLevelld, String mayorLineId,
        List<String> roleIds, String isAdmin) {

        R<List<Long>> remoteData = null;

        Long[] roleIdArr = null;
        if (!isManager(isAdmin)) {
            roleIdArr = roleIds.stream().map(id -> new Long(id)).collect(Collectors.toList())
                .toArray(new Long[] {});
        }

        Long[] orgIdArr = null;
        orgIdArr = Arrays.asList(orgIds.split(",")).stream().map(id -> new Long(id)).collect(Collectors.toList())
            .toArray(new Long[] {});

        final List<String> userIdList = new ArrayList<String>();
        switch (orgLevelld) {

        case "currentDept": //当前层级
        case "currentSuperior": //兼容历史版本
            for (final Long orgId : orgIdArr) {
                if (isManager(isAdmin)) {
                    remoteData = remoteUserService.getAdminUserIdListByDeptId(orgId);
                } else {
                    remoteData = remoteUserService.getUserIdListByDeptIdAndRoleIds(orgId, roleIdArr);
                }
                checkRemoteData(remoteData, "getUserByOrgIdForUserTaskCustomRule");
                userIdList.addAll(remoteData.getData().stream().map(id -> id.toString()).collect(Collectors.toList()));
            }
            return userIdList;

        case "currentDeptIncludeSub": //当前层级及其下属部门
        case "currentDepartments": //兼容历史版本
            for (final Long orgId : orgIdArr) {
                remoteData = remoteUserService.getUserIdListIncludeSubByDeptIdAndRoleIds(orgId,
                    roleIdArr);
                checkRemoteData(remoteData, "getUserByOrgIdForUserTaskCustomRule");
                userIdList.addAll(remoteData.getData().stream().map(id -> id.toString()).collect(Collectors.toList()));
            }
            return userIdList;

        /*case "horizontalSuperior": //上级层级(横向)
            break;*/
        case "verticalSuperior": //上级层级(纵向)
            for (final Long orgId : orgIdArr) {
                if (isManager(isAdmin)) {
                    remoteData = remoteUserService.getVerticalSuperiorAdminUserIdListByDeptId(orgId);
                } else {
                    remoteData = remoteUserService.getVerticalSuperiorUserIdListByDeptIdAndRoleIds(orgId,
                        roleIdArr);
                }
                checkRemoteData(remoteData, "getUserByOrgIdForUserTaskCustomRule");
                userIdList.addAll(remoteData.getData().stream().map(id -> id.toString()).collect(Collectors.toList()));
            }
            return userIdList;
        default:
            break;
        }
        return null;
    }

    private boolean isManager(String isAdmin) {
        return "manager".equals(isAdmin);
    }

    private List<String> getUserIdList(R<List<Long>> remoteData, String name) {
        checkRemoteData(remoteData, name);
        return remoteData.getData().stream().map(id -> id.toString()).collect(Collectors.toList());
    }

    /**
     *
     * @see com.cesgroup.api.user.UserConnector#findByRoleIdAndTenantId(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public List<UserDTO> findByRoleIdAndTenantId(String roleId, String tenantId) {
        return findByRoleId(roleId);
    }

    private String getMsg(String name, Object... objects) {
        final StringBuilder sb = new StringBuilder(name);

        for (final Object obj : objects) {
            sb.append(", ").append(obj.toString());
        }

        return sb.toString();
    }

}
