package com.cescloud.saas.archive.service.modular.archiveconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleUndefinedDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.MetadataBoxRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MetadataBoxRuleMapper extends BaseMapper<MetadataBoxRule> {

    List<MetadataBoxRuleUndefinedDTO> listOfUnDefined(@Param("storageLocate") String storageLocate, @Param("configId") Long configId);
}
