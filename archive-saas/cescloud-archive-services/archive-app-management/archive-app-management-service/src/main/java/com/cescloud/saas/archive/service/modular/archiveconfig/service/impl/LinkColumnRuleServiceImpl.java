
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveLinkColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkColumnRule;
import com.cescloud.saas.archive.common.constants.ConnectSignEnum;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.LinkColumnRuleMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.LinkColumnRuleService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.LinkLayerService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 挂接字段组成规则
 *
 * @author liudong1
 * @date 2019-05-14 11:15:33
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "link-column-rule")
public class LinkColumnRuleServiceImpl extends ServiceImpl<LinkColumnRuleMapper, LinkColumnRule> implements LinkColumnRuleService {

	@Autowired
	private LinkLayerService linkLayerService;

	/**
	 * 获取字段组成规则列表
	 *
	 * @param storageLocate
	 * @param linkLayerId
	 * @return
	 */
	@Cacheable(
			key = "'archive-app-management:link-column-rule:defiend:'+#storageLocate+':'+#linkLayerId",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<DefinedColumnRuleMetadata> listOfDefined(String storageLocate, Long linkLayerId) {
		if (ObjectUtil.isNull(linkLayerId)) {
			return Collections.emptyList();
		}
		//获取配置的元数据
		List<DefinedColumnRuleMetadata> definedColumnRuleMetadata = baseMapper.selectDefinedMetadata(storageLocate, linkLayerId);
		//排序
		log.debug("对字段组合规则进行排序！");
		CollectionUtil.sortByProperty(definedColumnRuleMetadata, "sortNo");

		return definedColumnRuleMetadata;
	}

	/**
	 * 获取未关联的字段组成规则列表
	 *
	 * @param storageLocate
	 * @param linkLayerId
	 * @return
	 */
	@Cacheable(
			key = "'archive-app-management:link-column-rule:undefiend:'+#storageLocate+':'+#linkLayerId",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<DefinedColumnRuleMetadata> listOfUnDefined(String storageLocate, Long linkLayerId) {
		List<DefinedColumnRuleMetadata> unDefinedColumnRuleMetadata = baseMapper.selectUnDefinedMetadata(storageLocate, linkLayerId);
		return unDefinedColumnRuleMetadata;
	}

	@Override
	public String getLinkLayerName(String storageLocate, Long linkLayerId) {
		List<DefinedColumnRuleMetadata> columnRuleMetadataList = listOfDefined(storageLocate, linkLayerId);
		final StringBuilder name = new StringBuilder();
		columnRuleMetadataList.stream().forEach(definedColumnRuleMetadata -> {
			name.append(definedColumnRuleMetadata.getMetadataChinese());
		});
		return name.toString();
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public R saveFileLinkColumnRule(SaveLinkColumnRuleMetadata saveLinkColumnRuleMetadata) {
		boolean empty = CollectionUtil.isEmpty(saveLinkColumnRuleMetadata.getData());
		if (empty) {
			log.error("字段组成规则数据集为空！");
			return new R().fail(null, "组成规则为空！");
		}

		log.debug("删除原来表<{}>的层次<{}>挂接字段组成规则配置", saveLinkColumnRuleMetadata.getStorageLocate(), saveLinkColumnRuleMetadata.getId());
		//删除原来的配置
		this.remove(Wrappers.<LinkColumnRule>query().lambda()
				.eq(LinkColumnRule::getStorageLocate, saveLinkColumnRuleMetadata.getStorageLocate())
				.eq(LinkColumnRule::getLinkLayerId, saveLinkColumnRuleMetadata.getId()));

		List<LinkColumnRule> linkColumnRules = IntStream.range(0, saveLinkColumnRuleMetadata.getData().size())
				.mapToObj(i -> {
					LinkColumnRule columnRule = new LinkColumnRule();
					columnRule.setStorageLocate(saveLinkColumnRuleMetadata.getStorageLocate());
					columnRule.setLinkLayerId(saveLinkColumnRuleMetadata.getId());
					columnRule.setMetadataId(saveLinkColumnRuleMetadata.getData().get(i).getMetadataId());
					if (saveLinkColumnRuleMetadata.getData().get(i).getZeroFlag() == null) {
						columnRule.setZeroFlag(0);
					} else {
						columnRule.setZeroFlag(saveLinkColumnRuleMetadata.getData().get(i).getZeroFlag() ? 1 : 0);
					}
					columnRule.setConnectSign(saveLinkColumnRuleMetadata.getData().get(i).getConnectSign());
					if (saveLinkColumnRuleMetadata.getData().get(i).getConnectSign().equals(ConnectSignEnum.CONNECT.getCode())) {
						columnRule.setConnectStr(saveLinkColumnRuleMetadata.getData().get(i).getMetadataChinese());
					}
					columnRule.setDictKeyValue(saveLinkColumnRuleMetadata.getData().get(i).getDictKeyValue());
					columnRule.setSortNo(i);

					return columnRule;
				}).collect(Collectors.toList());
		//批量插入
		log.debug("批量插入字段组成规则：{}", linkColumnRules.toString());
		if (CollectionUtil.isNotEmpty(linkColumnRules)) {
			this.saveBatch(linkColumnRules);
		}
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
		return this.remove(Wrappers.<LinkColumnRule>query().lambda()
				.eq(LinkColumnRule::getStorageLocate, storageLocate));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap, Map<Long,Long> srcDestLinkLayerIdMap) {
		List<LinkColumnRule> list = this.list(Wrappers.<LinkColumnRule>lambdaQuery().eq(LinkColumnRule::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(linkColumnRule -> {
				linkColumnRule.setId(null);
				linkColumnRule.setStorageLocate(destStorageLocate);
				linkColumnRule.setMetadataId(srcDestMetadataMap.get(linkColumnRule.getMetadataId()));
				linkColumnRule.setLinkLayerId(srcDestLinkLayerIdMap.get(linkColumnRule.getLinkLayerId()));
			});
			this.saveBatch(list);
		}
	}
}
