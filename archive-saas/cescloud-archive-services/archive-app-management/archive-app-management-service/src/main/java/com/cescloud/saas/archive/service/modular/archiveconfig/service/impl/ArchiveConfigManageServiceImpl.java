package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveConfigManage;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ConfiguredDefinition;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.TypedefEnum;
import com.cescloud.saas.archive.common.util.ArchiveTableUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchiveConfigManageMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ConfiguredDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ArchiveConfigManageServiceImpl
 * @Author zhangxuehu
 * @Date 2020/5/8 10:37
 **/
@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "archive-config-manage")
public class ArchiveConfigManageServiceImpl extends ServiceImpl<ArchiveConfigManageMapper, ArchiveConfigManage> implements ArchiveConfigManageService {

	private final ConfiguredDefinitionService configuredDefinitionService;

    @Cacheable(
            key = "'archive-app-management:archive-config-manage:defined:'+#storageLocate+':'+#sysId",
            unless = "#result == null || #result.size() == 0"
    )
	@Override
	public List<Map<String, Object>> getArchiveConfigManageList(String storageLocate, Long sysId) {
		String layer = ArchiveTableUtil.getArchiveLayerByStorageLocate(storageLocate);
		List<ConfiguredDefinition> configuredDefinitionList = configuredDefinitionService.list(Wrappers.<ConfiguredDefinition>lambdaQuery().eq(ConfiguredDefinition::getArchiveLayer, layer));
		final List<ArchiveConfigManage> archiveConfigManages = this.list(Wrappers.<ArchiveConfigManage>lambdaQuery().eq(ArchiveConfigManage::getStorageLocate, storageLocate));
		List<Map<String, Object>> list = CollUtil.newLinkedList();
		configuredDefinitionList.forEach(e -> {
			Map<String, Object> map = new HashMap<>(15);
			map.put("menuName", e.getMenuName());
			map.put("menuId", e.getMenuId());
			String configured = e.getConfigured();
			if (StrUtil.isNotBlank(configured)) {
				List<Integer> defs = Arrays.stream(e.getConfigured().split(StrUtil.COMMA)).map(Integer::new).collect(Collectors.toList());
				defs.forEach(def -> {
					ArchiveConfigManage configManage = archiveConfigManages.stream().filter(archiveConfigManage -> archiveConfigManage.getModuleId().equals(e.getMenuId())
							&& def.equals(archiveConfigManage.getTypedef())).findAny().orElse(null);
					String code = TypedefEnum.getEnum(def).getCode();
					if (ObjectUtil.isNotNull(configManage)) {
						map.put(code, configManage.getIsDefine());
					} else {
						map.put(code, 0);
					}
				});
			}
			list.add(map);
		});
		return list;
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean save(String storageLocate, Long moduleId, Integer typedef) {
		ArchiveConfigManage archiveConfigManage = this.getOne(Wrappers.<ArchiveConfigManage>lambdaQuery().eq(ArchiveConfigManage::getStorageLocate, storageLocate).eq(ArchiveConfigManage::getModuleId, moduleId).eq(ArchiveConfigManage::getTypedef, typedef));
		if (ObjectUtil.isNotNull(archiveConfigManage)) {
			archiveConfigManage.setIsDefine(BoolEnum.YES.getCode());
			this.updateById(archiveConfigManage);
		} else {
			ArchiveConfigManage archiveConfigManage1 = ArchiveConfigManage.builder().storageLocate(storageLocate).moduleId(moduleId).typedef(typedef).isDefine(BoolEnum.YES.getCode()).build();
			this.save(archiveConfigManage1);
		}
		return true;
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveBatchByModuleIds(String storageLocate, List<Long> moduleIds, Integer typedef) {
		final List<ArchiveConfigManage> archiveConfigManages = this.list(Wrappers.<ArchiveConfigManage>lambdaQuery().eq(ArchiveConfigManage::getStorageLocate, storageLocate).in(ArchiveConfigManage::getModuleId, moduleIds).eq(ArchiveConfigManage::getTypedef, typedef));
		final List<Long> ids = archiveConfigManages.stream().map(archiveConfigManage -> archiveConfigManage.getId()).collect(Collectors.toList());
		if (CollectionUtil.isNotEmpty(ids)) {
			ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().isDefine(BoolEnum.YES.getCode()).build();
			this.update(archiveConfigManage, Wrappers.<ArchiveConfigManage>lambdaQuery().in(ArchiveConfigManage::getId, ids));
		}
		final List<Long> definedModuleIds = archiveConfigManages.stream().map(archiveConfigManage -> archiveConfigManage.getModuleId()).collect(Collectors.toList());
		final List<Long> undefinedModuleIds = moduleIds.stream().filter(integer -> !definedModuleIds.contains(integer)).collect(Collectors.toList());
		final List<ArchiveConfigManage> archiveConfigManages1 = CollectionUtil.newArrayList();
		undefinedModuleIds.stream().forEach(integer -> {
			ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().storageLocate(storageLocate).isDefine(BoolEnum.YES.getCode()).moduleId(integer).typedef(typedef).build();
			archiveConfigManages1.add(archiveConfigManage);
		});
		if (CollectionUtil.isNotEmpty(archiveConfigManages1)) {
			this.saveBatch(archiveConfigManages1);
		}
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public Boolean update(String storageLocate, Long moduleId, Integer typedef, Integer isDefine) {
		if (ObjectUtil.isNull(isDefine)) {
			// 空为 未定义
			isDefine = 0;
		}
		ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().isDefine(isDefine).build();
		return this.update(archiveConfigManage, Wrappers.<ArchiveConfigManage>lambdaQuery().eq(ArchiveConfigManage::getTypedef, typedef).eq(ArchiveConfigManage::getModuleId, moduleId).eq(ArchiveConfigManage::getStorageLocate, storageLocate));
	}

	@Cacheable(
			key = "'archive-app-management:archive-config-manage:is-defined:'+#moduleId+':'+#storageLocate+':'+#typedef",
			unless = "#result == null"
	)
	@Override
	public Boolean checkModuleIsDefined(Long moduleId, String storageLocate, Integer typedef) {
		ArchiveConfigManage archiveConfigManage = this.getOne(
				Wrappers.<ArchiveConfigManage>lambdaQuery().eq(ArchiveConfigManage::getModuleId, moduleId)
						.eq(ArchiveConfigManage::getStorageLocate, storageLocate)
						.eq(ArchiveConfigManage::getTypedef, typedef)
		);
		if (ObjectUtil.isNull(archiveConfigManage)) {
			return Boolean.FALSE;
		}
		Integer isDefine = archiveConfigManage.getIsDefine();
		return BoolEnum.YES.getCode().equals(isDefine);
	}

	@Override
	@CacheEvict(allEntries = true)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate) {
		List<ArchiveConfigManage> list = this.list(Wrappers.<ArchiveConfigManage>lambdaQuery().eq(ArchiveConfigManage::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(archiveConfigManage -> {
				archiveConfigManage.setId(null);
				archiveConfigManage.setStorageLocate(destStorageLocate);
			});
			this.saveBatch(list);
		}
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveInit(String storageLocate, Long moduleId, Integer typedef, Long tenantId) {
		ArchiveConfigManage archiveConfigManage = this.getOne(Wrappers.<ArchiveConfigManage>lambdaQuery()
				.eq(ArchiveConfigManage::getStorageLocate, storageLocate).eq(ArchiveConfigManage::getModuleId, moduleId)
				.eq(ArchiveConfigManage::getTypedef, typedef).eq(ArchiveConfigManage::getTenantId, tenantId));
		if (ObjectUtil.isNotNull(archiveConfigManage)) {
			archiveConfigManage.setIsDefine(BoolEnum.YES.getCode());
			this.updateById(archiveConfigManage);
		} else {
			ArchiveConfigManage archiveConfigManage1 = ArchiveConfigManage.builder().tenantId(tenantId).storageLocate(storageLocate).moduleId(moduleId).typedef(typedef).isDefine(BoolEnum.YES.getCode()).build();
			this.save(archiveConfigManage1);
		}
		return true;
	}


}
