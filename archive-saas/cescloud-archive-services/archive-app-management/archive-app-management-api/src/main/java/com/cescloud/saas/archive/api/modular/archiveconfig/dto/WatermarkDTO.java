package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.WatermarkDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@ApiModel("水印方案实例")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WatermarkDTO implements Serializable {

	private List<WatermarkDetail> watermarkDetailList;

	private static final long serialVersionUID = 1L;
	/**
	 * 列表数据id,主键 false
	 */
	@ApiModelProperty("水印id,主键")
	@TableId
	private Long id;

	/**
	 * 电子文件表
	 */
	@ApiModelProperty("电子文件表")
	private String storageLocate;

	/**
	 * 水印格式
	 */
	@ApiModelProperty("水印格式")
	private String watermarkFormat;


	/**
	 * 水印分类
	 */
	@ApiModelProperty("水印分类")
	private Integer waterClassification;


	/**
	 * 利用方式
	 */
	@ApiModelProperty("利用方式(浏览,下载,打印)")
	private Integer usingType;

	/**
	 * 是否添加水印
	 */
	@ApiModelProperty("添加水印标记")
	private Boolean addWatermarkFlag;

	/**
	 * 源文件的名称
	 */
	@ApiModelProperty("源文件的名称")
	private String sourceFileName;


	/**
	 * 附件类型
	 */
	@ApiModelProperty("附件类型(关联文件浏览方式枚举)")
	private Integer AttachmentType;
	/**
	 * 源文件的类型
	 */
	@ApiModelProperty("源文件的类型")
	private String sourceFileType;

	/**
	 * 源文件的名称
	 */
	@ApiModelProperty("需要加水印的文件id")
	private Long fileId;

	/**
	 * 源文件的地址
	 */
	@ApiModelProperty("源文件的地址")
	private String sourceFilePath;

	/**
	 * 添加水印文件名称
	 */
	@ApiModelProperty("添加水印文件名称")
	private String watermarkFileName;

	/**
	 * 水印文件的地址
	 */
	@ApiModelProperty("水印文件的地址")
	private String watermarkFilePath;

	/**
	 * 是否默认配置
	 */
	@ApiModelProperty("是否默认配置")
	private Boolean isDefault;

	/**
	 * 水印文件的id
	 */
	@ApiModelProperty("水印文件的id")
	private Long watermarkFileId;

	/**
	 * 临时水印目录
	 */
	@ApiModelProperty("临时水印目录")
	private File[] files;


	/**
	 * 水印文件的大小
	 */
	@ApiModelProperty("水印文件的大小")
	private Long watermarkFileSize;

	/**
	 * 水印名称
	 */
	@ApiModelProperty("水印名称")
	private String watermarkName;

	/**
	 * 所属模块
	 */
	@ApiModelProperty("所属模块")
	private Long moduleId;

	@ApiModelProperty("所属租户id")
	@TableField(fill = FieldFill.INSERT)
	private Long tenantId;



	@ApiModelProperty("流程单id")
	private Long listId;

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
}
