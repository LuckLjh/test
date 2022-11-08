package com.cescloud.saas.archive.api.modular.syssetting.feign;

import com.cescloud.saas.archive.api.modular.syssetting.dto.FileEncryptionSettingDTO;
import com.cescloud.saas.archive.api.modular.syssetting.dto.SourceSettingDTO;
import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liwei
 */
@FeignClient(contextId = "remoteSysSettingService", value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteSysSettingService {


	/**
	 * 根据文件类型获取文本类型
	 *
	 * @param fileType
	 * @return
	 */
	@GetMapping("/sys-setting/doc-type/{tenantId}/{fileType}")
	R<Integer> getDocType(@PathVariable("tenantId") Long tenantId, @PathVariable("fileType") String fileType,
						  @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取加密设置
	 *
	 * @return
	 */
	@GetMapping("/sys-setting/encrypt-setting/{tenantId}")
	R<FileEncryptionSettingDTO> getEncryptSetting(@PathVariable("tenantId") Long tenantId,
												  @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 初始化 系统参数
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户 ID
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@RequestMapping(value = "/sys-setting/initialize", method = RequestMethod.POST)
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam("tenantId") Long tenantId);

	/**
	 * 获取租户系统参数配置 信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/sys-setting/data/{tenantId}")
	public R<List<ArrayList<String>>> getSystemParameterInfo(@PathVariable("tenantId") Long tenantId);


	/**
	 * @param code
	 * @return com.cescloud.saas.archive.service.modular.common.core.util.R<com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting>
	 * @Description 通过code获取系统参数配置信息
	 * @author qianbaocheng
	 * @date 2020-10-14 13:53
	 */
	@GetMapping(value = "/sys-setting/code/{tenantId}/{code}")
	public R<SysSetting> getSysSettingByTenantIdAndCode(@PathVariable("tenantId") Long tenantId, @PathVariable("code") String code,
														@RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/sys-setting/source-setting/{tenantId}")
	R<SourceSettingDTO> getSourceSetting(@PathVariable("tenantId") Long tenantId,@RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 获取 所有租户配置的全文类型（包括 文本、音频、视频、照片、其他）
	 * @param tenantId
	 * @return
	 */
	@GetMapping("/sys-setting/getAllowType/{tenantId}")
	R<List<String>> getAllowType(@PathVariable("tenantId")  Long tenantId);
}
