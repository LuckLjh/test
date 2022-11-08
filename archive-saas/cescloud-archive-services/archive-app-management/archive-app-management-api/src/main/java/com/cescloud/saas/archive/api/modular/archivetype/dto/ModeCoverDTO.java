package com.cescloud.saas.archive.api.modular.archivetype.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("模板覆盖")
@Data
public class ModeCoverDTO {

	@NotNull(message = "档案表名:storageLocate不能为空！")
	@ApiModelProperty("档案表名")
	private String storageLocate;

	@NotNull(message = "模板ID:modeId不能为空！")
	@ApiModelProperty("模板ID")
	private Long modeId;
}
