
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveSearchTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.TagSearch;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;


/**
 * 标签检索配置
 *
 * @author liudong1
 * @date 2019-05-27 15:55:01
 */
public interface TagSearchService extends IService<TagSearch> {

	List<DefinedSearchTag> listOfDefined();

	List<DefinedSearchTag> listOfUnDefined();

	R saveSearchDefined(SaveSearchTag saveSearchTag);

	/**
	 * 初始化标签检索配置
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取租户 元数据标签检索字段配置
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getMetadataTagsSearchFieldInfo(Long tenantId);

	Object getBasicRetrievalForm(Long tenantId);
}
