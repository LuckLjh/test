package com.cescloud.saas.archive.api.modular.fwimp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * oa导入列
 *
 * @author huangyuquan
 * @date 2021-03-26 12:04:54
 */
@Data
@TableName("oa_column")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaColumn extends Model<OaColumn> {
	private static final long serialVersionUID = 1L;

	/**
	 * ID,主键
	 */
	@TableId
	@ApiModelProperty(value = "ID", example = "1")
	private Long id;

	/**
	 * owner_ID,外键
	 * 主表条目 id  OA_IMPORT.ID = OA_COLUMN.OWNER_ID
	 * OA_IMPORT.ID = OA_LOG.OWNER_ID
	 */
	@ApiModelProperty(value = "ownerId", example = "1")
	private Long ownerId;

	/**
	 * OA字段名
	 */
	@NotBlank(message = "")
	@Size(max = 50, message = "OA字段名长度过长(不得超过50个中文字符)")
	@ApiModelProperty(value = "OA字段名", required = true,example = "流程类型")
	private String oaName;

	/**
	 * 字段名
	 */
	@NotBlank(message = "")
	@Size(max = 25, message = "字段名长度过长(不得超过50个中文字符)")
	@ApiModelProperty(value = "字段名", required = true,example = "流程类型")
	private String columnName;

	/**
	 * 档案字段名
	 */
	@NotBlank(message = "")
	@Size(max = 50, message = "档案字段名长度过长(不得超过50个中文字符)")
	@ApiModelProperty(value = "档案字段名", required = true,example = "流程类型")
	private String name;

	/**
	 * 默认值
	 */
	@NotBlank(message = "")
	@Size(max = 25, message = "默认值长度过长(不得超过50个中文字符)")
	@ApiModelProperty(value = "默认值", required = true,example = "流程类型")
	private String fixedValue;

	/**
	 * 前缀
	 */
	@NotBlank(message = "")
	@Size(max = 25, message = "前缀长度过长(不得超过25个字符)")
	@ApiModelProperty(value = "前缀", required = true,example = "流程类型")
	private String prefixion;

	/**
	 * 后缀
	 */
	@NotBlank(message = "")
	@Size(max = 25, message = "后缀长度过长(不得超过25个字符)")
	@ApiModelProperty(value = "后缀", required = true,example = "流程类型")
	private String suffixion;

	/**
	 * 截取
	 */
	@NotBlank(message = "")
	@Size(max = 25, message = "后缀长度过长(不得超过25个字符)")
	@ApiModelProperty(value = "截取", required = true,example = "截取")
	private String interception;

	/**
	 * owner_flowId,所属流程id
	 */
	@ApiModelProperty(value = "owner_flow_Id", example = "1")
	private Long ownerFlowId;

	/**
	 * 元数据类型 false
	 */
	@ApiModelProperty("元数据类型")
	@NotBlank(message = "字段类型不能为空")
	private String metadataType;

	/**
	 * 元数据字段长度 false
	 */
	@ApiModelProperty("元数据字段长度")
	@NotNull(message = "字段长度不能为空")
	private Integer metadataLength;

	/**
	 * 绑定的数据字典code
	 */
	@ApiModelProperty("绑定的数据字典code")
	private String dictCode;

	/**
	 * 是否配置过列详细
	 */
	@ApiModelProperty("是否配置过列详细")
	private String isContain;
}

