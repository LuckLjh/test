package com.cescloud.saas.archive.service.modular.relationrule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.relationrule.dto.PostArchiveRetentionRelation;
import com.cescloud.saas.archive.api.modular.relationrule.entity.ArchiveRetentionRelation;

import java.util.List;

public interface ArchiveRetentionRelationService extends IService<ArchiveRetentionRelation> {

    List<ArchiveRetentionRelation> getByTypeCode(String typeCode);

    Boolean saveRelation(PostArchiveRetentionRelation postArchiveRetentionRelation);

    void copyByTypeCode(String srcTypeCode, String destTypeCode);

}
