package com.cescloud.saas.archive.api.modular.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;

/**
 * 驾驶舱个人台账模板表
 *
 * @author bob
 * @date 2021-05-28 23:02:59
 */
@Data
@ToString
@TableName("apma_account_template_user")
@EqualsAndHashCode(callSuper = true)

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountTemplateUser extends Model<AccountTemplateUser> {
private static final long serialVersionUID = 1L;

    /**
   * id id
   */
  @ApiModelProperty("id")
  @TableId
  private Long id;
    /**
   * 模板id template_id
   */
  @ApiModelProperty("模板id")
  private Long templateId;
    /**
   * 模板名称 template_name
   */
  @ApiModelProperty("模板名称")
  private String templateName;
    /**
   * 用户id user_id
   */
  @ApiModelProperty("用户id")
  private Long userId;
    /**
   * 排序 order_no
   */
  @ApiModelProperty("排序")
  private Integer orderNo;
    /**
   * 是否默认 is_default
   */
  @ApiModelProperty("是否默认")
  private Integer isDefault;
    /**
   * 所属租户ID tenant_id
   */
  @ApiModelProperty("所属租户ID")
  @TableField(fill = FieldFill.INSERT)
  private Long tenantId;
    /**
   * 乐观锁 revision
   */
  @ApiModelProperty("乐观锁")
  @TableField(fill = FieldFill.INSERT)
  private Long revision;
    /**
   * 创建人 created_by
   */
  @ApiModelProperty("创建人")
  @TableField(fill = FieldFill.INSERT)
  private Long createdBy;
    /**
   * 创建时间 created_time
   */
  @ApiModelProperty("创建时间")
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdTime;
    /**
   * 修改人 updated_by
   */
  @ApiModelProperty("修改人")
  @TableField(fill = FieldFill.UPDATE)
  private Long updatedBy;
    /**
   * 更新时间 updated_time
   */
  @ApiModelProperty("更新时间")
  @TableField(fill = FieldFill.UPDATE)
  private LocalDateTime updatedTime;
  
}
