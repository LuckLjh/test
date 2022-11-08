package com.cescloud.saas.archive.api.modular.onlinefiling.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.Size;

/**
 *
 *
 */
@Data
@TableName("online_filing")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnlineFiling extends Model<OnlineFiling> {
	private static final long serialVersionUID = 1L;

	/**
	 * ID,主键
	 */
	@TableId
	@ApiModelProperty(value = "ID", example = "1")
	private Long id;

	/**
	 * 批次号
	 */
	@Size(max = 200, message = "批次号长度过长(不得超过200个字符)")
	@ApiModelProperty(value = "批次号",example = "0")
	private String batchNo;

	/**
	 * 档案门类名称（对应archive_type）
	 */
	@Size(max = 200, message = "档案门类名称长度过长(不得超过200个字符)")
	@ApiModelProperty(value = "档案门类名称",example = "0")
	private String archiveTypeName;

	/**
	 * 档案门类编码
	 */
	@Size(max = 50, message = "档案门类名称长度过长(不得超过50个字符)")
	@ApiModelProperty(value = "档案门类编码",example = "0")
	private String archiveTypeCode;

	/**
	 * 成功数量
	 */
	@Size(max = 10, message = "成功数量名称长度过长(不得超过10个字符)")
	@ApiModelProperty(value = "成功数量",example = "0")
	private int successNum;

	/**
	 * 失败数量
	 */
	@Size(max = 10, message = "失败数量名称长度过长(不得超过10个字符)")
	@ApiModelProperty(value = "失败数量",example = "0")
	private int failedNum;

	/**
	 * 结果编码
	 */
	@Size(max = 10, message = "结果编码名称长度过长(不得超过10个字符)")
	@ApiModelProperty(value = "结果编码",example = "0")
	private int code;

	/**
	 * 异常原因
	 */
	@Size(max = 2000, message = "档案门类名称长度过长(不得超过2000个字符)")
	@ApiModelProperty(value = "异常原因",example = "0")
	private String msg;

	/**
	 * 详细数据
	 */
	@ApiModelProperty(value = "详细数据",example = "0")
	private String data;

	/**
	 * 租户ID
	 */
	@ApiModelProperty(value = "租户ID", hidden = true)
	private Long tenantId;
}
