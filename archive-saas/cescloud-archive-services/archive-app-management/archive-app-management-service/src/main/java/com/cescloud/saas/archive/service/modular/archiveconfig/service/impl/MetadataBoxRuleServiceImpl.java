package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleUndefinedDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataBoxRule;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.MetadataBoxRuleMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.MetadataBoxRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName MetadataBoxRuleServiceImpl
 * @Author zhangxuehu
 * @Date 2020/7/27 10:50
 **/
@Service
@Slf4j
public class MetadataBoxRuleServiceImpl extends ServiceImpl<MetadataBoxRuleMapper, MetadataBoxRule> implements MetadataBoxRuleService {

    @Override
    public List<MetadataBoxRuleUndefinedDTO> listOfUnDefined(String storageLocate, Long configId) {
        return baseMapper.listOfUnDefined(storageLocate,configId);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void copyConfig(Map<Long,Long> srcDestMetadataMap, Map<Long,Long> srcDestConfigIdMap) {
		Set<Long> idSet = srcDestConfigIdMap.keySet();
		if (CollectionUtil.isEmpty(idSet)) {
			return;
		}
		List<MetadataBoxRule> list = this.list(Wrappers.<MetadataBoxRule>lambdaQuery().in(MetadataBoxRule::getConfigId, idSet));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(metadataBoxRule -> {
				metadataBoxRule.setId(null);
				metadataBoxRule.setConfigId(srcDestConfigIdMap.get(metadataBoxRule.getConfigId()));
				metadataBoxRule.setMetadataId(srcDestMetadataMap.get(metadataBoxRule.getMetadataId()));
			});
			this.saveBatch(list);
		}
	}
}
