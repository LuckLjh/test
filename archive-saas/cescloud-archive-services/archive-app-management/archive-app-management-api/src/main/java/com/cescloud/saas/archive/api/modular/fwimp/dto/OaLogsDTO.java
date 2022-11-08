package com.cescloud.saas.archive.api.modular.fwimp.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassName OaImportDTO
 * @Author huangyuquan
 * @Date
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaLogsDTO implements Serializable {

	/**
	 * ID,主键
	 */
	@TableId
	@ApiModelProperty(value = "ID", example = "1")
	private Long id;

	/**
	 * owner_ID,外键
	 * 主表条目 id  OA_IMPORT.ID = OA_COLUMN.OWNER_ID
	 * OA_IMPORT.ID = OA_LOG.OWNER_ID
	 */
	@ApiModelProperty(value = "ownerId", example = "1")
	private Long ownerId;

	/**
	 * xml名称
	 */
	@NotBlank(message = "")
	@Size(max = 25, message = "xml 文件名称长度过长(不得超过50个字符)")
	@ApiModelProperty(value = "xml 文件名称", required = true,example = "流程类型")
	private String xmlName;

	/**
	 * 创建时间 true
	 */
	@ApiModelProperty("开始时间")
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	private LocalDateTime startTime;

	/**
	 * 结束时间 true
	 */
	@ApiModelProperty("结束时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime endTime;

	/**
	 * 导入成功/失败   导入成功：1,导入失败2
	 */
	@ApiModelProperty(value = "导入状态",example = "0")
	private int status;

	/**
	 * 描述
	 */
	@Size(max = 225, message = "描述长度过长(不得超过225个字符)")
	@ApiModelProperty(value = "描述",example = "0")
	private String content;
	/**
	 * 检索关键字 false
	 */
	@ApiModelProperty(value = "检索关键字",example = "检索关键字")
	private String keyword;
}
