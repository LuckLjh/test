package com.cescloud.saas.archive.api.modular.archiveconfig.dto;

import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName MetadataBoxRuleDTO
 * @Author zhangxuehu
 * @Date 2020/8/3 15:35
 **/
@ApiModel("档案装盒规则元数据信息实体")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataBoxRuleDTO {

    @ApiModelProperty("起止顺序号字段信息")
    private Metadata metadataInfo;

    @ApiModelProperty("分组设置字段信息")
    private List<Metadata> metadatas;
}
