package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liwei
 */
@ApiModel("初始化表单实体")
@Data
public class ArchiveInitFormDataDTO implements Serializable {

	private static final long serialVersionUID = -7008495884003508650L;

	@ApiModelProperty(value = "模块id", required = true)
	@NotNull(message = "模块id不能为空")
	private Long moduleId;

	@NotBlank(message = "档案类型编码不能为空")
	@ApiModelProperty(value = "档案类型编码", required = true)
	private String typeCode;

	@NotNull(message = "模板ID不能为空")
	@ApiModelProperty(value = "模板ID", required = true)
	private Long templateTableId;

	@ApiModelProperty(value = "案卷模板id", required = true)
	private Long folderTemplateId;

	@ApiModelProperty(value = "全宗号", required = true)
	private String fondsCode;

	@ApiModelProperty(value = "档案树过滤条件", required = true)
	private String filter;

	@ApiModelProperty(value = "档案树分类号路径", required = true)
	private String path;

	@ApiModelProperty(value = "父目录id", required = false)
	private Long ownerId;

	@ApiModelProperty(value = "组卷勾选的文件的ids", required = true)
	private String ids;

	@ApiModelProperty("归档范围名称")
	private String className;

	@NotBlank(message = "新增或组卷标识不能为空")
	@ApiModelProperty(value = "组卷和新增标识，add标识新增表单数据初始化，compose表示组卷表单的初始化", required = true)
	private String type;

	@ApiModelProperty("组卷标识，compose=0未组卷")
	private Integer compose;
}
