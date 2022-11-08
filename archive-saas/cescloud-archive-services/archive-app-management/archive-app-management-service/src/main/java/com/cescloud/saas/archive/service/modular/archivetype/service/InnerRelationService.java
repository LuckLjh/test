
package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ColumnComputeRuleDTO;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationOutDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationPutDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.LayerRelationDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation;
import com.cescloud.saas.archive.common.constants.FormStatusEnum;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 档案类型关联
 *
 * @author xieanzhu
 * @date 2019-04-16 14:13:01
 */
public interface InnerRelationService extends IService<InnerRelation> {

	/**
	 * 根据ID查询档案类型关联
	 * @param id 主键id
	 * @return com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation
	 */
	InnerRelation getInnerRelationById(Long id);

	/**
	 * 根据ID删除关联关系规则
	 * @param id
	 * @return true or false
	 */
	boolean removeInnerRelationById(Long id,String storageLocate,Long moduleId);

	/**
	 * 获取档案类型关联列表
	 * @param storageLocate 存储表名
	 * @return
	 * @throws ArchiveBusinessException
	 */
    List<InnerRelationOutDTO> listByArchiveTableName(String storageLocate,Long moduleId) throws ArchiveBusinessException;

	/**
	 * 新增关联关系规则
	 * @param innerRelationPostDTO 关联关系对象
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R save(InnerRelationPostDTO innerRelationPostDTO) throws ArchiveBusinessException;

	/**
	 * 修改关联关系规则
	 * @param innerRelationPutDTO 关联关系对象
	 * @return
	 * @throws ArchiveBusinessException
	 */
    R update(InnerRelationPutDTO innerRelationPutDTO) throws ArchiveBusinessException;

	/**
	 * 根据表名判断关联关系配置是否被使用
	 * @param storageLocate 存储表名
	 * @return
	 */
	List<InnerRelation> listByStorageLocate(String storageLocate,Long moduleId);

	/**
	 * 根据表名获取关联关系元数据列表（包括关联和被关联表元数据）
	 * @param storagelocate 存储表名
	 * @return
	 * @throws ArchiveBusinessException
	 */
    Map<String,Object> getMetadataMap(String storagelocate) throws ArchiveBusinessException;

	/**
	 * 根据表名获取录入界面和组卷表单的字段计算规则
	 * @param storageLocate
	 * @param formStatusEnum
	 * @return
	 * @throws ArchiveBusinessException
	 */
	List<ColumnComputeRuleDTO> getComputeRuleByStorageLocate(String storageLocate, FormStatusEnum formStatusEnum) throws ArchiveBusinessException;

	/**
	 * 根据表名删除所有关联规则
	 * @param storageLocate 表名
	 */
	boolean deleteByStorageLocate(String storageLocate);

	/**
	 * 根据租户id获取门类关联关系信息
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getAssociationDefinitionInfo(Long tenantId) throws ArchiveBusinessException;

	/**
	 * 租户初始化关联关系
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 */
	R initializeInnerRelation(Long templateId, Long tenantId) throws ArchiveBusinessException;

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

	List<LayerRelationDTO> getLayerRelation(String storageLocate, Long moduleId);

	/**
	 * 配置拷贝
	 * @param srcStorageLocate
	 * @param destStorageLocate
	 * @param srcDestMetadataMap
	 */
	void copyByStorageLocate(String srcStorageLocate,String destStorageLocate,Map<Long,Long> srcDestMetadataMap, Map<String, String> destSrcStorageLocateMap);
}
