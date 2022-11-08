package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 排序定义保存对象
 * @param <T>
 */
@ApiModel("排序定义保存对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveSortMetadata extends SaveMetadata<DefinedSortMetadata> {

	@ApiModelProperty("档案类型")
	private String typeCode;

	@ApiModelProperty("模板表id")
	private Long templateTableId;

	@NotNull(message = "模块id不能为空")
	@ApiModelProperty("模块id")
	private Long moduleId;

	@ApiModelProperty("标识,公共配置 false 私有配置 true")
	private Boolean tagging;

	/**
	 * 此属性非专题不需要填写
	 */
	@ApiModelProperty("专题ID")
	private Long specialId;

}
