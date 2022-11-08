package com.cescloud.saas.archive.api.modular.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 元数据标签
 *
 * @author liudong1
 * @date 2019-05-23 15:37:43
 */
@Data
@TableName("apma_metadata_tag")
//@KeySequence("SEQ_APMA_METADATA_TAG")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MetadataTag extends Model<MetadataTag> {

	private static final long serialVersionUID = 1L;

	/**
	 * 元数据标签id,主键 false
	 */
	@ApiModelProperty("元数据标签id,主键")
	@TableId
	private Long id;
	/**
	 * 元数据标签中文名称 false
	 */
	@ApiModelProperty("元数据标签中文名称")
	private String tagChinese;
	/**
	 * 元数据标签英文名称 false
	 */
	@ApiModelProperty("元数据标签英文名称")
	private String tagEnglish;
	/**
	 * 元数据标签类型 false
	 */
	@ApiModelProperty("元数据标签类型")
	private String tagType;
	/**
	 * 元数据标签字段长度 false
	 */
	@ApiModelProperty("元数据标签字段长度")
	private Integer tagLength;
	/**
	 * 元数据标签小数位数 false
	 */
	@ApiModelProperty("元数据标签小数位数")
	private Integer tagDotLength;
	/**
	 * 元数据标签是否为空 false
	 */
	@ApiModelProperty("元数据标签是否为空")
	private Integer tagNull;
	/**
	 * 元数据标签默认值 false
	 */
	@ApiModelProperty("元数据标签默认值")
	private String tagDefaultValue;
	/**
	 * 元数据标签排序编号 false
	 */
	@ApiModelProperty("元数据标签排序编号")
	private Integer tagSort;
	/**
	 * 元数据标签描述信息 false
	 */
	@ApiModelProperty("元数据标签描述信息")
	private String tagDescription;
	/**
	 * 绑定的数据字典code false
	 */
	@ApiModelProperty("绑定的数据字典code")
	private String dictCode;
	/**
	 * 所属租户id
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
