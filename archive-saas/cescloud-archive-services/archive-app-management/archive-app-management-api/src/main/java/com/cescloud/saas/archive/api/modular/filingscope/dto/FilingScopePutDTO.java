package com.cescloud.saas.archive.api.modular.filingscope.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author xaz
 * @date 2019/8/12 - 10:09
 **/

@Data
@ApiModel(value = "修改归档范围定义参数")
public class FilingScopePutDTO {

    /**
     * 归档范围所属分类id,主键 false
     */
    @NotNull(message = "归档范围定义ID不能为空")
    @ApiModelProperty(value = "归档范围定义ID",required = true,example = "1")
    private Long id;

    /**
     * 归档范围所属父节点的id false
     */
	@NotNull(message = "父节点的id不能为空")
    @ApiModelProperty(value = "归档范围所属父节点的id(修改树节点时为0)",required = true,example = "01")
    private Long parentClassId;

    /**
     * 归档范围所属分类号 false
     */
	@Size(max = 20, message = "分类号长度过长(不得超过40个英文字符)")
    @ApiModelProperty(value = "归档范围所属分类号，同档案类型不能重复(修改树节点不填)",required = false,example = "01")
    private String classNo;

    /**
     * 归档范围所属的分类名称 false
     */
    @NotBlank(message = "分类名称不能为空")
	@Size(max = 50, message = "名称长度过长(不得超过100个英文字符或50个中文汉字)")
    @ApiModelProperty(value = "归档范围所属的分类名称",required = true,example = "归档范围树")
    private String className;

    /**
     * 档案类型code 例如wsda false
     */
    @ApiModelProperty(value = "档案类型code",required = true,example = "wsda")
    private String typeCode;
	/**
	 * 全宗号编码
	 */
	@ApiModelProperty("全宗号编码")
	private String fondsCode;
}
