package com.cescloud.saas.archive.service.modular.archivetype.service.rulecompute;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ColumnComputeRuleDTO;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;

/**
 * 根据规则，获取所有字段值
 * @author liwei
 */
public interface ArchiveRuleService {
	/**
	 * 获取规则
	 * @param storageLocate 表名
	 * @return
	 */
	Map<String,List<ColumnComputeRuleDTO>> handleComputeRule(String storageLocate, Long moduleId) throws ArchiveBusinessException;


}
