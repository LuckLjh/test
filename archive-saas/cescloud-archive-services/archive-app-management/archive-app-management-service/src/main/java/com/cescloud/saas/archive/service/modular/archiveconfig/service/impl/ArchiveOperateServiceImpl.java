
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveOperate;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveOperate;
import com.cescloud.saas.archive.common.constants.BusinessTypeEnum;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchiveOperateMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveOperateService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 档案字段组成规则
 *
 * @author liudong1
 * @date 2019-04-21 19:30:10
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "archive-operate-rule")
public class ArchiveOperateServiceImpl extends ServiceImpl<ArchiveOperateMapper, ArchiveOperate> implements ArchiveOperateService {


	@Cacheable(
			key = "'archive-app-management:archive-operate-rule:'+#businessId",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<ArchiveOperate> getOperateRule(Long businessId) {
		return this.list(Wrappers.<ArchiveOperate>query().lambda()
				.eq(ArchiveOperate::getBusinessId, businessId)
				.eq(ArchiveOperate::getBusinessType, BusinessTypeEnum.COLUMN_RULE.getValue()));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public R saveOperateDefined(SaveOperate saveOperate) {
		boolean empty = CollectionUtil.isEmpty(saveOperate.getData());
		if(empty){
			log.error("字段操作规则数据集为空！");
			return new R().fail(null, "操作数据项为空！");
		}
		log.debug("删除原来字段<{}>的操作配置", saveOperate.getBusinessId());
		//删除原来的配置
		this.remove(Wrappers.<ArchiveOperate>query().lambda()
				.eq(ArchiveOperate::getBusinessId, saveOperate.getBusinessId())
				.eq(ArchiveOperate::getBusinessType, saveOperate.getBusinessType()));
		//批量保存操作配置
		List<ArchiveOperate> archiveOperates = IntStream.range(0, saveOperate.getData().size())
				.mapToObj(i -> {
					ArchiveOperate operate = new ArchiveOperate();
					operate.setBusinessId(saveOperate.getBusinessId());
					operate.setBusinessType(BusinessTypeEnum.COLUMN_RULE.toString());
					operate.setStorageLocate(saveOperate.getStorageLocate());
					operate.setOperateType(saveOperate.getData().get(i).getOperateType());
					operate.setValueOne(saveOperate.getData().get(i).getValueOne());
					operate.setValueTwo(saveOperate.getData().get(i).getValueTwo());
					operate.setOperateFunction(saveOperate.getData().get(i).getOperateFunction());
					operate.setSortNo(i);

					return operate;
				}).collect(Collectors.toList());
		log.debug("批量插入字段操作规则：{}", archiveOperates.toString());
		this.saveBatch(archiveOperates);

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
		return this.remove(Wrappers.<ArchiveOperate>query().lambda()
				.eq(ArchiveOperate::getStorageLocate, storageLocate));
	}
}
