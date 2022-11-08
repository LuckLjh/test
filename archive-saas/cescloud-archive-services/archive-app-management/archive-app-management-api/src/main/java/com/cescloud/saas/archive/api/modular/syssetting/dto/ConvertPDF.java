package com.cescloud.saas.archive.api.modular.syssetting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("转PDF的类型")
public class ConvertPDF implements Serializable {

	private static final long serialVersionUID = 1891034427854019287L;

	@ApiModelProperty("文档")
	private List<String> doc;

	@ApiModelProperty("图像")
	private List<String> img;

	@ApiModelProperty("图形")
	private List<String> graph;

	public Boolean contains(String format) {
		return this.getDoc().contains(format.toLowerCase())
				|| this.getImg().contains(format.toLowerCase())
				|| this.getGraph().contains(format.toLowerCase());
	}
}
