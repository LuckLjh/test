package com.cescloud.saas.archive.api.modular.fwimp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.Size;

@Data
@TableName("oa_column_extend")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OaColumnExpand extends Model<OaColumnExpand> {
	/**
	 * ID,主键
	 */
	@TableId
	@ApiModelProperty(value = "ID", example = "1")
	private Long id;
	/**
	 * 校验方式 ：1：字段表达式匹配；2：表中某列匹配
	 */
	@Size(max = 1, message = "校验方式长度过长(不得超过1个字符)")
	@ApiModelProperty(value = "校验方式",example = "0")
	private int flag;
	/**
	 * owner_ID,外键
	 * 主表条目 id  OA_IMPORT.ID = OA_COLUMN.OWNER_ID
	 * OA_IMPORT.ID = OA_LOG.OWNER_ID
	 */
	@ApiModelProperty(value = "ownerId", example = "1")
	private Long ownerId;

	/**
	 * 前台条件 false
	 */
	@ApiModelProperty("前台条件")
	@TableField(fill = FieldFill.UPDATE)
	private Object pageCondition;

	/**
	 * 后台查询条件 false
	 */
	@ApiModelProperty("后台查询条件")
	@TableField(fill = FieldFill.UPDATE)
	private Object backCondition;
	/**
	 * owner_flowId,所属流程id
	 */
	@ApiModelProperty(value = "owner_flowid", example = "1")
	private Long ownerFlowid;
	/**
	 * ownerColumn,所属流程列
	 */
	@ApiModelProperty(value = "ownerColumn", example = "1")
	private String ownerColumn;
	/**
	 * 表名
	 */
	@ApiModelProperty(value = "targetTableColumn", example = "t_xx")
	private String targetTableName;

	/**
	 * targetFieldName,目标列的字段
	 */
	@ApiModelProperty(value = "targetFieldName", example = "t_xx.id")
	private String targetFieldName;

	/**
	 * relevanceFieldName ,转换列的字段
	 */
	@ApiModelProperty(value = "relevanceFieldName", example = "t_xx.id")
	private String relevanceFieldName;
	/**
	 *实际值
	 */
	@ApiModelProperty(value = "titleValue", example = "deme")
	private String titleValue;
	/**
	 *页面展示列
	 */
	@ApiModelProperty(value = "titleKey", example = "deme")
	private String titleKey;

}
