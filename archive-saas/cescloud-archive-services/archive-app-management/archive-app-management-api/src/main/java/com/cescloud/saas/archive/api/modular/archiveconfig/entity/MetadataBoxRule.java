package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("档案盒配置关联字段表")
@Data
@Builder
@TableName("apma_metadata_box_rule")
@NoArgsConstructor
@AllArgsConstructor
//@KeySequence("SEQ_APMA_METADATA_BOX_RULE")
public class MetadataBoxRule extends Model<MetadataBoxRule> {

    @ApiModelProperty("主键id")
    @TableId
    private Long id;

    @ApiModelProperty("盒配置id")
    private Long configId;

    @ApiModelProperty("类型：0、盒号组成字段，1、分组设置字段")
    private Integer type;

    @ApiModelProperty("组成元数据ID")
    private Long metadataId;

    @ApiModelProperty("元数据名称")
    private String metadataChinese;

    @ApiModelProperty("元数据英文名称")
    private String metadataEnglish;

    @ApiModelProperty("数据字典：0、存KEY  1、存值")
    private Integer dictKeyValue;

    @ApiModelProperty("连接字符")
    private String connectStr;

    @ApiModelProperty("连接标识: M:代表元数据字段 C:代表连接符")
    private String connectSign;

    @ApiModelProperty("是否显示编码 1:显示编码 0 :显示名称")
    private Integer isShowCode;

    @ApiModelProperty("数据字典编码")
    private String dictCode;

    @ApiModelProperty("排序")
    private Integer sortNo;
}
