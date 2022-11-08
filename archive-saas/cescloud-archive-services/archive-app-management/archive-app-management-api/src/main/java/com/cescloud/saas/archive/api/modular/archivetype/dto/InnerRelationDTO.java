package com.cescloud.saas.archive.api.modular.archivetype.dto;
/**
@author xaz
@date 2019/4/18 - 13:16
**/

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerRelationDTO implements Serializable {

	private static final long serialVersionUID = -5870890936930608237L;
	/**
     * 关联表名（左侧档案树表名）
     */
    @ApiModelProperty("关联表名")
    @TableId
    private String storageLocate;

    /**
     * 关联关系id false
     */
    @ApiModelProperty("档案类型关联ID")
    @TableId
    private Long id;

    /**
     * 关联元数据所属档案类型层级表 false
     */
    @ApiModelProperty("关联元数据所属档案类型层级表")
    private String sourceStorageLocate;

    /**
     * 关联元数据Id false
     */
    @ApiModelProperty("关联元数据Id")
    private Long sourceMetadataId;

    /**
     * 关联元数据英文名称 false
     */
    @ApiModelProperty("关联元数据英文名称")
    private String sourceMetadata;

    /**
     * 被关联元数据所属档案类型层级表 false
     */
    @ApiModelProperty("被关联元数据所属档案类型层级表")
    private String targetStorageLocate;

    /**
     * 被关联元数据英文Id false
     */
    @ApiModelProperty("被关联元数据英文Id")
    private Long targetMetadataId;

    /**
     * 被关联元数据英文名称 false
     */
    @ApiModelProperty("被关联元数据英文名称")
    private String targetMetadata;

    /**
     * 被关联元数据中文名称 false
     */
    @ApiModelProperty("被关联元数据中文名称")
    private String targetMetadataChinese;

    /**
     * 关联方式：1：相等 2：求和 3: 计数 4：求起止值 false
     */
    @ApiModelProperty("关联方式：1：相等 2：求和 3: 计数 4：求起止值")
    private Integer relationType;

    /**
     * 是否关联（0不关联 1关联） false
     */
    @ApiModelProperty("是否关联")
    private Boolean relation;

}
