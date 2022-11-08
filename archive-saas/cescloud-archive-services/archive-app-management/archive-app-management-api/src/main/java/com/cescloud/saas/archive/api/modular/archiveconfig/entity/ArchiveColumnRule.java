
package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 档案字段组成规则
 *
 * @author liudong1
 * @date 2019-04-19 15:06:53
 */
@ApiModel("档案字段组成规则")
@Data
@TableName("apma_archive_column_rule")
//@KeySequence("SEQ_APMA_ARCHIVE_COLUMN_RULE")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveColumnRule extends Model<ArchiveColumnRule> {
	private static final long serialVersionUID = 1L;

	/**
	 * 字段拼接规则id,主键 false
	 */
	@ApiModelProperty("字段拼接规则id")
	@TableId
	private Long id;
	/**
	 * 档案层级存储表名 false
	 */
	@ApiModelProperty("档案层级存储表名")
	private String storageLocate;
	/**
	 * 原元数据英文名 false
	 */
	@ApiModelProperty("数据规则所属id")
	private Long metadataSourceId;
	/**
	 * 组成元数据ID false
	 */
	@ApiModelProperty("组成元数据ID")
	private Long metadataId;
	/**
	 * 补零标识：1、补零   0、不补零 false
	 */
	@ApiModelProperty("补零标识：1、补零   0、不补零")
	private Integer zeroFlag;

	/**
	 * 补零位数 false
	 * 如果字段规则设置了补零标识 1，就要设置补零位数
	 */
	@ApiModelProperty("补零位数")
	private Integer digitZero;

	/**
	 * 上一层级标识 false
	 */
	@ApiModelProperty("上一层级标识 : 1:上一层级 0：本层级")
	private Integer upperLevel;

	/**
	 * 数据字典：0、存KEY  1、存值
	 */
	@ApiModelProperty("数据字典：0、存KEY  1、存值")
	private Integer dictKeyValue;

	/**
	 * 组成规则字段顺序编号 false
	 */
	@ApiModelProperty("组成规则字段顺序编号")
	private Integer sortNo;
	/**
	 * 连接标识: M:代表元数据字段 C:代表连接符 false
	 */
	@ApiModelProperty("连接标识: M:代表元数据字段 C:代表连接符")
	private String connectSign;

	/**
	 * 连接字符
	 */
	@ApiModelProperty("连接字符")
	private String connectStr;

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

	/**
	 * 元数据英文名称 false
	 */
	@ApiModelProperty("元数据英文名称")
	@TableField(exist = false)
	private String metadataEnglish;


	/**
	 * 元数据中文名称 false
	 */
	@ApiModelProperty("元数据中文名称")
	@TableField(exist = false)
	private String metadataChinese;

	/**
	 * 字典值 false
	 * 如果字段规则设置了只显示编码，就要做编码转换
	 */
	@ApiModelProperty("字典值")
	@TableField(exist = false)
	private String dictCode;



}
