package com.cescloud.saas.archive.api.modular.syssetting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("转FLV的类型")
public class ConvertFLV implements Serializable {

	private static final long serialVersionUID = -6897991714987241691L;

	@ApiModelProperty("视频")
	private List<String> video;

	public Boolean contains(String format) {
		return this.getVideo().contains(format.toLowerCase());
	}
}
