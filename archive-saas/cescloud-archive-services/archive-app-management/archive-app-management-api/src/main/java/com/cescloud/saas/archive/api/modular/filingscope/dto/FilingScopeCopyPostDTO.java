package com.cescloud.saas.archive.api.modular.filingscope.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

/**
 * @ClassName CopyPostDTO
 * @Author zhangxuehu
 * @Date 2020/5/20 14:11
 **/
@Data
@ApiModel("范围树复制到另一个归档范围树中去")
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class FilingScopeCopyPostDTO  {
	/**
	 * 归档范围所属分类id,主键 false
	 */
	@ApiModelProperty(value = "源归档范围定义ID(新增不传)",required = false,example = "1")
	private Long sourceId;
	/**
	 * 归档范围所属的分类名称 false
	 */
	@ApiModelProperty(value = "归档范围所属的分类名称",required = true,example = "归档范围树")
	private String sourceClassName;
	/**
	 * 归档范围所属父节点的id false
	 */
	@ApiModelProperty(value = "源归档范围所属父节点的id",required = true,example = "01")
	private Long sourceParentClassId;
	/**
	 * 全宗号编码
	 */
	@ApiModelProperty("全宗号编码")
	private String sourceFondsCode;



	/**
	 * 归档范围所属的分类名称 false
	 */
	@ApiModelProperty(value = "归档范围所属的分类名称",required = true,example = "归档范围树")
	private String targetClassName;
	/**
	 * 全宗号编码
	 */
	@ApiModelProperty("全宗号编码")
	private String targetFondsCode;
	/**
	 * 全宗名称
	 */
	@ApiModelProperty("全宗名称")
	private String targetFondsName;

	/**
	 * 全宗范围列表
	 */
	@ApiModelProperty("全宗范围列表")
	List<String> fondsCodes;

	@ApiModelProperty(value = "全局节点的名称",example = "全局")
	private String fondsName;

}
