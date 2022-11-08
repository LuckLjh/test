package com.cescloud.saas.archive.api.modular.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @ClassName AccountModule
 * @Author zhangxuehu
 * @Date 2021/2/24 上午9:28
 **/
@ApiModel("台账模块表")
@Data
@TableName("apma_account_module")
@Builder
public class AccountModule extends Model<AccountModule> {
    private static final long serialVersionUID = 7513951529693549870L;

    @ApiModelProperty("主键id")
    @TableId
    private Long id;

    @ApiModelProperty("模块名称")
    private String moduleName;

    @ApiModelProperty("模块code")
    private String moduleCode;

    @ApiModelProperty("排序号")
    private Integer sortNo;

    @ApiModelProperty("乐观锁")
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long revision;

    @ApiModelProperty("所属租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
