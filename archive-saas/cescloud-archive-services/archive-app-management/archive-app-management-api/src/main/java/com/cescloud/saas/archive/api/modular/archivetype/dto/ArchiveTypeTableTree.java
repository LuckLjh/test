package com.cescloud.saas.archive.api.modular.archivetype.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@ApiModel("档案门类表树")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ArchiveTypeTableTree {

	@ApiModelProperty("数据ID")
	private Long id;

	@ApiModelProperty("显示名字")
	private String name;

	@ApiModelProperty("节点类型，分类节点:C, 档案门类节点:A, 物理表节点:T")
	private String nodeType;

	@ApiModelProperty("数据编码，门类存type_code，物理表存storage_locate")
	private String dataCode;

	@ApiModelProperty("复选框是否可选")
	private Boolean disabled;

	@ApiModelProperty("授权方式")
	private String authTypeDesc;

	@ApiModelProperty("设置描述")
	private String settingDesc;

	@ApiModelProperty("子节点集合")
	private List<ArchiveTypeTableTree> children = new ArrayList<>();
}
