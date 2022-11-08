package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.DispAppraisalRule;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.role.entity.SysRole;
import com.cescloud.saas.archive.api.modular.role.feign.RemoteRoleService;
import com.cescloud.saas.archive.common.constants.ArchiveConstants;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.TypedefEnum;
import com.cescloud.saas.archive.common.constants.business.AppraisalTypeEnum;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.DispAppraisalRuleMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.DispAppraisalRuleService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@CacheConfig(cacheNames = "archive-disp-appraisal-rule")
public class DispAppraisalRuleServiceImpl extends ServiceImpl<DispAppraisalRuleMapper, DispAppraisalRule> implements DispAppraisalRuleService {

	@Autowired @Lazy
    private ArchiveConfigManageService archiveConfigManageService;

	@Autowired
    private ArchiveTableService archiveTableService;

	@Autowired
	private RemoteRoleService remoteRoleService;

    @Caching(evict = {
            @CacheEvict(cacheNames = "archive-disp-appraisal-rule", allEntries = true),
            @CacheEvict(cacheNames = "archive-config-manage", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean saveDispAppraisalRule(DispAppraisalRule dispAppraisalRule) {
        DispAppraisalRule byStorageLocate = this.getByStorageLocate(dispAppraisalRule.getStorageLocate());
//		if (dispAppraisalRule.getRetentionAutoReminder() != null && dispAppraisalRule.getRetentionAutoReminder() == 0) {
//			dispAppraisalRule.setBegunDateMetaId(null);
//			dispAppraisalRule.setRetentionPeriodMetaId(null);
//		}
//		if (dispAppraisalRule.getRetentionAutoReminder() != null && dispAppraisalRule.getRetentionAutoReminder() == 0) {
//			dispAppraisalRule.setOpenMetadataId(null);
//			dispAppraisalRule.setOpenValidity(null);
//		}
//		if (dispAppraisalRule.getSecurityAutoReminder() != null && dispAppraisalRule.getSecurityAutoReminder() == 0) {
//			dispAppraisalRule.setSecurityClassifMetaId(null);
//			dispAppraisalRule.setSecurityClassifValidity(null);
//		}
        if (ObjectUtil.isNull(byStorageLocate)) {
            this.save(dispAppraisalRule);
        } else {
			LambdaUpdateWrapper<DispAppraisalRule> wrapper = new LambdaUpdateWrapper<>();
			if (ObjectUtil.isNull(dispAppraisalRule.getRetentionLeadTime())) {
				wrapper.set(DispAppraisalRule::getRetentionLeadTime, null);
			}
			wrapper.eq(DispAppraisalRule::getId, byStorageLocate.getId());
            this.update(dispAppraisalRule, wrapper);
        }
        archiveConfigManageService.save(dispAppraisalRule.getStorageLocate(), ArchiveConstants.PUBLIC_MODULE_FLAG, TypedefEnum.APPRAISALRULE.getValue());
        return Boolean.TRUE;
    }

	@Cacheable(
			key = "'archive-app-management:archive-appraisal-rule:storageLocate:'+#storageLocate",
			unless = "#result == null "
	)
    @Override
    public DispAppraisalRule getByStorageLocate(String storageLocate) {
		DispAppraisalRule dispAppraisalRule = this.getOne(Wrappers.<DispAppraisalRule>lambdaQuery().eq(DispAppraisalRule::getStorageLocate, storageLocate));
		if (ObjectUtil.isNotNull(dispAppraisalRule)){
			String remindRoles = dispAppraisalRule.getRemindRoles();
			if (StrUtil.isBlank(remindRoles)){
				return dispAppraisalRule;
			}
			R<List<SysRole>> result = remoteRoleService.getRoleListByIds(remindRoles, SecurityConstants.FROM_IN);
			if (ObjectUtil.isNotNull(result) && CommonConstants.SUCCESS.equals(result.getCode())){
				List<SysRole> roleList = result.getData();
				dispAppraisalRule.setRoleList(roleList);
			}
		}
		return dispAppraisalRule;
    }

    @Override
    public List<DispAppraisalRule> getAll() {
        return this.list();
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(allEntries = true)
    @Override
    public Boolean deleteDispAppraisalRule(DispAppraisalRule dispAppraisalRule) {
        this.removeById(dispAppraisalRule.getId());
        archiveConfigManageService.update(dispAppraisalRule.getStorageLocate(), ArchiveConstants.PUBLIC_MODULE_FLAG, TypedefEnum.APPRAISALRULE.getValue(), BoolEnum.NO.getCode());
        return Boolean.TRUE;
    }

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
    public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap) {
		List<DispAppraisalRule> list = this.list(Wrappers.<DispAppraisalRule>lambdaQuery().eq(DispAppraisalRule::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(dispAppraisalRule -> {
				dispAppraisalRule.setId(null);
				dispAppraisalRule.setStorageLocate(destStorageLocate);
				dispAppraisalRule.setBegunDateMetaId(srcDestMetadataMap.get(dispAppraisalRule.getBegunDateMetaId()));
				dispAppraisalRule.setRetentionPeriodMetaId(srcDestMetadataMap.get(dispAppraisalRule.getRetentionPeriodMetaId()));
			});
			this.saveBatch(list);
		}
	}

    @Override
    public Boolean checkDispAppraisalRule(String typeCode, Integer appraisalType) throws ArchiveBusinessException {
        String storageLocate = getStorageLocate(typeCode);
        DispAppraisalRule dispAppraisalRule = this.getOne(Wrappers.<DispAppraisalRule>lambdaQuery().eq(DispAppraisalRule::getStorageLocate, storageLocate));
        AppraisalTypeEnum appraisalTypeEnum = AppraisalTypeEnum.getEnum(appraisalType);
        if(ObjectUtil.isNull(dispAppraisalRule)){
            throw new ArchiveBusinessException("该档案类型，鉴定规则尚未配置，请到门类管理进行配置！！！");
        }
        if (AppraisalTypeEnum.RETENTIONPERIOD.equals(appraisalTypeEnum)) {
	        Long metaId = dispAppraisalRule.getBegunDateMetaId();
	        if(ObjectUtil.isNull(metaId)){
		        throw new ArchiveBusinessException("该档案类型，到期鉴定规则尚未配置，请到门类管理进行配置！！！");
	        }
        }
        return Boolean.TRUE;
    }

    private String getStorageLocate(String typeCode) {
        List<ArchiveTable> archiveTables = archiveTableService.getTableListByTypeCode(typeCode);
        // 过滤各门类首层  项目层 取案卷层 表名
        ArchiveTable archiveTable = archiveTables.stream().filter(archiveTable1 -> ArchiveLayerEnum.ONE.getCode().equals(archiveTable1.getArchiveLayer())
                || ArchiveLayerEnum.SINGLE.getCode().equals(archiveTable1.getArchiveLayer())
                || ArchiveLayerEnum.FOLDER.getCode().equals(archiveTable1.getArchiveLayer())
                || ArchiveLayerEnum.PRE.getCode().equals(archiveTable1.getArchiveLayer())).findAny().orElse(null);
        if (ObjectUtil.isNotEmpty(archiveTable)) {
            return archiveTable.getStorageLocate();
        }
        return null;
    }
}
