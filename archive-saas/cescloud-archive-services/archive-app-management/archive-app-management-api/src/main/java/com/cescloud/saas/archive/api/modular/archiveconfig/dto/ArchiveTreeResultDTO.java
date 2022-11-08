package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 表单字段绑定下拉树的数据
 *
 * @author LS
 * @date 2021/9/24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveTreeResultDTO implements Serializable {

	private static final long serialVersionUID = -611955465710836006L;

	@ApiModelProperty("树ID")
	private Long id;

	@ApiModelProperty("树结构中实际存储的编码或id")
	private Object value;

	@ApiModelProperty("选择后显示的名称")
	private Object title;

	@ApiModelProperty("树节点名称")
	private String name;

	@ApiModelProperty("父ID")
	private Long parentId;

	@ApiModelProperty("是否是叶子节点")
	private Boolean isLeaf;

	@ApiModelProperty("是否禁用")
	private Boolean disabled;

	@ApiModelProperty("归档范围层级path")
	private String path;
}
