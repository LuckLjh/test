package com.cescloud.saas.archive.api.modular.syssetting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("转MP4的类型")
public class ConvertMP4 implements Serializable {

	private static final long serialVersionUID = -8305218696944382505L;

	@ApiModelProperty("视频")
	private List<String> video;

	public Boolean contains(String format) {
		return this.getVideo().contains(format.toLowerCase());
	}
}
