package com.cescloud.saas.archive.api.modular.report.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xaz
 * @date 2019/8/10 - 14:17
 **/

@Data
@ApiModel(value = "报表查询对象")
public class ReportQueryDTO {

    /**
     * 报表名称 false
     */
    @ApiModelProperty(value = "报表名称",required = false,example = "全引目录")
    private String reportTopic;

    /**
     * 报表所属档案数据表名  false
     */
    @ApiModelProperty(value = "报表所属档案数据表名",required = false,example = "T_1_WS_WSDA_V")
    private String storageLocate;

}
