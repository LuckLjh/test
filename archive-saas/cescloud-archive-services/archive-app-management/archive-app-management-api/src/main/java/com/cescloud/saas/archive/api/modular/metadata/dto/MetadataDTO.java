package com.cescloud.saas.archive.api.modular.metadata.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 元数据DTO 包含 标签信息
 */
@ApiModel("元数据DTO")
@Data
public class MetadataDTO implements Serializable {

	private static final long serialVersionUID = 1760140099745489373L;

	/**
     * 档案门类层级目录元数据存储表名 false
     */
    @ApiModelProperty("元数据存储表名")
    private String storageLocate;

    @ApiModelProperty("元数据英文名称")
    private String metadataEnglish;

    @ApiModelProperty("元数据中文名称")
    private String metadataChinese;

    @ApiModelProperty("标签英文名称")
    private String tagEnglish;

    /**
     * 检索关键字 false
     */
    @ApiModelProperty(value = "检索关键字", example = "检索关键字")
    private String keyword;

}
