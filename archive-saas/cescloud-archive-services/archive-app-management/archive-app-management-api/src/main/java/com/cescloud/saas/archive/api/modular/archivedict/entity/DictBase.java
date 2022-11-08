package com.cescloud.saas.archive.api.modular.archivedict.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 基础数据字典
 *
 * @author liudong1
 * @date 2019-04-25 16:10:20
 */
@Data
@TableName("apma_dict_base")
//@KeySequence("SEQ_APMA_DICT_BASE")
@EqualsAndHashCode(callSuper = true)
public class DictBase extends Model<DictBase> {
	private static final long serialVersionUID = 1L;

	/**
	 * 字典项ID false
	 */
	@ApiModelProperty("字典项ID")
	@TableId
	private Long id;

	/**
	 * 字典项KEY（用于前台获取）
	 */
	@ApiModelProperty("字典项KEY（用于前台获取）")
	private String dictKey;
	/**
	 * 字典项编码 false
	 */
	@ApiModelProperty("字典项编码")
	private String dictCode;
	/**
	 * 字典项标签 false
	 */
	@ApiModelProperty("字典项标签")
	private String dictLabel;
	/**
	 * 字典项描述 false
	 */
	@ApiModelProperty("字典项描述")
	private String dictDescribe;
	/**
	 * 字典项隐藏编码（防止重复） false
	 */
	@ApiModelProperty("字典项隐藏编码")
	private String dictCodeHidden;
	/**
	 * 字典项编码组成值（防止重复） false
	 */
	@ApiModelProperty("字典项编码组成值")
	private Integer dictCodeNo;
	/**
	 * 子典类别（0：系统字段，1：业务字段） false
	 */
	@ApiModelProperty("子典类别（0：系统字段，1：业务字段）")
	private Integer dictClass;

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
