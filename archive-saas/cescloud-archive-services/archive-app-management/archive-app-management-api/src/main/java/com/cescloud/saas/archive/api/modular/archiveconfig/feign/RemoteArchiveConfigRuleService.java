package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.*;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEditForm;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.api.modular.report.dto.ReportDTO;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.localcache.LocalCacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(contextId = "remoteArchiveConfigRuleService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveConfigRuleService {

	//根据表名和关联类型获取关联关系
	@RequestMapping(value = "/config-rule/inner-relation/relation", method = RequestMethod.GET)
	R getRelationByStorageLocate(@RequestParam("storageLocate") String storageLocate, @RequestParam("type") int type);

	//打印报表
	@RequestMapping(value = "/config-rule/report/print", method = RequestMethod.GET)
	R print(@RequestParam("id") Long id, @RequestParam("ids") String ids);

	//根据表名获取报表列表
	@RequestMapping(value = "/config-rule/report/list", method = RequestMethod.GET)
	R<List<ReportDTO>> getReportList(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId, @RequestParam("moduleId") Long moduleId);

	/**
	 * 获取档案列表配置
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 */
	@GetMapping("/config-rule/archive-list")
	R<List<Map<String, Object>>> getConfigArchiveList(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId, @RequestParam("moduleId")Long moduleId);

	@GetMapping("/config-rule/list")
	R<List<DefinedListMetadata>> getConfigList(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId, @RequestParam("moduleId")Long moduleId);

	/**
	 * 获取表单字段配置
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 */
	@GetMapping("/config-rule/archive-edit")
	R<List<DefinedEditMetadata>> getConfigArchiveEdit(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId, @RequestParam("moduleId") Long moduleId);

	/**
	 * 获取档案排序配置
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 */
	@GetMapping("/config-rule/archive-sort")
	R<List<DefinedSortMetadata>> getConfigArchiveSort(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId, @RequestParam("moduleId") Long moduleId);

	/**
	 * 获取档案查重配置
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 */
	@GetMapping("/config-rule/check-repeat")
	R<List<DefinedRepeatMetadata>> getConfigCheckRepeat(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId);

	/**
	 * 获取挂接规则配置
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 */
	@GetMapping("/config-rule/link-rule")
	R<List<LinkLayer>> getConfigLinkRule(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId,@RequestParam("moduleId") Long moduleId);


	/**
	 * 获取挂接规则配置 (外部调用)
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 */
	@GetMapping("/config-rule/inner/link-rule")
	R<List<LinkLayer>> getConfigLinkRuleInner(@RequestParam("typeCode") String typeCode,
											  @RequestParam("templateTableId") Long templateTableId,
											  @RequestParam("moduleId") Long moduleId,
											  @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 挂接规则文件名配置
	 * @param typeCode
	 * @param templateTableId
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer
	 */
	@GetMapping("/config-rule/link-rule/fileName")
	R<LinkLayer> getConfigFileNameLinkRule(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId, @RequestParam("moduleId") Long moduleId,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取文件下载命名设置
	 * @param typeCode
	 * @param templateTableId
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer
	 */
	@GetMapping("/config-rule/link-rule/download/name-setting")
	R<LinkLayer> getConfigDocNameLinkRule(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId, @RequestParam("moduleId") Long moduleId);

	/**
	 * 获取已定义的字段列表
	 *
	 * @param storageLocate
	 * @param linkLayerId
	 * @return
	 */
	@GetMapping("/config-rule/link-rule/list/def")
	R<List<DefinedColumnRuleMetadata>> getLinkColumnRuleList(@RequestParam("storageLocate") String storageLocate, @RequestParam("linkLayerId") Long linkLayerId);


	/**
	 * 获取字段组成规则配置
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 */
	@GetMapping("/config-rule/archive-column-rule")
	R<List<DefinedColumnRuleMetadata>> getConfigArchiveColumnRule(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId, @RequestParam("metadataSourceId") Long metadataSourceId);

	/**
	 * 获取标签列表配置
	 *
	 * @param archiveLayer
	 * @return
	 */
	@GetMapping("/config-rule/tag-list")
	R<List<DefinedListTag>> getConfigTagList(@RequestParam("archiveLayer") String archiveLayer);

	/**
	 * 获取标签检索配置
	 *
	 * @param archiveLayer
	 * @return
	 */
	@GetMapping("/config-rule/tag-search")
	R<List<DefinedListTag>> getConfigTagSearch(@RequestParam("archiveLayer") String archiveLayer);

	/**
	 * 获取标签配置
	 *
	 * @param storageLocate
	 * @return
	 */
	@GetMapping("/config-rule/tag")
	R<ConfigTagDTO> getConfigTag(@RequestParam("storageLocate") String storageLocate);

	/**
	 * 根据表名获取字段规则
	 *
	 * @param storageLocate 表名
	 * @return
	 */
	@GetMapping("/config-rule/autovalue-rule/{storageLocate}")
	R<List<MetadataAutovalue>> getAutoValueRule(@PathVariable("storageLocate") String storageLocate);

	/**
	 * 根据数据规则id获取字段规则
	 * @param autoValueId 数据规则id
	 * @param metadataId 元字段id
	 * @return
	 */
	@GetMapping("/config-rule/autovalue-rule/{autoValueId}/{metadataId}")
	R<MetadataAutovalue> getAutoValueRuleByIdAndMetadataId(@PathVariable("autoValueId")  Long autoValueId ,@PathVariable("metadataId")  Long metadataId);

	/**
	 * 获取字段拼接规则
	 *
	 * @param storageLocate
	 * @return
	 */
	@GetMapping("/edit-form")
	R<ArchiveEditForm> getEditFormByStorageLocate(@RequestParam("storageLocate") String storageLocate,@RequestParam("moduleId") Long moduleId);


	@RequestMapping(value = "/edit-form/column", method = RequestMethod.GET)
	R<List<String>> getEditFormColumn(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId);

	/**
	 * 获取字段计算规则
	 * @param moduleId
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 */
	@RequestMapping(value = "/config-rule/compute-rule", method = RequestMethod.GET)
	R<List<ColumnComputeRuleDTO>> getComputeRule(@RequestParam("moduleId") Long moduleId, @RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId);

	/**
	 * 获取表单中字段初始化值的规则
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 */
	@RequestMapping(value = "/config-rule/edit-form-compute-rule", method = RequestMethod.GET)
	R<List<ColumnComputeRuleDTO>> getEditFormComputeRule(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId);

	/**
	 * 根据关联关系触发条件获取字段规则
	 * @param storageLocate 表名
	 * @param type 关联关系触发条件
	 * @return
	 */
	@RequestMapping(value = "/config-rule/column/rule", method = RequestMethod.GET)
	R<Map<String, List<ColumnComputeRuleDTO>>> getColumnRuleByType(@RequestParam("moduleId") Long moduleId, @RequestParam("storageLocate") String storageLocate, @RequestParam("type") String type);

	@RequestMapping(value = "/tag-search/list/def", method = RequestMethod.GET)
	R<List<DefinedSearchTag>> getArchiveListdefList();
}
