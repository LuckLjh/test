package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.*;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.api.modular.report.dto.ReportDTO;
import com.cescloud.saas.archive.common.constants.FormStatusEnum;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;

public interface ArchiveConfigRuleService {

    List<InnerRelationDTO> getRelationByStorageLocate(String srcStorageLocate, String tarStorageLocate, int isRelation);

    List<ReportDTO> getReportList(String typeCode,Long templateTableId, Long moduleId) ;

	Map<String, Object> getConfigArchiveList(String typeCode, Long templateTableId, Long moduleId);

	List<DefinedListTag> getConfigTagList() ;

	List<DefinedSearchTag> getConfigTagSearch() ;

	ConfigTagDTO getConfigTag();

	List<DefinedListMetadata> getConfigList(String typeCode, Long templateTableId, Long moduleId) throws ArchiveBusinessException;

	List<DefinedEditMetadata> getConfigArchiveEdit(String typeCode, Long templateTableId, Long moduleId) throws ArchiveBusinessException;

	List<DefinedSortMetadata> getConfigArchiveSort(String typeCode, Long templateTableId,Long moduleId) throws ArchiveBusinessException;

	List<DefinedColumnRuleMetadata> getConfigArchiveColumnRule(Long metadataSourceId) throws ArchiveBusinessException;

	List<DefinedRepeatMetadata> getConfigCheckRepeat(String typeCode, Long templateTableId) throws ArchiveBusinessException;

	List<LinkLayer> getConfigLinkRule(String typeCode, Long templateTableId,Long moduleId) throws ArchiveBusinessException;

	/**
	 * 挂接规则文件名配置
	 * @param typeCode 档案类型编码
	 * @param templateTableId 档案表模板id
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer
	 */
	LinkLayer getConfigFileNameLinkRule(String typeCode, Long templateTableId,Long moduleId) throws ArchiveBusinessException;

	/**
	 * 获取文件下载命名设置
	 * @param typeCode 档案类型编码
	 * @param templateTableId 档案表模板id
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer
	 */
	LinkLayer getConfigDocNameLinkRule(String typeCode, Long templateTableId ,Long moduleId) throws ArchiveBusinessException;

	Boolean checkUsed(Long metadataId, String storageLocate) throws ArchiveBusinessException;

	/**
	 * 根据业务表名获取字段规则
	 * @param storageLocate 表名
	 * @return 字段规则
	 */
	List<MetadataAutovalue> getAutoValueRule(String storageLocate);

	/**
	 * 根据数据规则id、元数据id 获取字段规则
	 * @param autoValueId 数据规则id
	 * @param metadataId 元数据id
	 * @return 字段规则
	 */
	MetadataAutovalue getAutoValueRule(Long autoValueId, Long metadataId);

	List<ColumnComputeRuleDTO> getComputeRuleByStorageLocate(Long moduleId, String storageLocate, FormStatusEnum formStatusEnum) throws ArchiveBusinessException;

	List<ColumnComputeRuleDTO> getEditFormComputeRule(String storageLocate,Long moduleId) throws ArchiveBusinessException;

	boolean deleteByStorageLocate(String storageLocate) throws ArchiveBusinessException;

	/**
	 * 检查必输项
	 */
	CheckRequiredDTO  checkRequired (Long moduleId,String storageLocate,String dataId)  throws ArchiveBusinessException ;
}
