package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveConfigManage;

import java.util.List;
import java.util.Map;

public interface ArchiveConfigManageService extends IService<ArchiveConfigManage> {

	List<Map<String,Object>> getArchiveConfigManageList(String storageLocate,Long sysId);

	Boolean save(String storageLocate,Long moduleId,Integer typedef);

	Boolean saveBatchByModuleIds(String storageLocate, List<Long> moduleIds, Integer typedef);

	Boolean update(String storageLocate, Long moduleId, Integer typedef, Integer isDefine);

	/**
	 * 根据模块id 校验 该模块是否定义
	 * @param moduleId
	 * @param storageLocate
	 * @param typedef
	 * @return
	 */
	Boolean checkModuleIsDefined(Long moduleId, String storageLocate, Integer typedef);

	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate);

	Boolean saveInit(String storageLocate, Long moduleId, Integer value, Long tenantId);

	}
