package com.cescloud.saas.archive.api.modular.syssetting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("转MP3的类型")
public class ConvertMP3 implements Serializable {

	private static final long serialVersionUID = -6469686472288075235L;

	@ApiModelProperty("音频")
	private List<String> audio;

	public Boolean contains(String format) {
		return this.getAudio().contains(format.toLowerCase());
	}
}
