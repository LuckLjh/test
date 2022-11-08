package com.cescloud.saas.archive.api.modular.archivetype.dto;

import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统字段
 * 使用insert into时候 插入系统字段
 */
@ApiModel("系统字段")
@Data
public class SystemColumn {

	/**
	 * 所属租户id
	 */
	@ApiModelProperty("所属租户id")
	private Long tenantId;


	/**
	 * 乐观锁,数据版本号
	 */
	@ApiModelProperty("乐观锁")
	private Long revision;

	/**
	 * 创建人
	 */
	@ApiModelProperty("创建人")
	private Long createdBy;

	/**
	 * 创建时间
	 */
	@ApiModelProperty("创建时间")
	private LocalDateTime createdTime;

	/**
	 * 更新人
	 */
	@ApiModelProperty("更新人")
	private Long updatedBy;

	/**
	 * 更新时间
	 */
	@ApiModelProperty("更新时间")
	private LocalDateTime updatedTime;

	public SystemColumn(){

	}

	public SystemColumn(Long tenantId, Long userId){
		this.tenantId = tenantId == null ? Long.valueOf(0) : tenantId;
		this.revision = 1L;
		this.createdBy = userId == null ? Long.valueOf(0) : userId;
		this.createdTime = LocalDateTime.now();
	}

	public SystemColumn(CesCloudUser user){
		this.tenantId = user.getTenantId() == null ? Long.valueOf(0) : user.getTenantId();
		this.revision = 1L;
		this.createdBy = user.getId() == null ? Long.valueOf(0) : user.getId();
		this.createdTime = LocalDateTime.now();
	}
}
