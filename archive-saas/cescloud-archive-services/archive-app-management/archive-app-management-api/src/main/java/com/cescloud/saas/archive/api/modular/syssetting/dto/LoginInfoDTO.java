package com.cescloud.saas.archive.api.modular.syssetting.dto;

import com.cescloud.saas.archive.api.modular.tenant.entity.Tenant;
import lombok.Data;

import java.util.List;

@Data
public class LoginInfoDTO {

	//是否在线方式
	Boolean isOnline;

	//所有租户信息
	List<Tenant> tenants;

	//系统名称
	SystemNameSetDTO systemNameSet;

}
