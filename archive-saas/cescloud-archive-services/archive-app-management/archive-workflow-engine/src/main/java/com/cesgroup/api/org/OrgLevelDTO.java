package com.cesgroup.api.org;

/**
 * Created by wxl on 2018/2/2.
 * 用于用户任务自定义规则中的租户组织级别
 *
 * @author wxl
 */
public class OrgLevelDTO {
    private String id; //组织级别ID
    private String name; //组织级别名称

    public OrgLevelDTO() {
    }

    public OrgLevelDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TenantOrgDTO{" 
                   + "id='" + id + '\'' 
                   +
                   ","
                   + "name='" + name + '\'' 
                   +
                '}';
    }
}
