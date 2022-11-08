
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveColumnRule;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.Map;


/**
 * 档案字段组成规则
 *
 * @author liudong1
 * @date 2019-04-19 15:06:53
 */
public interface ArchiveColumnRuleService extends IService<ArchiveColumnRule> {

	List<DefinedColumnRuleMetadata> listOfDefined(Long metadataSourceId);

	MetadataAutovalue getFlowNoColumn(Long moduleId, String storageLocate, List<DefinedColumnRuleMetadata> definedColumnRuleMetadatas);

	DefinedColumnRuleDTO mapOfUnDefined(String storageLocate, Long metadataSourceId,Long metadataId,Long moduleId);

	boolean saveColumnRuleDefined(SaveColumnRuleMetadata saveColumnRuleMetadata);

	boolean removeByWrappers(Wrapper<ArchiveColumnRule> queryWrapper);

	@CacheEvict(allEntries = true)
	boolean deleteByStorageLocate(String storageLocate);

	/**
	 * 拷贝配置
	 * @param srcStorageLocate
	 * @param destStorageLocate
	 * @param srcDestAutovalueIdMap
	 * @param srcDestMetadataMap
	 */
	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate,
							 Map<Long,Long> srcDestAutovalueIdMap, Map<Long, Long> srcDestMetadataMap);
}
