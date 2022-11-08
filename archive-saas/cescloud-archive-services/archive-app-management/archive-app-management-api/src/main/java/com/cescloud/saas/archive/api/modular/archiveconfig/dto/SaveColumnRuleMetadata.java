package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 字段组成规则保存对象
 */
@ApiModel("字段组成规则保存对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveColumnRuleMetadata extends SaveMetadata<DefinedColumnRuleMetadata> {

	@NotBlank(message = "参数storageLocate不能为空！")
	@ApiModelProperty("存储表名")
	private String storageLocate;

	/**
	 * 字段拼接规则id
	 */
	@NotNull(message = "参数metadataSourceId不能为空！")
	@ApiModelProperty("字段拼接规则id")
	private Long metadataSourceId;
}
