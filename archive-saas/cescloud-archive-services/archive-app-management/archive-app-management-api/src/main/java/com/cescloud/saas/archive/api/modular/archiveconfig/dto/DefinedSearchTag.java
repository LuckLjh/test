package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("标签检索字段")
@Data
@EqualsAndHashCode(callSuper = true)
public class DefinedSearchTag extends DefinedTag {

	private static final long serialVersionUID = 467481855632614945L;

	/**
	 * 条件方式
	 */
	@ApiModelProperty("条件方式")
	private String conditionType;
}
