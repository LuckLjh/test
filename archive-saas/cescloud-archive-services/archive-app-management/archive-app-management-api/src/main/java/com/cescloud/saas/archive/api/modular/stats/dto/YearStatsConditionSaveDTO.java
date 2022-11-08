package com.cescloud.saas.archive.api.modular.stats.dto;

import com.cescloud.saas.archive.api.modular.authority.dto.DataConditionDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("年报统计条件保存DTO")
public class YearStatsConditionSaveDTO {


	/**
	 * id
	 */
	@ApiModelProperty("id")
	private Long id;


	/**
	 * yearStatsId
	 */
	@ApiModelProperty("yearStatsId")
	@NotNull(message = "年报id不能为空")
	private Long yearStatsId;

	/**
	 * 行数
	 */
	@ApiModelProperty("行数")
	@NotNull(message = "行数不能为空")
	private Integer numberLine;

	/**
	 * 全宗号
	 */
	@ApiModelProperty("全宗号")
	@NotEmpty(message = "全宗号不能为空")
	private String fondsCode;

	/**
	 * 档案门类名称 false
	 */
	@ApiModelProperty("档案门类名称")
	private String archiveTypeName;

	/**
	 * 档案门类编码 false
	 */
	@ApiModelProperty("档案门类编码")
	private String archiveTypeCode;

	/**
	 * 档案表
	 */
	@ApiModelProperty("档案表")
	private String storageLocate;

	/**
	 * 档案表id
	 */
	@ApiModelProperty("档案表id")
	private Long templateTableId;

	/**
	 * 中文显示条件
	 */
	@ApiModelProperty("中文显示条件")
	private String chineseCondition;


	/**
	 * 租户id
	 */
	@ApiModelProperty("租户id")
	private Long tenantId;

	@ApiModelProperty("条件集合")
	private List<DataConditionDTO> dataConditionDTOList;
}
