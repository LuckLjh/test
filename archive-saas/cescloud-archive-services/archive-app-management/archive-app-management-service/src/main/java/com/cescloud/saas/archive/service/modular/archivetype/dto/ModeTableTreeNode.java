package com.cescloud.saas.archive.service.modular.archivetype.dto;

import com.cescloud.saas.archive.service.modular.common.core.tree.SyncTreeNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 元数据模板树
 */
@ApiModel("元数据模板树")
@Data
@EqualsAndHashCode(callSuper = true)
public class ModeTableTreeNode extends SyncTreeNode {

	/**
	 * 模板名称
	 */
	@ApiModelProperty("模板名称")
	private String modeName;

	/**
	 * 档案层次
	 */
	@ApiModelProperty("档案层次")
	private String archiveLayer;
}
