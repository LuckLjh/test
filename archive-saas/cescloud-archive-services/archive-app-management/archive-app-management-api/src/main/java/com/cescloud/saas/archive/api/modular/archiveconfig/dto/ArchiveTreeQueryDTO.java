package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author LS
 * @date 2021/9/24
 */
@Data
public class ArchiveTreeQueryDTO implements Serializable {

	private static final long serialVersionUID = 2579831658926975391L;

	/**
	 *分类号树
	 */
	public static final Integer CLASS_NO_TREE = 1;

	@ApiModelProperty("档案门类编码")
	private String typeCode;

	@ApiModelProperty("全宗号")
	private String fondsCode;

	@NotNull(message = "父id不能为空")
	@ApiModelProperty("父id")
	private Long parentId;

	@ApiModelProperty("模版id")
	private Long templateTableId;

	@ApiModelProperty("案卷模版id")
	private Long folderTemplateId;

	@ApiModelProperty("层级")
	private String archiveLayer;

	@ApiModelProperty("树节点的值")
	private String treeValue;

	@ApiModelProperty("归档类型")
	private String filingType;

	@ApiModelProperty("树节点的path ")
	private String path;
}
