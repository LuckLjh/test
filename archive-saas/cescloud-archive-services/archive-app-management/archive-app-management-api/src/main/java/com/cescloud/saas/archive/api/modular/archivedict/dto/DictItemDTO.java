package com.cescloud.saas.archive.api.modular.archivedict.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DictItemDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 字典项编码
	 */
	@ApiModelProperty(value = "字典项编码",example = "字典项编码")
	private String dictCode;
	/**
	 * 字典项标签
	 */
	@ApiModelProperty(value = "字典项标签",example = "字典项标签")
	private String dictLabel;
	/**
	 * 字典项描述
	 */
	@ApiModelProperty(value = "字典项描述",example = "字典项描述")
	private String dictDescribe;
	/**
	 * 字典值编码
	 */
	@ApiModelProperty(value = "字典值编码",example = "字典值编码")
	private String itemCode;
	/**
	 * 字典值名称
	 */
	@ApiModelProperty(value = "字典值名称",example = "字典值名称")
	private String itemLabel;
	/**
	 * 字典值的具体值
	 */
	@ApiModelProperty(value = "字典值的具体值",example = "字典值的具体值")
	private String itemValue;
	/**
	 * 字典项描述
	 */
	@ApiModelProperty(value = "字典项描述",example = "字典项描述")
	private String itemDescribe;
	/**
	 * 检索关键字 false
	 */
	@ApiModelProperty(value = "检索关键字",example = "检索关键字")
	private String keyword;

}
