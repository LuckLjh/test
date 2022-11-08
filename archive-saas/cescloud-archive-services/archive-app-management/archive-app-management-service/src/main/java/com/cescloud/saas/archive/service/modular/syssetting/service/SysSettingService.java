package com.cescloud.saas.archive.service.modular.syssetting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.syssetting.dto.*;
import com.cescloud.saas.archive.api.modular.syssetting.entity.SysSetting;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liwei
 */
public interface SysSettingService extends IService<SysSetting> {

	/**
	 * 系统参数设置列表查询（不分页）
	 *
	 * @param keyword 检索关键字
	 * @return Map<String, Object>
	 */
	Map<String, Object> getSysSettingList(String keyword);


	/**
	 * 根据code查询系统参数设置
	 *
	 * @param code
	 * @return
	 */
	SysSetting getSysSettingByCode(String code);

	/**
	 * 根据tenantId、code查询系统参数设置
	 * @param tenantId
	 * @param code
	 * @return
	 */
	SysSetting getSysSettingByTenantIdAndCode(Long tenantId, String code);

	FileEncryptionSettingDTO getEncryptSetting(Long tenantId);

	/**
	 * 修改系统参数设置
	 *
	 * @param map 参数map
	 * @return
	 */
	R updateSysSetting(Map<String, Object> map);

	Integer getDocType(Long tenantId, String fileType);

	/**
	 * 获取允许上传的全文类型
	 * @param tenantId
	 * @return
	 */
	List<String> getAllowType(Long tenantId);

	/**
	 * 初始化 系统参数设置
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户ID
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取租户系统参数设置
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getSystemParameterInfo(Long tenantId);


	/**
	 *系统初始化时获取系统配置参数信息进行缓存
	 */
	List<SysSetting> getSystemSettingInfo();

	/**
	 *系统初始化时获取系统配置参数信息进行缓存
	 */
	List<SysSetting> getSystemSettingInfo(Long tenantId);
	/**
	 * 保存全文加密设置
	 * @param dto
	 * @return
	 */
	R  saveFileEncryptionSetting(FileEncryptionSettingDTO dto);

	/**
	 * 用户锁定时间
	 * @param tenantId
	 * @return
	 */
	Integer getUserLockTime(Long tenantId);

	/**
	 * 用户密码最大出错次数
	 * @param tenantId
	 * @return
	 */
	Integer getPasswordErrorTimes(Long tenantId);

	/**
	 * 用户密码错误间隔时间
	 * @param tenantId
	 * @return
	 */
	Integer getPasswordErrorSeparation(Long tenantId);

	boolean saveFileComvertSetting(FileConvertSettingDTO dto);

	FileConvertSettingDTO getFileComvertSetting();

	SystemNameSetDTO getSystemNameSet();

	boolean enabledFileComvertSetting(Integer enabled);

    Boolean saveEmailSetting(EmailSettingDTO emailSettingDTO);

	SystemNameSetDTO saveSystemNameSet(SystemNameSetDTO systemNameSetDTO);

	SystemNameSetDTO uploadSystemNameSet(MultipartFile file);

	Boolean saveNoteSetting(NoteSettingDTO noteSettingDTO);

	SourceSettingDTO getSourceSetting(Long tenantId);

	Boolean saveSystemWatermarkSetting(SystemWatermarkDTO systemWatermarkDTO);
}
