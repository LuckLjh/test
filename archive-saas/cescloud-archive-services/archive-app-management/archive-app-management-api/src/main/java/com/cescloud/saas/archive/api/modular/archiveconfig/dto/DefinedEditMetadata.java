package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 录入字段元数据
 */
@ApiModel("录入字段元数据")
@Data
@EqualsAndHashCode(callSuper = true)
public class DefinedEditMetadata extends DefinedMetadata {

	private static final long serialVersionUID = 4301894327234192528L;
}
