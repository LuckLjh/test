
package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedEditMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveEditMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEdit;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 档案录入配置
 *
 * @author liudong1
 * @date 2019-04-18 16:06:51
 */
public interface ArchiveEditService extends IService<ArchiveEdit> {

	/**
	 * 获取已定义的录入字段列表
	 *
	 * @param storageLocate
	 * @return
	 */
	List<DefinedEditMetadata> listOfDefined(String storageLocate,Long moduleId);

	/**
	 * 获取未定义的录入字段列表
	 *
	 * @param storageLocate
	 * @return
	 */
	List<DefinedEditMetadata> listOfUnDefined(String storageLocate,Long moduleId);

	/**
	 * 保存档案录入字段配置
	 *
	 * @param saveEditMetadata
	 * @return
	 */
	R saveEditDefined(SaveEditMetadata saveEditMetadata);

	/**
	 * 根据存储表名删除配置
	 *
	 * @param storageLocate 存储表名
	 */
	boolean deleteByStorageLocate(String storageLocate, Long moduleId);

	/**
	 * 租户配置初始化
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取租户档案门类表单字段信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getFormFieldInfo(Long tenantId) throws ArchiveBusinessException;

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
	 * 根据表名复制配置数据
	 * @param srcStorageLocate 源表名
	 * @param destStorageLocate 目标表名
	 * @param srcDestMetadataMap 源元字段和目标元字段map对照表
	 */
	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap);
}
