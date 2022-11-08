package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字段规则配置
 */
@ApiModel("字段规则配置")
@Data
@EqualsAndHashCode(callSuper = true)
public class DefinedColumnRuleMetadata extends DefinedMetadata{

	/**
	 * 补零标识：1、补零   0、不补零
	 */
	@ApiModelProperty("补零标识：1、补零   0、不补零")
	private Boolean zeroFlag;

	/**
	 * 连接字符
	 */
	@ApiModelProperty("连接字符")
	private String connectStr;

	/**
	 * 连接标识: M:代表元数据字段 C:代表连接符
	 */
	@ApiModelProperty("连接标识: M:代表元数据字段 C:代表连接符")
	private String connectSign;

	/**
	 * 数据字典：0、存KEY  1、存值
	 */
	@ApiModelProperty("数据字典：0、存KEY  1、存值")
	private Integer dictKeyValue;

	/**
	 * 元数据字段所属存储表
	 */
	@ApiModelProperty("元数据字段所属存储表")
	private String storageLocate;

	/**
	 * 元数据字段补零位数
	 */
	@ApiModelProperty("元数据字段补零位数")
	private Integer digitZero;

	/**
	 * 上一层级标识
	 */
	@ApiModelProperty("上一层级标识")
	private Integer upperLevel;
}
