
package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SourceDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SourcePostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataSource;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;


/**
 * 参照源数据表
 *
 * @author liwei
 * @date 2019-04-16 14:52:34
 */
public interface MetadataSourceService extends IService<MetadataSource> {

	List<SourceDTO> getMetaDataSources(Long id, String storageLocate , Long metadataId, Long moduleId);

	/**
	 * 根据表名和目标字段id获取分组字段
	 * @param storageLocate 表名
	 * @param targetId 目标累加字段
	 * @return 分组字段集合
	 */
	List<SourceDTO> getMetadataSourcesByStorageAndTargetId(String storageLocate, Long targetId);

	/**
	 * 保存分组规则
	 * @param sourcePostDTO 分组规则
	 * @return
	 * @throws ArchiveBusinessException
	 */
	boolean saveMetadataSource(SourcePostDTO sourcePostDTO) throws ArchiveBusinessException;

	/**
	 * 根据表名获取分组规则
	 * @param storageLocate 表名
	 * @return com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataSource
	 */
	List<MetadataSource> getMetaDataSourceByStorageLocate(String storageLocate);

	/**
	 * 根据字段规则id获取分组规则集合
	 * @param targetId 字段规则id
	 * @return com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataSource
	 */
	List<MetadataSource> getMetaDataSourceByTargetId(Long targetId);

	/**
	 * 根据查询条件删除记录
	 * @param queryWrapper 查询条件
	 * @return true or false
	 */
	boolean removeByWrappers(Wrapper<MetadataSource> queryWrapper);

	boolean deleteByStorageLocate(String storageLocate);

	/**
	 * 配置信息复制
	 * @param srcStorageLocate
	 * @param destStorageLocate
	 * @param srcDestAutovalueIdMap
	 */
	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate,
							 Map<Long,Long> srcDestAutovalueIdMap, Map<Long, Long> srcDestMetadataMap);
}
