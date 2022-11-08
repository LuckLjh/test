package com.cescloud.saas.archive.api.modular.archivetype.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 累加规则的分组规则
 *
 * @author liwei
 * @date 2019-04-16 14:52:34
 */
@ApiModel("累加规则的分组规则")
@Data
@TableName("apma_metadata_source")
//@KeySequence("SEQ_APMA_METADATA_SOURCE")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataSource extends Model<MetadataSource> {

	private static final long serialVersionUID = -3963547675867484966L;
	/**
	 * 参照元数据id,主键 false
	 */
	@TableId
	@ApiModelProperty("主键ID")
	private Long id;
	/**
	 * 字段所在档案类型层级表 false
	 */
	@ApiModelProperty("字段所在档案类型层级表")
	private String storageLocate;
	/**
	 * 数据规则所属记录id
	 */
	@ApiModelProperty("数据规则所属记录id")
	private Long metadataTargetId;
	/**
	 * 数据规则分组字段元数据id
	 */
	@ApiModelProperty("数据规则分组字段元数据id")
	private Long metadataSourceId;
	/**
	 * 分组字段元数据所属编号 false
	 */
	@ApiModelProperty("分组字段元数据所属编号")
	private Integer sortNo;
	/**
	 * 所属租户ID true
	 */
	@ApiModelProperty("所属租户id")
	@TableField(fill = FieldFill.INSERT)
	private Long tenantId;
	/**
	 * 乐观锁 true
	 */
	@ApiModelProperty("乐观锁")
	@TableField(fill = FieldFill.INSERT)
	@Version
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

	/**
	 * 元数据英文名称 false
	 */
	@ApiModelProperty("元数据英文名称")
	@TableField(exist = false)
	private String metadataSourceEnglish;

}