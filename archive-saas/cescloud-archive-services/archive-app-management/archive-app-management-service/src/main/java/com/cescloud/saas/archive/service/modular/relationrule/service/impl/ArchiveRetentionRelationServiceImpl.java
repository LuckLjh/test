package com.cescloud.saas.archive.service.modular.relationrule.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.relationrule.dto.PostArchiveRetentionRelation;
import com.cescloud.saas.archive.api.modular.relationrule.entity.ArchiveRetentionRelation;
import com.cescloud.saas.archive.service.modular.relationrule.mapper.ArchiveRetentionRelationMapper;
import com.cescloud.saas.archive.service.modular.relationrule.service.ArchiveRetentionRelationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArchiveRetentionRelationServiceImpl  extends ServiceImpl<ArchiveRetentionRelationMapper, ArchiveRetentionRelation> implements ArchiveRetentionRelationService {

    @Override
    public List<ArchiveRetentionRelation> getByTypeCode(String typeCode) {
        return this.list(Wrappers.<ArchiveRetentionRelation>lambdaQuery().eq(ArchiveRetentionRelation::getArchiveTypeCode,typeCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRelation(PostArchiveRetentionRelation postArchiveRetentionRelation) {
        String archiveTypeCode = postArchiveRetentionRelation.getArchiveTypeCode();
        List<ArchiveRetentionRelation> archiveRetentionRelations = postArchiveRetentionRelation.getArchiveRetentionRelations();
        this.remove(Wrappers.<ArchiveRetentionRelation>lambdaQuery().eq(ArchiveRetentionRelation::getArchiveTypeCode,archiveTypeCode));
        return this.saveBatch(archiveRetentionRelations);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyByTypeCode(String srcTypeCode, String destTypeCode) {
		List<ArchiveRetentionRelation> srcList = this.list(Wrappers.<ArchiveRetentionRelation>lambdaQuery().eq(ArchiveRetentionRelation::getArchiveTypeCode, srcTypeCode));
		if (CollectionUtil.isNotEmpty(srcList)) {
			List<ArchiveRetentionRelation> destList = srcList.stream().map(e -> {
				ArchiveRetentionRelation archiveRetentionRelation = new ArchiveRetentionRelation();
				BeanUtil.copyProperties(e, archiveRetentionRelation);
				archiveRetentionRelation.setId(null);
				archiveRetentionRelation.setArchiveTypeCode(destTypeCode);
				return archiveRetentionRelation;
			}).collect(Collectors.toList());
			this.saveBatch(destList);
		}
	}
}
