
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveListMetadata;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.common.constants.ModelTypeEnum;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveListService;
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
import java.util.Map;


/**
 * 档案列表配置
 *
 * @author liudong1
 * @date 2019-04-18 21:12:08
 */
@Api(value = "archiveList", tags = "应用管理-档案门类管理:档案列表配置管理")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/list")
public class ArchiveListController {

	private final ArchiveListService archiveListService;

	/**
	 * 已定义的列表字段列表
	 *
	 * @param storageLocate
	 * @return
	 */
	@ApiOperation(value = "已定义的列表字段列表(档案门类配置用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/def")
	public R<Map<String,Object>> getArchiveListdefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
	                                    @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
	                                    @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空") Boolean tagging) {
		return new R<>(archiveListService.listOfDefinedAndPageNode(storageLocate, moduleId,archiveListService.isPublicUserId(tagging)));
	}

	@ApiOperation(value = "已定义的列表字段列表(业务模块使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/business/list/def")
	public R<List<DefinedListMetadata>> getArchiveBusinessListdefList(@ApiParam(name = "templateTableId", value = "表模板id", required = true) @NotNull(message = "表模板id不能为空") Long templateTableId,
																	  @ApiParam(name = "typeCode", value = "档案类型", required = true) @NotBlank(message = "档案类型不能为空") String typeCode,
																	  @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
																	  @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空") Boolean tagging) {
		return new R<>(archiveListService.listBusinessOfDefined(templateTableId,typeCode, moduleId,archiveListService.isPublicUserId(tagging)));
	}

	@ApiOperation(value = "未定义的列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/undef")
	public R<List<DefinedListMetadata>> getArchiveListUndefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
																@ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
																@ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空")Boolean tagging) {
		return new R<>(archiveListService.listOfUnDefined(storageLocate, moduleId,archiveListService.isPublicUserId(tagging)));
	}
	@ApiOperation(value = "未定义的列表字段列表(业务模块使用)", httpMethod = SwaggerConstants.GET)
	@GetMapping("/business/list/undef")
	public R<List<DefinedListMetadata>> getArchiveBusinessListUndefList(@ApiParam(name = "templateTableId", value = "表模板id", required = true) @NotNull(message = "表模板id不能为空") Long templateTableId,
																		@ApiParam(name = "typeCode", value = "档案类型", required = true) @NotBlank(message = "档案类型不能为空") String typeCode,
																		  @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
																		  @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空")Boolean tagging) {
		return new R<>(archiveListService.listBusinessOfUnDefined(templateTableId,typeCode, moduleId,archiveListService.isPublicUserId(tagging)));
	}
	@ApiOperation(value = "恢复列表定义配置默认值", httpMethod = SwaggerConstants.DELETE)
	@SysLog("恢复列表定义配置默认值")
	@DeleteMapping("/recovery")
	public R restoreDefault(@ApiParam(name = "templateTableId", value = "表模板id", required = true) @NotNull(message = "表模板id不能为空") Long templateTableId,
							@ApiParam(name = "typeCode", value = "档案类型", required = true) @NotBlank(message = "档案类型不能为空") String typeCode,
							@ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId){
		return new R(archiveListService.clearListConfiguration(templateTableId,typeCode, moduleId));
	}
	/**
	 * 保存档案列表配置
	 *
	 * @param saveListMetadata 档案列表配置
	 * @return R
	 */
	@ApiOperation(value = "保存档案列表配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存档案列表配置")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "saveListMetadata", value = "排序保存对象", required = true) @Valid SaveListMetadata saveListMetadata) {
		return archiveListService.saveListDefined(saveListMetadata);
	}

	@ApiOperation(value = "获取档案门类列表定义信息", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取档案门类列表定义信息")
	public R<List<ArrayList<String>>> getListDefinitionInfo(@PathVariable("tenantId") Long tenantId) throws ArchiveBusinessException {
		return new R(archiveListService.getListDefinitionInfo(tenantId));
	}

	@SysLog("清除列表配置")
	@ApiOperation(value = "清除列表配置信息", httpMethod = SwaggerConstants.DELETE)
	@DeleteMapping("/remove-config")
	public R remove(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
					@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R(archiveListService.removeByModuleId(storageLocate, moduleId));
	}

	@SysLog("复制到其他模块")
	@ApiOperation(value = "复制到其他模块接口", httpMethod = SwaggerConstants.POST)
	@PostMapping("/copy")
	public R copyToModule(@Valid @RequestBody @ApiParam(name = "copyPostDTO", value = "复制到其他模块参数DTO") CopyPostDTO copyPostDTO) {
		return archiveListService.copy(copyPostDTO);
	}





	/**
	 * 已定义的专题列表字段列表
	 *
	 * @param storageLocate
	 * @return
	 */
	@ApiOperation(value = "已定义的专题列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/specialDef")
	public R<List<DefinedListMetadata>> listSpecialOfDefined(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
															 @ApiParam(name = "specialId", value = "专题ID", required = true) @NotNull(message = "专题ID不能为空") Long specialId,
															  @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
															  @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空") Boolean tagging) {
		return new R<>(archiveListService.listSpecialOfDefined(storageLocate,specialId, moduleId,archiveListService.isPublicUserId(tagging)));
	}



	@ApiOperation(value = "未定义的专题列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/specialUnDef")
	public R<List<DefinedListMetadata>> listSpecialOfUnDefined(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
															   @ApiParam(name = "specialId", value = "专题ID", required = true) @NotNull(message = "专题ID不能为空") Long specialId,
															   @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId,
															   @ApiParam(name = "tagging", value = "标识,公共配置 false 私有配置 true", required = true) @NotNull(message = "标识不能为空")Boolean tagging,
															   @ApiParam(name = "moduleCode", value = "分类代码", required = true) @NotNull(message = "分类代码不能为空")String moduleCode
																) {
		return new R<>(archiveListService.listSpecialOfUnDefined(storageLocate,specialId, moduleId,archiveListService.isPublicUserId(tagging), ModelTypeEnum.SPECIAL.getValue(),  moduleCode));
	}

	/**
	 * 保存专题档案列表配置
	 *
	 * @param saveListMetadata 档案列表配置
	 * @return R
	 */
	@ApiOperation(value = "保存档案列表配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存档案列表配置")
	@PostMapping("/list/saveSpecialDefined")
	public R saveSpecialDefined(@RequestBody @ApiParam(name = "saveListMetadata", value = "排序保存对象", required = true) @Valid SaveListMetadata saveListMetadata) {
		return archiveListService.saveSpecialDefined(saveListMetadata);
	}


	@ApiOperation(value = "生成默认的专题定义字段", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/saveDefaultSpecialDefined")
	public R saveDefaultSpecialDefined(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
									   @ApiParam(name = "specialId", value = "专题ID", required = true) @NotNull(message = "专题ID不能为空") Long specialId,
									   @ApiParam(name = "moduleCode", value = "分类代码", required = true) @NotNull(message = "分类代码不能为空")String moduleCode) {
		return archiveListService.saveDefaultSpecialDefined(storageLocate,specialId,moduleCode);
	}



	@ApiOperation(value = "清空专题", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/removeSpecialDefinedBySpecialId")
	public R removeSpecialDefinedBySpecialId(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate) {
		return new R<>(archiveListService.removeSpecialDefinedBySpecialId(storageLocate));
	}


}
