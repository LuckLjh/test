package com.cescloud.saas.archive.api.modular.archivetype.dto;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedMetadata;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * 字段值设值
 */
@ApiModel("字段值设值")
@Data
@EqualsAndHashCode(callSuper = true)
public class AutovalueDTO extends DefinedMetadata {

	private static final long serialVersionUID = -2239037282537519002L;

	@ApiModelProperty("数据规则id")
	private Long id;

	@ApiModelProperty("元字段数据类型")
	private String metadataType;

	@ApiModelProperty("档案层级存储表")
	private String storageLocate;

	/**
	 * 元数据id
	 */
	@ApiModelProperty("元数据id")
	private Long metadataId;

	/**
	 * 类型：0、累加，1、档案字段组成拼接
	 */
	@ApiModelProperty("类型：0、累加，1、档案字段组成拼接")
	private Integer type;

	/**
	 * 补零标识：1、补零   0、不补零
	 */
	@Range(min = 0, max = 9, message = "最大支持补零到9位")
	@ApiModelProperty("补零标识：1、补零   0、不补零")
	private Integer flagZero;

	@ApiModelProperty("模块id")
	private Long moduleId;
	/**
	 * 勾选的排序字段id
	 */
	@ApiModelProperty(value = "勾选的排序字段id",required = true)
	private List<Long> sortFieldIds;
	/**
	 * 排序标识:ASC:代表升序 DESC：代表降序 false
	 */
	@ApiModelProperty("排序标识")
	private String sortSign;
	/**
	 * 勾选的分组字段id
	 */
	@ApiModelProperty(value = "勾选的分组字段id",required = true)
	private List<Long> groupFieldIds;

	/**
	 * 拼接设置组成字段集合
	 */
	@ApiModelProperty("拼接设置组成字段集合")
	private List<DefinedColumnRuleMetadata> splicingFields;

	/**
	 * 页数页号规则详细设置的值
	 *
	 * 值：1.根据页数（8）生成页号（1-8）
	 * 值：2-（生成例子与规则）
	 * 		2.1根据页号（1-3）生成页数（3）
	 * 		2.2根据页号（4-5）生成页数（2）
	 * 		2.3、 、
	 *  	2.4根据页号（6-10）生成页数（5）
	 */
	@ApiModelProperty("设置页数页号规则详细设置的值")
	private Integer pageOrPageNoRule;
}
