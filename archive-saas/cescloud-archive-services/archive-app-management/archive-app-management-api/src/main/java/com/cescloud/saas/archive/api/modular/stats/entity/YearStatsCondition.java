package com.cescloud.saas.archive.api.modular.stats.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/*
 * Description: 年报统计条件
 * @auther: qianbaocheng
 * @date: 2021-6-28 16:22
 */
@Data
@TableName("apma_year_stats_condition")
@EqualsAndHashCode(callSuper = true)
public class  YearStatsCondition extends Model<YearStatsCondition> {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@ApiModelProperty("主键")
	@TableId
	private Long id;

	/**
	 * 年报的行数
	 */
	@ApiModelProperty("年报的行数")
	private Integer numberLine;

	/**
	 * 全宗号
	 */
	@ApiModelProperty("全宗号")
	private String fondsCode;

	/**
	 * 年报id
	 */
	@ApiModelProperty("年报id")
	private Long yearStatsId;

	/**
	 * 档案类型名称集
	 */
	@ApiModelProperty("档案类型名称")
	private String archiveTypeName;

	/**
	 * 档案类型编码
	 */
	@ApiModelProperty("档案类型编码")
	private String archiveTypeCode;

	/**
	 * 表模板ID false
	 */
	@ApiModelProperty("表模板ID")
	private Long templateTableId;

	/**
	 * 中文显示条件 false
	 */
	@ApiModelProperty("中文显示条件")
	private String chineseCondition;

	/**
	 * 前台条件 false
	 */
	@ApiModelProperty("前台条件")
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	private Object pageCondition;

	/**
	 * 后台查询条件 false
	 */
	@ApiModelProperty("后台查询条件")
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	private Object backCondition;

	/**
	 * 档案表
	 */
	@ApiModelProperty("档案表")
	private String storageLocate;

	/**
	 * 所属租户id
	 */
	@ApiModelProperty("所属租户id")
	@TableField(fill = FieldFill.INSERT)
	private Long tenantId;

	/**
	 * 创建人 true
	 */
	@ApiModelProperty("创建人")
	@TableField(fill = FieldFill.INSERT)
	private Long createdBy;

	/**
	 * 创建时间 true
	 */
	@ApiModelProperty("创建时间")
	@TableField(fill = FieldFill.INSERT)
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
