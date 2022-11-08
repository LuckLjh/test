package com.cescloud.saas.archive.api.modular.report.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 报表关联元数据表
 *
 * @author xieanzhu
 * @date 2019-04-29 18:40:02
 */
@Data
@TableName("apma_report_metadata")
//@KeySequence("SEQ_APMA_REPORT_METADATA")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportMetadata extends Model<ReportMetadata> {
private static final long serialVersionUID = 1L;

      /**
     * 报表关联元数据表id,主键 false
     */
  @TableId
  private Long id;
      /**
     * 报表id false
     */
  private Long reportId;
    /**
     * 档案数据表名 false
     */
  private String storageLocate;
      /**
     * 元数据中文名称 false
     */
  private String metadataChinese;
      /**
     * 元数据英文名称 false
     */
  private String metadataEnglish;
      /**
     * 所属租户ID true
     */
  @TableField(fill = FieldFill.INSERT)
  private Long tenantId;
      /**
     * 乐观锁 true
     */
  @TableField(fill = FieldFill.INSERT)
  private Long revision;
      /**
     * 创建人 true
     */
  @TableField(fill = FieldFill.INSERT)
  private Long createdBy;
      /**
     * 创建时间 true
     */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdTime;
      /**
     * 更新人 true
     */
  @TableField(fill = FieldFill.UPDATE)
  private Long updatedBy;
      /**
     * 更新时间 true
     */
  @TableField(fill = FieldFill.UPDATE)
  private LocalDateTime updatedTime;
  
}
