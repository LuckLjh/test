
package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ColumnComputeRuleDTO;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.AutovalueDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 元数据字段自动赋值
 *
 * @author liwei
 * @date 2019-04-15 15:16:12
 */
public interface MetadataAutovalueService extends IService<MetadataAutovalue> {

	R saveMetadataAutovalue(AutovalueDTO autovalueDTO) throws ArchiveBusinessException;

	/**
	 * 根据字段规则id获取字段规则
	 *
	 * @param id
	 * @return
	 */
	MetadataAutovalue getAutovalueById(Long id);

	List<MetadataAutovalue> getDefinedAutovalues(String storageLocate, Long moduleId);

	List<MetadataAutovalue> getAllDefinedAutovalues(String storageLocate);

	List<AutovalueDTO> listByStorageLocate(String storageLocate,Long moduleId);

	/**
	 * 根据档案类型编码和档案层级获取字段规则
	 *
	 * @param archiveTypeCode 档案类型编码
	 * @param templateTableId    档案模板id
	 * @param moduleId 模块id
	 * @return 根据档案类型编码和档案层级获取字段规则
	 */
	List<AutovalueDTO> getAutovaluesByCodeAndLayer(String archiveTypeCode, Long templateTableId,Long moduleId);

	R update(AutovalueDTO autovalueVo) throws ArchiveBusinessException;

	List<ColumnComputeRuleDTO> getComputeRuleByStorageLocate(Long moduleId, String storageLocate) throws ArchiveBusinessException;

	/**
	 * 删除字段规则
	 *
	 * @param id 规则id
	 * @return 是否成功
	 */
	boolean removeAutovalue(Long id,String storageLocate,Long moduleId);

	/**
	 * 通过档案类别层级表级联删除数据规则定义
	 *
	 * @param storageLocate
	 * @return
	 */
	boolean deleteByStorageLocate(String storageLocate);

	/**
	 * 获取档案门类数据规则信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getDataRuleDefinitionInfo(Long tenantId) throws ArchiveBusinessException;

	/**
	 * 租户初始化数据规则定义
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 */
	R initializeDataRule(Long templateId, Long tenantId) throws ArchiveBusinessException;

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
	R copy(CopyPostDTO copyPostDTO) throws ArchiveBusinessException;

	/**
	 * 根据表名复制配置数据
	 * @param srcStorageLocate 源表名
	 * @param destStorageLocate 目标表名
	 * @param srcDestMetadataMap 源元字段和目标元字段map对照表
	 */
	void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap);
}
