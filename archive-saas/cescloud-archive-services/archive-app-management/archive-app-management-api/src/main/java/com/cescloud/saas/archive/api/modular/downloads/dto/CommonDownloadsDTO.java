package com.cescloud.saas.archive.api.modular.downloads.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author LS
 * @Date 2021/3/8
 */
@Data
public class CommonDownloadsDTO {

	@ApiModelProperty(value = "常用下载主键ID")
	private Long id;

	@NotBlank(message = "标题不能为空")
	@Size(max = 200, message = "标题过长，不能超过200个字符")
	@ApiModelProperty(value = "文件标题")
	private String fileName;

}
