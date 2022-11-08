package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName SearchListDTO
 * @Author zhangxuehu
 * @Date 2020/3/31 10:39
 **/
@ApiModel("标签检索定义查询对象")
@Data
@ToString
public class SearchListDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank(message = "档案类型不能为空")
	@ApiModelProperty("档案类型code")
	private String typeCode;

	@NotNull(message = "表模板ID不能为空")
	@ApiModelProperty("表模板ID")
	private Long templateTableId;

	@NotNull(message = "检索类型不能为空")
	@ApiModelProperty("检索类型：1：快速检索 2：基本检索")
	private Integer searchType;

	@ApiModelProperty("标识,公共配置 false 私有配置 true")
	private Boolean tagging;

	@NotNull(message = "模块id不能为空")
	@ApiModelProperty("模块id")
	private Long moduleId;

	/*
	* 专题参数，可为空
	* */


	@ApiModelProperty("专题ID")
	private Long specialId;

}
