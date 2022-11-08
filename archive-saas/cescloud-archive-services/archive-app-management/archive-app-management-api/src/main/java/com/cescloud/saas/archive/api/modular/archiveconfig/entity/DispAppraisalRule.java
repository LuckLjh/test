package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.cescloud.saas.archive.api.modular.role.entity.SysRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel("档案鉴定规则表")
@Data
@TableName("apma_disp_appraisal_rule")
//@KeySequence("SEQ_APMA_DISP_APPRAISAL_RULE")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispAppraisalRule extends Model<DispAppraisalRule> {

    @ApiModelProperty("列表数据id,主键")
    @TableId
    private Long id;

    @ApiModelProperty("档案层级存储表")
    private String storageLocate;

    @ApiModelProperty("到期鉴定起始日期绑定字段")
    private Long begunDateMetaId;

    @ApiModelProperty("到期鉴定保管期限绑定字段")
    private Long retentionPeriodMetaId;

    @ApiModelProperty("到期鉴定是否自动提醒 （1是0否）")
    private Integer retentionAutoReminder;

    @ApiModelProperty("到期鉴定提前提醒时间 (天）")
    private Integer retentionLeadTime;

	@ApiModelProperty("到期鉴定提醒角色")
	private String remindRoles;

	@TableField(exist = false)
	@ApiModelProperty("到期鉴定提醒角色")
	private List<SysRole> roleList;

    @ApiModelProperty("所属租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
