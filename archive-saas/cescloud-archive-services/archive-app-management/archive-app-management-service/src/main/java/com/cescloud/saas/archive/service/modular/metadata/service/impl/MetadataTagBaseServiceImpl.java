
package com.cescloud.saas.archive.service.modular.metadata.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTagBase;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.metadata.mapper.MetadataTagBaseMapper;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagBaseService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础标签
 *
 * @author liudong1
 * @date 2019-09-16 10:14:57
 */
@Service
public class MetadataTagBaseServiceImpl extends ServiceImpl<MetadataTagBaseMapper, MetadataTagBase> implements MetadataTagBaseService {

	@Autowired
	private MetadataTagService metadataTagService;

	/**
	 * 将base表数据插入
	 *
	 * @param tenantId
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertIntoMetadataTagFromBase(Long tenantId) {
//		SystemColumn systemColumn = new SystemColumn(tenantId, SecurityUtils.getUser().getId());
//		getBaseMapper().insertIntoMetadataTagFromBase(systemColumn);
		// 查询模板表中的数据
		List<MetadataTagBase> metadataTagBases = this.list(Wrappers.<MetadataTagBase>lambdaQuery()
				.select(MetadataTagBase::getTagChinese, MetadataTagBase::getTagEnglish, MetadataTagBase::getTagType, MetadataTagBase::getTagLength,
						MetadataTagBase::getTagDotLength, MetadataTagBase::getTagNull, MetadataTagBase::getTagDefaultValue, MetadataTagBase::getTagSort,
						MetadataTagBase::getTagDescription, MetadataTagBase::getDictCode));
		List<MetadataTag> list = metadataTagBases.stream().map(metadataTagBase -> {
			MetadataTag metadataTag = new MetadataTag();
			BeanUtil.copyProperties(metadataTagBase, metadataTag);
			metadataTag.setTenantId(tenantId);
			metadataTag.setRevision(1L);
			metadataTag.setCreatedBy(SecurityUtils.getUser().getId());
			metadataTag.setCreatedTime(LocalDateTime.now());
			return metadataTag;
		}).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(list)) {
			metadataTagService.saveBatch(list);
		}
	}

}
