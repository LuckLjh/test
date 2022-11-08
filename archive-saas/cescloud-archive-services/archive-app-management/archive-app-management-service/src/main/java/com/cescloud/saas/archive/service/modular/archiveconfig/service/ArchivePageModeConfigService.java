package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchivePageModeConfig;

/**
 * @author LS
 * @date 2021/12/7
 */
public interface ArchivePageModeConfigService extends IService<ArchivePageModeConfig> {


	/**
	 * 获取分页方式配置
	 *
	 * @param templateTableId 表模板id
	 * @param typeCode 档案类型code
	 * @param moduleId 模块id
	 * @return ArchivePageModeConfig 分页方式配置
	 */
	ArchivePageModeConfig getArchivePageModeConfig(Long templateTableId, String typeCode, Long moduleId);

	/**
	 * 获取分页方式配置
	 *
	 * @param storageLocate 表名
	 * @param moduleId 模块id
	 * @return ArchivePageModeConfig 分页方式配置
	 */
	ArchivePageModeConfig getArchivePageModeConfigByStorageLocate(String storageLocate, Long moduleId);


	/**
	 * 更新分页方式配置
	 *
	 * @param pageMode 分页方式
	 * @param moduleId 模块id
	 * @param storageLocate 表名
	 */
	void saveArchivePageModeConfig(Integer pageMode, Long moduleId, String storageLocate);
}
