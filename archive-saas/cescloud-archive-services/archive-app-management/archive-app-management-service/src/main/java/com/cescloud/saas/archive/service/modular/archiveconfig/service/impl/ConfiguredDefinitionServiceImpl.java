package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ConfiguredDefinition;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ConfiguredDefinitionMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ConfiguredDefinitionService;
import org.springframework.stereotype.Service;

/**
 * 配置定义Service实现
 *
 * @author LS
 * @date 2021/6/30
 */
@Service
public class ConfiguredDefinitionServiceImpl extends ServiceImpl<ConfiguredDefinitionMapper, ConfiguredDefinition> implements ConfiguredDefinitionService {

	@Override
	public ConfiguredDefinition getDataPermissionDef(Long menuId) {
		return this.getOne(Wrappers.<ConfiguredDefinition>lambdaQuery().eq(ConfiguredDefinition::getMenuId, menuId).isNull(ConfiguredDefinition::getArchiveLayer));
	}
}
