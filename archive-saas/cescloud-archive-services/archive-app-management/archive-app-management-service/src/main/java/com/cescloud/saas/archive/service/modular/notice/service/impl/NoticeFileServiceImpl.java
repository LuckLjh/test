package com.cescloud.saas.archive.service.modular.notice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.filecenter.entity.OtherFileStorage;
import com.cescloud.saas.archive.api.modular.notice.dto.NoticeFileDto;
import com.cescloud.saas.archive.api.modular.notice.entity.NoticeFile;
import com.cescloud.saas.archive.api.modular.storage.constants.StorageConstants;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.filecenter.service.OtherFileStorageOpenService;
import com.cescloud.saas.archive.service.modular.notice.mapper.NoticeFileMapper;
import com.cescloud.saas.archive.service.modular.notice.service.NoticeFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author LiShuai
 * @Description: 通知公告附件管理
 * @date 2020/9/28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeFileServiceImpl extends ServiceImpl<NoticeFileMapper, NoticeFile> implements NoticeFileService {


	@Autowired
	private OtherFileStorageOpenService otherFileStorageOpenService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public NoticeFileDto uploadNoticeFile(MultipartFile file) throws ArchiveBusinessException {
		Assert.isTrue(file.getOriginalFilename().length() <= 200, "上传文件名称过长，请将上传文件名称长度调整为200字符以下");
		OtherFileStorage otherFileStorage;
		try {
			otherFileStorage = otherFileStorageOpenService.upload(file, StorageConstants.NOTICE_FILE_STORAGE);
		} catch (Exception e) {
			log.error("上传失败!", e);
			throw new ArchiveBusinessException("上传失败!", e);
		}
		final NoticeFileDto noticeFileDto = new NoticeFileDto();
		noticeFileDto.setFileStorageId(otherFileStorage.getId());
		noticeFileDto.setFileFormat(otherFileStorage.getFileType());
		noticeFileDto.setName(otherFileStorage.getFileSourceName());
		return noticeFileDto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteNoticeFileById(Long id) throws ArchiveBusinessException {
		final NoticeFile noticeFile = getById(id);
		otherFileStorageOpenService.deleteByIds(new Long[]{noticeFile.getFileStorageId()});
		return this.removeById(id);
	}

}
