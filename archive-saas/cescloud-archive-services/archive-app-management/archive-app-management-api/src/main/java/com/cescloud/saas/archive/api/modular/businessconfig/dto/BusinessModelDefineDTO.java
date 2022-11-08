package com.cescloud.saas.archive.api.modular.businessconfig.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ClassName BusinessModelDefineDTO
 * @Author zhangxuehu
 * @Date 2019/10/30 10:51 上午
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessModelDefineDTO implements Serializable {

	@ApiModelProperty(value = "主键id")
	private Long id;

	@ApiModelProperty(value = "模板类型（1：利用表单,3、销毁表单，5、鉴定表单，7、移交表单，9、归档表单，11、编研表单，13、保管表单）",required = true,example = "1")
	@NotNull(message = "模板类型 不能为空")
	private Integer modelType;

	@ApiModelProperty(value = "字段中文名称",required = true,example = "字段名")
	@NotBlank(message = "字段中文名称 不能为空")
	private String metadataChinese;

	@ApiModelProperty("字段英文名称")
	private String metadataEnglish;

	@ApiModelProperty(value = "字段类型",required = true,example = "varchar")
	@NotBlank(message = "字段类型 不能为空")
	private String metadataType;

	@ApiModelProperty(value = "字段长度",required = true,example = "200")
	private Integer metadataLength;

	@ApiModelProperty(value = "小数点位数",example = "2")
	private Integer metadataDotLength;

	@ApiModelProperty(value = "字典项编码",example = "D10")
	private  String dictCode;
	/**
	 * 字段描述信息 false
	 */
	@ApiModelProperty(value = "字段描述信息",example = "描述")
	private String metadataDescription;

	@ApiModelProperty("默认值")
	private String metadataDefaultValue;

	@ApiModelProperty("是否条件字段（0：否，1：是）")
	private Integer isFilter;

	@ApiModelProperty("是否部门字段（0：否，1：是）")
	private Integer isDept;

}
