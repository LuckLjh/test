package com.cescloud.saas.archive.api.modular.archivetype.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
@author xaz
@date 2019/6/25 - 15:48
**/

@Data
@ApiModel(value = "关联关系定义修改参数")
public class InnerRelationPutDTO implements Serializable {

	private static final long serialVersionUID = 5596892757272979810L;
	/**
     * 关联关系id false
     */
	@NotNull(message = "档案类型关联ID不能为空")
    @ApiModelProperty(value = "档案类型关联ID",required = true,example = "1")
    private Long id;

    /**
     * 关联元数据所属档案类型层级表 false
     */
	@NotBlank(message = "关联表名不能为空")
    @ApiModelProperty(value = "关联元数据所属档案类型层级表",required = true,example = "t_1_ws_wsda_v")
    private String sourceStorageLocate;

    /**
     * 关联元数据Id false
     */
	@NotNull(message = "关联元数据id不能为空")
    @ApiModelProperty(value = "关联元数据Id",required = true,example = "1")
    private Long sourceMetadataId;

    /**
     * 被关联元数据所属档案类型层级表 false
     */
	@NotBlank(message = "被关联元数据所属档案类型层级表")
    @ApiModelProperty(value = "被关联元数据所属档案类型层级表",required = true,example = "t_1_ws_wsda_v")
    private String targetStorageLocate;

    /**
     * 被关联元数据Id false
     */
	@NotNull(message = "被关联元数据id不能为空")
    @ApiModelProperty(value = "被关联元数据Id",required = true,example = "1")
    private Long targetMetadataId;

    /**
     * 关联方式：1：相等 2：求和 3: 计数 4：求起止值 false
     */
	@NotNull(message = "关联方式不能为空")
    @ApiModelProperty(value = "关联方式：1：相等 2：求和 3: 计数 4：求起止值 等等",required = true,example = "1")
    private Integer relationType;

    /**
     * 是否关联（0不关联 1关联） false
     */
	@NotNull(message = "是否关联不能为空")
    @ApiModelProperty(value = "是否关联",required = true,example = "true")
    private Boolean relation;

	@NotNull(message = "模块id不能为空")
	@ApiModelProperty("模块id")
	private Long moduleId;
}