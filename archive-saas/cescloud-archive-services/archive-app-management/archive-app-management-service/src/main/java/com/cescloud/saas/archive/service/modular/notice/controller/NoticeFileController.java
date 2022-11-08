package com.cescloud.saas.archive.service.modular.notice.controller;

import com.cescloud.saas.archive.api.modular.notice.dto.NoticeDto;
import com.cescloud.saas.archive.api.modular.notice.entity.NoticeFile;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.notice.service.NoticeFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author LiShuai
 * @Description: 公告附件管理
 * @date 2020/9/28
 */
@Api(value = "notice-file", tags = "公告附件管理")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/notice-file")
public class NoticeFileController {

	private final NoticeFileService noticeFileService;


	/***
	 *  上传附件
	 * @param file 附件文件
	 */
	@ApiOperation(value = "上传附件", httpMethod = "POST")
	@SysLog("上传附件")
	@PostMapping("/upload")
	public R uploadNoticeFile(@RequestParam("file") @ApiParam(value = "文件", name = "file", required = true) MultipartFile file) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("上传附件-附件名称【%s】",file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(noticeFileService.uploadNoticeFile(file));
	}

	/***
	 *  删除附件
	 * @param id 附件id
	 */
	@ApiOperation(value = "删除附件", httpMethod = "DELETE")
	@SysLog("删除附件")
	@DeleteMapping("/{id}")
	public R deleteNoticeFile(@PathVariable("id") @ApiParam(value = "附件ID", name = "id", required = true) Long id) throws ArchiveBusinessException {
		try {
			NoticeFile byId = noticeFileService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除附件-附件名称【%s】",byId.getFileName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(noticeFileService.deleteNoticeFileById(id));
	}

}
