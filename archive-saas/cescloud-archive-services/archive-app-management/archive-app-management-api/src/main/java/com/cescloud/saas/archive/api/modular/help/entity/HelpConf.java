package com.cescloud.saas.archive.api.modular.help.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;

/**
 * 基本数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
@Data
@TableName("apma_help_conf")
@EqualsAndHashCode(callSuper = true)
public class HelpConf extends Model<HelpConf> {
private static final long serialVersionUID = 1L;

    /**
   * 主键 id
   */
  @ApiModelProperty("主键")
  @TableId
  private Long id;
    /**
   * 显示 全宗初始化拷贝 开启：0，不开启：默认 null 及 1 open
   */
  @ApiModelProperty("显示 全宗初始化拷贝 开启：0，不开启：默认 null 及 1")
  private Long open;
    /**
   * 全宗号编码 fonds_code
   */
  @ApiModelProperty("全宗号编码")
  private String fondsCode;
    /**
   * 全宗名称 fonds_name
   */
  @ApiModelProperty("全宗名称")
  private String fondsName;
    /**
   * 设置人 created_user_name
   */
  @ApiModelProperty("设置人")
  @TableField(fill = FieldFill.INSERT)
  private String createdUserName;
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
