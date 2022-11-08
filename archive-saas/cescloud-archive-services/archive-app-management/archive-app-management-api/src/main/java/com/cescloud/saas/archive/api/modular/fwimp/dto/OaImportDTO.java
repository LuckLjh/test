package com.cescloud.saas.archive.api.modular.fwimp.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
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

/**
 * @ClassName OaImportDTO
 * @Author huangyuquan
 * @Date
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaImportDTO implements Serializable {

	/**
	 * ID,主键
	 */
	@ApiModelProperty(value = "ID", example = "1")
	private Long id;
	/**
	 * 名称
	 */
	@Size(max = 50, message = "流程类型长度过长(不得超过50个中文字符)")
	@ApiModelProperty(value = "流程类型", required = true,example = "流程类型")
	private String name;
	/**
	 * 流程id
	 */
	@ApiModelProperty(value = "流程id", required = true,example = "1234")
	private Long oaFlowid;
	/**
	 * 校验方式 ：覆盖：0,跳过：1
	 */
	@ApiModelProperty(value = "校验方式",example = "0")
	private Long dataCheck;
	/**
	 * 描述
	 */
	@ApiModelProperty(value = "描述",example = "0")
	private String content;

	/**
	 * 状态 ： 未激活：0，已激活：1，已停止：2
	 */
	@ApiModelProperty(value = "校验方式",example = "0")
	private Long status;

	/**
	 * 是否导入html  :是：0，否：1
	 */
	@ApiModelProperty(value = "是否导入html",example = "0")
	private Long importHtml;

	/**
	 * 导入后状态 :待归档：30，待整理：60
	 */
	@ApiModelProperty(value = "导入后状态",example = "0")
	private Long importStatus;

	/**
	 * 时间策略
	 */
	@ApiModelProperty(value = "时间策略",example = "0")
	private String timeStrategy;

	/**
	 * 时间策略
	 */
	@ApiModelProperty(value = "文件路径",example = "0")
	private String filePath;

	/**
	 * 所属租户id false
	 */
	@ApiModelProperty(hidden = true)
	private Long tenantId;

	/**
	 * 所属全宗id false
	 */
	@ApiModelProperty(hidden = true)
	private String fondsId;


	/**
	 * 检索关键字 false
	 */
	@ApiModelProperty(value = "检索关键字",example = "检索关键字")
	private String keyword;

	/**
	 * 选中id
	 */
	@ApiModelProperty(value = "选中id",example = "选中id")
	private String  selectId;

	/**
	 * 档案树节点
	 */
	@Size(max = 225, message = "文件路径长度过长(不得超过225个字符)")
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
