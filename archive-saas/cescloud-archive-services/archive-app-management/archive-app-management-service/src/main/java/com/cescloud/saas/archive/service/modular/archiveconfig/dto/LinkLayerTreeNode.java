package com.cescloud.saas.archive.service.modular.archiveconfig.dto;

import com.cescloud.saas.archive.service.modular.common.core.tree.SyncTreeNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("挂接层次同步树")
@Data
@EqualsAndHashCode(callSuper = true)
public class LinkLayerTreeNode extends SyncTreeNode {

	@ApiModelProperty("名称")
	private String name;
}
