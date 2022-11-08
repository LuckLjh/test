package com.cescloud.saas.archive.api.modular.metadata.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 基础元数据标签DTO
 * @author ldong
 */
@Data
@EqualsAndHashCode
public class MetadataBaseLayerDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 元数据标签id,主键 false
	 */
	@ApiModelProperty("基础元数据标签id,主键")
	private Long id;

	/**
	 * 档案门类分类标识（ws、kj、kj等） false
	 */
	@ApiModelProperty("档案门类分类标识")
	@NotBlank(message = "档案门类分类标识不能为空")
	private String classType;

	/**
	 * 组卷标识，1:案卷、卷内 2: 一文一件 3:项目、案卷、卷内 4:单套制
	 */
	@ApiModelProperty("组卷标识，1:案卷、卷内 2: 一文一件 3:项目、案卷、卷内 4:单套制")
	@NotNull(message = "组卷标识不能为空")
	private Integer filingType;

	/**
	 * 元数据标签中文名称 false
	 */
	@ApiModelProperty("基础元数据标签中文名称")
	@NotBlank(message = "字段中文不能为空")
	private String metadataChinese;
	/**
	 * 元数据标签英文名称 false
	 */
	@ApiModelProperty("基础元数据标签英文名称")
	@NotBlank(message = "字段英文不能为空")
	private String metadataEnglish;

	/**
	 * 元数据业务系统标识（1：系统字段，0：非系统字段） false
	 */
	@ApiModelProperty("元数据业务系统标识（1：系统字段，0：非系统字段）")
	private Integer metadataSys;

	/**
	 * 元数据标签类型 false
	 */
	@ApiModelProperty("基础元数据标签类型")
	@NotBlank(message = "字段类型不能为空")
	private String metadataType;
	/**
	 * 元数据标签字段长度 false
	 */
	@ApiModelProperty("基础元数据标签字段长度")
	@NotNull(message = "字段长度不能为空")
	@Min(value = 0, message = "字段长度最小为0")
	private Integer metadataLength;
	/**
	 * 元数据标签小数位数 false
	 */
	@ApiModelProperty("基础元数据标签小数位数")
	private Integer metadataDotLength;
	/**
	 * 元数据标签是否为空 false
	 */
	@ApiModelProperty("基础元数据标签是否为空")
	private Integer metadataNull;
	/**
	 * 元数据标签默认值 false
	 */
	@ApiModelProperty("基础元数据标签默认值")
	private String metadataDefaultValue;
	/**
	 * 元数据标签排序编号 false
	 */
	@ApiModelProperty("基础元数据标签排序编号")
	private Integer metadataSort;
	/**
	 * 元数据标签描述信息 false
	 */
	@ApiModelProperty("基础元数据标签描述信息")
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
	@NotNull(message = "存在项目级不能为空")
	private Boolean inProject;
	/**
	 * 是否存在案卷级 false
	 */
	@ApiModelProperty("是否存在案卷级")
	@NotNull(message = "存在案卷级不能为空")
	private Boolean inFolder;
	/**
	 * 是否存在文件级 false
	 */
	@ApiModelProperty("是否存在文件级")
	@NotNull(message = "存在文件级不能为空")
	private Boolean inFile;

	/**
	 * 是否存在一文一件级 false
	 */
	@ApiModelProperty("是否存在一文一件级")
	@NotNull(message = "存在一文一件不能为空")
	private Boolean inOne;

	/**
	 * 是否存在单套制 false
	 */
	@ApiModelProperty("是否存在单套制")
	@NotNull(message = "存在单套制不能为空")
	private Boolean inSingle;

	/**
	 * 是否存在全文级 false
	 */
	@ApiModelProperty("是否存在全文级")
	@NotNull(message = "存在全文级不能为空")
	private Boolean inDocument;

	/**
	 * 检索关键字 false
	 */
	@ApiModelProperty(value = "检索关键字",example = "检索关键字")
	private String keyword;
}
