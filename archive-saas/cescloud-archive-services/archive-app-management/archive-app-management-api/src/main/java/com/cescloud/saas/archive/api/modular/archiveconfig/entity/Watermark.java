package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;


@Data
@TableName("apma_watermark")
//@KeySequence("SEQ_APMA_WATERMARK")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Watermark extends Model<Watermark> {

	private static final long serialVersionUID = 1L;
	/**
	 * 列表数据id,主键 false
	 */
	@ApiModelProperty("水印id,主键")
	@TableId
	private Long id;

	/**
	 * 档案表名
	 */
	@ApiModelProperty("档案表名")
    private String storageLocate;

	/**
	 * 水印格式
	 */
	@ApiModelProperty("水印格式")
	private String watermarkFormat;

	/**
	 * 水印分类
	 */
	@ApiModelProperty("水印分类")
	private Integer waterClassification;


	/**
	 * 默认配置
	 */
	@ApiModelProperty("默认配置")
	private Boolean isDefault;


	/**
	 * 档案表名
	 */
	@ApiModelProperty("水印名称")
    private String watermarkName;

	/**
	 * 所属模块
	 */
	@ApiModelProperty("所属模块")
    private Long moduleId;

	@ApiModelProperty("所属租户id")
	@TableField(fill = FieldFill.INSERT)
	private Long tenantId;

	/**
	 * 乐观锁,数据版本号
	 */
	@ApiModelProperty("乐观锁")
	@TableField(fill = FieldFill.INSERT)
	@Version
	private Long revision;

	/**
	 * 创建人
	 */
	@ApiModelProperty("创建人")
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	private Long createdBy;

	/**
	 * 创建时间
	 */
	@ApiModelProperty("创建时间")
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	private LocalDateTime createdTime;

	/**
	 * 更新人
	 */
	@ApiModelProperty("更新人")
	@TableField(fill = FieldFill.UPDATE)
	private Long updatedBy;

	/**
	 * 更新时间
	 */
	@ApiModelProperty("更新时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updatedTime;

}