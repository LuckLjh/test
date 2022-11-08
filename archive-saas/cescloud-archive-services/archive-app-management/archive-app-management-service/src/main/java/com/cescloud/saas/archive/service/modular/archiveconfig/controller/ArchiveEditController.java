
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedEditMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveEditMetadata;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveEditService;
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
import java.util.List;


/**
 * 档案录入配置
 *
 * @author liudong1
 * @date 2019-04-18 16:06:51
 */
@Api(value = "archiveEdit", tags = "应用管理-档案门类管理:档案录入配置管理")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/edit")
public class ArchiveEditController {

	private final ArchiveEditService archiveEditService;

	/**
	 * 已定义的录入字段列表
	 *
	 * @param storageLocate
	 * @return
	 */
	@ApiOperation(value = "已定义的录入字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/def")
	public R<List<DefinedEditMetadata>> getArchiveEditdefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
															  @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(archiveEditService.listOfDefined(storageLocate, moduleId));
	}

	/**
	 * 未定义的录入字段列表
	 *
	 * @param storageLocate
	 * @return
	 */
	@ApiOperation(value = "未定义的录入字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/undef")
	public R<List<DefinedEditMetadata>> getArchiveEditUndefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
																@ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(archiveEditService.listOfUnDefined(storageLocate, moduleId));
	}

	/**
	 * 保存档案录入字段配置
	 *
	 * @param saveEditMetadata 档案录入字段配置
	 * @return R
	 */
	@ApiOperation(value = "保存档案录入字段配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存档案录入字段配置")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "saveEditMetadata", value = "录入字段保存对象", required = true) @Valid SaveEditMetadata saveEditMetadata) {
		return archiveEditService.saveEditDefined(saveEditMetadata);
	}

	@ApiOperation(value = "获取档案门类表单字段配置信息", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取档案门类表单字段配置信息")
	public R getFormFieldInfo(@PathVariable("tenantId") Long tenantId) throws ArchiveBusinessException {
		return new R(archiveEditService.getFormFieldInfo(tenantId));
	}

	@SysLog("清除表单配置")
	@ApiOperation(value = "清除表单配置信息", httpMethod = SwaggerConstants.DELETE)
	@DeleteMapping("/remove-config")
	public R remove(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
					@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R(archiveEditService.removeByModuleId(storageLocate, moduleId));
	}

	@SysLog("复制到其他模块")
	@ApiOperation(value = "复制到其他模块接口", httpMethod = SwaggerConstants.POST)
	@PostMapping("/copy")
	public R copyToModule(@Valid @RequestBody @ApiParam(name = "copyPostDTO",value = "复制到其他模块参数DTO") CopyPostDTO copyPostDTO) {
		return archiveEditService.copy(copyPostDTO);
	}
}
