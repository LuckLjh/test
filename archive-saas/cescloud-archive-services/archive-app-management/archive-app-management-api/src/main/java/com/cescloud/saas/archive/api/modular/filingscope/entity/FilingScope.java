package com.cescloud.saas.archive.api.modular.filingscope.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 归档范围定义
 *
 * @author xieanzhu
 * @date 2019-04-22 15:45:22
 */
@ApiModel("归档范围定义")
@Data
@TableName("apma_filing_scope")
//@KeySequence("SEQ_APMA_FILING_SCOPE")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilingScope extends Model<FilingScope> {
	private static final long serialVersionUID = 1L;

	/**
	 * 归档范围所属分类id,主键 false
	 */
	@ApiModelProperty("归档范围定义ID")
	@TableId
	private Long id;
	/**
	 * 归档范围所属分类号 false
	 */
	@ApiModelProperty("归档范围所属分类号")
	private String classNo;
	/**
	 * 归档范围所属的分类名称 false
	 */
	@ApiModelProperty("归档范围所属的分类名称")
	private String className;
	/**
	 * 归档范围所属父节点的id false
	 */
	@ApiModelProperty("归档范围所属父节点的id")
	private Long parentClassId;

	/**
	 * 档案类型code 例如wsda false
	 */
	@ApiModelProperty("档案类型code")
	private String typeCode;

	/**
	 * 归档范围层级
	 */
	@ApiModelProperty("归档范围层级path")
	private String path;
	/**
	 * 全宗号编码
	 */
	@ApiModelProperty("全宗号编码")
	private String fondsCode;
	/**
	 * 全宗名称
	 */
	@ApiModelProperty("全宗名称")
	private String fondsName;
	/**
	 * 设置人
	 */
	@ApiModelProperty("设置人")
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	private String createdUserName;

	@ApiModelProperty("顺序号")
	private Integer sortNo;
	/**
	 * 所属租户id true
	 */
	@ApiModelProperty("所属租户id")
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
