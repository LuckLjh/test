package com.cescloud.saas.archive.api.modular.archivetype.dto;

import com.cescloud.saas.archive.service.modular.common.core.tree.AsyncTreeNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 档案门类树
 */
@ApiModel("档案门类树")
@Data
@EqualsAndHashCode(callSuper = true)
public class ArchiveTypeTreeNode extends AsyncTreeNode {

	private static final long serialVersionUID = -4349740812902604069L;
	/**
	 * 档案门类名称
	 */
	@ApiModelProperty("档案门类名称")
	private String typeName;

	/**
	 * 存储表名
	 */
	@ApiModelProperty("存储表名")
	private String storageLocate;

	/**
	 * 档案分类
	 */
	@ApiModelProperty("档案分类")
	private String classType;

	/**
	 * 模板类型表ID
	 */
	@ApiModelProperty("模板类型表ID")
	private Long templateTypeId;

	/**
	 * 模板表ID
	 */
	@ApiModelProperty("模板表ID")
	private Long templateTableId;

	/**
	 * 档案层次
	 */
	@ApiModelProperty("档案层次")
	private String archiveLayer;

	/**
	 * 档案类型编码
	 */
	@ApiModelProperty("档案类型编码")
	private String typeCode;

	/**
	 * 模板门类模板名称
	 */
	@ApiModelProperty("模板门类模板名称")
	private String templateTypeName;

	/**
	 * 全宗号编码
	 */
	@ApiModelProperty("全宗号编码")
	private String fondsCode;
	/**
	 * 全宗名称
	 */
	@ApiModelProperty("全宗名称")
	private String fondsName;
	/**
	 * 设置人
	 */
	@ApiModelProperty("设置人")
	private String createdUserName;

	@ApiModelProperty("创建时间")
	private LocalDateTime createdTime;

	@ApiModelProperty("树状唯一索引pk")
	private String pk;
}
