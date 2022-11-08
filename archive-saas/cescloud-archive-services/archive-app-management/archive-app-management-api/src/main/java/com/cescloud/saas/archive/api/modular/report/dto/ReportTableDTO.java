package com.cescloud.saas.archive.api.modular.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
@author  xaz
@date 2019/5/7 - 10:24
**/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportTableDTO {

    /**
     * 报表关联档案数据表中文名  false
     */
    @ApiModelProperty(value = "报表关联档案数据表中文名",required = true)
    private String storageLocateName;

    /**
     * 报表关联档案数据表名  false
     */
    @ApiModelProperty(value = "报表关联档案数据表名",required = true)
    private String storageLocate;

}
