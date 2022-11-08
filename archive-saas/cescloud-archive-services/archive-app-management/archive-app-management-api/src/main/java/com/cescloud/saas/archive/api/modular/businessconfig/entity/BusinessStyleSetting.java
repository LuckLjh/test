package com.cescloud.saas.archive.api.modular.businessconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author liwei
 */
@ApiModel("业务表单定义")
@TableName("apma_business_style_setting")
//@KeySequence("SEQ_APMA_BUSI_STYLE_SETTING")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessStyleSetting extends Model<BusinessStyleSetting> {
	private static final long serialVersionUID = 4685228930453827853L;

	@TableId
	@ApiModelProperty("主键id")
	private Long id;

	/**
	 * 模板类型
	 */
	@NotNull(message = "模板类型不能为空")
	@ApiModelProperty("模板类型:（1：利用表单,2、鉴定表单）")
	private Integer modelType;

	/**
	 * 表单定义内容 false
	 */
	@NotNull(message = "表单定义内容不能为空")
	@ApiModelProperty("表单定义内容")
	private Object formContent;

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
