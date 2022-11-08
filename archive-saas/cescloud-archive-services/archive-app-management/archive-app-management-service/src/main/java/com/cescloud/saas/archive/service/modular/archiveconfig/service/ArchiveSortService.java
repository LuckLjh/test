
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSortMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveSortMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveSort;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 档案排序配置
 *
 * @author liudong1
 * @date 2019-04-18 21:18:05
 */
public interface ArchiveSortService extends IService<ArchiveSort> {

	/**
	 * 获取已定义的排序字段列表
	 *
	 * @param storageLocate 存储表名
	 * @param moduleId 模块id
	 * @return
	 */
	List<DefinedSortMetadata> listOfDefined(String storageLocate,Long moduleId,Long userId);

	List<DefinedSortMetadata> listBusinessOfDefined(Long templateTableId, String typeCode, Long moduleId, Long userId);

	/**
	 * 获取未定义的排序字段列表
	 *
	 * @param storageLocate 存储表名
	 * @param moduleId 模块id
	 * @return
	 */
	List<DefinedSortMetadata> listOfUnDefined(String storageLocate,Long moduleId,Boolean tagging);

	List<DefinedSortMetadata> listBusinessOfUnDefined(Long templateTableId , String typeCode, Long moduleId, Long userId);

	/**
	 * 保存档案排序配置
	 *
	 * @param saveSortMetadata
	 * @return
	 */
	R saveSortDefined(SaveSortMetadata saveSortMetadata);

	/**
	 * 根据存储表名删除配置
	 *
	 * @param storageLocate 存储表名
	 */
	boolean deleteByStorageLocate(String storageLocate,Long moduleId,Long userId);

	/**
	 * 租户初始化
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取 档案门类 -列表排序信息
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getSortDefinitionInfo (Long tenantId) throws ArchiveBusinessException;

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

	List<DefinedSortMetadata> getArchiveSortDefList(String storageLocate,Long moduleId);

	Boolean clearListConfiguration(Long templateTableId,String  typeCode, Long moduleId);


	/**
	 * 专题表列表配置复用 ArchiveList ---------------------------------------------------------------------------------
	 * by 王谷华 2021-04-08
	 * */



	List<DefinedSortMetadata> listSpecialOfDefined(String storageLocate,Long specialId, Long moduleId, Boolean tagging);


	/**
	 * 读取未定义的排序
	 * @param storageLocate
	 * @param moduleId
	 * @param userId
	 * @param moduleType
	 * @param moduleCode
	 * @return
	 */
	List<DefinedSortMetadata> listSpecialOfUnDefined(String storageLocate, Long specialId,Long moduleId,Boolean tagging,
													 Integer moduleType, String moduleCode);

	/**
	 * 保存档案排序配置
	 *
	 * @param saveSortMetadata
	 * @return
	 */
	R saveSpecialSortDefined(SaveSortMetadata saveSortMetadata);

	/**
	 * 删除整个专题的配置
	 * @param storageLocate
	 * @param specialId
	 * @return
	 */
	boolean removeSpecialDefinedBySpecialId(String storageLocate);


	/**
	 * 元数据的排序字段，一个租户全局通用
	 * 故 moduleId userId 固定为 -1
	 * @return
	 */
	List<DefinedSortMetadata> listMetadataOfDefined();


	/**
	 * 同上 未使用的的元数据排序字段
	 * @return
	 */
	List<DefinedSortMetadata> listMetadataOfUnDefined();

	/**
	 * 保存 元数据排序字段（同时删除旧的）
	 * @param saveSortMetadata
	 * @return
	 */
	R savetMetadataSortDefined(SaveSortMetadata saveSortMetadata);

}
