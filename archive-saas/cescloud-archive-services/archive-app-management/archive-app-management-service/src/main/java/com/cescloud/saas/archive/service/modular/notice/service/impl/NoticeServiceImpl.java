package com.cescloud.saas.archive.service.modular.notice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.filecenter.entity.OtherFileStorage;
import com.cescloud.saas.archive.api.modular.notice.dto.NoticeDto;
import com.cescloud.saas.archive.api.modular.notice.dto.NoticeFileDto;
import com.cescloud.saas.archive.api.modular.notice.entity.Notice;
import com.cescloud.saas.archive.api.modular.notice.entity.NoticeFile;
import com.cescloud.saas.archive.api.modular.storage.constants.StorageConstants;
import com.cescloud.saas.archive.common.constants.NoticeIssueStatusEnum;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.localcache.LocalCacheable;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filecenter.service.OtherFileStorageOpenService;
import com.cescloud.saas.archive.service.modular.notice.mapper.NoticeMapper;
import com.cescloud.saas.archive.service.modular.notice.service.NoticeFileService;
import com.cescloud.saas.archive.service.modular.notice.service.NoticeService;
import com.cescloud.saas.archive.service.modular.notice.service.helper.NoticeServiceHelper;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LiShuai
 * @Description: 通知公告管理
 * @date 2020/9/28
 */
