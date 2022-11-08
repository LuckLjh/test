package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.DispAppraisalRule;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;

public interface DispAppraisalRuleService extends IService<DispAppraisalRule> {

    Boolean saveDispAppraisalRule(DispAppraisalRule dispAppraisalRule);

    DispAppraisalRule getByStorageLocate(String storageLocate);

    List<DispAppraisalRule> getAll();

    Boolean deleteDispAppraisalRule(DispAppraisalRule dispAppraisalRule);

    void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap);

    Boolean checkDispAppraisalRule(String typeCode, Integer appraisalType) throws ArchiveBusinessException;

}
