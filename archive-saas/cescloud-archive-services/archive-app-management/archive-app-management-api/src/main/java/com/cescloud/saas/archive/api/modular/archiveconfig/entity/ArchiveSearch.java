
package com.cescloud.saas.archive.api.modular.archiveconfig.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 档案检索配置
 *
 * @author liudong1
 * @date 2019-05-27 16:52:00
 */
@Data
@TableName("apma_config_archive_search")
//@KeySequence("SEQ_APMA_ARCHIVE_SEARCH")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveSearch extends Model<ArchiveSearch> {
	private static final long serialVersionUID = 1L;

	/**
	 * 列表数据id,主键 false
	 */
	@ApiModelProperty("列表数据id,主键")
	@TableId
	private Long id;
	/**
	 * 档案层级存储表 false
	 */
	@ApiModelProperty("档案层级存储表")
	private String storageLocate;
	/**
	 * 元数据标签ID false
	 */
	@ApiModelProperty("元数据标签ID")
	private Long metadataId;
	/**
	 * 条件方式：= like >= false
	 */
	@ApiModelProperty("条件方式：= like >=")
	private String conditionType;
	/**
	 * 元数据编号，针对多字段排序的情况 false
	 */
	@ApiModelProperty("元数据编号，针对多字段排序的情况")
	private Integer sortNo;
	/**
	 * 用户ID false
	 */
	@ApiModelProperty("用户ID")
	private Long userId;
	/**
	 * 模块ID false
	 */
	@ApiModelProperty("模块id")
	private Long moduleId;
	/**
	 * 检索类型：1：快速检索 2：基本检索 false
	 */
	@ApiModelProperty("检索类型：1：快速检索 2：基本检索")
	private Integer searchType;

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

	/**
	 * 检索定义 0编码  1名称
	 */
	@ApiModelProperty("检索定义 0编码  1名称")
	private Integer dictKeyValue;
}
