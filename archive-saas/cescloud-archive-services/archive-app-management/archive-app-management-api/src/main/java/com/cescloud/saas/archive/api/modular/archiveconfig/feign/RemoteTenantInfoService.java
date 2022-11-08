package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 初始化表单feign
 *
 * @author zhangxuehu
 */
@FeignClient(contextId = "remoteTenantInfoService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT,path = "/tenant-info")
public interface RemoteTenantInfoService {

	/**
	 * 表单数据初始化
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 */
	@RequestMapping(value = "/form-data", method = RequestMethod.POST)
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId);

	/**
	 * 档案类型模板初始化
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@RequestMapping(value = "/archive-type-template", method = RequestMethod.POST)
	public R initializeArchive(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId);

	/**
	 * 初始化 档案门类信息
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@RequestMapping(value = "/archive-type", method = RequestMethod.POST)
	public R initializeArchiveType(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId) ;

	/**
	 * 清除租户基本配置和门类配置信息
	 * @param tenantId  租户id
	 * @return
	 */
	@DeleteMapping("/remove/basic/{tenantId}")
	public R clearAppBasicConfiguration(@PathVariable(value = "tenantId") Long tenantId);

	/**
	 * 清除租户门类信息
	 * @param tenantId  租户id
	 * @return
	 */
	@DeleteMapping("/remove/archive/{tenantId}")
	public R clearAppArchive(@PathVariable(value = "tenantId") Long tenantId);

}
