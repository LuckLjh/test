package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 档案录入配置
 *
 * @author liudong1
 * @date 2019-04-18 16:06:51
 */
@ApiModel("录入配置")
@Data
@TableName("apma_config_archive_edit")
//@KeySequence("SEQ_APMA_ARCHIVE_EDIT")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveEdit extends Model<ArchiveEdit> {
	private static final long serialVersionUID = 1L;

	/**
	 * 编辑数据id,主键 false
	 */
	@ApiModelProperty("录入数据id")
	@TableId
	private Long id;
	/**
	 * 档案表名
	 */
	@ApiModelProperty("档案表名")
	private String storageLocate;
	/**
	 * 元数据ID
	 */
	@ApiModelProperty("元数据ID")
	private Long metadataId;
	/**
	 * 排序编号，针对多字段排序的情况 false
	 */
	@ApiModelProperty("录入排序编号")
	private Integer sortNo;

	@ApiModelProperty("模块id")
	private Long moduleId;
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
