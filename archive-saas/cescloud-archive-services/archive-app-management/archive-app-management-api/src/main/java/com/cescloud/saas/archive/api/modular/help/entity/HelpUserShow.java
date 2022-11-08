package com.cescloud.saas.archive.api.modular.help.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;

/**
 * 档案类型数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
@Data
@TableName("apma_help_user_show")
@EqualsAndHashCode(callSuper = true)
public class HelpUserShow extends Model<HelpUserShow> {
private static final long serialVersionUID = 1L;

    /**
   * 主键 id
   */
  @ApiModelProperty("主键")
  @TableId
  private Long id;
    /**
   * 观众id user_id
   */
  @ApiModelProperty("观众id")
  private Long userId;
    /**
   * 菜单ID menu_id
   */
  @ApiModelProperty("菜单ID")
  private Long menuId;
    /**
   * 全宗号编码 fonds_code
   */
  @ApiModelProperty("全宗号编码")
  private String fondsCode;
    /**
   * 所属租户ID tenant_id
   */
  @ApiModelProperty("所属租户ID")
  @TableField(fill = FieldFill.INSERT)
  private Long tenantId;
    /**
   * 帮助文档的版本 help_version
   */
  @ApiModelProperty("帮助文档的版本")
  private Integer helpVersion;
  
}
