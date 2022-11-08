package com.cescloud.saas.archive.api.modular.filingscope.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author xaz
 * @date 2019/8/10 - 13:15
 **/

@Data
@ApiModel(value = "新增归档范围定义参数")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilingScopePostDTO {
    /**
     * 归档范围所属分类号 false
     */
	@Size(max = 20, message = "分类号长度过长(不得超过40个英文字符)")
    @ApiModelProperty(value = "归档范围所属分类号，同档案类型不能重复(新增树节点不填)",required = false,example = "01")
    private String classNo;
    /**
     * 归档范围所属的分类名称 false
     */
    @NotBlank(message = "分类名称不能为空")
	@Size(max = 50, message = "名称长度过长(不得超过100个英文字符或50个中文汉字)")
    @ApiModelProperty(value = "归档范围所属的分类名称",required = true,example = "归档范围树")
    private String className;

    /**
     * 归档范围所属父节点的id false
     */
	@NotNull(message = "归档范围所属父节点的id不能为空")
    @ApiModelProperty(value = "归档范围所属父节点的id(新增树节点时为0)",required = true,example = "01")
    private Long parentClassId;

    /**
     * 档案类型code 例如wsda false
     */
    @ApiModelProperty(value = "档案类型code",required = true,example = "wsda")
    private String typeCode;
	/**
	 * 档案类型code 例如wsda false
	 */
	@ApiModelProperty(value = "租户ID",hidden = true)
	private Long tenantId;
	/**
	 * 全宗号编码
	 */
	@ApiModelProperty("全宗号编码")
	private String fondsCode;
	/**
	 * 全宗名称
	 */
	@ApiModelProperty("全宗名称")
	private String fondsName;
}
