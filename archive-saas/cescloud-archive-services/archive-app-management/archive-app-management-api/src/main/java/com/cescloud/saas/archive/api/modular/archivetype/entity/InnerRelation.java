package com.cescloud.saas.archive.api.modular.archivetype.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 档案类型关联
 *
 * @author xieanzhu
 * @date 2019-04-16 14:13:01
 */
@ApiModel("档案类型关联")
@Data
@TableName("apma_inner_relation")
//@KeySequence("SEQ_APMA_INNER_RELATION")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InnerRelation extends Model<InnerRelation> {


	private static final long serialVersionUID = -849114348333856510L;
	/**
	 * 关联关系id false
	 */
	@ApiModelProperty("档案类型关联ID")
	@TableId
	private Long id;

	/**
	 * 关联元数据id
	 */
	@ApiModelProperty("关联元数据id")
	@TableField(insertStrategy = FieldStrategy.IGNORED)
	private Long sourceMetadataId;
	/**
	 * 关联元数据所属档案类型层级表 false
	 */
	@ApiModelProperty("关联元数据所属档案类型层级表")
	@TableField(insertStrategy = FieldStrategy.IGNORED)
	private String sourceStorageLocate;
	/**
	 * 关联元数据id
	 */
	@ApiModelProperty("被关联元数据id")
	@TableField(insertStrategy = FieldStrategy.IGNORED)
	private Long targetMetadataId;
	/**
	 * 被关联元数据所属档案类型层级表 false
	 */
	@ApiModelProperty("被关联元数据所属档案类型层级表")
	private String targetStorageLocate;
	/**
	 * 所属全宗id false
	 */
	@ApiModelProperty("所属全宗id")
	private Long fondsId;
	/**
	 * 关联方式：1：相等 2：求和 3: 计数 4：求起止值 false
	 */
	@TableField(insertStrategy = FieldStrategy.IGNORED)
	@ApiModelProperty("关联方式：1：相等 2：求和 3: 计数 4：求起止值 5：最大值 6：最小值 7：平均值")
	private Integer relationType;
	/**
	 * 是否关联（0不关联 1关联） false
	 */
	@ApiModelProperty("是否关联")
	private Integer isRelation;

	@ApiModelProperty("模块id")
	private Long moduleId;
	/**
	 * 所属租户ID true
	 */
	@ApiModelProperty("所属租户ID")
	@TableField(fill = FieldFill.INSERT)
	private Long tenantId;
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
