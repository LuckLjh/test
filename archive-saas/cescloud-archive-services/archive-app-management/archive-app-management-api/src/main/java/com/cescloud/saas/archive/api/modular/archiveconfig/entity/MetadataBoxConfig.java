package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @ClassName MetadataBoxConfig
 * @Author zhangxuehu
 * @Date 2020/7/27 10:57
 **/
@ApiModel("档案盒配置")
@Data
@TableName("apma_metadata_box_config")
//@KeySequence("SEQ_APMA_METADATA_BOX_CONFIG")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataBoxConfig extends Model<MetadataBoxConfig> {

    @ApiModelProperty("主键id")
    @TableId
    private Long id;

    @ApiModelProperty("档案类型层级表名")
    private String storageLocate;

    @ApiModelProperty("组成元数据ID")
    private Long metadataId;

    @ApiModelProperty("盒号流水号补零位数")
    private Integer digitFlag;

    @ApiModelProperty("模块id")
    private Long moduleId;
    /**
     * 所属租户id
     */
    @ApiModelProperty("所属租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;


    /**
     * 乐观锁,数据版本号
     */
    @ApiModelProperty("乐观锁")
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long revision;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;
}
