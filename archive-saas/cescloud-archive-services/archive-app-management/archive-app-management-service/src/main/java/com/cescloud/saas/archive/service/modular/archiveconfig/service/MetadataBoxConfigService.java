package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxConfigDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleUndefinedDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataBoxConfig;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface MetadataBoxConfigService extends IService<MetadataBoxConfig> {
    /**
     * 保存盒规则
     * @param metadataBoxConfigDTO
     * @return
     */
    Boolean saveBoxConfig(MetadataBoxConfigDTO metadataBoxConfigDTO);

    MetadataBoxConfigDTO getBoxConfig(String storageLocate,Long moduleId);

    List<MetadataBoxRuleUndefinedDTO> listOfUnDefined(String storageLocate,Long moduleId);

    List<Map<String, Object>> initForm(String typeCode, Long templateTableId,Long moduleId) throws ArchiveBusinessException;

    MetadataBoxRuleDTO getBoxFieldInfo(String storageLocate,Long moduleId) throws ArchiveBusinessException;

    Boolean removeByModuleId(String storageLocate, Long moduleId);

	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap);

	/**
	 * 租户初始化--装盒规则初始化
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户ID
	 * @return R
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	List<ArrayList<String>> getMetadataBoxConfigInfo(Long tenantId) throws ArchiveBusinessException;
}
