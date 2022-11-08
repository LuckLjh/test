package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel("定义的配置公用字段")
@Data
@EqualsAndHashCode
public class DefinedConfig implements Serializable {

	private static final long serialVersionUID = -7360053374872450294L;

	@ApiModelProperty("定义配置ID")
	private Long id;

	@ApiModelProperty("排序号")
	private Integer sortNo;

	@ApiModelProperty("绑定的数据字典code")
	private String dictCode;

	@ApiModelProperty("模块id")
	private Long moduleId;

}
