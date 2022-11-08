package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import java.util.List;

/**
 * 定义保存对象
 * @param <T>
 */
@ApiModel("定义保存对象")
@Data
@EqualsAndHashCode
public class SaveMetadata<T extends DefinedConfig> {

	@Valid
	@ApiModelProperty("保存对象集合")
	private List<T> data;
}
