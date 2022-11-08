package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * 标签列表定义保存对象
 */
@ApiModel("标签列表定义保存对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveListTag extends SaveMetadata<DefinedListTag> {

	@NotBlank(message = "参数档案层级不能为空！")
	@ApiModelProperty("档案层级")
	private String archiveLayer;
}
