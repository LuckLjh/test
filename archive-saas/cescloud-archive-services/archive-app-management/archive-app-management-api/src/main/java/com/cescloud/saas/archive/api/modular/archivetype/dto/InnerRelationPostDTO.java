package com.cescloud.saas.archive.api.modular.archivetype.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
@author xaz
@date 2019/6/25 - 15:36
**/
@Data
@ApiModel(value = "关联关系定义新增参数")
public class InnerRelationPostDTO implements Serializable {

    /**
     * 关联表名（左侧档案树表名）
     */
    @NotBlank(message = "关联表名不能为空")
    @ApiModelProperty(value = "关联表名（左侧档案树表名）",required = true,example = "t_1_ws_wsda_v")
    private String storageLocate;

    /**
     * 关联元数据Id false
     */
	@NotNull(message = "关联元数据id不能为空")
    @ApiModelProperty(value = "关联元数据Id",required = true,example = "1")
    private Long sourceMetadataId;

    /**
     * 被关联元数据英文Id false
     */
	@NotNull(message = "被关联元数据id不能为空")
    @ApiModelProperty(value = "被关联元数据英文Id",required = true,example = "1")
    private Long targetMetadataId;

    /**
     * 关联方式：1：相等 2：求和 3: 计数 4：求起止值 false
     */
	@NotNull(message = "关联方式不能为空")
    @ApiModelProperty(value = "关联方式：1：相等 2：求和 3: 计数 4：求起止值",required = true,example = "1")
    private Integer relationType;

    /**
     * 是否关联（0不关联 1关联） false
     */
	@NotNull(message = "是否关联不能为空")
    @ApiModelProperty( value = "是否关联",required = true,example = "true")
    private Boolean relation;

	@NotNull(message = "模块id不能为空")
	@ApiModelProperty("模块id")
	private Long moduleId;
}
