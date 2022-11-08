package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ConfiguredDefinition;

/**
 * 配置定义Service
 *
 * @author LS
 * @date 2021/6/30
 */
public interface ConfiguredDefinitionService extends IService<ConfiguredDefinition> {

	/**
	 * 根据菜单id获取数据权限的配置屏蔽定义信息
	 *
	 * @param menuId 菜单id
	 * @return ConfiguredDefinition
	 */
	ConfiguredDefinition getDataPermissionDef(Long menuId);
}
