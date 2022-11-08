package com.cescloud.saas.archive.api.modular.syssetting.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 参数设置
 * @author liwei
 */
@Data
@TableName("apma_sys_setting")
//@KeySequence("SEQ_APMA_SYS_SETTING")
@EqualsAndHashCode(callSuper = true)
@ApiModel("参数设置实体")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysSetting extends Model<SysSetting> {

	private static final long serialVersionUID = 7863061487243347222L;

	/**
	 * id,主键 false
	 */
	@TableId
	@ApiModelProperty("参数配置项主键")
	private Long id;

	/**
	 * type,按钮类型
	 */
	@ApiModelProperty("按钮类型")
	private String type;

	/**
	 * 配置项编码
	 */
	@ApiModelProperty("配置项编码")
	private String code;

	/**
	 * 配置名称
	 */
	@ApiModelProperty("配置名称")
	private String name;

	/**
	 * 配置值
	 */
	@ApiModelProperty("配置值")
	private String value;

	/**
	 * 配置说明
	 */
	@ApiModelProperty("配置说明")
	private String remark;

	/**
	 * 所属租户id true
	 */
	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty("租户ID")
	private Long tenantId;

	/**
	 * 乐观锁 true
	 */
	@TableField(fill = FieldFill.INSERT)
	@Version
	@ApiModelProperty("乐观锁标识")
	private Long revision;

	/**
	 * 创建人 true
	 */
	@TableField(fill = FieldFill.INSERT)
	private Long createdBy;

	/**
	 * 创建时间 true
	 */
	@TableField(fill = FieldFill.INSERT)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime createdTime;

	/**
	 * 更新人 true
	 */
	@TableField(fill = FieldFill.UPDATE)
	private Long updatedBy;

	/**
	 * 更新时间 true
	 */
	@TableField(fill = FieldFill.UPDATE)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime updatedTime;

}
