package com.cescloud.saas.archive.api.modular.notice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * @author LiShuai
 * @Description:
 * @date 2020/9/29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeFileDto implements Serializable {

	/**
	 * 公告附件id,主键 false
	 */
	@ApiModelProperty(value = "公告附件主键ID")
	private Long id;

	/**
	 * 载体路径位置(电子文件中心id)
	 */
	@ApiModelProperty(value = "载体路径位置(电子文件中心id)")
	private Long fileStorageId;

	/**
	 * 文件名称 false
	 */
	@ApiModelProperty(value = "文件名称")
	private String name;

	/**
	 * 文件格式 false
	 */
	@ApiModelProperty(value = "文件格式")
	private String fileFormat;

	/**
	 * 所属公告ID
	 */
	@ApiModelProperty(value = "所属公告ID")
	private Long noticeId;
}
