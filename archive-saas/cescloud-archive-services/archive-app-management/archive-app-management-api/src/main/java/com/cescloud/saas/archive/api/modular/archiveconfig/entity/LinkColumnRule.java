package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 挂接字段组成规则
 *
 * @author liudong1
 * @date 2019-05-14 11:15:33
 */
@ApiModel("挂接字段组成规则")
@Data
@TableName("apma_config_link_column_rule")
//@KeySequence("SEQ_APMA_LINK_COLUMN_RULE")
@EqualsAndHashCode(callSuper = true)
public class LinkColumnRule extends Model<LinkColumnRule> {

	private static final long serialVersionUID = 1L;

	/**
	 * 挂接字段组成规则id,主键 false
	 */
	@ApiModelProperty("字段规则id")
	@TableId
	private Long id;
	/**
	 * 档案类型层级表 false
	 */
	@ApiModelProperty("档案层级存储表名")
	private String storageLocate;
	/**
	 * 挂接层次ID
	 */
	@ApiModelProperty("挂接层次ID")
	private Long linkLayerId;
	/**
	 * 元数据ID false
	 */
	@ApiModelProperty("组成元数据ID")
	private Long metadataId;
	/**
	 * 补零标识：1、补零   0、不补零
	 */
	@ApiModelProperty("补零标识：1、补零   0、不补零")
	private Integer zeroFlag;
	/**
	 * 元数据分组排序标识 false
	 */
	@ApiModelProperty("组成规则字段顺序编号")
	private Integer sortNo;
	/**
	 * 连接字符 false
	 */
	@ApiModelProperty("连接字符")
	private String connectStr;
	/**
	 * 连接标识: M:代表元数据字段 C:代表连接符 false
	 */
	@ApiModelProperty("连接标识: M:代表元数据字段 C:代表连接符")
	private String connectSign;
	/**
	 * 数据字典：0、存KEY  1、存值 false
	 */
	@ApiModelProperty("数据字典：0、存KEY  1、存值")
	private Integer dictKeyValue;
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
