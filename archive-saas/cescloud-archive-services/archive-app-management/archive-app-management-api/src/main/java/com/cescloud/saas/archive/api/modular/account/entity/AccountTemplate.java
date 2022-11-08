package com.cescloud.saas.archive.api.modular.account.entity;

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
 * @ClassName AccountTemplate
 * @Author zhangxuehu
 * @Date 2021/2/24 上午9:31
 **/
@ApiModel("台账模块表")
@Data
@TableName("apma_account_template")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountTemplate extends Model<AccountTemplate> {
    private static final long serialVersionUID = 7513951529693549870L;

    @ApiModelProperty("主键id")
    @TableId
    private Long id;

    @ApiModelProperty("模板名称")
    private String templateName;

    @ApiModelProperty("台账模板布局详情")
    private Object templateDetail;

    @ApiModelProperty("是否默认")
    private Integer isDefault;

    @ApiModelProperty("乐观锁")
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long revision;

    @ApiModelProperty("所属租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;

    @ApiModelProperty("更新人")
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;
}
