package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 字段拼接规则配置
 */
@ApiModel("字段拼接规则配置")
@Data
public class DefinedColumnRuleDTO implements Serializable {

	private static final long serialVersionUID = 8020707100108827964L;

	/**
	 * 上一层级标识 : 0 代表本层级，1 代表上一层级
	 */
	@ApiModelProperty("上一层级标识")
	private Integer upperLevel;

	/**
	 * 本层级拼接字段
	 */
	@ApiModelProperty("本层级")
	private List<DefinedColumnRuleMetadata> currentLevelFields;

	/**
	 * 上一层级拼接字段
	 */
	@ApiModelProperty("上一层级")
	private List<DefinedColumnRuleMetadata> upperLevelFields;
}
