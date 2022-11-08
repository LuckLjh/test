package com.cescloud.saas.archive.api.modular.archivetype.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author liwei
 */
@ApiModel("分组规则")
@Data
@EqualsAndHashCode
public class SourcePostDTO implements Serializable {

	private static final long serialVersionUID = 2704679989598135701L;

	@ApiModelProperty(value = "数据规则id",required = true)
	private Long id;

	@ApiModelProperty(value = "档案层级存储表",required = true)
	private String storageLocate;

	@ApiModelProperty(value = "勾选的分组字段id",required = true)
	private List<Long> groupFieldIds;
}
