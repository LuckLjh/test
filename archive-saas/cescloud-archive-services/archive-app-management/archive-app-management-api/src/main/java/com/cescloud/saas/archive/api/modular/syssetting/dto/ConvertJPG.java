package com.cescloud.saas.archive.api.modular.syssetting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("转JPG的类型")
public class ConvertJPG implements Serializable {

	private static final long serialVersionUID = -1071293422545920335L;

	@ApiModelProperty("图像")
	private List<String> img;

	public Boolean contains(String format) {
		return this.getImg().contains(format.toLowerCase());
	}
}
