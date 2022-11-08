package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.*;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.api.modular.report.dto.ReportDTO;
import com.cescloud.saas.archive.common.constants.FormStatusEnum;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigRuleService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveEditFormService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.LinkColumnRuleService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 档案配置规则
 */
@Api(value = "archiveConfigRule", tags = "档案配置规则")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/config-rule")
public class ArchiveConfigRuleController {

	private final ArchiveConfigRuleService archiveConfigRuleService;

	private final LinkColumnRuleService linkColumnRuleService;

	private final ArchiveTableService archiveTableService;

	private final ArchiveEditFormService archiveEditFormService;

	/**
	 * 通过表名和是否关联获取关联信息
	 *
	 * @param srcStorageLocate 关联表名
	 * @param tarStorageLocate 被关联表名
	 * @param isRelation       是否关联 0表示返回所有规则，1表示只返回勾选了是否关联的规则
	 * @return R
	 */
	@ApiOperation(value = "获取关联关系")
	@GetMapping("/inner-relation/relation")
	public R getRelationByStorageLocate(@RequestParam("srcStorageLocate") @ApiParam(name = "srcStorageLocate", value = "关联表名", required = true) String srcStorageLocate,
										@RequestParam("tarStorageLocate") @ApiParam(name = "tarStorageLocate", value = "被关联表名", required = true) String tarStorageLocate,
										@RequestParam("isRelation") @ApiParam(name = "isRelation", value = "isRelation 是否关联 0表示返回所有规则，1表示只返回勾选了是否关联的规则", required = true) int isRelation) {
		return new R<>(archiveConfigRuleService.getRelationByStorageLocate(srcStorageLocate, tarStorageLocate, isRelation));
	}

	/**
	 * 查询自定义列表
	 *
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@ApiOperation(value = "查询自定义列表")
	@GetMapping("/archive-list")
	public R<Map<String, Object>> getConfigArchiveList(@RequestParam("typeCode") @ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
													   @RequestParam("templateTableId") @ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId,
													   @RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) {
		return new R<>(archiveConfigRuleService.getConfigArchiveList(typeCode, templateTableId, moduleId));
	}

	@ApiOperation(value = "查询档案门类下所有报表")
	@GetMapping("/report/list")
	public R<List<ReportDTO>> getReportList(@RequestParam("typeCode") @ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
											@RequestParam("templateTableId") @ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId,
											@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(archiveConfigRuleService.getReportList(typeCode, templateTableId, moduleId));
	}

	@ApiOperation(value = "查询自定义列表")
	@GetMapping("/list")
	public R<List<DefinedListMetadata>> getConfigList(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
													  @ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId,
													  @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.getConfigList(typeCode, templateTableId, moduleId));
	}

	/**
	 * 查询标签自定义列表
	 *
	 * @return
	 */
	@ApiOperation(value = "查询标签自定义列表")
	@GetMapping("/tag-list")
	public R<List<DefinedListTag>> getConfigTagList() {
		return new R<>(archiveConfigRuleService.getConfigTagList());
	}

	/**
	 * 查询标签自定义检索条件
	 *
	 * @return
	 */
	@ApiOperation(value = "查询标签自定义检索条件")
	@GetMapping("/tag-search")
	public R<List<DefinedSearchTag>> getConfigTagSearch() {
		return new R<>(archiveConfigRuleService.getConfigTagSearch());
	}

	/**
	 * 查询标签自定义
	 *
	 * @return
	 */
	@ApiOperation(value = "查询标签自定义")
	@GetMapping("/tag")
	public R<ConfigTagDTO> getConfigTag() {
		return new R<>(archiveConfigRuleService.getConfigTag());
	}

