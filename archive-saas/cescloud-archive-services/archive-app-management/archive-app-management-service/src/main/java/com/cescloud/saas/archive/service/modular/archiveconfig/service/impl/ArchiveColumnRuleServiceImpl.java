
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveColumnRuleMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveColumnRule;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.ConnectSignEnum;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchiveColumnRuleMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveColumnRuleService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataAutovalueService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 档案字段组成规则
 *
 * @author liudong1
 * @date 2019-04-19 15:06:53
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "archive-column-rule")
public class ArchiveColumnRuleServiceImpl extends ServiceImpl<ArchiveColumnRuleMapper, ArchiveColumnRule> implements ArchiveColumnRuleService {

	@Autowired
	private MetadataService metadataService;
	@Autowired
	private ArchiveTableService archiveTableService;
	@Autowired
	private MetadataAutovalueService metadataAutovalueService;
	/**
	 * 获取字段组成规则列表
	 *
	 * @param metadataSourceId
	 * @return
	 */
	@Cacheable(
			key = "'archive-app-management:archive-column-rule:defined:'+#metadataSourceId",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<DefinedColumnRuleMetadata> listOfDefined(Long metadataSourceId) {
		//获取配置的元数据
		List<DefinedColumnRuleMetadata> definedColumnRuleMetadata = baseMapper.selectDefinedMetadata(metadataSourceId);
		//排序
		log.debug("对字段组合规则进行排序！");
		CollectionUtil.sortByProperty(definedColumnRuleMetadata, "sortNo");

		return definedColumnRuleMetadata;
	}

	@Override
	public MetadataAutovalue getFlowNoColumn(Long moduleId, String storageLocate, List<DefinedColumnRuleMetadata> definedColumnRuleMetadatas) {
		if (CollectionUtil.isEmpty(definedColumnRuleMetadatas)) {
			return null;
		}
		final Set<Long> metadataIds = definedColumnRuleMetadatas.stream()
				.filter(definedColumnRuleMetadata -> ConnectSignEnum.METADATA.getCode().equals(definedColumnRuleMetadata.getConnectSign()))
				.map(DefinedColumnRuleMetadata::getMetadataId).collect(Collectors.toSet());
		final List<MetadataAutovalue> definedAutovalues = metadataAutovalueService.getDefinedAutovalues(storageLocate, moduleId);
		// type == 0 说明是累加规则
		final MetadataAutovalue metadataAutovalue = definedAutovalues.stream()
				.filter(definedAutovalue -> BoolEnum.NO.getCode().equals(definedAutovalue.getType()))
				.filter(definedAutovalue -> metadataIds.contains(definedAutovalue.getMetadataId()))
				.findFirst().orElse(null);
		return metadataAutovalue;
	}

	/**
	 * 获取未定义字段组成规则列表
	 * @param storageLocate
	 * @param metadataSourceId
	 * @return
	 */
	@Cacheable(
			key = "'archive-app-management:archive-column-rule:undefined:'+#storageLocate+':'+#metadataSourceId+':'+#metadataId+':'+#moduleId",
			unless = "#result == null "
	)
	@Override
	public DefinedColumnRuleDTO mapOfUnDefined(String storageLocate, Long metadataSourceId, Long metadataId, Long moduleId) {
		//Map<String,List<DefinedColumnRuleMetadata>>  mapOfUnDefined = new HashMap<String,List<DefinedColumnRuleMetadata>>();
		DefinedColumnRuleDTO definedColumnRuleDTO = new DefinedColumnRuleDTO();
		definedColumnRuleDTO.setCurrentLevelFields(listOfUnDefined(storageLocate,metadataSourceId,metadataId,moduleId));
		//根据当前存储表获取上层存储表
		ArchiveTable archiveTable = archiveTableService.getUpTableByStorageLocate(storageLocate);
		Integer upperLevel = 0;
		if(ObjectUtil.isNotNull(archiveTable)){
			definedColumnRuleDTO.setUpperLevelFields(listOfUnDefined(archiveTable.getStorageLocate(),metadataSourceId,metadataId,moduleId));
			upperLevel = 1;
		}
		definedColumnRuleDTO.setUpperLevel(upperLevel);
		return definedColumnRuleDTO;
	}


	public  List<DefinedColumnRuleMetadata> listOfUnDefined(String storageLocate, Long metadataSourceId, Long metadataId,Long moduleId){
		List<DefinedColumnRuleMetadata> unDefinedColumnRuleMetadata = baseMapper.selectUnDefinedEdit(storageLocate, metadataSourceId,metadataId,moduleId);
		if (CollectionUtil.isEmpty(unDefinedColumnRuleMetadata)) {
			return unDefinedColumnRuleMetadata;
		}
		//查询metadata表给metadata_chinese、metadata_english、metadata_length、dict_code和sort_no属性赋值
		List<Long> idList = unDefinedColumnRuleMetadata.parallelStream().map(r -> r.getMetadataId()).collect(Collectors.toList());
		Collection<Metadata> metadataList = metadataService.listByIds(idList);
		Map<Long, Metadata> idMetadataMap = metadataList.parallelStream().collect(Collectors.toMap(Metadata::getId, Function.identity()));
		unDefinedColumnRuleMetadata.parallelStream().forEach(r -> {
			Metadata m = idMetadataMap.get(r.getMetadataId());
			if (ObjectUtil.isNotNull(m)) {
				r.setMetadataChinese(m.getMetadataChinese());
				r.setMetadataEnglish(m.getMetadataEnglish());
				r.setMetadataLength(m.getMetadataLength());
				r.setStorageLocate(storageLocate);
				r.setDictCode(m.getDictCode());
				r.setSortNo(m.getSortNo());
			}
		});
		return unDefinedColumnRuleMetadata;
	}

	/**
	 * 保存字段组成规则
	 * @param saveColumnRuleMetadata
	 * @return
	 */
	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveColumnRuleDefined(SaveColumnRuleMetadata saveColumnRuleMetadata) {
		boolean empty = CollectionUtil.isEmpty(saveColumnRuleMetadata.getData());
		if (empty) {
			log.error("字段组成规则数据集为空！");
			return false;
		}
		if (log.isDebugEnabled()){
			log.debug("删除原来表<{}>字段<{}>的字段组成规则配置", saveColumnRuleMetadata.getStorageLocate(), saveColumnRuleMetadata.getMetadataSourceId());
		}
		//删除原来的配置
		this.remove(Wrappers.<ArchiveColumnRule>query().lambda()
				.eq(ArchiveColumnRule::getMetadataSourceId, saveColumnRuleMetadata.getMetadataSourceId()));

		List<ArchiveColumnRule> archiveColumnRules = IntStream.range(0, saveColumnRuleMetadata.getData().size())
				.mapToObj(i -> {
					ArchiveColumnRule columnRule = new ArchiveColumnRule();
					columnRule.setStorageLocate(saveColumnRuleMetadata.getData().get(i).getStorageLocate());
					columnRule.setMetadataSourceId(saveColumnRuleMetadata.getMetadataSourceId());
					columnRule.setMetadataId(saveColumnRuleMetadata.getData().get(i).getMetadataId());
					if (ObjectUtil.isNull(saveColumnRuleMetadata.getData().get(i).getZeroFlag())) {
						columnRule.setZeroFlag(0);
					} else {
						columnRule.setZeroFlag(saveColumnRuleMetadata.getData().get(i).getZeroFlag() ? 1 : 0);
					}
					columnRule.setDigitZero(saveColumnRuleMetadata.getData().get(i).getDigitZero());
					columnRule.setUpperLevel(saveColumnRuleMetadata.getData().get(i).getUpperLevel());
					columnRule.setConnectSign(saveColumnRuleMetadata.getData().get(i).getConnectSign());
					if (saveColumnRuleMetadata.getData().get(i).getConnectSign().equals(ConnectSignEnum.CONNECT.getCode())) {
						columnRule.setConnectStr(saveColumnRuleMetadata.getData().get(i).getMetadataChinese());
					}
					columnRule.setDictKeyValue(saveColumnRuleMetadata.getData().get(i).getDictKeyValue());
					columnRule.setSortNo(i);

					return columnRule;
				}).collect(Collectors.toList());
		//批量插入
		if (log.isDebugEnabled()){
			log.debug("批量插入字段组成规则：{}", archiveColumnRules.toString());
		}
		this.saveBatch(archiveColumnRules);

		return true;

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean removeByWrappers(Wrapper<ArchiveColumnRule> queryWrapper) {
		return this.remove(queryWrapper);
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
		return this.remove(Wrappers.<ArchiveColumnRule>query().lambda()
				.eq(ArchiveColumnRule::getStorageLocate, storageLocate));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate,
										 Map<Long,Long> srcDestAutovalueIdMap, Map<Long, Long> srcDestMetadataMap) {
		Set<Long> srcIdSet = srcDestAutovalueIdMap.keySet();
		if (CollectionUtil.isEmpty(srcIdSet)) {
			return;
		}
		List<ArchiveColumnRule> list = this.list(Wrappers.<ArchiveColumnRule>lambdaQuery().in(ArchiveColumnRule::getMetadataSourceId, srcIdSet));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(archiveColumnRule -> {
				archiveColumnRule.setId(null);
				if (ConnectSignEnum.METADATA.getCode().equals(archiveColumnRule.getConnectSign())) {
					archiveColumnRule.setStorageLocate(destStorageLocate);
					archiveColumnRule.setMetadataId(srcDestMetadataMap.get(archiveColumnRule.getMetadataId()));
				}
				archiveColumnRule.setMetadataSourceId(srcDestAutovalueIdMap.get(archiveColumnRule.getMetadataSourceId()));
			});
			this.saveBatch(list);
		}
	}

}
