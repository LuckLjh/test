package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

/**
 * 录入字段元数据
 */
@ApiModel("列表字段元数据")
@Data
@EqualsAndHashCode(callSuper = true)
public class DefinedListMetadata extends DefinedMetadata {

	private static final long serialVersionUID = 4507792416088736066L;
	/**
	 * 对齐方式
	 */
	@ApiModelProperty("对齐方式")
	private String align;
	/**
	 * 宽度
	 */
	@Range(min = 0, max = 99999, message = "只能输入最大5位正整数")
	@ApiModelProperty("宽度")
	private Integer width;

	@ApiModelProperty("用户id")
	private Long userId;
}
