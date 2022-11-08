package com.cescloud.saas.archive.api.modular.syssetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author LS
 * @date 2021/12/27
 */
@Data
public class SystemWatermarkDTO {

	@ApiModelProperty(value = "是否显示水印", required = true)
	private Boolean enabled;

	@ApiModelProperty(value = "水印内容设置", required = true)
	private Integer watermarkContentSettings;

	@ApiModelProperty(value = "水印大小", required = true)
	private Integer watermarkSize;

	@ApiModelProperty(value = "水印透明度", required = true)
	private String watermarkTransparency;

}
