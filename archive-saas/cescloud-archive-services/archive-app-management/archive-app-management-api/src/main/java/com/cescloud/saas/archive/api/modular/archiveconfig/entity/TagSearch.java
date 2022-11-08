
package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 标签检索配置
 *
 * @author liudong1
 * @date 2019-05-27 15:55:01
 */
@Data
@TableName("apma_config_tag_search")
//@KeySequence("SEQ_APMA_TAG_SEARCH")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagSearch extends Model<TagSearch> {
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
	 * 条件方式：= like >= false
	 */
	@ApiModelProperty("条件方式：= like >=")
	private String conditionType;
	/**
	 * 元数据编号，针对多字段排序的情况 false
	 */
	@ApiModelProperty("元数据编号，针对多字段排序的情况")
	private Integer sortNo;

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
