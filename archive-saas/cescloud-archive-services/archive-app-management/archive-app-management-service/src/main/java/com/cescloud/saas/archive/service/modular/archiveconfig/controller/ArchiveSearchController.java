
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveSearchMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SearchListDTO;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.actuator.annotation.Actuator;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveSearchService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
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
import java.util.ArrayList;
import java.util.List;


/**
 * 档案检索配置
 *
 * @author liudong1
 * @date 2019-05-27 16:52:00
 */
@Api(value = "ArchiveSearch", tags = "应用管理-档案门类管理:档案检索配置")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class ArchiveSearchController {

	private final ArchiveSearchService archiveSearchService;

	@ApiOperation(value = "已定义的列表字段列表(档案门类配置使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/def")
	public R<List<DefinedSearchMetadata>> getArchiveListdefList(@ApiParam(name = "searchListDTO", value = "检索列表查询对象", required = true) SearchListDTO searchListDTO) {
		return new R<>(archiveSearchService.listOfDefined(searchListDTO));
	}

	@ApiOperation(value = "已定义的列表字段列表(业务模块使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/business/list/def")
	public R<List<DefinedSearchMetadata>> getArchiveBusinessListdefList(@ApiParam(name = "searchListDTO", value = "检索列表查询对象", required = true) SearchListDTO searchListDTO) {
		return new R<>(archiveSearchService.listBusinessOfDefined(searchListDTO));
	}

	@ApiOperation(value = "未定义的列表字段列表(档案门类配置使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/undef")
	public R<List<DefinedSearchMetadata>> getArchiveListUndefList(@ApiParam(name = "searchListDTO", value = "检索列表查询对象", required = true) SearchListDTO searchListDTO) {
		return new R<>(archiveSearchService.listOfUnDefined(searchListDTO));
	}

	@ApiOperation(value = "未定义的列表字段列表(业务模块使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/business/list/undef")
	public R<List<DefinedSearchMetadata>> getArchiveBusinessListUndefList(@ApiParam(name = "searchListDTO", value = "检索列表查询对象", required = true) SearchListDTO searchListDTO) {
		return new R<>(archiveSearchService.listBusinessOfUnDefined(searchListDTO));
	}

	@ApiOperation(value = "检索自定义-恢复默认", httpMethod = SwaggerConstants.PUT)
	@SysLog("恢复档案检索配置默认值")
	@PutMapping("/recovery")
	public R restoreDefault(@RequestBody @ApiParam(name = "searchListDTO", value = "检索列表查询对象", required = true)SearchListDTO searchListDTO){
		return new R(archiveSearchService.clearListConfiguration(searchListDTO));
	}
	/**
	 * 通过物理表名获取已定义的列表字段列表
	 * 内部调用 使用
	 */
	@Actuator(name="getArchiveListdefList[/search/list]")
	@GetMapping("/list")
	public R<List<DefinedSearchMetadata>> getArchiveListdefList(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) String storageLocate,
																@RequestParam("searchType") @ApiParam(name = "searchType", value = "检索类型：1：快速检索 2：基本检索", required = true) Integer searchType,
																@RequestParam("tagging") @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) Boolean tagging,
																@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) Long moduleId) {
		return new R<>(archiveSearchService.getArchiveListdefList(storageLocate, searchType, tagging,moduleId));
	}

	/**
	 * 保存档案检查配置
	 *
	 * @param saveSearchMetadata 档案列表配置
	 * @return R
	 */
	@ApiOperation(value = "保存档案检查配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存档案检查配置")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "saveSearchMetadata", value = "检索定义保存对象", required = true) @Valid SaveSearchMetadata saveSearchMetadata) {
		return archiveSearchService.saveSearchDefined(saveSearchMetadata);
	}

	@Actuator(name="getRetrieveDefinitionInfo[/search/data/{tenantId}]")
	@ApiOperation(value = "获取档案门类列表检索信息", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取档案门类列表检索信息")
	public R<List<ArrayList<String>>> getRetrieveDefinitionInfo(@PathVariable("tenantId") Long tenantId) throws ArchiveBusinessException {
		return new R(archiveSearchService.getRetrieveDefinitionInfo(tenantId));
	}

	@ApiOperation(value = "获取基本检索表单配置", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/search-from/{moduleId}/{typeCode}/{templateTableId}")
	@SysLog("获取档案门类列表检索信息")
	public R getBasicRetrievalForm(@PathVariable("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true, defaultValue = "1231") Long moduleId,
								   @PathVariable("typeCode") @ApiParam(name = "typeCode", value = "档案类型code", required = true, defaultValue = "wsda") String typeCode,
								   @PathVariable("templateTableId") @ApiParam(name = "templateTableId", value = "表模板ID", required = true, defaultValue = "123") Long templateTableId) {
		return new R(archiveSearchService.getBasicRetrievalForm(moduleId,typeCode, templateTableId));
	}

	@SysLog("清除检索定义配置")
	@ApiOperation(value = "清除检索定义配置信息", httpMethod = SwaggerConstants.DELETE)
	@DeleteMapping("/remove-config")
	public R remove(@RequestParam("storageLocate")@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
					@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId ){
		return new R(archiveSearchService.removeByModuleId(storageLocate,moduleId));
	}
	@SysLog("复制到其他模块")
	@ApiOperation(value = "复制到其他模块接口", httpMethod = SwaggerConstants.POST)
	@PostMapping("/copy")
	public R copyToModule(@Valid @RequestBody @ApiParam(name = "copyPostDTO",value = "复制到其他模块参数DTO") CopyPostDTO copyPostDTO) {
		return archiveSearchService.copy(copyPostDTO);
	}



	@ApiOperation(value = "已定义的专题列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/special/list/def")
	public R<List<DefinedSearchMetadata>> listOfSpecialDefined(@ApiParam(name = "searchListDTO", value = "检索列表查询对象", required = true) SearchListDTO searchListDTO) {
		return new R<>(archiveSearchService.listOfSpecialDefined(searchListDTO));
	}

	@ApiOperation(value = "未定义的专题列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/special/list/undef")
	public R<List<DefinedSearchMetadata>> listSpecialOfUnDefined(@ApiParam(name = "searchListDTO", value = "检索列表查询对象", required = true) SearchListDTO searchListDTO) {
		return new R<>(archiveSearchService.listSpecialOfUnDefined(searchListDTO));
	}

	/**
	 * 保存专题搜索配置
	 *
	 * @param saveSearchMetadata 档案列表配置
	 * @return R
	 */
	@ApiOperation(value = "保存档案检查配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存档案检查配置")
	@PostMapping("special/save")
	public R saveSpecialSearchDefined(@RequestBody @ApiParam(name = "saveSearchMetadata", value = "检索定义保存对象", required = true) @Valid SaveSearchMetadata saveSearchMetadata) {
		return archiveSearchService.saveSpecialSearchDefined(saveSearchMetadata);
	}
}
