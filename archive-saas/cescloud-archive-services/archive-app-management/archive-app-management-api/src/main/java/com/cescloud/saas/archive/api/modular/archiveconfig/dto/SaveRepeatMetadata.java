package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * 查重配置保存对象
 */
@ApiModel("查重配置保存对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveRepeatMetadata extends SaveMetadata<DefinedRepeatMetadata> {

	@NotBlank(message = "参数storageLocate不能为空！")
	@ApiModelProperty("存储表名")
	private String storageLocate;
}
