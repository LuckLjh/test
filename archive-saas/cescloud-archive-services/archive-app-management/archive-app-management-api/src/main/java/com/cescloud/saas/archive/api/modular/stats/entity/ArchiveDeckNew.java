package com.cescloud.saas.archive.api.modular.stats.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;

/**
 * 驾驶舱新增量
 *
 * @author bob
 * @date 2021-05-11 16:57:30
 */
@Data
@TableName("apma_archive_deck_new")
@EqualsAndHashCode(callSuper = true)
public class ArchiveDeckNew extends Model<ArchiveDeckNew> {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID id
   */
  @ApiModelProperty("主键ID")
  @TableId
  private Long id;
    /**
   * 档案门类分类 archive_class_type
   */
  @ApiModelProperty("档案门类分类")
  private String archiveClassType;
    /**
   * 档案门类编码 archive_type_code
   */
  @ApiModelProperty("档案门类编码")
  private String archiveTypeCode;
    /**
   * 整理方式档案类型整理方式：V 案卷、卷内 O 一文一件 P 项目、案卷、卷内 S 单套制 R 预归档 layer_code
   */
  @ApiModelProperty("整理方式档案类型整理方式：V 案卷、卷内 O 一文一件 P 项目、案卷、卷内 S 单套制 R 预归档")
  private String layerCode;
    /**
   * 档案类型模板id template_type_id
   */
  @ApiModelProperty("档案类型模板id")
  private Long templateTypeId;
    /**
   * 全宗号 fonds_code
   */
  @ApiModelProperty("全宗号")
  private String fondsCode;
    /**
   * 统计值 stats_amount
   */
  @ApiModelProperty("统计值")
  private Integer statsAmount;
    /**
   * 页数/时长sum(amount_of_pages) page_amount
   */
  @ApiModelProperty("页数/时长sum(amount_of_pages)")
  private Integer pageAmount;
    /**
   * 已数字化数量sum(file_size) digited_amount
   */
  @ApiModelProperty("已数字化数量sum(file_size)")
  private Integer digitedAmount;
    /**
   * 创建时间年月日 created_time
   */
  @ApiModelProperty("创建时间年月日")
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdTime;
    /**
   * 创建对应的年 created_year
   */
  @ApiModelProperty("创建对应的年")
  private Integer createdYear;
    /**
   * 创建对应的月 created_month
   */
  @ApiModelProperty("创建对应的月")
  private Integer createdMonth;
    /**
   * 创建对应的间日 created_day
   */
  @ApiModelProperty("创建对应的间日")
  private Integer createdDay;
    /**
   * 创建对应的周 created_week
   */
  @ApiModelProperty("创建对应的周")
  private Integer createdWeek;
    /**
   * 创建对应的年的周 created_year_week
   */
  @ApiModelProperty("创建对应的年的周")
  private Integer createdYearWeek;
    /**
   * 租户ID tenant_id
   */
  @ApiModelProperty("租户ID")
  @TableField(fill = FieldFill.INSERT)
  private Long tenantId;
    /**
   * 状态 status
   */
  @ApiModelProperty("状态")
  private Integer status;
    /**
   * 更新时间 updated_time
   */
  @ApiModelProperty("更新时间")
  @TableField(fill = FieldFill.UPDATE)
  private LocalDateTime updatedTime;
  
}
