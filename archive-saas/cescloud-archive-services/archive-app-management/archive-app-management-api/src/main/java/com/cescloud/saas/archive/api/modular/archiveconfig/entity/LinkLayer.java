
package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 挂接目录规则配置
 *
 * @author liudong1
 * @date 2019-05-14 10:33:56
 */
@ApiModel("挂接目录规则配置")
@Data
@TableName("apma_config_link_layer")
//@KeySequence("SEQ_APMA_LINK_LAYER")
@EqualsAndHashCode(callSuper = true)
public class LinkLayer extends Model<LinkLayer> {

	private static final long serialVersionUID = 1L;

	/**
	 * 挂接规则id,主键 false
	 */
	@ApiModelProperty("挂接规则id,主键")
	@TableId
	private Long id;
	/**
	 * 档案类型层级表名 false
	 */
	@NotNull(message = "档案类型层级表名不能为空")
	@ApiModelProperty("档案类型层级表名")
	private String storageLocate;
	/**
	 * 节点名称 false
	 */
	@ApiModelProperty("节点名称")
	private String name;
	/**
	 * 父节点ID false
	 */
	@ApiModelProperty("父节点ID")
	private Long parentId;
	/**
	 * 父节点ID集合
	 */
	@ApiModelProperty("父节点ID集合")
	private String parentIds;
	/**
	 * 根节点关联的是文件中心文件夹ID false
	 */
	@ApiModelProperty("关联id")
	private Long relationId;
	/**
	 * 文件或文件夹
	 */
	@ApiModelProperty("文件或文件夹：1文件存储路径配置，0文件名设置， 2文件下载命名设置")
	private Integer isDir;

	/**
	 * 模块ID false
	 */
	@NotNull(message = "模块id不能为空")
	@ApiModelProperty("模块id")
	private Long moduleId;
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

	@ApiModelProperty(value = "层次对应的规则列表", hidden = true)
	@TableField(exist = false)
	private List<DefinedColumnRuleMetadata> linkColumnRule = new ArrayList<>();

}
