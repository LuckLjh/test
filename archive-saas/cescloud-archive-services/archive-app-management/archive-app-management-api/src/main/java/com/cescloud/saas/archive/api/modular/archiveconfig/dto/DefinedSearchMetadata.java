package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 录入字段元数据
 */
@ApiModel("检索字段元数据")
@Data
@EqualsAndHashCode(callSuper = true)
public class DefinedSearchMetadata extends DefinedMetadata {

	private static final long serialVersionUID = 3421672246371959606L;
	/**
	 * 条件方式
	 */
	@ApiModelProperty("条件方式")
	private String conditionType;

	/**
	 * 检索定义 0编码  1名称
	 */
	@ApiModelProperty("检索定义 0编码  1名称")
	private Integer dictKeyValue;
}
