package com.cescloud.saas.archive.api.modular.downloads.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 常用下载实体类
 *
 * @author LS
 * @Date 2021/3/8
 */
@Data
@Builder
@TableName("apma_common_downloads")
@EqualsAndHashCode(callSuper = true)
public class CommonDownloads extends Model<CommonDownloads> {
	private static final long serialVersionUID = 1L;

	/**
	 * 常用下载id
	 */
	@TableId
	@ApiModelProperty(value = "常用下载主键ID")
	private Long id;

	/**
	 * 文件标题
	 */
	@ApiModelProperty(value = "文件标题")
	private String fileName;

	/**
	 * 文件大小
	 */
	@ApiModelProperty(value = "文件大小")
	private Long fileSize;

	/**
	 * 存储路径位置id
	 */
	@ApiModelProperty(value = "存储路径id")
	private Long fileStorageId;

	/**
	 * 下载次数
	 */
	@ApiModelProperty(value = "下载次数")
	private Integer downloadTimes;
	/**
	 * 租户ID
	 */
	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty(value = "租户ID", hidden = true)
	private Long tenantId;
	/**
	 * 创建人姓名
	 */
	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty(value = "创建人", hidden = true)
	private String createdUserName;
	/**
	 * 创建人id
	 */
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value = "创建人", hidden = true)
	private Long createdBy;
	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value = "创建时间", hidden = true)
	private LocalDateTime createdTime;

	/**
	 * 乐观锁,数据版本号 true
	 */
	@TableField(fill = FieldFill.INSERT)
	@Version
	@ApiModelProperty(value = "乐观锁标识", hidden = true)
	private Long revision;


}
