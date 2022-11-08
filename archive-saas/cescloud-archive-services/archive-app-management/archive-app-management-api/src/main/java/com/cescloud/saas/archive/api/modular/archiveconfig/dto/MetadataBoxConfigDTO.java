package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataBoxRule;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * @ClassName MetadataBoxConfigDTO
 * @Author zhangxuehu
 * @Date 2020/7/27 13:28
 **/
@ApiModel("保存装盒规则")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataBoxConfigDTO {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("档案类型层级表名")
    private String storageLocate;

    @ApiModelProperty("起止序号字段id")
    private Long metadataId;

    @ApiModelProperty("盒号流水号补零位数")
    private Integer digitFlag;

    @ApiModelProperty("模块id")
    private Long moduleId;

    @ApiModelProperty("分组字段集")
    List<MetadataBoxRule> groupingFields;

    @ApiModelProperty("盒号设置")
    List<MetadataBoxRule> boxRules;
}
