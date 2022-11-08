package com.cescloud.saas.archive.api.modular.metadata.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode
public class MetadataTagLayerDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 元数据标签id,主键 false
	 */
	@ApiModelProperty("元数据标签id,主键")
	private Long id;
	/**
	 * 元数据标签中文名称 false
	 */
	@ApiModelProperty("元数据标签中文名称")
	private String metadataChinese;
	/**
	 * 元数据标签英文名称 false
	 */
	@ApiModelProperty("元数据标签英文名称")
	private String metadataEnglish;
	/**
	 * 元数据标签类型 false
	 */
	@ApiModelProperty("元数据标签类型")
	private String metadataType;
	/**
	 * 元数据标签字段长度 false
	 */
	@ApiModelProperty("元数据标签字段长度")
	private Integer metadataLength;
	/**
	 * 元数据标签小数位数 false
	 */
	@ApiModelProperty("元数据标签小数位数")
	private Integer metadataDotLength;
	/**
	 * 元数据标签是否为空 false
	 */
	@ApiModelProperty("元数据标签是否为空")
	private Integer metadataNull;
	/**
	 * 元数据标签默认值 false
	 */
	@ApiModelProperty("元数据标签默认值")
	private String metadataDefaultValue;
	/**
	 * 元数据标签排序编号 false
	 */
	@ApiModelProperty("元数据标签排序编号")
	private Integer metadataSort;
	/**
	 * 元数据标签描述信息 false
	 */
	@ApiModelProperty("元数据标签描述信息")
	private String metadataDescription;
	/**
	 * 绑定的数据字典code false
	 */
	@ApiModelProperty("绑定的数据字典code")
	private String dictCode;
	/**
	 * 是否存在项目级 false
	 */
	@ApiModelProperty("是否存在项目级")
	private Boolean inProject;
	/**
	 * 是否存在案卷级 false
	 */
	@ApiModelProperty("是否存在案卷级")
	private Boolean inFolder;
	/**
	 * 是否存在文件级 false
	 */
	@ApiModelProperty("是否存在文件级")
	private Boolean inFile;
	/**
	 * 是否存在全文级 false
	 */
	@ApiModelProperty("是否存在全文级")
	private Boolean inDocument;

	/**
	 * 所属租户id
	 */
	@ApiModelProperty(name = "所属租户id",hidden = true)
	private Long tenantId;
	/**
	 * 检索关键字 false
	 */
	@ApiModelProperty(value = "检索关键字",example = "检索关键字")
	private String keyword;
}
