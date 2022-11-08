package com.cescloud.saas.archive.api.modular.archivedict.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 基础数据字典值
 *
 * @author liudong1
 * @date 2019-09-14 19:33:15
 */
@Data
@TableName("apma_dict_item_base")
//@KeySequence("SEQ_APMA_DICT_ITEM_BASE")
@EqualsAndHashCode(callSuper = true)
@ApiModel("数据字典值")
public class DictItemBase extends Model<DictItemBase> {

	private static final long serialVersionUID = 1L;

	/**
	 * 字典值ID false
	 */
	@TableId
	@ApiModelProperty("字典值ID")
	private Long id;
	/**
	 * 字典项编码 false
	 */
	@ApiModelProperty("字典项编码")
	private String dictCode;
	/**
	 * 字典值编码 false
	 */
	@ApiModelProperty("字典值编码")
	private String itemCode;
	/**
	 * 字典值标签 false
	 */
	@ApiModelProperty("字典值标签")
	private String itemLabel;
	/**
	 * 字典值的具体值 false
	 */
	@ApiModelProperty("字典值的具体值")
	private String itemValue;
	/**
	 * 字典项描述 false
	 */
	@ApiModelProperty("字典项描述")
	private String itemDescribe;
	/**
	 * 排序 false
	 */
	@ApiModelProperty("排序")
	private Integer sortNo;
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
