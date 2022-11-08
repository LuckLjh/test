package com.cescloud.saas.archive.api.modular.filingscope.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
@author  xaz
@date 2019/4/22 - 15:56
**/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "归档范围定义参数")
public class FilingScopeDTO {

    /**
     * 归档范围所属分类id,主键 false
     */
    @ApiModelProperty(value = "归档范围定义ID(新增不传)",required = false,example = "1")
    private Long id;
    /**
     * 归档范围所属分类号 false
     */
    @ApiModelProperty(value = "归档范围所属分类号，同档案类型不能重复(新增树节点不填)",required = false,example = "01")
    private String classNo;
    /**
     * 归档范围所属的分类名称 false
     */
    @ApiModelProperty(value = "归档范围所属的分类名称",required = true,example = "归档范围树")
    private String className;

    /**
     * 归档范围所属父节点的id false
     */
    @ApiModelProperty(value = "归档范围所属父节点的id",required = true,example = "01")
    private Long parentClassId;

	/**
	 * 归档范围所属父节点的id false
	 */
	@ApiModelProperty(value = "归档范围所属父节点的名称",required = false)
	private String parentClassName;

    /**
     * 归档范围,可以是多个关键词，也可以是一段描述 false
     */
    @ApiModelProperty(value = "归档范围,可以是多个关键词，也可以是一段描述(新增树节点不填)",required = false,example = "归档范围信息")
    private String filingScope;

    /**
     * 档案类型code 例如wsda false
     */
    @ApiModelProperty(value = "档案类型code",required = true,example = "wsda")
    private String typeCode;

    /**
     * 档案类型名称
     */
    @ApiModelProperty(value = "档案类型名称",required = true,example = "文书档案")
    private String typeName;

    /**
     * 是否是叶子节点
     */
    @ApiModelProperty(value = "是否是叶子节点",required = true,example = "true")
    private Boolean leaf;

	/**
	 * 档案类型code 例如wsda false
	 */
	@ApiModelProperty(value = "租户ID",hidden = true)
	private Long tenantId;

	/**
	 * 归档范围路径
	 */
	@ApiModelProperty(value = "归档范围路径")
	private String path;
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
	/**
	 * 创建时间
	 */
	@ApiModelProperty("创建时间")
	private LocalDateTime createdTime;

	@ApiModelProperty("树状唯一索引pk")
	private String pk;

	@ApiModelProperty("顺序号")
	private Integer sortNo;
}
