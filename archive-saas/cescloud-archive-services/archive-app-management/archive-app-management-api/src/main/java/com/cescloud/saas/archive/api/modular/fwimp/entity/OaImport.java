package com.cescloud.saas.archive.api.modular.fwimp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * oa导入主表
 *
 * @author huangyuquan
 * @date 2021-03-26 12:04:54
 */
@Data
@TableName("oa_import")
//@KeySequence("SEQ_APMA_FONDS")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaImport extends Model<OaImport> {
	private static final long serialVersionUID = 1L;

	/**
	 * ID,主键
	 */
	@TableId
	@ApiModelProperty(value = "ID", example = "1")
	private Long id;
	/**
	 * 流程类型
	 */
	@NotBlank(message = "")
	@Size(max = 50, message = "流程类型长度过长(不得超过50个中文字符)")
	@ApiModelProperty(value = "流程类型", required = true,example = "流程类型")
	private String name;
	/**
	 * 流程id
	 */
	@Size(max = 25, message = "流程id长度过长(不得超过25个字符)")
	@ApiModelProperty(value = "流程id", required = true,example = "1234")
	private Long oaFlowid;
	/**
	 * 校验方式 ：覆盖：0,跳过：1
	 */
	@Size(max = 1, message = "校验方式长度过长(不得超过1个字符)")
	@ApiModelProperty(value = "校验方式",example = "0")
	private int dataCheck;
	/**
	 * 描述
	 */
	@Size(max = 225, message = "描述长度过长(不得超过225个字符)")
	@ApiModelProperty(value = "描述",example = "0")
	private String content;

	/**
	 * 状态 ： 未激活：0，已激活：1，已停止：2
	 */
	@ApiModelProperty(value = "状态",example = "0")
	private int status;

	/**
	 * 是否导入html  :是：0，否：1
	 */
	@Size(max = 1, message = "是否导入html长度过长(不得超过1个字符)")
	@ApiModelProperty(value = "是否导入html",example = "0")
	private int importHtml;

	/**
	 * 导入后状态 :待归档：30，待整理：60
	 */
	@Size(max = 2, message = "导入后状态长度过长(不得超过2个字符)")
	@ApiModelProperty(value = "导入后状态",example = "0")
	private int importStatus;

	/**
	 * 时间策略
	 */
	@Size(max = 225, message = "时间策略长度过长(不得超过225个字符)")
	@ApiModelProperty(value = "时间策略",example = "0")
	private String timeStrategy;

	/**
	 * 文件路径
	 */
	@Size(max = 225, message = "文件路径长度过长(不得超过225个字符)")
	@ApiModelProperty(value = "文件路径",example = "0")
	private String filePath;

	/**
	 * 所属租户id false
	 */
	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty(hidden = true)
	private Long tenantId;

	/**
	 * 所属全宗id false
	 */
	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty(hidden = true)
	private String fondsId;

	/**
	 * 档案树节点
	 */
	@Size(max = 225, message = "树节点ID长度过长(不得超过225个字符)")
	@ApiModelProperty(value = "",example = "0")
	private String treeNode;

	/**
	 * 档案树节点名称
	 */
	@Size(max = 225, message = "文件路径长度过长(不得超过225个字符)")
	@ApiModelProperty(value = "",example = "0")
	private String treeName;

	/**
	 * 档案树的实际表名
	 */
	@Size(max = 225, message = "文件路径长度过长(不得超过225个字符)")
	@ApiModelProperty(value = "",example = "0")
	private String tableName;

	/**
	 * 执行情况
	 */
	@Size(max = 225, message = "执行情况长度过长(不得超过225个字符)")
	@ApiModelProperty(value = "",example = "0")
	private String executeMess;
}
