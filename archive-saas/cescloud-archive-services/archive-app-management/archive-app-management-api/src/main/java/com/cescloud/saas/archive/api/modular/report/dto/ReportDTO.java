package com.cescloud.saas.archive.api.modular.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportDTO {

    /**
     * 报表数据id,主键 false
     */
    @ApiModelProperty(value = "报表数据id(修改不需要传)",example = "1")
    private Long id;

	/**
	 * 模块id
	 */
	@ApiModelProperty(value = "模块id",example = "1")
    private Long moduleId;

    /**
     * 报表名称 false
     */
    @ApiModelProperty(value = "报表名称（新增必填）",example = "测试报表")
    private String reportTopic;

	/**
	 *报表图标 false
	 */
	@ApiModelProperty(value = "报表图标流(选填)",example = "文件中心/file/xx.png")
	private String reportPicture;

	/**
	 *报表类型，分两种：独立；复合；
	 */
	@ApiModelProperty(value = "报表类型(必填)",required = true,example = "0(0:独立，1：复合)")
	private String reportType;

	/**
	 * 报表格式，比如pdf,word,excel false
	 */
	@ApiModelProperty(value = "报表格式，比如pdf,word,excel（必填）",required = true)
	private String reportFormat;

	/**
	 *报表分页字段（多个用","隔开）
	 */
	@ApiModelProperty(value = "报表分页字段,可以多个,按顺序用','隔开",required = true,example = "year,fondsCode")
	private String pageField;

	/**
	 *报表分页行数
	 */
	@ApiModelProperty(value = "报表分页行数",required = true,example = "10")
	private Integer reportPageLines;

	/**
     * 报表所属档案数据表名  false
     */
    @ApiModelProperty(value = "报表所属档案数据表名",example = "t_1_wsda_v（如果是复合型，以逗号隔开）")
    private String storageLocate;

    /**
     * 关联档案数据表名  false
     */
    @ApiModelProperty(value = "关联档案数据表名",example = "t_1_wsda_v")
    private String[] relationStorageLocates;

    /**
     * 报表模板，已存在 或 不存在
     */
    @ApiModelProperty(value = "报表模板，已存在或不存在（回显用到）")
    private String reportModel;

    /**
     * 报表模板上传者
     */
    @ApiModelProperty(value = "报表模板上传者（回显用到）")
    private String uploadName;

}
