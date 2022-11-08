package com.cescloud.saas.archive.service.modular.archiveconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleUndefinedDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataBoxRule;

import java.util.List;
import java.util.Map;

public interface MetadataBoxRuleService extends IService<MetadataBoxRule> {

    List<MetadataBoxRuleUndefinedDTO> listOfUnDefined(String storageLocate, Long configId);

    void copyConfig(Map<Long,Long> srcDestMetadataMap, Map<Long,Long> srcDestConfigIdMap);

}
