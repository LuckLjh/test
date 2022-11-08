package com.cescloud.saas.archive.api.modular.archivetype.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 档案门类
 *
 * @author liudong1
 * @date 2019-03-25 10:19:54
 */
@ApiModel("档案门类实体")
@Data
@TableName("apma_archive_type")
//@KeySequence("SEQ_APMA_ARCHIVE_TYPE")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ArchiveType extends Model<ArchiveType> {

    private static final long serialVersionUID = 1L;

    /**
     * 档案门类唯一标识 false
     */
    @ApiModelProperty("档案门类ID")
    @TableId
    private Long id;

    /**
     * 模板ID
     */
    @ApiModelProperty("模板ID")
    private Long templateTypeId;

    /**
     * 档案门类编码 false
     */
    @ApiModelProperty("档案门类编码")
    private String typeCode;

    /**
     * 档案门类名字 false
     */
    @ApiModelProperty("档案门类名字")
    private String typeName;

    /**
     * 父档案门类ID false
     */
    @ApiModelProperty("父档案门类ID")
    private Long parentId;

    /**
     * 档案门类分类标识（ws、kj、kj等） false
     */
    @ApiModelProperty("档案门类分类标识")
    private String classType;

    /**
     * 无限层级后
     * 是否删除整理方式
     */
    @ApiModelProperty("整理方式")
    private String filingType;

    /**
     * 组卷分组，1:单点组卷 2:两层组卷，如：机构+部门 3：三层组卷，很少用到 false
     */
    @ApiModelProperty("组卷分组，1:单点组卷 2:两层组卷")
    private Integer filingGroup;

    /**
     * 分类标识，C:代表分类节点 D:档案门类节点 false
     */
    @ApiModelProperty("分类标识，C:代表分类节点 D:档案门类节点")
    private String nodeType;
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
	/**
	 * 设置人
	 */
	@ApiModelProperty("设置人")
	private String createdUserName;
    /**
     * 所属租户id
     */
    @ApiModelProperty("所属租户id")
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 乐观锁,数据版本号
     */
    @ApiModelProperty("乐观锁")
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long revision;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 是否是叶子节点
     */
    @TableField(exist = false)
    private Boolean isLeaf;

	/**
	 * 排序值
	 */
	@ApiModelProperty("排序值")
	private Integer sortNo;

}
