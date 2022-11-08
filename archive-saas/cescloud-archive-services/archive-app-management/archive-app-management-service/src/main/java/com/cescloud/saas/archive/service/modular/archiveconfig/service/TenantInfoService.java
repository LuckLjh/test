package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

public interface TenantInfoService  {

	/**
	 * 初始化档案类型模板
	 * @param templateId
	 * @param tenantId
	 * @return
	 */
	R initializeArchive(Long templateId, Long tenantId);

	/**
	 * 初始化档案类型模板
	 * @param templateId
	 * @param tenantId
	 * @return
	 */
	R initializeArchiveType(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 初始化门类配置
	 * @param templateId
	 * @param tenantId
	 * @return
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 清除租户基本配置和门类配置信息
	 * @param tenantId
	 * @return
	 */
	R clearAppBasicConfiguration(Long tenantId);

	/**
	 * 清除租户门类信息
	 * @param tenantId
	 * @return
	 */
	R clearAppArchive(Long tenantId) throws ArchiveBusinessException;
}
