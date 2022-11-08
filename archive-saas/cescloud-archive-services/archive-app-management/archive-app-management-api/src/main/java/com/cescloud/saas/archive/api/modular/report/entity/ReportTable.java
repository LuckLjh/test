package com.cescloud.saas.archive.api.modular.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 报表关联元数据表
 *
 * @author xieanzhu
 * @date 2019-04-29 18:38:01
 */
@Data
@TableName("apma_report_table")
//@KeySequence("SEQ_APMA_REPORT_TABLE")
@EqualsAndHashCode(callSuper = true)
public class ReportTable extends Model<ReportTable> {
private static final long serialVersionUID = 1L;

      /**
     * 报表关联档案数据表id,主键 false
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
     * 档案数据表中文名称 false
     */
  private String storageName;
      /**
     * 所属租户id true
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
