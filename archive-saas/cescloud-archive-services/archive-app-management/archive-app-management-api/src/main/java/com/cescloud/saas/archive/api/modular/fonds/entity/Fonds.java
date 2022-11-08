package com.cescloud.saas.archive.api.modular.fonds.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 全宗
 *
 * @author zhangpeng
 * @date 2019-03-21 12:04:54
 */
@Data
@TableName("apma_fonds")
//@KeySequence("SEQ_APMA_FONDS")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Fonds extends Model<Fonds> {
	private static final long serialVersionUID = 1L;

	/**
	 * 全宗ID,主键 false
	 */
	@TableId
	@ApiModelProperty(value = "全宗主键(按需使用)", example = "1")
	private Long fondsId;
	/**
	 * 全宗号 false
	 */
	@NotBlank(message = "全宗代码不能为空")
	@Size(max = 50, message = "全宗编码长度过长(不得超过100个英文字符)")
	@ApiModelProperty(value = "全宗代码", required = true,example = "QZGL")
	private String fondsCode;
	/**
	 * 全宗名称 false
	 */
	@NotBlank(message = "全宗名称不能为空")
	@Size(max = 50, message = "全宗名称长度过长(不得超过50个中文字或100个英文字符)")
	@ApiModelProperty(value = "全宗名称", required = true,example = "全宗名称")
	private String fondsName;
	/**
	 * 描述信息 false
	 */
	@Size(max = 200, message = "全宗描述长度过长(不得超过100个中文字符或200个英文字符)")
	@ApiModelProperty(value = "描述",example = "描述")
	private String description;
	/**
	 * 所属租户id false
	 */
	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty(hidden = true)
	private Long tenantId;
	/**
	 * 乐观锁,数据版本号 true
	 */
	@TableField(fill = FieldFill.INSERT)
	@Version
	@ApiModelProperty(value = "乐观锁标识", hidden = true)
	private Long revision;
	/**
	 * 创建人 true
	 */
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value ="创建人id",hidden=true)
	private Long createdBy;
	/**
	 * 创建时间 true
	 */
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value ="创建时间",hidden = true)
	private LocalDateTime createdTime;
	/**
	 * 更新人 true
	 */
	@TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty(value = "更新人id",hidden = true)
	private Long updatedBy;
	/**
	 * 更新时间 true
	 */
	@TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty(value = "更新时间",hidden = true)
	private LocalDateTime updatedTime;


}
