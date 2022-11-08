package com.cescloud.saas.archive.api.modular.notice.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author LiShuai
 * @Description:
 * @date 2020/9/28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDto implements Serializable {

	/**
	 * 公告id,主键 false
	 */
	@TableId
	@ApiModelProperty(value = "公告主键ID")
	private Long id;

	/**
	 * 标题 false
	 */
	@ApiModelProperty(value = "标题", required = true)
	private String titleProper;

	/**
	 * 发布状态 false
	 */
	@ApiModelProperty(value = "发布状态（0:未发布;1:已发布;2:已撤销）")
	private Integer issueStatus;

	/**
	 * 网站html页面存放路径（电子文件中心id）
	 */
	@ApiModelProperty(value = "网站html页面存放路径（电子文件中心id）",hidden = true)
	private Long fileStorageId;

	/**
	 * 公告内容 false
	 */
	@ApiModelProperty(value = "公告内容")
	private String content;

	/**
	 * 创建人 true
	 */
	@ApiModelProperty(value = "创建人id")
	private Long createdBy;
	/**
	 * 创建时间 true
	 */
	@ApiModelProperty(value = "创建时间")
	private LocalDateTime createdTime;

	/**
	 * 更新人 true
	 */
	@ApiModelProperty(value = "更新人id")
	private Long updatedBy;
	/**
	 * 更新时间 true
	 */
	@ApiModelProperty(value = "更新时间")
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
	 * 附件列表 true
	 */
	@ApiModelProperty(value = "附件列表")
	private List<NoticeFileDto> noticeFiles;

}
