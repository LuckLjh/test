package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ConfiguredDefinition;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 配置屏蔽定义Feign接口
 *
 * @author LS
 * @date 2021/7/1
 */
@FeignClient(contextId = "remoteConfiguredDefinitionService", value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteConfiguredDefinitionService {
	/**
	 * 根据菜单id获取数据权限的配置屏蔽定义信息
	 *
	 * @param menuId 菜单id
	 * @return R<ConfiguredDefinition>
	 */
	@GetMapping("/configured-definition/list/data-permission/{menuId}")
	R<ConfiguredDefinition> getDataPermissionDef(@PathVariable(value = "menuId") Long menuId);
}