@Service
@Slf4j
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

	@Autowired
	private OtherFileStorageOpenService otherFileStorageOpenService;

	@Autowired
	private NoticeFileService noticeFileService;

	@Autowired
	private NoticeServiceHelper noticeServiceHelper;


	@Override
	public IPage<Notice> getPage(long current, long size, String titleProper) {
		LambdaQueryWrapper<Notice> queryWrapper = Wrappers.lambdaQuery();
		if (StrUtil.isNotBlank(titleProper)) {
			queryWrapper.like(Notice::getTitleProper, StrUtil.trim(titleProper));
		}
		return this.page(new Page<>(current, size), queryWrapper);
	}

	@Override
	public IPage<Notice> getMainPage(long current, long size, String titleProper) {
		LambdaQueryWrapper<Notice> queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(Notice::getIssueStatus, NoticeIssueStatusEnum.PUBLISHED.getValue());
		if (StrUtil.isNotBlank(titleProper)) {
			queryWrapper.like(Notice::getTitleProper, StrUtil.trim(titleProper));
		}
		queryWrapper.orderByDesc(Notice::getUpdatedTime);
		return this.page(new Page<>(current, size), queryWrapper);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveNotice(Notice notice) throws ArchiveBusinessException {
		if (StrUtil.isNotBlank(notice.getContent())) {
			OtherFileStorage otherFileStorage = uploadContentFile(notice.getContent());
			notice.setFileStorageId(otherFileStorage.getId());
		}
		notice.setIssueStatus(NoticeIssueStatusEnum.UNPUBLISHED.getValue());
		notice.setDeptName(SecurityUtils.getUser().getDeptName());
		boolean result = this.save(notice);
		List<NoticeFileDto> noticeFileList = notice.getNoticeFiles();
		if (CollUtil.isNotEmpty(noticeFileList)) {
			List<NoticeFile> collect = noticeFileList.stream().map(e -> {
				NoticeFile noticeFile = new NoticeFile();
				BeanUtil.copyProperties(e, noticeFile);
				noticeFile.setFileName(e.getName());
				noticeFile.setNoticeId(notice.getId());
				return noticeFile;
			}).collect(Collectors.toList());
			noticeFileService.saveBatch(collect);
		}
		return new R<>(result);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R updateNotice(Notice notice) throws ArchiveBusinessException {
		if (NoticeIssueStatusEnum.PUBLISHED.getValue().equals(notice.getIssueStatus())) {
			return new R<>().fail(Boolean.FALSE, "不能修改已发布的公告");
		}
		//删除原公告内容文件信息
		if (ObjectUtil.isNotNull(notice.getFileStorageId())) {
			otherFileStorageOpenService.deleteByIds(new Long[]{notice.getFileStorageId()});
		}
		if (StrUtil.isNotBlank(notice.getContent())) {
			OtherFileStorage otherFileStorage = uploadContentFile(notice.getContent());
			notice.setFileStorageId(otherFileStorage.getId());
		} else {
			notice.setFileStorageId(null);
		}
		//修改通知公告表信息
		boolean result = this.updateById(notice);
		//删除该公告的附件信息
		noticeFileService.remove(Wrappers.<NoticeFile>lambdaQuery().eq(NoticeFile::getNoticeId, notice.getId()));
		final List<NoticeFileDto> noticeFileList = notice.getNoticeFiles();
		if (CollUtil.isNotEmpty(noticeFileList)) {
			List<NoticeFile> collect = noticeFileList.stream().map(e -> {
				NoticeFile noticeFile = new NoticeFile();
				BeanUtil.copyProperties(e, noticeFile);
				noticeFile.setFileName(e.getName());
				noticeFile.setNoticeId(notice.getId());
				return noticeFile;
			}).collect(Collectors.toList());
			//保存通知公告附件
			noticeFileService.saveBatch(collect);
		}
		return new R<>(result);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R removeNoticeById(Long noticeId) throws ArchiveBusinessException {
		//删除公告其他附件文件信息
		final NoticeDto noticeDto = getNoticeById(noticeId);
		List<Long> idList = CollUtil.newLinkedList();
		if (ObjectUtil.isNotNull(noticeDto.getFileStorageId())) {
			idList.add(noticeDto.getFileStorageId());
		}
		List<NoticeFileDto> noticeFiles = noticeDto.getNoticeFiles();
		if (CollUtil.isNotEmpty(noticeFiles)) {
			noticeFiles.forEach(e -> idList.add(e.getFileStorageId()));
		}
		if (CollUtil.isNotEmpty(idList)) {
			Long[] ids = idList.toArray(new Long[0]);
			otherFileStorageOpenService.deleteByIds(ids);
		}
		//删除附件信息
		noticeFileService.remove(Wrappers.<NoticeFile>lambdaQuery().eq(NoticeFile::getNoticeId, noticeId));
		//删除通知公告信息
		return new R<>(removeById(noticeId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R releaseNotice(Long noticeId) {
		final Notice notice = this.getById(noticeId);
		if (ObjectUtil.isNull(notice)) {
			return new R<>().fail(Boolean.FALSE, "没有找到对应的系统公告！");
		}
		if (NoticeIssueStatusEnum.PUBLISHED.getValue().equals(notice.getIssueStatus())) {
			return new R<>().fail(Boolean.FALSE, "该公告已发布！");
		}
		notice.setIssueStatus(NoticeIssueStatusEnum.PUBLISHED.getValue());
		return new R<>(updateById(notice));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R revocationNotice(Long noticeId) {
		final Notice notice = this.getById(noticeId);
		if (ObjectUtil.isNull(notice)) {
			return new R<>().fail(Boolean.FALSE, "没有找到对应的系统公告！");
		}
		if (!NoticeIssueStatusEnum.PUBLISHED.getValue().equals(notice.getIssueStatus())) {
			return new R<>().fail(Boolean.FALSE, "只能撤销已发布的公告！");
		}
		notice.setIssueStatus(NoticeIssueStatusEnum.REVOKED.getValue());
		return new R<>(updateById(notice));

	}

	@Override
	public NoticeDto getNoticeById(Long noticeId) throws ArchiveBusinessException {
		final NoticeDto noticeDto = baseMapper.getNoticeById(noticeId);
		try {
			if (ObjectUtil.isNotNull(noticeDto.getFileStorageId())) {
				@Cleanup InputStream inputStream = otherFileStorageOpenService.getInputStream(noticeDto.getFileStorageId(), SecurityUtils.getUser().getTenantId());
				String content = IoUtil.read(inputStream, CharsetUtil.UTF_8);
				noticeDto.setContent(content);
			} else {
				noticeDto.setContent("");
			}
		} catch (Exception e) {
			log.error("获取公告内容失败！", e);
			throw new ArchiveBusinessException("获取公告内容失败！", e);
		}
		return noticeDto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public NoticeDto readNotice(Long noticeId) throws ArchiveBusinessException {
		noticeServiceHelper.updateNumberOfHits(noticeId);
		return getNoticeById(noticeId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> uploadFile(MultipartFile file) throws Exception {
		Map<String, Object> resultMap = MapUtil.newHashMap(3);
		final NoticeFileDto noticeFile = noticeFileService.uploadNoticeFile(file);
		final Long tenantId = SecurityUtils.getUser().getTenantId();
		String objectUrl = otherFileStorageOpenService.getObjectUrl(noticeFile.getFileStorageId(), tenantId);
		resultMap.put("fileId", noticeFile.getFileStorageId());
		resultMap.put("fileName", noticeFile.getName());
		resultMap.put("imgUrl", objectUrl);
		return resultMap;
	}

	/**
	 * 将公告内容保存为html文件上传
	 **/
	private OtherFileStorage uploadContentFile(String noticeContent) throws ArchiveBusinessException {
		File file = null;
		OtherFileStorage otherFileStorage;
		try {
			file = File.createTempFile("notice", ".html");
			FileUtil.appendUtf8String(noticeContent, file);
			@Cleanup InputStream inputStream = new FileInputStream(file);
			otherFileStorage = new OtherFileStorage();
			otherFileStorage.setName(file.getName());
			otherFileStorage.setFileSize(file.length());
			otherFileStorage.setFileSourceName(file.getName());
			otherFileStorage.setFileType("html");
			otherFileStorage.setParentPath(StorageConstants.NOTICE_FILE_STORAGE);
			otherFileStorage.setContentType(ContentType.TEXT_HTML.getValue());
			otherFileStorage = otherFileStorageOpenService.upload(inputStream, otherFileStorage);
		} catch (Exception e) {
			log.error("公告内容文件上传失败", e);
			throw new ArchiveBusinessException("公告内容文件上传异常", e);
		} finally {
			FileUtil.del(file);
		}
		return otherFileStorage;
	}

}
