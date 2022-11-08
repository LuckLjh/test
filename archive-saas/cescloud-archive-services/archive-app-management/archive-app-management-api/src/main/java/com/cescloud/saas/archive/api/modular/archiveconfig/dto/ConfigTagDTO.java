package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 标签配置
 */
@ApiModel("标签配置")
@Data
@Builder
@EqualsAndHashCode
public class ConfigTagDTO {

	private List<DefinedListTag> tagList;

	private List<DefinedSearchTag> tagSearch;

}
