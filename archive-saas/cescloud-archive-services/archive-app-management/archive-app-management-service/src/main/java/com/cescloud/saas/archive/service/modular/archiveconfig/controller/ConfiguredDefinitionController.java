package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ConfiguredDefinition;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ConfiguredDefinitionService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * @author LS
 * @date 2021/7/1
 */
@Api(value = "configuredDefinition", tags = "配置屏蔽定义")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/configured-definition")
public class ConfiguredDefinitionController {

	private final ConfiguredDefinitionService configuredDefinitionService;

	@ApiOperation(value = "根据菜单id获取数据权限的配置屏蔽定义信息", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/data-permission/{menuId}")
	public R<ConfiguredDefinition> getDataPermissionDef(@PathVariable @NotNull(message = "菜单id不能为空") Long menuId) {
		return new R<>(configuredDefinitionService.getDataPermissionDef(menuId));
	}
}
