package com.cescloud.saas.archive.api.modular.archivetype.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 档案表
 *
 * @author liudong1
 * @date 2019-03-27 12:48:29
 */
@ApiModel("档案表实体")
@Data
@TableName("apma_archive_table")
//@KeySequence("SEQ_APMA_ARCHIVE_TABLE")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveTable extends Model<ArchiveTable> {

    private static final long serialVersionUID = 1L;

    /**
     * 档案表存储唯一标识 false
     */
	@ApiModelProperty("档案表ID")
    @TableId
    private Long id;

	/**
	 * 模板ID
	 */
	@ApiModelProperty("模板ID")
	private Long templateTableId;

    /**
     * 档案门类编码 false
     */
	@ApiModelProperty("档案门类编码")
    private String archiveTypeCode;

    /**
     * 档案门类存储层级名称，如：文书档案(案卷级)、文书档案(卷内级)、文书档案(文件级) false
     */
	@ApiModelProperty("档案门类存储名称")
    private String storageName;

    /**
     * 存储表名，属于动态创建 规则：租户标识+档案门类分类标识+档案门类标识+层级标识 false
     */
	@ApiModelProperty("存储表名")
    private String storageLocate;

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
     * 档案层次 false
     */
	@ApiModelProperty("档案层次")
    private String archiveLayer;

	/**
	 * 排序字段
	 */
	@ApiModelProperty("排序字段")
	private Integer sortNo;

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

	@ApiModelProperty("全宗名称")
	@TableField(exist = false)
	private String fondsName;
}
