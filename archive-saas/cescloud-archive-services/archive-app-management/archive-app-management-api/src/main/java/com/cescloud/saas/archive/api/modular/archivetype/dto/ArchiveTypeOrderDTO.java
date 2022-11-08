package com.cescloud.saas.archive.api.modular.archivetype.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author LS
 * @Description: 档案门类排序DTO
 * @date 2020/11/16
 */
@Data
@ApiModel(description = "档案门类排序DTO")
public class ArchiveTypeOrderDTO implements Serializable {

	@ApiModelProperty("排序后ids")
	private List<Long> ids;

	@ApiModelProperty("门类父ID")
	private Long parentId;
}
