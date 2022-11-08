package com.cescloud.saas.archive.api.modular.filingscope.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @ClassName FilingScopeTypePutDTO
 * @Author zhangxuehu
 * @Date 2020/6/29 14:20
 **/
@Data
@ApiModel(value = "修改归档范围定义参数")
public class FilingScopeTypePutDTO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "父节点的id")
    @NotNull(message = "父id 不能为空")
    private Long parentId;

    @ApiModelProperty(value = "序号")
    @NotBlank(message = "序号不能为空")
    private String catalogueNo;

    @ApiModelProperty("归档范围,可以是多个关键词，也可以是一段描述")
    @NotBlank(message = "归档范围不能为空")
    private String filingScope;

    @ApiModelProperty("保管期限,取值范围：永久、定期30年、定期10年")
    @NotBlank(message = "保管期限不能为空")
    private String retentionPeriod;

    @ApiModelProperty("处置方式：续存，销毁")
    private String disposalMethod;

    @ApiModelProperty("部门id")
    private Long deptId;

    @ApiModelProperty("部门名称")
    private String deptName;

	/**
	 * 年度 year_code
	 */
	@ApiModelProperty("年度")
	@Max(value=9999,message = "年度不能大于9999")
	@Min(value=0,message = "年度不能小于0")
	@NotNull(message ="年度不能为空")
	private Integer yearCode;
}
