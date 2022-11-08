package com.cescloud.saas.archive.api.modular.report.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author plez
 * @date 2019/9/04 - 10:29
 **/

@Data
@ApiModel(value = "新增报表对象")
public class ReportPostDTO {

	@ApiModelProperty(value = "模块id", required = true, example = "-1")
	private Long moduleId;

    /**
     * 报表名称 false
     */
    @ApiModelProperty(value = "报表名称",required = true,example = "测试报表")
	@NotBlank(message = "报表名称不能为空")
    private String reportTopic;

    /**
     * 报表所属档案数据表名  false
     */
    @ApiModelProperty(value = "报表主表名",required = true,example = "t_1_wsda_v")
	@NotBlank(message = "报表主表名不能为空")
    private String storageLocate;

    /**
     * 报表格式，比如pdf,word,excel false
     */
    @ApiModelProperty(value = "报表格式，比如pdf,word,excel",required = true,example = "pdf")
	@NotBlank(message = "报表格式不能为空")
    private String reportFormat;

	/**
	 *报表类型，分两种：独立；复合；
	 */
	@ApiModelProperty(value = "报表类型(必填)",required = true,example = "0(0:独立，1：复合)")
	@NotBlank(message = "报表类型不能为空")
	private String reportType;

	/**
	 *报表分页字段（多个用","隔开）
	 */
	@Size(max = 100,message = "报表分页字段超出限制")
	@ApiModelProperty(value = "报表分页字段,可以用多个','按顺序用隔开(修改报表配置接口内必填，修改报表接口不填)",required = true,example = "year,fondsCode")
	private String pageField;

	/**
	 *报表分页行数
	 */
	@ApiModelProperty(value = "报表分页行数(修改报表配置接口内必填，修改报表接口不填)",required = true,example = "10")
	private Integer reportPageLines;

}
