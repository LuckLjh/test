package com.cescloud.saas.archive.api.modular.account.dto;


import lombok.Data;
import lombok.ToString;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 驾驶舱个人台账模板表
 *
 * @author bob
 * @date 2021-05-28 23:02:59
 */
@Data
@ToString
public class AccountTemplateUserDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   * id id
   */
    @ApiModelProperty("id")
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
    private Long tenantId;
    /**
   * 乐观锁 revision
   */
    @ApiModelProperty("乐观锁")
    private Long revision;
    /**
   * 创建人 created_by
   */
    @ApiModelProperty("创建人")
    private Long createdBy;
    /**
   * 创建时间 created_time
   */
    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;
    /**
   * 修改人 updated_by
   */
    @ApiModelProperty("修改人")
    private Long updatedBy;
    /**
   * 更新时间 updated_time
   */
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedTime;
  
}
