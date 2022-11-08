package com.cescloud.saas.archive.service.modular.downloads.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.downloads.dto.CommonDownloadsDTO;
import com.cescloud.saas.archive.api.modular.downloads.entity.CommonDownloads;
import com.cescloud.saas.archive.api.modular.filecenter.entity.OtherFileStorage;
import com.cescloud.saas.archive.api.modular.fileclient.status.FileStorageConstant;
import com.cescloud.saas.archive.api.modular.storage.constants.StorageConstants;
import com.cescloud.saas.archive.common.config.MinioAccessTimeProperties;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.tenantfilter.TenantContextHolder;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.downloads.mapper.CommonDownloadsMapper;
import com.cescloud.saas.archive.service.modular.downloads.service.CommonDownloadsService;
import com.cescloud.saas.archive.service.modular.filecenter.service.OtherFileStorageCommonService;
import com.cescloud.saas.archive.service.modular.filecenter.service.OtherFileStorageOpenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 常用下载Service实现
 *
 * @author LS
 * @Date 2021/3/8
 */
@Slf4j
@Service
@AllArgsConstructor
public class CommonDownloadsServiceImpl extends ServiceImpl<CommonDownloadsMapper, CommonDownloads> implements CommonDownloadsService {

	private final OtherFileStorageOpenService otherFileStorageOpenService;

	private final OtherFileStorageCommonService otherFileStorageCommonService;

	private final MinioAccessTimeProperties minioAccessTimeProperties;


	@Override
	public IPage<CommonDownloads> getPage(Page<CommonDownloads> page, String keyword) {
		final LambdaQueryWrapper<CommonDownloads> wrapper = Wrappers.lambdaQuery();
		wrapper.orderByDesc(CommonDownloads::getDownloadTimes);
		if (StrUtil.isNotBlank(keyword)) {
			wrapper.like(CommonDownloads::getFileName, StrUtil.trim(keyword));
		}
		return this.page(page, wrapper);
	}

	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(cacheNames = "common-downloads", allEntries = true)
	@Override
	public Boolean upload(MultipartFile file) throws ArchiveBusinessException {
		Assert.isTrue(file.getOriginalFilename().length() <= 200, "上传文件名称过长，请将上传文件名称长度调整为200字符以下");
		OtherFileStorage otherFileStorage;
		try {
			otherFileStorage = otherFileStorageOpenService.upload(file, StorageConstants.COMMON_DOWNLOADS);
		} catch (Exception e) {
			log.error("文件上传失败!", e);
			throw new ArchiveBusinessException("文件上传失败!", e);
		}
		String fileSourceName = StrUtil.subBefore(otherFileStorage.getFileSourceName(), StrUtil.DOT, true);
		CommonDownloads commonDownloads = CommonDownloads.builder().fileName(fileSourceName).downloadTimes(0)
				.createdUserName(SecurityUtils.getUser().getChineseName()).fileSize(otherFileStorage.getFileSize())
				.fileStorageId(otherFileStorage.getId()).build();
		return this.save(commonDownloads);
	}

	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(cacheNames = "common-downloads", allEntries = true)
	@Override
	public Boolean deleteById(Long id) throws ArchiveBusinessException {
		final CommonDownloads commonDownloads = this.getById(id);
		otherFileStorageOpenService.deleteByIds(new Long[]{commonDownloads.getFileStorageId()});
		return this.removeById(id);
	}

	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(cacheNames = "common-downloads", allEntries = true)
	@Override
	public Boolean updateById(CommonDownloadsDTO commonDownloads) {
		return this.updateById(CommonDownloads.builder().id(commonDownloads.getId()).fileName(commonDownloads.getFileName()).build());
	}

	@Override
	@CacheEvict(cacheNames = "common-downloads", allEntries = true)
	public String download(Long id) throws Exception {
		final CommonDownloads commonDownloads = getById(id);
		final Long tenantId = TenantContextHolder.getTenantId();
		final Long fileStorageId = commonDownloads.getFileStorageId();
		this.updateById(CommonDownloads.builder().id(id).downloadTimes(commonDownloads.getDownloadTimes() + 1).build());
		Map<String, String> reqParams = new HashMap<>();
		reqParams.put("response-content-type", "application/octet-stream");
		reqParams.put("response-content-disposition", "attachment");
		reqParams.put("response-cache-control", "No-cache");
		reqParams.put(FileStorageConstant.isEncrypt, "0");
		OtherFileStorage fileStorage = otherFileStorageCommonService.getById(tenantId, fileStorageId);
		if (ObjectUtil.isNull(fileStorage)) {
			throw new ArchiveRuntimeException("没有找到该文件！");
		}
		String storageLocate = fileStorage.getFileStorageLocate();
		return otherFileStorageCommonService.getTimeLimitUrl(tenantId, fileStorage.getBucketName(), storageLocate,
				minioAccessTimeProperties.getBrowsingAccessTime(), reqParams);
	}

	@Override
	@Cacheable(cacheNames = "common-downloads", key = "#size")
	public IPage<CommonDownloads> getMainPage(Long size) {
		Page<CommonDownloads> page = new Page<>();
		page.setCurrent(1L);
		page.setSize(size);
		return getPage(page,"");
	}

}
