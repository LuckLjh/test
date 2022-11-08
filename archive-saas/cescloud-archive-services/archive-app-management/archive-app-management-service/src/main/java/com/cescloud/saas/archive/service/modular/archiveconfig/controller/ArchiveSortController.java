
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSortMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveSortMetadata;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.actuator.annotation.Actuator;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveListService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveSortService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 档案排序配置
 *
 * @author liudong1
 * @date 2019-04-18 21:18:05
 */
@Api(value = "archiveSort", tags = "应用管理-档案门类管理:档案排序配置管理")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/sort")
public class ArchiveSortController {

	private final ArchiveSortService archiveSortService;

	private final ArchiveListService archiveListService;

	@ApiOperation(value = "已定义的排序字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/def")
	public R<List<DefinedSortMetadata>> getArchiveSortdefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
															  @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
															  @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空") Boolean tagging) {
		return new R<>(archiveSortService.listOfDefined(storageLocate, moduleId, archiveListService.isPublicUserId(tagging)));
	}

	@ApiOperation(value = "已定义的列表字段列表(业务模块使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/business/list/def")
	public R<List<DefinedSortMetadata>> getArchiveBusinessListdefList(@ApiParam(name = "templateTableId", value = "表模板id", required = true) @NotNull(message = "表模板id不能为空") Long templateTableId,
																	  @ApiParam(name = "typeCode", value = "档案类型", required = true) @NotBlank(message = "档案类型不能为空") String typeCode,
																	  @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
																	  @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空") Boolean tagging) {
		return new R<>(archiveSortService.listBusinessOfDefined(templateTableId, typeCode, moduleId, archiveListService.isPublicUserId(tagging)));
	}

	@ApiOperation(value = "未定义的排序字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/undef")
	public R<List<DefinedSortMetadata>> getArchiveSortUndefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
																@ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId,
																@ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空") Boolean tagging) {
		return new R<>(archiveSortService.listOfUnDefined(storageLocate, moduleId, tagging));
	}

	@ApiOperation(value = "未定义的列表字段列表(业务模块使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/business/list/undef")
	public R<List<DefinedSortMetadata>> getArchiveBusinessListUndefList(@ApiParam(name = "templateTableId", value = "表模板id", required = true) @NotNull(message = "表模板id不能为空") Long templateTableId,
																		@ApiParam(name = "typeCode", value = "档案类型", required = true) @NotBlank(message = "档案类型不能为空") String typeCode,
																		@ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
																		@ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空") Boolean tagging) {
		return new R<>(archiveSortService.listBusinessOfUnDefined(templateTableId, typeCode, moduleId, archiveListService.isPublicUserId(tagging)));
	}

	@ApiOperation(value = "恢复列表定义配置默认值", httpMethod = SwaggerConstants.DELETE)
	@SysLog("恢复列表定义配置默认值")
	@DeleteMapping("/recovery")
	public R restoreDefault(@ApiParam(name = "templateTableId", value = "表模板id", required = true) @NotNull(message = "表模板id不能为空") Long templateTableId,
							@ApiParam(name = "typeCode", value = "档案类型", required = true) @NotBlank(message = "档案类型不能为空") String typeCode,
							@ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R(archiveSortService.clearListConfiguration(templateTableId, typeCode, moduleId));
	}

	@ApiOperation(value = "保存档案排序配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存档案排序配置")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "saveSortMetadata", value = "排序保存对象", required = true) @Valid SaveSortMetadata saveSortMetadata) {
		return archiveSortService.saveSortDefined(saveSortMetadata);
	}

	@ApiOperation(value = "获取档案门类列表定义信息", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取档案门类列表定义信息")
	public R getSortDefinitionInfo(@PathVariable("tenantId") Long tenantId) throws ArchiveBusinessException {
		return new R(archiveSortService.getSortDefinitionInfo(tenantId));
	}

	@SysLog("清除排序定义配置")
	@ApiOperation(value = "清除排序定义配置信息", httpMethod = SwaggerConstants.DELETE)
	@DeleteMapping("/remove-config")
	public R remove(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
					@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R(archiveSortService.removeByModuleId(storageLocate, moduleId));
	}

	@SysLog("复制到其他模块")
	@ApiOperation(value = "复制到其他模块接口", httpMethod = SwaggerConstants.POST)
	@PostMapping("/copy")
	public R copyToModule(@RequestBody @ApiParam(name = "copyPostDTO", value = "复制到其他模块参数DTO") @Valid CopyPostDTO copyPostDTO) {
		return archiveSortService.copy(copyPostDTO);
	}

	@Actuator(name = "getArchiveSortDefList[/sort/list]")
	@ApiOperation(value = "获取排序定义列表（业务使用）", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list")
	public R<List<DefinedSortMetadata>> getArchiveSortDefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
															  @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(archiveSortService.listOfDefined(storageLocate, moduleId, SecurityUtils.getUser().getId()));
	}


	@ApiOperation(value = "已定义的专题列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/business/listSpecialOfDefined")
	public R<List<DefinedSortMetadata>> listSpecialOfDefined(@ApiParam(name = "storageLocate", value = "表名", required = true) @NotBlank(message = "表名不能为空") String storageLocate,
															 @ApiParam(name = "specialId", value = "专题ID", required = true) @NotNull(message = "专题ID不能为空") Long specialId,
															 @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
															 @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空") Boolean tagging) {
		return new R<>(archiveSortService.listSpecialOfDefined(storageLocate, specialId, moduleId, tagging));
	}

	@ApiOperation(value = "未定义的专题列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/business/listSpecialOfUnDefined")
	public R<List<DefinedSortMetadata>> listSpecialOfUnDefined(@ApiParam(name = "storageLocate", value = "表名", required = true) @NotBlank(message = "表名不能为空") String storageLocate,
															   @ApiParam(name = "specialId", value = "专题ID", required = true) @NotNull(message = "专题ID不能为空") Long specialId,
															   @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
															   @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空") Boolean tagging,
															   @ApiParam(name = "moduleType", value = "专题类型", required = true) @NotNull(message = "专题类型不能为空") Integer moduleType,
															   @ApiParam(name = "moduleCode", value = "专题code", required = true) @NotBlank(message = "专题code不能为空") String moduleCode) {
		return new R<>(archiveSortService.listSpecialOfUnDefined(storageLocate, specialId, moduleId, tagging, moduleType, moduleCode));
	}


	@ApiOperation(value = "保存专题排序配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存专题排序配置")
	@PostMapping("/saveSpecialSortDefined")
	public R saveSpecialSortDefined(@RequestBody @ApiParam(name = "saveSortMetadata", value = "排序保存对象", required = true) @Valid SaveSortMetadata saveSortMetadata) {
		return archiveSortService.saveSpecialSortDefined(saveSortMetadata);
	}

	@ApiOperation(value = "已定义的元数据标签列表字段列表(业务模块使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/metadata/list/def")
	public R<List<DefinedSortMetadata>> listMetadataOfDefined() {
		return new R<>(archiveSortService.listMetadataOfDefined());
	}

	@ApiOperation(value = "未定义的元数据标签排序字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/metadata/list/undef")
	public R<List<DefinedSortMetadata>> listMetadataOfUnDefined() {
		return new R<>(archiveSortService.listMetadataOfUnDefined());
	}

	@ApiOperation(value = "保存元数据标签排序配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存档案排序配置")
	@PostMapping("/metadata/save")
	public R savetMetadataSortDefined(@RequestBody @ApiParam(name = "saveSortMetadata", value = "排序保存对象", required = true) @Valid SaveSortMetadata saveSortMetadata) {
		return archiveSortService.savetMetadataSortDefined(saveSortMetadata);
	}

}
