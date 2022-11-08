
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveListTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.TagList;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;


/**
 * 标签列表配置
 *
 * @author liudong1
 * @date 2019-05-27 15:20:07
 */
public interface TagListService extends IService<TagList> {

	List<DefinedListTag> listOfDefined();

	List<DefinedListTag> listOfUnDefined();

	R saveListDefined(SaveListTag saveListMetadata);

	/**
	 * 初始化标签列表配置
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取元数据检索列表配置信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getMetadataTagsSearchListInfo(Long tenantId);
}
