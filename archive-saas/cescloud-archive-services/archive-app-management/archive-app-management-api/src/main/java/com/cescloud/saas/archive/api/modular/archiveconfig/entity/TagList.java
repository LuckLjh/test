
package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 标签列表配置
 *
 * @author liudong1
 * @date 2019-05-27 15:20:07
 */
@Data
@TableName("apma_config_tag_list")
//@KeySequence("SEQ_APMA_TAG_LIST")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagList extends Model<TagList> {
	private static final long serialVersionUID = 1L;

	/**
	 * 列表数据id,主键 false
	 */
	@ApiModelProperty("列表数据id,主键")
	@TableId
	private Long id;
	/**
	 * 档案层级组 false
	 */
	@ApiModelProperty("档案层级")
	private String archiveLayer;
	/**
	 * 元数据标签ID false
	 */
	@ApiModelProperty("标签英文")
	private String tagEnglish;
	/**
	 * 显示别名
	 */
	@ApiModelProperty("显示别名")
	private String showAlias;
	/**
	 * 对齐方式：C居中，L左对齐，R右对齐 false
	 */
	@ApiModelProperty("对齐方式：C居中，L左对齐，R右对齐")
	private String align;
	/**
	 * 元数据编号，针对多字段排序的情况 false
	 */
	@ApiModelProperty("元数据编号，针对多字段排序的情况")
	private Integer sortNo;

	@ApiModelProperty("宽度")
	private Integer width;

	/**
	 * 所属租户id
	 */
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
