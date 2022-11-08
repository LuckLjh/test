package com.cescloud.saas.archive.api.modular.archivetype.dto;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedMetadata;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("累加规则的分组规则")
@Data
@EqualsAndHashCode(callSuper = true)
public class SourceDTO extends DefinedMetadata {

	private static final long serialVersionUID = -7775977235522257195L;

	@ApiModelProperty("是否是参照字段，true：是，false：不是")
	private Boolean isSource;
}
