package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * @author liwei
 */
@ApiModel("计算流水号实体")
@Data
public class ComputeFlowNoDTO implements Serializable {
	private static final long serialVersionUID = 8020707100008847964L;

	@NotNull(message = "模块id不能为空")
	@ApiModelProperty(value = "模块id", required = true)
	private Long moduleId;

	@NotBlank(message = "档案类型编码不能为空")
	@ApiModelProperty(value = "档案类型编码", required = true)
	private String typeCode;

	@NotNull(message = "模板ID不能为空")
	@ApiModelProperty(value = "模板ID", required = true)
	private Long templateTableId;

	@ApiModelProperty(value = "父档案id", required = false)
	private Long ownerId;

	@ApiModelProperty(value = "分组字段的键值对", required = true)
	private Map<String,Object> data;
}
