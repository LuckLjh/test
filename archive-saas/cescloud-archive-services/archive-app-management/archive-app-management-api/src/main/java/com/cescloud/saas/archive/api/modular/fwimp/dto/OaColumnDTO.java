package com.cescloud.saas.archive.api.modular.fwimp.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * huangyuquan
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaColumnDTO implements Serializable {

	/**
	 * ID,主键
	 */
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
	@ApiModelProperty(value = "OA字段名", required = true,example = "流程类型")
	private String oaName;

	/**
	 * 字段名
	 */
	@ApiModelProperty(value = "字段名", required = true,example = "流程类型")
	private String columnName;

	/**
	 * 档案字段名
	 */
	@ApiModelProperty(value = "档案字段名", required = true,example = "流程类型")
	private String name;

	/**
	 * 默认值
	 */
	@ApiModelProperty(value = "默认值", required = true,example = "流程类型")
	private String fixedValue;

	/**
	 * 前缀
	 */
	@ApiModelProperty(value = "前缀", required = true,example = "流程类型")
	private String prefixion;

	/**
	 * 后缀
	 */
	@ApiModelProperty(value = "后缀", required = true,example = "流程类型")
	private String suffixion;

	/**
	 * 截取
	 */
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
