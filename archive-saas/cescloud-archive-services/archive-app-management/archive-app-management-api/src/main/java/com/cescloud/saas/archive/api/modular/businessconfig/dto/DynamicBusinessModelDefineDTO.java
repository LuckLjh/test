package com.cescloud.saas.archive.api.modular.businessconfig.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DynamicModelDefine 加了 BusinessModelDefine 基础字段，用于更新用（缝合怪）
 * @ClassName DynamicBusinessModelDefineDTO
 * @Author 王谷华
 * @Date 2021/04/01
 *
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynamicBusinessModelDefineDTO implements Serializable {

	@ApiModelProperty(value = "主键id")
	private Long id;

	@ApiModelProperty(value = "模板类型", required = true, example = "1")
	@NotNull(message = "模板类型 不能为空")
	private Integer modelType;

	/**
	 * 模板code model_code
	 */
	@ApiModelProperty("模板code")
	private String modelCode;
	/**
	 * 业务模板定义ID defined_id
	 */
	@ApiModelProperty("业务模板定义ID")
	private Long definedId;
	/**
	 * 租户id tenant_id
	 */
	@ApiModelProperty("租户id")
	@TableField(fill = FieldFill.INSERT)
	private Long tenantId;


	/**
	 * 全宗号 fonds_code
	 */
	@ApiModelProperty("全宗号")
	private String fondsCode;

	@ApiModelProperty("字段中文名称")
	private String metadataChinese;

	@ApiModelProperty("字段英文名称")
	private String metadataEnglish;

	/**
	 * 字段英文隐藏名称防止重复 false
	 */
	@ApiModelProperty("字段英文隐藏名称")
	private String metadataEnglishHidden;

	/**
	 * 字段英文名称编号 false
	 */
	@ApiModelProperty("字段英文名称编号")
	private Integer metadataEnglishNo;

	@ApiModelProperty("字段类型")
	private String metadataType;

	@ApiModelProperty("字段长度")
	private Integer metadataLength;

	@ApiModelProperty("小数点位数")
	private Integer metadataDotLength;

	@ApiModelProperty("字典项编码")
	private  String dictCode;

	/**
	 * 字段描述信息 false
	 */
	@ApiModelProperty("字段描述信息")
	private String metadataDescription;

	@ApiModelProperty("排序号")
	private Integer metadataSort;

	/**
	 * 元数据是否为空 false
	 */
	@ApiModelProperty("是否为空")
	private Integer metadataNull;

	/**
	 * 默认值
	 */
	@ApiModelProperty("默认值")
	private String metadataDefaultValue;

	@ApiModelProperty("是否可编辑（0：不可编辑，1：可编辑）")
	private Integer isEdit;

	@ApiModelProperty("是否必填项(0:非必填，1：必填)")
	private Integer isRequired;

	@ApiModelProperty("是否显示（0：不显示，1：显示）")
	private Integer isShow;

	@ApiModelProperty("是否条件字段（0：否，1：是）")
	private Integer isFilter;

	@ApiModelProperty("前台列表是否不可见字段（0：否，1：是）")
	private Integer isSys;

	@ApiModelProperty("是否部门字段（0：否，1：是）")
	private Integer isDept;

	@ApiModelProperty("元数据业务系统标识（1：系统字段，0：业务字段）")
	private Integer metadataSys;


	/**
	 * 乐观锁,数据版本号
	 */
	@ApiModelProperty("乐观锁")
	@Version
	private Long revision;

}
