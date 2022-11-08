package com.cescloud.saas.archive.api.modular.archivetree.dto;

import com.cescloud.saas.archive.service.modular.common.core.tree.SyncTreePkNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("全宗档案树同步树")
@Data
public class FondsArchiveTreeSyncTreeNode extends SyncTreePkNode<FondsArchiveTreeSyncTreeNode> {

	@ApiModelProperty("F-全宗节点，T-档案树节点")
	private String nodeClass;

	@ApiModelProperty("全宗号")
	private String fondsCode;

	@ApiModelProperty("档案树ID")
	private Long archiveTreeId;

	@ApiModelProperty("节点名称")
	private String name;

	@ApiModelProperty("是否叶子节点")
	private Boolean isLeaf;

	@ApiModelProperty(name = "是否为层级节点")
	private Boolean showLayer;

	@ApiModelProperty("档案树节点类型：T：树根节点； C：分类节点；F：全宗节点；  A：档案类型节点；  L：层级表节点； D：自定义节点规则；S：归档范围节点；")
	private String nodeType;

}
