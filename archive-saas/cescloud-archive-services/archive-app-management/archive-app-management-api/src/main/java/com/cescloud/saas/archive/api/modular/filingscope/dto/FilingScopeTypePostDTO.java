package com.cescloud.saas.archive.api.modular.filingscope.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * @ClassName FilingScopeTypePostDTO
 * @Author zhangxuehu
 * @Date 2020/6/29 14:16
 **/
@Data
@ApiModel(value = "新增归档范围信息参数")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilingScopeTypePostDTO {

    @ApiModelProperty(value = "父节点的id",required = true,example = "01")
    @NotNull(message = "父id不能为空")
    private Long parentId;

    @ApiModelProperty(value = "序号",required = true)
    @NotBlank(message = "序号不能为空")
    @Size(max = 50,message = "序号过长")
    private String catalogueNo;

    @ApiModelProperty("归档范围,可以是多个关键词，也可以是一段描述")
    @NotBlank(message = "归档范围不能为空")
    @Size(max = 200,message = "归档范围过长")
    private String filingScope;

    @ApiModelProperty("保管期限,取值范围：永久、定期30年、定期10年")
    @NotBlank(message = "保管期限不能为空")
    private String retentionPeriod;
    /**
     * 处置方式：续存，销毁 false
     */
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
