package com.cescloud.saas.archive.api.modular.relationrule.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("档案门类与保管期限关联表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("apma_archive_retention_rel")
public class ArchiveRetentionRelation extends Model<ArchiveRetentionRelation> {

    @ApiModelProperty("id")
    @TableId
    private Long id;

    @ApiModelProperty("档案类型编码")
    private String archiveTypeCode;

    @ApiModelProperty("保管期限编码id")
    private Long dictId;

    @ApiModelProperty("租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;


}
