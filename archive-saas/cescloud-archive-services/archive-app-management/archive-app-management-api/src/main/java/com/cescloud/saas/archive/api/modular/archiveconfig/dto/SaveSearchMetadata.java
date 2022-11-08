package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签列表定义保存对象
 */
@ApiModel("标签检索定义保存对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveSearchMetadata extends SaveMetadata<DefinedSearchMetadata> {

	@ApiModelProperty("档案类型")
	private String typeCode;

	@ApiModelProperty("模板表id")
	private Long templateTableId;

	@ApiModelProperty("菜单id")
	private Long moduleId;

	@ApiModelProperty("标识,公共配置 false 私有配置 true")
	private Boolean tagging;

	@ApiModelProperty("检索类型")
	private Integer searchType;


	/*
	 * 专题参数，可为空
	 * */


	@ApiModelProperty("专题ID")
	private Long specialId;
}
