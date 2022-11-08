
package com.cescloud.saas.archive.api.modular.archivetree.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.cescloud.saas.archive.api.modular.archivetree.constant.ArchiveTreeNodeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 档案树定义
 *
 * @author qiucs
 * @date 2019-04-12 13:36:59
 */
@Data
@TableName("apma_archive_tree")
//@KeySequence("SEQ_APMA_ARCHIVE_TREE")
@EqualsAndHashCode(callSuper = true)
@ApiModel("档案树定义")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveTree extends Model<ArchiveTree> {

    private static final long serialVersionUID = 1L;

    /**
     * 档案树节点id false
     */
    @TableId
    @ApiModelProperty("档案树节点id")
    private Long id;

    /**
     * 档案树节点编码 false
     */
    @ApiModelProperty(name = "档案树节点编码", hidden = true)
    private String treeCode;

    /**
     * 档案树节点名称 false
     */
    @ApiModelProperty(name = "档案树节点名称", required = true)
    private String treeName;

    /**
     * 树节点父节点ID，如果是树根节点，则为-1
     */
    @ApiModelProperty(name = "树节点父节点ID，如果是树根节点，则为-1", required = true)
    private Long parentId;

    /**
     * 组卷标识：1-案卷、卷内 ，2-一文一件，3-项目、案卷、卷内，4-单套制
     */
    @ApiModelProperty(name = "整理方式：V:案卷、卷内 O: 一文一件 P:项目、案卷、卷内 S:单套制 R:预归档 C:自定义", hidden = true)
    private String filingType;

    /**
     * 是否为子节点
     */
    @ApiModelProperty(name = "是否为子节点", hidden = true)
    private Boolean isLeaf = true;

    /**
     * 排序号 false
     */
    @ApiModelProperty(name = "排序号", hidden = true)
    private Integer sortNo;

    /**
     * 档案树节点层级（1：节点编码3位，2：节点编码6位） false
     */
    @ApiModelProperty(name = "档案树节点层级", hidden = true)
    private Integer treeLevel = 1;

    /**
     * 档案树节点值：
     * 当节点类型为分类节点时，为null；
     * 当节点类型为全宗节点时，为全宗号；
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

    /**
     * 档案类型编码
     */
    @ApiModelProperty(name = "档案类型编码", hidden = true)
    private String archiveTypeCode;

    @ApiModelProperty(name = "档案门类表模板ID", hidden = true)
    private Long templateTableId;

    @ApiModelProperty(name = "档案门类表模板对应的层级编码", hidden = true)
    private String layerCode;

    /**
     * 归档范围ID
     */
    @ApiModelProperty(name = "归档范围ID", hidden = true)
    private Long filingScopeId;

    /**
     * 是否为默认树
     */
    @ApiModelProperty(name = "是否为默认树", hidden = true)
    private Boolean isDefault;

    /**
     * 是否为层级节点
     */
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

    /**
     * 显示方式: 0-名称，1-编码，2-编码加名称
     */
    @ApiModelProperty(name = "显示方式", hidden = true)
    private String showType = "0";

    /**
     * 所属租户id true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "所属租户id", hidden = true)
    private Long tenantId;

    /**
	 * 乐观锁 true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "乐观锁", hidden = true)
    private Long revision;

    /**
     * 创建人 true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "创建人", hidden = true)
    private Long createdBy;

    /**
     * 创建时间 true
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(name = "创建时间", hidden = true)
    private LocalDateTime createdTime;

    /**
     * 更新人 true
     */
    @TableField(fill = FieldFill.UPDATE)
    @ApiModelProperty(name = "更新人", hidden = true)
    private Long updatedBy;

    /**
     * 更新时间 true
     */
    @TableField(fill = FieldFill.UPDATE)
    @ApiModelProperty(name = "更新时间", hidden = true)
    private LocalDateTime updatedTime;

    public String getNodeType() {
        return (null != nodeType) ? nodeType.toUpperCase() : null;
    }

    public Boolean getIsDefault() {
        return ArchiveTreeNodeEnum.TREE_ROOT.getCode().equals(nodeType) ? isDefault : null;
    }

	@ApiModelProperty(name = "全宗号")
	private String fondsCode;

	@ApiModelProperty(name = "全宗名称")
	private String fondsName;

	@TableField(exist = false)
	@ApiModelProperty(name = "树节点父节点名称")
	private String parentTreeName;

	@TableField(exist = false)
	@ApiModelProperty("档案门类分类标识")
	private String classType;

	@TableField(exist = false)
	@ApiModelProperty("类目名称")
	private String className;

	@TableField(exist = false)
	@ApiModelProperty("类型名称")
	private String archiveTypeName;

}
