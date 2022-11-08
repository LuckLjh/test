package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 录入、列表、排序 已经定义的字段
 */
@ApiModel("定义的字段元数据")
@Data
@EqualsAndHashCode
public class DefinedMetadata extends DefinedConfig {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("元数据ID")
	private Long metadataId;

	@ApiModelProperty("元数据中文")
	private String metadataChinese;

	@ApiModelProperty("元数据英文")
	private String metadataEnglish;

	@ApiModelProperty("元数据字段长度")
	private Integer metadataLength;

	@ApiModelProperty("元数据字段类型")
	private String metadataType;

}
