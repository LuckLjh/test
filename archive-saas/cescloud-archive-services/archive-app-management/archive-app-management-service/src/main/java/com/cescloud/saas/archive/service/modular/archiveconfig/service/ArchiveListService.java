
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveListMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveList;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 档案列表配置
 *
 * @author liudong1
 * @date 2019-04-18 21:12:08
 */
public interface ArchiveListService extends IService<ArchiveList> {
	/**
	 * 已定义的列表字段列表
	 *
	 * @param storageLocate 存储表名
	 * @param moduleId      模块 id
	 * @return
	 */
	List<DefinedListMetadata> listOfDefined(String storageLocate, Long moduleId, Long userId);

	/**
	 * 已定义的列表字段列表及分页方式
	 * @param storageLocate
	 * @param moduleId
	 * @param userId
	 * @return
	 */
	Map<String,Object> listOfDefinedAndPageNode(String storageLocate, Long moduleId, Long userId);

	List<DefinedListMetadata> listBusinessOfDefined(Long templateTableId ,String typeCode, Long moduleId, Long userId);

	/**
	 * 未定义的列表字段列表
	 *
	 * @param storageLocate 存储表名
	 * @param moduleId      模块id
	 * @return
	 */
	List<DefinedListMetadata> listOfUnDefined(String storageLocate, Long moduleId, Long userId);

	List<DefinedListMetadata> listBusinessOfUnDefined(Long templateTableId ,String typeCode, Long moduleId, Long userId);

	/**
	 * 保存档案列表配置
	 *
	 * @param saveListMetadata 排序保存对象
	 * @return
	 */
	R saveListDefined(SaveListMetadata saveListMetadata);

	/**
	 * 根据存储表名删除列表配置
	 *
	 * @param storageLocate 存储表名
	 * @param moduleId      模块 id
	 */
	boolean deleteByStorageLocate(String storageLocate, Long userId, Long moduleId);

	/**
	 * 租户初始化--列表配置初始化
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户ID
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取 门类列表定义
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getListDefinitionInfo(Long tenantId) throws ArchiveBusinessException;

	/**
	 * 根据模块id和storageLocate 删除 配置信息
	 *
	 * @param storageLocate 物理表名
	 * @param moduleId      模块id
	 * @return
	 */
	Boolean removeByModuleId(String storageLocate, Long moduleId);

	/**
	 * 复制到另一模块
	 *
	 * @param copyPostDTO
	 * @return
	 */
	R copy(CopyPostDTO copyPostDTO);

	/**
	 * 清除配置信息
	 * @param templateTableId
	 * @param moduleId
	 * @return
	 */
	Boolean clearListConfiguration(Long templateTableId ,String typeCode, Long moduleId);

	/**
	 * 复制配置
	 * @param srcStorageLocate
	 * @param destStorageLocate
	 * @param srcDestMetadataMap
	 */
	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap);

	Long isPublicUserId(Boolean tagging);

	/**
	 * 专题表列表配置复用 ArchiveList ---------------------------------------------------------------------------------
	 * by 王谷华 2021-04-08
	 * */

	/**
	 * @param storageLocate
	 * @param specialId
	 * @param moduleId
	 * @param userId
	 * @return
	 */
	List<DefinedListMetadata> listSpecialOfDefined( String storageLocate,Long specialId,  Long moduleId, Long userId);


	/**
	 *
	 * @param storageLocate
	 * @param moduleId
	 * @param userId
	 * @param moduleType
	 * @param moduleCode
	 * @return
	 */
	List<DefinedListMetadata> listSpecialOfUnDefined( String storageLocate,Long specialId,Long moduleId, Long userId,
													  Integer moduleType, String moduleCode);


	/**
	 * 专题列表配置保存
	 * @param saveListMetadata
	 * @return
	 */
	R saveSpecialDefined(SaveListMetadata saveListMetadata);

	/**
	 * 生成默认的专题定义字段（用于专题生成时）
	 * @param storageLocate
	 * @param specialId
	 * @param moduleCode
	 * @return
	 */
	R saveDefaultSpecialDefined(String storageLocate,Long specialId, String moduleCode);


	/**
	 * 删除整个专题的配置
	 * @param storageLocate
	 * @param specialId
	 * @return
	 */
	boolean removeSpecialDefinedBySpecialId(String storageLocate);
}
