package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签列表、查询 已经定义的字段
 */
@ApiModel("定义的标签")
@Data
@EqualsAndHashCode
public class DefinedTag extends DefinedConfig {

	private static final long serialVersionUID = 8021323584424879303L;

	@ApiModelProperty("标签ID")
	private Long tagId;

	@ApiModelProperty("标签中文")
	private String tagChinese;

	@ApiModelProperty("标签英文")
	private String tagEnglish;

	@ApiModelProperty("标签长度")
	private Integer tagLength;

	@ApiModelProperty("标签类型")
	private String tagType;
}
