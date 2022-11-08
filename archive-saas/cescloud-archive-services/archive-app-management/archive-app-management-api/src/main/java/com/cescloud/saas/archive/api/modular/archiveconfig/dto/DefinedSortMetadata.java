package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 录入字段元数据
 */
@ApiModel("排序字段元数据")
@Data
@EqualsAndHashCode(callSuper = true)
public class DefinedSortMetadata extends DefinedMetadata {

	/**
	 * 排序标识:ASC:代表升序 DESC：代表降序
	 */
	@ApiModelProperty("排序标识")
	private String sortSign;
}
