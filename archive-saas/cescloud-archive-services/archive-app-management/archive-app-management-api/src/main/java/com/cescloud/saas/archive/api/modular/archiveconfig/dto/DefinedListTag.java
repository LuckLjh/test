package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

@ApiModel("定义的字段元数据")
@Data
@EqualsAndHashCode
public class DefinedListTag extends DefinedTag {

	private static final long serialVersionUID = 5073132037060179805L;

	/**
	 * 对齐方式
	 */
	@ApiModelProperty("对齐方式")
	private String align;

	@Range(min = 0, max = 99999, message = "只能输入最大5位正整数")
	@ApiModelProperty("宽度")
	private Integer width;

}
