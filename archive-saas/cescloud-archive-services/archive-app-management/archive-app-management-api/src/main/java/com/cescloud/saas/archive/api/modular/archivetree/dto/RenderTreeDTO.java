package com.cescloud.saas.archive.api.modular.archivetree.dto;
/**
@author xaz
@date 2019/6/21 - 16:32
*/

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class RenderTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("档案树节点id")
    private String id;

    @ApiModelProperty(name = "档案树节点名称", required = true)
    private String treeName;

    @ApiModelProperty(name = "树节点父节点ID，如果是树根节点，则为-1", required = true)
    private String parentId;

    /**
     * 组卷标识：1-案卷、卷内 ，2-一文一件，3-项目、案卷、卷内，4-单套制
     */
    @ApiModelProperty(name = "整理方式：V:案卷、卷内 O: 一文一件 P:项目、案卷、卷内 S:单套制 R:预归档 C:自定义", hidden = true)
    private String filingType;

    @ApiModelProperty(name = "是否为子节点", hidden = true)
    private Boolean isLeaf = true;

    /**
     * 档案树节点值：
     * 当节点类型为分类节点时，为null；
     * 当节点类型为全宗节点时，为全宗号；
     * 当节点类型为自定义节点时，为自定义值；
     * 当节点类型为档案门类节点时，为档案门类编码；
     * 当节点类型为层级表节点时，为表模板ID；
     * 当节点类型为归档范围节点时，为归档范围值
     * 当节点类型为自定义节点时，为自定义节点属性值
     */
    @ApiModelProperty(name = "档案树节点值")
    private String treeValue;

    /**
     * 自定义节点属性 保管期限 年度 密级 部门
     */
    @ApiModelProperty(name = "自定义节点属性", hidden = true)
    private String metadataEnglish;

    @ApiModelProperty(name = "档案类型编码", hidden = true)
    private String archiveTypeCode;

    @ApiModelProperty(name = "档案门类表模板ID", hidden = true)
    private Long templateTableId;

    @ApiModelProperty(name = "档案门类表模板对应的层级编码", hidden = true)
    private String layerCode;

    @ApiModelProperty(name = "归档范围ID", hidden = true)
    private Long filingScopeId;

    @ApiModelProperty(name = "是否为层级节点")
    private Boolean showLayer;

    /**
     * 档案树节点类型：
     * T：树根节点；
     * C：分类节点；
     * F：全宗节点；
     * A：档案门类节点；
     * L：层级表节点；
     * D：部门节点；
     * S：归档范围节点；
     * Y：动态数据节点
     *
     * 分类节点只能建在分类节点下，档案门类节点只能建在分类节点下，自定义节点只能建立在档案门类节点下
     */
    @ApiModelProperty("档案树节点类型：T：树根节点； C：分类节点；F：全宗节点；  A：档案类型节点；  L：层级表节点； D：自定义节点规则；S：归档范围节点；")
    private String nodeType;

    @ApiModelProperty(value = "过滤条件", example = "fonds=1000;menuId=123456")
    private String filter;

    @ApiModelProperty(value = "归档范围节点path", example = "1/1.1")
    private String path;

    @ApiModelProperty(value = "节点数量")
    private Integer count;

	/**
	 * 档案门类分类标识（ws、kj、kj等） false
	 */
	@ApiModelProperty("档案门类分类标识")
	private String classType;

	@ApiModelProperty("类目名称")
	private String className;

	@ApiModelProperty("是否屏蔽新增按钮")
	private Boolean disableAdd;

	@ApiModelProperty("树节点所属租户，G：全局")
	private String fondsCode;
}
