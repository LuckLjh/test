
package com.cescloud.saas.archive.service.modular.metadata.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.service.modular.common.core.util.R;

import java.util.ArrayList;
import java.util.List;


/**
 * 档案层次的元数据标签
 *
 * @author liudong1
 * @date 2019-05-23 15:37:43
 */
public interface MetadataTagService extends IService<MetadataTag> {

	List<MetadataTag> listByTenantId(Long tenantId);

	IPage<MetadataTag> page(IPage<MetadataTag> page, String keyword);

	List<Layer> getLayerCommonTree();

	/**
	 * 获取租户元数据信息
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getMetadataTagsInfo(Long tenantId);

	/**
	 * 初始化 元数据
	 * @param templateId
	 * @param tenantId
	 * @return
	 */
	R initializeMetadata(Long templateId, Long tenantId);

	boolean removeMetadataTagById(Long id);
}
