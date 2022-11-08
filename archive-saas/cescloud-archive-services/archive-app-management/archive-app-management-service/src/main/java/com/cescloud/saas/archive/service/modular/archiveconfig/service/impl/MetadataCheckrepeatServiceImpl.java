
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.constant.ConfigConstant;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedRepeatMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveRepeatMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataCheckrepeat;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.MetadataCheckrepeatMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.MetadataCheckrepeatService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 查重设置
 *
 * @author liudong1
 * @date 2019-04-23 12:08:06
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "metadata-check-repeat")
public class MetadataCheckrepeatServiceImpl extends ServiceImpl<MetadataCheckrepeatMapper, MetadataCheckrepeat> implements MetadataCheckrepeatService {

	@Cacheable(
			key = "'archive-app-management:metadata-check-repeat:defiend:'+#storageLocate",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<DefinedRepeatMetadata> listOfDefined(String storageLocate) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseDefined(storageLocate);
		} else {
			return baseMapper.listOfDefined(storageLocate);
		}
	}

	@Cacheable(
			key = "'archive-app-management:metadata-check-repeat:undefiend:'+#storageLocate",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<DefinedRepeatMetadata> listOfUnDefined(String storageLocate) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseUnDefined(storageLocate);
		} else {
			return baseMapper.listOfUnDefined(storageLocate);
		}
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveReportDefined(SaveRepeatMetadata saveRepeatMetadata) {
		boolean empty = CollectionUtil.isEmpty(saveRepeatMetadata.getData());
		if (empty) {
			log.error("查重配置规则数据集为空！");
			return new R().fail(null, "查重配置项为空！");
		}
		log.debug("删除原来表<{}>的字段重复配置", saveRepeatMetadata.getStorageLocate());
		//删除原来的配置
		this.remove(Wrappers.<MetadataCheckrepeat>query().lambda()
				.eq(MetadataCheckrepeat::getStorageLocate, saveRepeatMetadata.getStorageLocate()));
		//批量插入配置
		List<MetadataCheckrepeat> metadataCheckrepeatList = saveRepeatMetadata.getData().stream().map(definedRepeatMetadata -> {
			MetadataCheckrepeat checkRepeat = new MetadataCheckrepeat();
			//可以将 1 转为 true, 0 转为 false
			BeanUtil.copyProperties(definedRepeatMetadata, checkRepeat);
			checkRepeat.setStorageLocate(saveRepeatMetadata.getStorageLocate());

			return checkRepeat;
		}).collect(Collectors.toList());

		log.debug("批量插入字段重复定义规则：{}", metadataCheckrepeatList.toString());
		this.saveBatch(metadataCheckrepeatList);

		return new R().success(null, "保存成功！");
	}

	/**
	 * 删除规则
	 *
	 * @param storageLocate
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean deleteByStorageLocate(String storageLocate) {
		return this.remove(Wrappers.<MetadataCheckrepeat>query().lambda()
				.eq(MetadataCheckrepeat::getStorageLocate, storageLocate));
	}
}
