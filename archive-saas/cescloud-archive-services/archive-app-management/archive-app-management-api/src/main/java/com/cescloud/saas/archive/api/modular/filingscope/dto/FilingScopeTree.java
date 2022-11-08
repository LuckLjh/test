package com.cescloud.saas.archive.api.modular.filingscope.dto;

import com.cescloud.saas.archive.service.modular.common.core.tree.SyncTreeNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author LS
 */
@ApiModel("归档范围同步树")
@Data
public class FilingScopeTree extends SyncTreeNode<FilingScopeTree> {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("归档范围所属分类号")
	private String classNo;

	@ApiModelProperty("归档范围所属的分类名称")
	private String className;

	@ApiModelProperty("归档范围层级path")
	private String path;
}
