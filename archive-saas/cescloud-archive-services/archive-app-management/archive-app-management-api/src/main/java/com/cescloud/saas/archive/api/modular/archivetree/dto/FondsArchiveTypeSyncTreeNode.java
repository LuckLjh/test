package com.cescloud.saas.archive.api.modular.archivetree.dto;

import com.cescloud.saas.archive.service.modular.common.core.tree.SyncTreePkNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("全宗档案门类同步树")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FondsArchiveTypeSyncTreeNode extends SyncTreePkNode<FondsArchiveTypeSyncTreeNode> {

	@ApiModelProperty("节点名称")
	private String name;

	@ApiModelProperty("F-全宗节点， C-档案门类分类节点，A-档案门类节点, T-档案表节点")
	private String nodeClass;

	@ApiModelProperty("全宗号")
	private String fondsCode;

	@ApiModelProperty("档案树ID")
	private Long archiveTypeId;

	@ApiModelProperty("档案门类编码")
	private String typeCode;

	@ApiModelProperty("模板ID")
	private Long templateTypeId;

	@ApiModelProperty("模板表ID")
	private Long templateTableId;

	@ApiModelProperty("首层表名")
	private String storageLocate;

	@ApiModelProperty("档案层级")
	private String archiveLayer;

	@ApiModelProperty("是否叶子节点")
	private Boolean isLeaf;
}
