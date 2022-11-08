
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveSearchMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SearchListDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveSearch;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 档案检索配置
 *
 * @author liudong1
 * @date 2019-05-27 16:52:00
 */
public interface ArchiveSearchService extends IService<ArchiveSearch> {
	/**
	 * 已定义的列表字段列表
	 *
	 * @param searchListDTO 检索实体
	 * @return
	 */
	List<DefinedSearchMetadata> listOfDefined(SearchListDTO searchListDTO);

	List<DefinedSearchMetadata> listBusinessOfDefined(SearchListDTO searchListDTO);

	List<DefinedSearchMetadata> getArchiveListdefList(String storageLocate,Integer searchType,Boolean tagging,Long moduleId);
	/**
	 * 根据表名称获取已定义的列表字段列表
	 *
	 * @param storageLocate 表名称
	 * @param searchType    检索类型
	 * @return
	 */
	List<DefinedSearchMetadata> listOfDefinedByStorageLocate(String storageLocate, Integer searchType, Boolean tagging,Long moduleId);

	/**
	 * 未定义的列表字段列表
	 *
	 * @param searchListDTO 检索类型
	 * @return
	 */
	List<DefinedSearchMetadata> listOfUnDefined(SearchListDTO searchListDTO);

	List<DefinedSearchMetadata> listBusinessOfUnDefined(SearchListDTO searchListDTO);

	/**
	 * 保存档案检查配置
	 *
	 * @param saveSearchMetadata 检索定义保存对象
	 * @return
	 */
	R saveSearchDefined(SaveSearchMetadata saveSearchMetadata);

	/**
	 * 根据存储表名删除检索配置
	 *
	 * @param storageLocate 存储表名
	 */
	boolean deleteByStorageLocate(String storageLocate, Long userId, Integer searchType,Long moduleId);

	/**
	 * 租户初始化--检索配置初始化
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户ID
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取门类信息-检索列表信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getRetrieveDefinitionInfo(Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取基本检索表单
	 *
	 * @param typeCode        档案类型
	 * @param templateTableId 模板表id
	 * @return
	 */
	Object getBasicRetrievalForm(Long moduleId,String typeCode, Long templateTableId);

	/**
	 * 清除用户配置信息
	 *
	 * @param searchListDTO
	 * @return
	 */
	Boolean clearListConfiguration(SearchListDTO searchListDTO);

	/**
	 * 根据模块id和storageLocate 删除 配置信息
	 * @param storageLocate 物理表名
	 * @param moduleId 模块id
	 * @return
	 */
	Boolean removeByModuleId(String storageLocate,Long moduleId);

	/**
	 * 复制到另一模块
	 * @param copyPostDTO
	 * @return
	 */
	R copy(CopyPostDTO copyPostDTO);

	/**
	 * 拷贝配置
	 * @param srcStorageLocate
	 * @param destStorageLocate
	 * @param srcDestMetadataMap
	 */
	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap);

	Map fieldPublicProperties(Map map, String type, String name, String icon, Object options, Long key, String model, List rules);

	Map inputConfiguration();

	Map configConfiguration();



	/**
	 * 根据表名称获取已定义的列表字段列表
	 *
	 * @param storageLocate 表名称
	 * @param searchType    检索类型
	 * @return
	 */
	List<DefinedSearchMetadata> listOfSpecialDefined(SearchListDTO searchListDTO);


	List<DefinedSearchMetadata> listSpecialOfUnDefined(SearchListDTO searchListDTO);


	R saveSpecialSearchDefined(SaveSearchMetadata saveSearchMetadata);
}
