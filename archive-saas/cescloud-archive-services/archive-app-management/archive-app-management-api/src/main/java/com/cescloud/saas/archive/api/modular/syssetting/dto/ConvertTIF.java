package com.cescloud.saas.archive.api.modular.syssetting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("转TIF的类型")
public class ConvertTIF implements Serializable {

	private static final long serialVersionUID = 2611292706513120388L;

	@ApiModelProperty("PDF")
	private List<String> pdf;

	@ApiModelProperty("图像")
	private List<String> img;

	public Boolean contains(String format) {
		return this.getPdf().contains(format.toLowerCase())
				|| this.getImg().contains(format.toLowerCase());
	}
}
