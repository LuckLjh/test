package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 挂接字段组成规则保存对象
 */
@ApiModel("挂接字段组成规则保存对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class SaveLinkColumnRuleMetadata extends SaveMetadata<DefinedColumnRuleMetadata> {

	@NotBlank(message = "参数storageLocate不能为空！")
	@ApiModelProperty("存储表名")
	private String storageLocate;

	/**
	 * 挂接层次ID
	 */
	@ApiModelProperty("挂接层次ID")
	private Long id;
	/**
	 * 父ID
	 */
	@ApiModelProperty("父ID")
	private Long parentId;

	@Override
	@Size(max = 8, message = "挂接字段不能超过8个")
	public List<DefinedColumnRuleMetadata> getData() {
		return super.getData();
	}

	@NotNull(message = "模块id不能为空")
	@ApiModelProperty("菜单id")
	private Long moduleId;
}
