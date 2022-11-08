package com.cescloud.saas.archive.api.modular.report.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author xaz
 * @date 2019/8/12 - 10:37
 **/

@Data
@ApiModel(value = "修改报表对象")
public class ReportPutDTO {

    /**
     * 报表数据id,主键 false
     */
    @ApiModelProperty(value = "报表id",required = true,example = "1")
    private Long id;

    /**
     * 报表名称 false
     */
    @ApiModelProperty(value = "报表名称（修改报表接口内必填，修改报表配置接口不填）",required = true,example = "测试报表")
    private String reportTopic;

	/**
	 *报表类型，分两种：独立；复合；
	 */
	@ApiModelProperty(value = "报表类型(修改报表接口内必填，修改报表配置接口不填)",required = true,example = "0(0:独立，1：复合)")
	private String reportType;

	/**
	 * 报表格式，比如pdf,word,excel false
	 */
	@ApiModelProperty(value = "报表格式，比如pdf,word,excel(修改报表接口内必填，修改报表配置接口不填)",required = true,example = "pdf")
	private String reportFormat;

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

	/**
	 *报表图标 false
	 */
	@ApiModelProperty(value = "报表图标流(选填)",example = "文件中心/file/xx.png")
	private String reportPicture;
}