	@ApiOperation(value = "查询自定义表单字段")
	@GetMapping("/archive-edit")
	public R<List<DefinedEditMetadata>> getConfigArchiveEdit(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
															 @ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId,
															 @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.getConfigArchiveEdit(typeCode, templateTableId, moduleId));
	}

	@ApiOperation(value = "查询自定义排序")
	@GetMapping("/archive-sort")
	public R<List<DefinedSortMetadata>> getConfigArchiveSort(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
															 @ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId,
															 @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.getConfigArchiveSort(typeCode, templateTableId, moduleId));
	}

	@ApiOperation(value = "查询查重字段")
	@GetMapping("/check-repeat")
	public R<List<DefinedRepeatMetadata>> getConfigCheckRepeat(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
															   @ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId) throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.getConfigCheckRepeat(typeCode, templateTableId));
	}

	@ApiOperation(value = "挂接规则配置")
	@GetMapping("/link-rule")
	public R<List<LinkLayer>> getConfigLinkRule(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
												@ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId,
												@ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.getConfigLinkRule(typeCode, templateTableId,moduleId));
	}

	@ApiOperation(value = "挂接规则配置(外部调用)")
	@GetMapping("/inner/link-rule")
	@Inner
	public R<List<LinkLayer>> getConfigLinkRuleInner(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
												@ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId,
												@ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.getConfigLinkRule(typeCode, templateTableId,moduleId));
	}

	@ApiOperation(value = "挂接规则文件名配置")
	@GetMapping("/link-rule/fileName")
	@Inner
	public R<LinkLayer> getConfigFileNameLinkRule(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
												  @ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId,
												  @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) throws ArchiveBusinessException {
		return new R<LinkLayer>(archiveConfigRuleService.getConfigFileNameLinkRule(typeCode, templateTableId, moduleId));
	}

	@ApiOperation(value = "获取文件下载命名设置")
	@GetMapping("/link-rule/download/name-setting")
	public R<LinkLayer> getConfigDocNameLinkRule(@ApiParam(name = "typeCode", value = "档案门类编码", required = true) String typeCode,
												 @ApiParam(name = "templateTableId", value = "模板ID", required = true) Long templateTableId,
												 @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) throws ArchiveBusinessException {
		return new R<LinkLayer>(archiveConfigRuleService.getConfigDocNameLinkRule(typeCode, templateTableId,moduleId));
	}

	@ApiOperation(value = "获取已定义的字段列表")
	@GetMapping("/link-rule/list/def")
	public R<List<DefinedColumnRuleMetadata>> getLinkColumnRuleList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) String storageLocate,
																	@ApiParam(name = "linkLayerId", value = "被设置的层次ID", required = true) Long linkLayerId) throws ArchiveBusinessException {
		return new R<>(linkColumnRuleService.listOfDefined(storageLocate, linkLayerId));
	}

	@ApiOperation(value = "查询自定义字段组成规则")
	@GetMapping("/archive-column-rule")
	public R<List<DefinedColumnRuleMetadata>> getConfigArchiveColumnRule(@ApiParam(name = "metadataSourceId", value = "字段ID", required = true) Long metadataSourceId) throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.getConfigArchiveColumnRule(metadataSourceId));
	}

	@GetMapping("/used")
	public R getCheckUsed(@RequestParam("metadataId") Long metadataId, @RequestParam("storageLocate") String storageLocate) throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.checkUsed(metadataId, storageLocate));
	}

	@ApiOperation(value = "根据表名获取字段拼接规则")
	@GetMapping(value = "/autovalue-rule/{storageLocate}")
	public R<List<MetadataAutovalue>> getAutoValueRule(@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) String storageLocate) {
		return new R<>(archiveConfigRuleService.getAutoValueRule(storageLocate));
	}

	@ApiOperation(value = "根据数据规则id、元数据id 获取字段拼接规则")
	@GetMapping(value = "/autovalue-rule/{autoValueId}/{metadataId}")
	public R<MetadataAutovalue> getAutoValueRuleByIdAndMetadataId(@PathVariable("autoValueId") @ApiParam(name = "autoValueId", value = "数据规则id", required = true) Long autoValueId,
																  @PathVariable("metadataId") @ApiParam(name = "metadataId", value = "元字段id", required = true) Long metadataId)  throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.getAutoValueRule(autoValueId, metadataId));
	}

	/**
	 * 获取字段初始化值的规则
	 * @param moduleId
	 * @param typeCode
	 * @param templateTableId
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@ApiOperation(value = "获取字段初始化值的规则")
	@GetMapping("/compute-rule")
	public R<List<ColumnComputeRuleDTO>> getComputeRule(Long moduleId,String typeCode, Long templateTableId) throws ArchiveBusinessException {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		return new R<>(archiveConfigRuleService.getComputeRuleByStorageLocate(moduleId,archiveTable.getStorageLocate(), FormStatusEnum.ADD));
	}

	@ApiOperation(value = "获取表单中字段初始化值的规则")
	@GetMapping("/edit-form-compute-rule")
	public R<List<ColumnComputeRuleDTO>> getEditFormComputeRule(@RequestParam("typeCode") String typeCode, @RequestParam("templateTableId") Long templateTableId, @RequestParam("moduleId") Long moduleId) throws ArchiveBusinessException {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		return new R<>(archiveConfigRuleService.getEditFormComputeRule(archiveTable.getStorageLocate(), moduleId));
	}

	@ApiOperation(value = "根据关联关系触发条件获取字段规则", httpMethod = "GET")
	@GetMapping("/column/rule")
	public R<Map<String, List<ColumnComputeRuleDTO>>> getColumnRuleByType(@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId,
																		  @RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "表名", required = true) String storageLocate,
																		  @RequestParam("type") @ApiParam(name = "type", value = "关联关系触发条件", required = true, defaultValue = "save") String type) throws ArchiveBusinessException {
		return new R<>(archiveEditFormService.getColumnRuleByType(moduleId,storageLocate,type));
	}

	@ApiOperation(value = "提交、归档、入库 时候根据表单必输项触发校验", httpMethod = "GET")
	@GetMapping("/check-required")
	public R<CheckRequiredDTO> checkRequired(@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId,
											 @RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "表名", required = true) String storageLocate,
											 @RequestParam("dataId") @ApiParam(name = "dataId", value = "数据id", required = true) String dataId) throws ArchiveBusinessException {
		return new R<>(archiveConfigRuleService.checkRequired(moduleId,storageLocate,dataId));
	}
}
