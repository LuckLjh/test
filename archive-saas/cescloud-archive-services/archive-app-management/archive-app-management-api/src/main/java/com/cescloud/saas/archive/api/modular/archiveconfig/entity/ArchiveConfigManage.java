package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @ClassName ArchiveConfigManage
 * @Author zhangxuehu
 * @Date 2020/5/8 10:26
 **/
@ApiModel("档案门类配置信息")
@Data
@TableName("apma_archive_config_manage")
//@KeySequence("SEQ_APMA_ARCHIVE_CONFIG_MANAGE")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveConfigManage extends Model<ArchiveConfigManage> {

	/**
	 * 列表数据id,主键 false
	 */
	@ApiModelProperty("列表数据id")
	@TableId
	private Long id;

	/**
	 * 档案表名
	 */
	@ApiModelProperty("档案表名")
	private String storageLocate;

	@ApiModelProperty("模块id")
	private Long moduleId;

	@ApiModelProperty("是否定义（1是 0否）")
	private Integer isDefine;

	@ApiModelProperty("定义类型")
	private Integer typedef;

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
