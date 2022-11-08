package com.cescloud.saas.archive.api.modular.help.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 全局数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
@Data
@TableName("apma_help_items")
@EqualsAndHashCode(callSuper = true)
public class HelpItems extends Model<HelpItems> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键 id
	 */
	@ApiModelProperty("主键")
	@TableId
	private Long id;
	/**
	 * 储存ID file_storage_id
	 */
	@ApiModelProperty("储存ID")
	private Long fileStorageId;
	/**
	 * 储存名字 storage_name
	 */
	@ApiModelProperty("储存名字")
	private String storageName;
	/**
	 * 访问路径 url
	 */
	@ApiModelProperty("访问路径")
	private String url;
	/**
	 * 排序 order_no
	 */
	@ApiModelProperty("排序")
	private Integer orderNo;
	/**
	 * 菜单ID menu_id
	 */
	@ApiModelProperty("菜单ID")
	private Long menuId;
	/**
	 * 帮助文档的版本 help_version
	 */
	@ApiModelProperty("帮助文档的版本")
	private Integer helpVersion;
	/**
	 * 全宗号编码 fonds_code
	 */
	@ApiModelProperty("全宗号编码")
	private String fondsCode;
	/**
	 * 全宗名称 fonds_name
	 */
	@ApiModelProperty("全宗名称")
	private String fondsName;
	/**
	 * 所属租户ID tenant_id
	 */
	@ApiModelProperty("所属租户ID")
	@TableField(fill = FieldFill.INSERT)
	private Long tenantId;
	/**
	 * 乐观锁 revision
	 */
	@ApiModelProperty("乐观锁")
	@TableField(fill = FieldFill.INSERT)
	private Long revision;

	/**
	 * 是否删除 is_delete
	 */
	@ApiModelProperty("是否删除")
	private Integer isDelete;

	/**
	 * 创建人 created_by
	 */
	@ApiModelProperty("创建人")
	@TableField(fill = FieldFill.INSERT)
	private Long createdBy;
	/**
	 * 创建时间 created_time
	 */
	@ApiModelProperty("创建时间")
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createdTime;
	/**
	 * 修改人 updated_by
	 */
	@ApiModelProperty("修改人")
	@TableField(fill = FieldFill.UPDATE)
	private Long updatedBy;
	/**
	 * 更新时间 updated_time
	 */
	@ApiModelProperty("更新时间")
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updatedTime;

}
