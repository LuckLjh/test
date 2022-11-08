package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.TenantInfoService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * @ClassName TenantInfoController
 * @Author zhangxuehu
 * @Date 2020/4/22 11:10
 **/
@Api(value = "tenant-info", tags = "初始化门类配置")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/tenant-info")
public class TenantInfoController {

	private final TenantInfoService tenantInfoService;

	@ApiOperation(value = "初始化档案类型模板", httpMethod = SwaggerConstants.POST, hidden = true)
	@PostMapping("/archive-type-template")
	@SysLog("初始化档案类型模板")
	public R initializeArchive(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") @NotNull(message = "租户Id不能为空") Long tenantId) {
			return tenantInfoService.initializeArchive(templateId,tenantId);
	}

	@ApiOperation(value = "初始化档案门类", httpMethod = SwaggerConstants.POST, hidden = true)
	@PostMapping("/archive-type")
	@SysLog("初始化档案门类")
	public R initializeArchiveType(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") @NotNull(message = "租户Id不能为空") Long tenantId) throws ArchiveBusinessException {
		return tenantInfoService.initializeArchiveType(templateId,tenantId);
	}

	@ApiOperation(value = "初始化门类配置", httpMethod = SwaggerConstants.POST, hidden = true)
	@PostMapping("/form-data")
	@SysLog("初始化门类配置")
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") @NotNull(message = "租户Id不能为空") Long tenantId) throws ArchiveBusinessException {
		return tenantInfoService.initializeHandle(templateId,tenantId);
	}

	@ApiOperation(value = "清除租户基本配置和门类配置信息", httpMethod = SwaggerConstants.DELETE, hidden = true)
	@DeleteMapping("/remove/basic/{tenantId}")
	@SysLog("清除租户基本配置和门类配置信息")
	public R clearAppBasicConfiguration(@PathVariable(value = "tenantId") @NotNull(message = "租户Id不能为空") Long tenantId) {
		return tenantInfoService.clearAppBasicConfiguration(tenantId);
	}

	@ApiOperation(value = "清除租户门类信息", httpMethod = SwaggerConstants.DELETE, hidden = true)
	@DeleteMapping("/remove/archive/{tenantId}")
	@SysLog("清除租户门类信息")
	public R clearAppArchive(@PathVariable(value = "tenantId") @NotNull(message = "租户Id不能为空") Long tenantId) throws ArchiveBusinessException {
		return tenantInfoService.clearAppArchive(tenantId);
	}
}
