package com.cescloud.saas.archive.api.modular.archivetype.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveColumnRule;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 元数据字段自动赋值
 *
 * @author liwei
 * @date 2019-04-15 15:16:12
 */
@ApiModel("元数据字段自动赋值实体")
@Data
@TableName("apma_metadata_autovalue")
//@KeySequence("SEQ_APMA_METADATA_AUTOVALUE")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataAutovalue extends Model<MetadataAutovalue> {

	private static final long serialVersionUID = -6786174461628254688L;

	/**
	 * 自动生成元数据id,主键 false
	 */
	@TableId
	@ApiModelProperty("主键id")
	private Long id;

	@ApiModelProperty("类型：0、累加，1、档案字段组成拼接，2、当前日期")
	private Integer type;
	/**
	 * 档案层级存储表 false
	 */
	@ApiModelProperty("档案层级存储表")
	private String storageLocate;

	/**
	 * 元数据id
	 */
	@ApiModelProperty("元数据id")
	private Long metadataId;
	/**
	 * 补零标识， >0:代表补零,具体的数值代表补零位数，如果是4，则补零到4位
	 *           0：代表不补零 false
	 */
	@ApiModelProperty("补零标识，1:代表补零 0：代表不补零")
	private Integer flagZero;
	/**
	 * 排序元数据ID
	 */
	@ApiModelProperty("排序元数据ID")
	private String sortMetadataIds;
	/**
	 * 排序标识:ASC:代表升序 DESC：代表降序 false
	 */
	@ApiModelProperty("排序标识")
	private String sortSign;

	@ApiModelProperty("模块id")
	private Long moduleId;

	/**
	 * 所属租户ID true
	 */
	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty("所属租户id")
	private Long tenantId;
	/**
	 * 乐观锁 true
	 */
	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty("乐观锁")
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
	private String metadataEnglish;

	/**
	 * 累加规则
	 */
	@ApiModelProperty("累加规则")
	@TableField(exist = false)
	private List<MetadataSource> metadataSources;

	/**
	 * 档案字段组成规则
	 */
	@ApiModelProperty("档案字段组成规则")
	@TableField(exist = false)
	private List<ArchiveColumnRule> archiveColumnRules;

	/**
	 * 页数页号规则详细设置的值
	 *
	 * 值：1.根据页数（8）生成页号（1-8）
	 * 值：2-（生成例子与规则）
	 * 		2.1根据页号（1-3）生成页数（3）
	 * 		2.2根据页号（4-5）生成页数（2）
	 * 		2.3、 、
	 *  	2.4根据页号（6-10）生成页数（5）
	 */
	@ApiModelProperty("设置页数页号规则详细设置的值")
	private Integer pageOrPageNoRule;
}
