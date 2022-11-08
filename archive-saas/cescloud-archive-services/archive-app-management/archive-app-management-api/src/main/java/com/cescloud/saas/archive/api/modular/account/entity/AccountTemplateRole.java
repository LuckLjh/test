package com.cescloud.saas.archive.api.modular.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @ClassName AccountTemplateRole
 * @Author zhangxuehu
 * @Date 2021/2/24 上午9:38
 **/
@ApiModel("台账模板与角色关系表")
@TableName("apma_account_template_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTemplateRole extends Model<AccountTemplateRole> {
    private static final long serialVersionUID = 7513951529693549870L;

    @ApiModelProperty("主键id")
    @TableId
    private Long id;

    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("角色id")
    private Long roleId;

    @ApiModelProperty("模板id")
    private Long templateId;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;

    @ApiModelProperty("所属租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

	@ApiModelProperty("是否V8,1:是/0:不是")
	private Long isV8;
}
