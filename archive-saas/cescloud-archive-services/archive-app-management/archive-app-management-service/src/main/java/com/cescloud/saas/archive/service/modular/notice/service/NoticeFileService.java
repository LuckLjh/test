package com.cescloud.saas.archive.service.modular.notice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.notice.dto.NoticeFileDto;
import com.cescloud.saas.archive.api.modular.notice.entity.NoticeFile;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通知公告附件管理
 *
 * @author LiShuai
 * @date 2020/9/28
 */
public interface NoticeFileService extends IService<NoticeFile> {

	/***
	 *  上传附件
	 * @param file 附件文件
	 */
	NoticeFileDto uploadNoticeFile(MultipartFile file) throws ArchiveBusinessException;

	/***
	 *  根据公告附件ID删除附件
	 * @param id 公告附件ID
	 * @return
	 */
	Boolean deleteNoticeFileById(Long id) throws ArchiveBusinessException;

}
