package com.cescloud.saas.archive.api.modular.filingscope.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author LS
 * @date 2022/4/20
 */
@Data
public class FilingScopeExcelDTO {
	/**
	 * 归档范围所属父节点的id false
	 */
	@ApiModelProperty(value = "归档范围所属父节点的名称", required = false)
	private String parentClassName;
	/**
	 * 归档范围所属的分类名称 false
	 */
	@ApiModelProperty("归档范围所属的分类名称")
	private String className;
	/**
	 * 归档范围所属分类号 false
	 */
	@ApiModelProperty("归档范围所属分类号")
	private String classNo;
	/**
	 * 档案类型code 例如wsda false
	 */
	@ApiModelProperty(value = "档案类型code", required = true, example = "wsda")
	private String typeCode;

	/**
	 * 行
	 */
	private Integer rowIndex;

}
