package com.cescloud.saas.archive.api.modular.fwimp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * oa导入日志表
 *
 * @author huangyuquan
 * @date 2021-03-26 12:04:54
 */
@Data
@TableName("oa_log")
//@KeySequence("SEQ_APMA_FONDS")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaLogs extends Model<OaLogs> {
	private static final long serialVersionUID = 1L;

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
}
