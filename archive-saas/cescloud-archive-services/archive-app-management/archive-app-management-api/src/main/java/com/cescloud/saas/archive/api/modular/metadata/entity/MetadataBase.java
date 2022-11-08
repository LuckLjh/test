package com.cescloud.saas.archive.api.modular.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 基础元数据
 *
 * @author liudong1
 * @date 2019-03-27 14:33:25
 */
@ApiModel("基础元数据实体")
@Data
@TableName("apma_metadata_base")
//@KeySequence("SEQ_APMA_METADATA_BASE")
@EqualsAndHashCode(callSuper = true)
public class MetadataBase extends Model<MetadataBase> {

    private static final long serialVersionUID = 1L;

    /**
     * 元数据id,主键 false
     */
	@ApiModelProperty("基础元数据ID")
	@TableId
    private Long id;

    /**
     * 元数据英文层次，SYS代表属于系统字段 false
     */
	@ApiModelProperty("档案层级")
	private String archiveLayer;

    /**
     * 元数据中文名称 false
     */
	@ApiModelProperty("元数据中文名称")
	private String metadataChinese;

    /**
     * 元数据英文名称 false
     */
	@ApiModelProperty("元数据英文名称")
	private String metadataEnglish;

	/**
	 * 元数据业务系统标识（0：系统字段，1：业务字段）
	 */
	@ApiModelProperty("元数据业务系统标识（0：系统字段，1：业务字段）")
	private Integer metadataClass;

    /**
     * 元数据类型 false
     */
	@ApiModelProperty("元数据类型（字符型，数字型，日期型...）")
	private String metadataType;

    /**
     * 元数据字段长度 false
     */
	@ApiModelProperty("元数据字段长度")
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
	private Integer sortNo;

	/**
	 * 元数据是否可以为空 false
	 */
	@ApiModelProperty("数据是否为空（0: 可以为空，1：不允许为空）")
	private Integer metadataNull;

	@ApiModelProperty("元数据中文描述信息")
	private String chineseDescription;

	@ApiModelProperty("元数据英文描述信息")
	private String englishDescription;

	@ApiModelProperty("元数据备注")
	private String remark;

	/**
	 * 元数据默认值
	 */
	@ApiModelProperty("元数据默认值")
	private String metadataDefaultValue;

	/**
	 * 绑定的数据字典code
	 */
	@ApiModelProperty("绑定的数据字典项编码")
	private String dictCode;

	/**
	 * 绑定的元数据标签英文
	 */
	@ApiModelProperty("绑定的元数据标签英文")
	private String tagEnglish;

    /**
     * 乐观锁,数据版本号
     */
    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long revision;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;

}
