package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 操作内容实体
 */
@ApiModel("操作内容实体")
@Data
@EqualsAndHashCode
public class OperationDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 操作方式：R、代表替换  D、代表删除
	 */
	@ApiModelProperty("操作方式：R、代表替换  D、代表删除")
	private String operateType;

	/**
	 * 待替换值或待删除值
	 */
	@ApiModelProperty("待替换值或待删除值")
	private String valueOne;
	/**
	 * 替换值
	 */
	@ApiModelProperty("替换值")
	private String valueTwo;
	/**
	 * 排序号
	 */
	@ApiModelProperty("排序号")
	private Integer sortNo;
	/**
	 * 扩展属性，可自定义，标明属于哪个业务、哪个模块以及哪个功能的元数据值替换或删除操作
	 */
	@ApiModelProperty("扩展属性")
	private String operateFunction;

}
