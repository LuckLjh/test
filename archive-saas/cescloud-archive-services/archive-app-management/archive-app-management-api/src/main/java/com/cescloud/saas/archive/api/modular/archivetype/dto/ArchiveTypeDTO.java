package com.cescloud.saas.archive.api.modular.archivetype.dto;

import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ApiModel("档案门类实体")
public class ArchiveTypeDTO extends ArchiveType {

    @ApiModelProperty("档案门类最高层的模板id")
    private Long templateTableId;
}
