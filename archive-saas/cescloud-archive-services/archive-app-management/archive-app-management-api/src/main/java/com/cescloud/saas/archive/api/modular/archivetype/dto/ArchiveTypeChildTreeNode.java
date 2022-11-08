package com.cescloud.saas.archive.api.modular.archivetype.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 档案门类树
 */
@ApiModel("档案门类级联树")
@Data
public class ArchiveTypeChildTreeNode  implements Serializable {
	/** 节点值 **/
	@ApiModelProperty("节点值")
	protected String value;
	/** 节点类型 **/
	@ApiModelProperty("节点显示")
	protected String label;
	/** 节点列表 **/
	@ApiModelProperty("节点列表")
	protected List<ArchiveTypeChildTreeNode> children;

	public void add(ArchiveTypeChildTreeNode t) {
		children.add(t);
	}
}
