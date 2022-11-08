package com.cescloud.saas.archive.api.modular.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 报表定义
 *
 * @author liaorunzhe
 * @date 2019-09-02 16:04:36
 */
@ApiModel("报表定义对象")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("apma_report")
//@KeySequence("SEQ_APMA_REPORT")
@EqualsAndHashCode(callSuper = true)
public class Report extends Model<Report> {
private static final long serialVersionUID = 1L;

	/**
     * 报表数据id,主键 false
     */
    @ApiModelProperty("报表数据id")
    @TableId
    private Long id;

    @ApiModelProperty("模块id")
    private Long moduleId;

    /**
     * 报表图标流 false
     */
    @ApiModelProperty("报表图标流")
    private String reportPicture;

    /**
	 * 报表类型 false
	 */
    @ApiModelProperty("报表类型，分2种（0：独立，1：复合）")
	private String reportType;

    /**
     * 报表标题 false
     */
  	@ApiModelProperty("报表标题")
  	private String reportTopic;

  	/**
     * 报表描述信息 false
     */
  	@ApiModelProperty("报表描述信息")
  	private String reportDescription;

  	/**
     * 报表存放路径
     */
  	@ApiModelProperty("报表存放路径")
  	private byte[] reportPath;

	/**
	 * 报表编译后存放路径
	 */
	@ApiModelProperty("报表编译后存放路径")
	private byte[] jasperContext;

  	/**
     * 报表分页行数 false
     */
  	@ApiModelProperty("报表分页行数")
  	private Integer reportPageLines;

  	/**
     * 报表格式，比如pdf,word,excel false
     */
  	@ApiModelProperty("报表格式，比如pdf,word,excel")
  	private String reportFormat;

  	/**
     * 报表所属档案数据表名 false
     */
  	@ApiModelProperty("报表所属档案数据表名")
  	private String storageLocate;

    /**
     * 报表模板，已存在 或 不存在
     */
  	@ApiModelProperty(value = "报表模板，已存在或不存在")
  	private String reportModel;

  	/**
     * 所属租户ID false
     */
  	@ApiModelProperty("所属租户ID")
  	@TableField(fill = FieldFill.INSERT)
  	private Long tenantId;

  	/**
     * 报表分页字段 false
     */
 	 @ApiModelProperty("报表分页字段")
  	private String pageField;

 	 /**
     * 所属全宗id false
     */
  	 @ApiModelProperty("所属全宗id")
  	 private Long fondId;

  	 /**
     * 乐观锁 true
     */
  	@ApiModelProperty("乐观锁")
  	@TableField(fill = FieldFill.INSERT)
  	private Long revision;

  	/**
     * 创建人 true
     */
  	@ApiModelProperty("创建人")
  	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
  	private Long createdBy;

  	/**
     * 创建时间 true
     */
  	@ApiModelProperty("创建时间")
  	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
  	private LocalDateTime createdTime;

  	/**
     * 更新人 true
     */
  	@ApiModelProperty("更新人")
  	@TableField(fill = FieldFill.UPDATE)
  	private Long updatedBy;

  	/**
     * 更新时间 true
     */
  	@ApiModelProperty("更新时间")
  	@TableField(fill = FieldFill.UPDATE)
  	private LocalDateTime updatedTime;
  
}
