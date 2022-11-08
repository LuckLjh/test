package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author LS
 * @date 2021/12/7
 */
@ApiModel("分页模式配置")
@Data
@TableName("apma_config_page_mode")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchivePageModeConfig implements Serializable {

	private static final long serialVersionUID = 3640848263375671519L;

	@ApiModelProperty("主键id")
	@TableId
	private Long id;

	@ApiModelProperty("档案表名")
	private String storageLocate;

	@ApiModelProperty("模块id")
	private Long moduleId;

	@ApiModelProperty("分页方式")
	private Integer pageMode;

	@ApiModelProperty("所属租户id")
	@TableField(fill = FieldFill.INSERT)
	private Long tenantId;

	@ApiModelProperty("创建人")
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	private Long createdBy;

	@ApiModelProperty("创建时间")
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	private LocalDateTime createdTime;

	@ApiModelProperty("更新人")
	@TableField(fill = FieldFill.UPDATE)
	private Long updatedBy;

	@ApiModelProperty("更新时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updatedTime;
}
