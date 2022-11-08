package com.cescloud.saas.archive.api.modular.relationrule.dto;

import com.cescloud.saas.archive.api.modular.relationrule.entity.ArchiveRetentionRelation;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostArchiveRetentionRelation implements Serializable {

    @ApiModelProperty(value = "档案类型", required = true)
    private String archiveTypeCode;

    @ApiModelProperty(value = "关联关系集", required = true)
    private List<ArchiveRetentionRelation> archiveRetentionRelations;
}
