
package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 查重设置
 *
 * @author liudong1
 * @date 2019-04-23 12:08:06
 */
@ApiModel("查重配置")
@Data
@TableName("apma_metadata_checkrepeat")
//@KeySequence("SEQ_APMA_METADATA_CHECKREPEAT")
@EqualsAndHashCode(callSuper = true)
public class MetadataCheckrepeat extends Model<MetadataCheckrepeat> {
	private static final long serialVersionUID = 1L;

	/**
	 * 查重规则id,主键 false
	 */
	@ApiModelProperty("查重规则id")
	@TableId
	private Long id;
	/**
	 * 存储表名 false
	 */
	@ApiModelProperty("存储表名")
	private String storageLocate;
	/**
	 * 元数据ID
	 */
	@ApiModelProperty("元数据ID")
	private Long metadataId;
	/**
	 * 查重字段是否分组：1：分组 0：不分组 false
	 */
	@ApiModelProperty("查重字段是否分组：1：分组 0：不分组")
	private Integer isGroup;
	/**
	 * 元数据编号，针对多字段排序的情况 false
	 */
	@ApiModelProperty("排序编号")
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
