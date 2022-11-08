package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel("水印复制DTO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WatermarkCopyDTO {

	@ApiModelProperty("水印id")
	private Long watermarkId;

	@ApiModelProperty("模块")
	private List<Long> targetModuleIds;
}
