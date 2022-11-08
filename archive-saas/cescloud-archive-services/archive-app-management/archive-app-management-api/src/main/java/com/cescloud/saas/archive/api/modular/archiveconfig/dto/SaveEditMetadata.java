package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 录入字段定义保存对象
 */
@ApiModel("录入字段定义保存对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveEditMetadata extends SaveMetadata<DefinedEditMetadata> {

	@NotBlank(message = "参数storageLocate不能为空！")
	@ApiModelProperty("存储表名")
	private String storageLocate;

	@NotNull(message = "模块id不能为空")
	@ApiModelProperty("模块id")
	private Long moduleId;
}
