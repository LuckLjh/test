package com.cesgroup.api.userrepo;

/**
 * 用户仓库DTO
 * 
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class UserRepoDTO {

    private String id;

    private String code;

    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
