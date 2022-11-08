package com.cescloud.saas.archive.api.modular.syssetting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("转OFD的类型")
public class ConvertOFD implements Serializable {

	private static final long serialVersionUID = 9054813719737340454L;

	@ApiModelProperty("文档")
	private List<String> doc;

	@ApiModelProperty("图像")
	private List<String> img;

	public Boolean contains(String format) {
		return this.getDoc().contains(format.toLowerCase())
				|| this.getImg().contains(format.toLowerCase());
	}
}
