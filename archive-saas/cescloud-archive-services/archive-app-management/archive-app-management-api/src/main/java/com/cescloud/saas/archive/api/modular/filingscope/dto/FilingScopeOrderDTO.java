package com.cescloud.saas.archive.api.modular.filingscope.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author LS
 * @Description: 归档范围排序DTO
 * @date 2021/06/28
 */
@Data
@ApiModel(description = "归档范围排序DTO")
public class FilingScopeOrderDTO implements Serializable {

	private static final long serialVersionUID = 3606776654443645604L;

	@ApiModelProperty("排序后ids")
	private List<Long> ids;

	@ApiModelProperty("归档范围父ID")
	private Long parentId;
}
