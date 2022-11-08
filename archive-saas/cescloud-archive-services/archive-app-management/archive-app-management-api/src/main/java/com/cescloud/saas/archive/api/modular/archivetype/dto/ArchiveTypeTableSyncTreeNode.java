package com.cescloud.saas.archive.api.modular.archivetype.dto;

import com.cescloud.saas.archive.api.modular.common.constants.SysConstant;
import com.cescloud.saas.archive.service.modular.common.core.tree.SyncTreePkNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ApiModel("档案门类同步树节点")
public class ArchiveTypeTableSyncTreeNode extends SyncTreePkNode<ArchiveTypeTableSyncTreeNode> {

	@ApiModelProperty("数据ID")
	private Long id;

	@ApiModelProperty("节点名称")
	private String name;

	@ApiModelProperty("节点编码")
	private String dataCode;

	@ApiModelProperty("父节点编码")
	private String parentDataCode;

	@ApiModelProperty("节点类型")
	private String nodeType;

	@ApiModelProperty("全宗名称")
	private String fondsName;

	@ApiModelProperty("是否继承全局权限，默认继承")
	private Integer isExtendsGlobal = 1;

	@ApiModelProperty("是否已经设置权限，默认没设置")
	private String settingDesc = SysConstant.AUTH_SETTING.NO;
}
