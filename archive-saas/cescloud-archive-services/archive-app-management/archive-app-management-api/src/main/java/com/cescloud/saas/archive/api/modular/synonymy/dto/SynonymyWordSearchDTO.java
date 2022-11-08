package com.cescloud.saas.archive.api.modular.synonymy.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@ApiModel("同义词查询")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
public class SynonymyWordSearchDTO implements Serializable {

    @ApiModelProperty(value = "关键字", required = true)
    private List<String> word;

}
