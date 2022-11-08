
package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据值操作规则实体
 *
 * @author liudong1
 * @date 2019-04-21 19:30:10
 */
@ApiModel("数据值操作规则实体")
@Data
@TableName("apma_config_archive_operate")
//@KeySequence("SEQ_APMA_ARCHIVE_OPERATE")
@EqualsAndHashCode(callSuper = true)
public class ArchiveOperate extends Model<ArchiveOperate> {
	private static final long serialVersionUID = 1L;

	/**
	 * 数据值操作规则id,主键 false
	 */
    @ApiModelProperty("数据值操作规则id")
	@TableId
	private Long id;
	/**
	 * 关联业务主键 false
	 */
	@ApiModelProperty("关联业务主键")
	private Long businessId;
	/**
	 * 业务类型（column_rule元数据；batch_link挂接） false
	 */
	@ApiModelProperty("业务类型（column_rule元数据；batch_link挂接）")
	private String businessType;
	/**
	 * 档案层级存储表 false
	 */
	@ApiModelProperty("档案层级存储表")
	private String storageLocate;
	/**
	 * 操作方式：R、代表操作  D、代表删除 false
	 */
	@ApiModelProperty("操作方式：R、代表操作  D、代表删除")
	private String operateType;
	/**
	 * 待操作值或待删除值 false
	 */
	@ApiModelProperty("待操作值或待删除值")
	private String valueOne;
	/**
	 * 替换值 false
	 */
	@ApiModelProperty("替换值")
	private String valueTwo;
	/**
	 * 排序编号
	 */
	@ApiModelProperty("排序编号")
	private Integer sortNo;
	/**
	 * 扩展属性，可自定义，标明属于哪个业务、哪个模块以及哪个功能的元数据值操作或删除操作 false
	 */
	@ApiModelProperty("扩展属性")
	private String operateFunction;
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
