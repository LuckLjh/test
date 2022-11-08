package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查重字段元数据
 */
@ApiModel("查重字段元数据")
@Data
@EqualsAndHashCode(callSuper = true)
public class DefinedRepeatMetadata extends DefinedMetadata {

	private static final long serialVersionUID = -8756554651704510888L;
	/**
	 * 查重字段是否分组：1：分组 0：不分组
	 */
	@ApiModelProperty("查重字段是否分组：1：分组 0：不分组")
	private Boolean isGroup;
}
