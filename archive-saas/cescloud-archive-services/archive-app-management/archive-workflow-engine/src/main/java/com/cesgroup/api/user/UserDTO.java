package com.cesgroup.api.user;

import com.cesgroup.core.util.WorkflowConstants;

/**
 * 用户DTO
 *
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class UserDTO {

    /** 用户在数据库里的唯一标识. */
    private String id;

    /** 用户登录使用的账号. */
    private String username;

    /** 外部主键. */
    private String ref;

    /** 账号体系. */
    private String userRepoRef;

    /** 用户的状态. */
    private int status;

    /** 显示名. */
    private String displayName;

    /** 昵称. */
    private String nickName;

    /** 邮箱. */
    private String email;

    /** 手机. */
    private String mobile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getUserRepoRef() {
        return userRepoRef;
    }

    public void setUserRepoRef(String userRepoRef) {
        this.userRepoRef = userRepoRef;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "UserDTO [id=" + id + ", username=" + username + ", ref=" + ref + ", userRepoRef="
            + userRepoRef + ", status=" + status + ", displayName=" + displayName + ", nickName="
            + nickName + ", email=" + email + ", mobile=" + mobile + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserDTO other = (UserDTO) obj;
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * 默认表达式用户
     *
     * @param expression
     *            表达式
     * @return UserDTO
     */
    public static UserDTO expressionUserDto(String expression) {
        final UserDTO userDTO = new UserDTO();
        userDTO.setDisplayName(expression);
        return userDTO;
    }

    /**
     * 自动提交用户
     * 
     * @return UserDTO
     */
    public static UserDTO autoCommitUserDto() {
        final UserDTO userDTO = new UserDTO();
        userDTO.setId("-1");
        userDTO.setUsername(WorkflowConstants.HumanTaskConstants.USER_AUTO_COMMIT);
        userDTO.setDisplayName("自动提交");
        return userDTO;
    }

}
