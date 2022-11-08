package com.cescloud.saas.archive.api.modular.businessconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.cescloud.saas.archive.service.modular.common.data.entity.DataTypeInterface;
import com.cescloud.saas.archive.service.modular.common.tableoperation.service.MetadataInterface;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author liwei
 */
@ApiModel("业务模板定义")
@TableName("apma_business_model_define")
//@KeySequence("SEQ_APMA_BUSI_MODEL_DEFINE")
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessModelDefine extends Model<BusinessModelDefine> implements MetadataInterface, DataTypeInterface {

	private static final long serialVersionUID = -8369499932813181080L;

	@TableId
	@ApiModelProperty("主键id")
	private Long id;

	@ApiModelProperty("模板类型（1：利用表单,2、鉴定表单，销毁表单，移交表单，归档表单，编研表单，保管表单）")
	private Integer modelType;

	@ApiModelProperty("字段中文名称")
	private String metadataChinese;

	@ApiModelProperty("字段英文名称")
	private String metadataEnglish;

	/**
	 * 字段英文名称编号 false
	 */
	@ApiModelProperty("字段英文名称编号")
	private Integer metadataEnglishNo;

	@ApiModelProperty("字段类型")
	private String metadataType;

	@ApiModelProperty("字段长度")
	private Integer metadataLength;

	@ApiModelProperty("小数点位数")
	private Integer metadataDotLength;

	@ApiModelProperty("字典项编码")
	private  String dictCode;

	/**
	 * 字段英文隐藏名称防止重复 false
	 */
	@ApiModelProperty("字段英文隐藏名称")
	private String metadataEnglishHidden;

	/**
	 * 字段描述信息 false
	 */
	@ApiModelProperty("字段描述信息")
	private String metadataDescription;

	@ApiModelProperty("排序号")
	private Integer metadataSort;

	/**
	 * 元数据是否为空 false
	 */
	@ApiModelProperty("是否为空")
	private Integer metadataNull;

	/**
	 * 默认值
	 */
	@ApiModelProperty("默认值")
	private String metadataDefaultValue;

	@ApiModelProperty("是否可编辑（0：不可编辑，1：可编辑）")
	private Integer isEdit;

	@ApiModelProperty("是否必填项(0:非必填，1：必填)")
	private Integer isRequired;

	@ApiModelProperty("是否显示（0：不显示，1：显示）")
	private Integer isShow;

	@ApiModelProperty("是否条件字段（0：否，1：是）")
	private Integer isFilter;

	@ApiModelProperty("前台列表是否不可见字段（0：否，1：是）")
	private Integer isSys;

	@ApiModelProperty("是否部门字段（0：否，1：是）")
	private Integer isDept;

	@ApiModelProperty("元数据业务系统标识（1：系统字段，0：业务字段）")
	private Integer metadataSys;

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


	@Override
	public String getRealMetadataType() {
		return Objects.isNull(metadataType)?null:metadataType;
	}
}
