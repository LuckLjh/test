package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName MetadataBoxRuleUndefinedDTO
 * @Author zhangxuehu
 * @Date 2020/7/30 16:03
 **/
@ApiModel("档案装盒规则元数据实体")
@Data
public class MetadataBoxRuleUndefinedDTO extends Metadata {

    @ApiModelProperty("元数据id")
    private Long metadataId;

    @ApiModelProperty("链接符号")
    private String connectSign;

    @ApiModelProperty("是否显示编码 1:显示编码 0 :显示名称")
    private Integer IsShowCode;

    @ApiModelProperty("数据字典：0、存KEY  1、存值")
    private Integer dictKeyValue;
}
