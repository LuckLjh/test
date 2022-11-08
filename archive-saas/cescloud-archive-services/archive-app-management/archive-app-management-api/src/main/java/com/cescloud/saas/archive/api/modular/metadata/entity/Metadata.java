package com.cescloud.saas.archive.api.modular.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.cescloud.saas.archive.service.modular.common.tableoperation.service.MetadataInterface;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 档案元数据
 *
 * @author liudong1
 * @date 2019-03-28 09:42:53
 */
@ApiModel("档案元数据实体")
@Data
@TableName("apma_metadata")
//@KeySequence("SEQ_APMA_METADATA")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Metadata extends Model<Metadata> implements MetadataInterface {

    private static final long serialVersionUID = 1L;

    /**
     * 元数据id,主键 false
     */
	@ApiModelProperty("元数据id")
    @TableId
    private Long id;

	/**
	 * 档案表ID
	 */
	@ApiModelProperty("档案表ID")
	private Long tableId;
    /**
     * 档案门类层级目录元数据存储表名 false
     */
	@ApiModelProperty("元数据存储表名")
    private String storageLocate;

    /**
     * 元数据英文名称 false
     */
	@ApiModelProperty("元数据中文名称")
	@NotBlank(message = "字段名称不能为空")
    private String metadataChinese;

    /**
     * 元数据英文名称 false
     */
	@ApiModelProperty("元数据英文名称")
    private String metadataEnglish;

	/**
	 * 元数据类别（0：系统字段，1：业务字段） false
	 */
	@ApiModelProperty("元数据类别（0：系统字段，1：业务字段）")
	private Integer metadataClass;

    /**
     * 元数据类型 false
     */
	@ApiModelProperty("元数据类型")
	@NotBlank(message = "字段类型不能为空")
	private String metadataType;

    /**
     * 元数据字段长度 false
     */
	@ApiModelProperty("元数据字段长度")
	@NotNull(message = "字段长度不能为空")
	private Integer metadataLength;

    /**
     * 元数据的小数位数 false
     */
	@ApiModelProperty("元数据的小数位数")
    private Integer metadataDotLength;

    /**
     * 元数据排序编号 false
     */
	@ApiModelProperty("元数据排序编号")
    private Integer metadataSort;

	/**
	 * 元数据是否为空 false(0:允许为空，1:不允许为空)
	 */
	@ApiModelProperty("元数据是否为空")
	private Integer metadataNull;

	/**
	 * 绑定的数据字典code
	 */
	@ApiModelProperty("绑定的数据字典code")
	private String dictCode;
	/**
	 * 元数据标签ID
	 */
	@ApiModelProperty("元数据标签ID")
	@TableField(fill = FieldFill.UPDATE, insertStrategy = FieldStrategy.NOT_EMPTY)
	private String tagEnglish;
	/**
	 * 元数据默认值
	 */
	@ApiModelProperty("元数据默认值")
	private String metadataDefaultValue;

	@ApiModelProperty("元数据中文描述信息")
	private String chineseDescription;

	@ApiModelProperty("元数据英文描述信息")
	private String englishDescription;

	@ApiModelProperty("元数据备注")
	private String remark;

    /**
     * 是否列表显示 false
     */
	@ApiModelProperty("是否列表显示")
    private Integer isList;

    /**
     * 是否参与编辑 false
     */
	@ApiModelProperty("是否参与编辑")
    private Integer isEdit;

    /**
     * 是否参与检索 false
     */
	@ApiModelProperty("是否参与检索")
    private Integer isSearch;
	/**
     * 是否允许重复
     */
	@ApiModelProperty("是否允许重复 0 允许重复；1不允许重复")
    private Integer isRepeat;

    /**
     * 排序号 false
     */
	@ApiModelProperty("排序号")
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


	/**
	 * 存放元数据的一些表单配置(无数据库字段)
	 */
	@Transient
	@TableField(exist=false)
	@ApiModelProperty("存放元数据的一些表单配置(无数据库字段)")
	private  Object option;


}
