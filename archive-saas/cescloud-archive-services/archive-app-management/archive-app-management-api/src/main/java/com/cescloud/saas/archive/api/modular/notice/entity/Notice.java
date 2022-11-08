package com.cescloud.saas.archive.api.modular.notice.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.cescloud.saas.archive.api.modular.notice.dto.NoticeFileDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author LiShuai
 * @Description: 通知公告实体类
 * @date 2020/9/28
 */
@Data
@TableName("apma_notice")
@EqualsAndHashCode(callSuper = true)
public class Notice extends Model<Notice> {
	private static final long serialVersionUID = 1L;

	/**
	 * 公告id,主键 false
	 */
	@TableId
	@ApiModelProperty(value = "公告主键ID")
	private Long id;

	/**
	 * 标题 false
	 */
	@NotBlank(message = "标题不能为空")
	@Size(max = 100 ,message = "标题过长")
	@ApiModelProperty(value = "标题", required = true)
	private String titleProper;

	/**
	 * 发布状态 false
	 */
	@ApiModelProperty(value = "发布状态（0:未发布;1:已发布;2:已撤销）")
	private Integer issueStatus;

	/**
	 * 公告内容 false
	 */
	@TableField(exist = false)
	@ApiModelProperty(value = "公告内容")
	private String content;

	/**
	 * 网站html页面存放路径（电子文件中心id）
	 */
	@TableField(updateStrategy = FieldStrategy.IGNORED)
	@ApiModelProperty(value = "网站html页面存放路径（电子文件中心id）",hidden = true)
	private Long fileStorageId;

	/**
	 * 租户ID
	 */
	@TableField(fill = FieldFill.INSERT)
	@ApiModelProperty(value = "租户ID", hidden = true)
	private Long tenantId;

	/**
	 * 创建人 true
	 */
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value = "创建人",hidden = true)
	private Long createdBy;
	/**
	 * 创建时间 true
	 */
	@TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_EMPTY)
	@ApiModelProperty(value = "创建时间",hidden = true)
	private LocalDateTime createdTime;
	/**
	 * 更新人 true
	 */
	@TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty(value = "更新人",hidden = true)
	private Long updatedBy;
	/**
	 * 更新时间 true
	 */
	@TableField(fill = FieldFill.UPDATE)
	@ApiModelProperty(value = "更新时间",hidden = true)
	private LocalDateTime updatedTime;

	/**
	 * 部门名称 true
	 */
	@ApiModelProperty(value = "部门名称")
	private String deptName;

	/**
	 * 点击量
	 */
	@ApiModelProperty(value = "点击量")
	private Integer numberOfHits;

	/**
	 * 附件集合 true
	 */
	@TableField(exist = false)
	@ApiModelProperty(value = "附件")
	private List<NoticeFileDto> noticeFiles;

}
