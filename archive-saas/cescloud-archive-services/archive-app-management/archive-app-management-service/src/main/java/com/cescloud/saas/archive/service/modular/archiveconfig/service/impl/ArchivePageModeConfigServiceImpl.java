package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.constant.ConfigConstant;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchivePageModeConfig;
import com.cescloud.saas.archive.common.constants.ArchiveConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchivePageModeConfigMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchivePageModeConfigService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 分页方式配置
 *
 * @author LS
 * @date 2021/12/7
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "archive-page-mode")
public class ArchivePageModeConfigServiceImpl extends ServiceImpl<ArchivePageModeConfigMapper, ArchivePageModeConfig> implements ArchivePageModeConfigService {

	@Autowired
	private ArchiveTableService archiveTableService;

	@Override
	@Cacheable(key = "'archive-app-management:archive-page-mode:defined' + #typeCode + ':'+ #templateTableId + ':' + #moduleId",
			unless = "#result == null"
	)
	public ArchivePageModeConfig getArchivePageModeConfig(Long templateTableId, String typeCode, Long moduleId) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		ArchivePageModeConfig paginationConfig = this.getOne(Wrappers.<ArchivePageModeConfig>lambdaQuery().eq(ArchivePageModeConfig::getStorageLocate, storageLocate)
				.eq(ArchivePageModeConfig::getModuleId, moduleId).select(ArchivePageModeConfig::getPageMode));
		if (ObjectUtil.isNull(paginationConfig)){
			paginationConfig = this.getOne(Wrappers.<ArchivePageModeConfig>lambdaQuery().eq(ArchivePageModeConfig::getStorageLocate, storageLocate)
					.eq(ArchivePageModeConfig::getModuleId, ArchiveConstants.PUBLIC_MODULE_FLAG).select(ArchivePageModeConfig::getPageMode));
		}
		if (ObjectUtil.isNull(paginationConfig)){
			paginationConfig = ArchivePageModeConfig.builder().pageMode(ConfigConstant.SHOW_TOTAL_OF_PAGES).build();
		}
		return paginationConfig;
	}

	@Override
	public ArchivePageModeConfig getArchivePageModeConfigByStorageLocate(String storageLocate, Long moduleId) {
		return this.getOne(Wrappers.<ArchivePageModeConfig>lambdaQuery().eq(ArchivePageModeConfig::getStorageLocate, storageLocate)
				.eq(ArchivePageModeConfig::getModuleId, moduleId).select(ArchivePageModeConfig::getPageMode));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveArchivePageModeConfig(Integer pageMode, Long moduleId, String storageLocate) {
		ArchivePageModeConfig paginationConfig = this.getOne(Wrappers.<ArchivePageModeConfig>lambdaQuery().eq(ArchivePageModeConfig::getStorageLocate, storageLocate)
				.eq(ArchivePageModeConfig::getModuleId, moduleId));
		if (ObjectUtil.isNull(paginationConfig)){
			paginationConfig = ArchivePageModeConfig.builder().storageLocate(storageLocate).moduleId(moduleId).build();
		}
		paginationConfig.setPageMode(pageMode);
		this.saveOrUpdate(paginationConfig);
	}

}
