package com.cescloud.saas.archive.api.modular.notice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LiShuai
 * @Description: 公告附件实体类
 * @date 2020/9/28
 */
@Data
@TableName("apma_notice_file")
@EqualsAndHashCode(callSuper = true)
public class NoticeFile extends Model<NoticeFile> {
	private static final long serialVersionUID = 1L;

	/**
	 * 公告附件id,主键 false
	 */
	@TableId
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
	private String fileName;

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
