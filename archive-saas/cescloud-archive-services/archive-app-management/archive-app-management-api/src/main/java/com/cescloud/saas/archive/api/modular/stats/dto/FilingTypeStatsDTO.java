/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:FilingTypeStatsDTO.java</p>
 * <p>创建时间:2020年9月25日 上午9:44:14</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月25日
 */
@ApiModel("按整理方式统计")
@Data
public class FilingTypeStatsDTO implements Serializable {

    /**
    *
    */
    private static final long serialVersionUID = -3825926042248611615L;

    @ApiModelProperty("档案门类编码")
    private String archiveTypeCode;

	@ApiModelProperty("档案门类名称")
	private String archiveTypeName;

    @ApiModelProperty("档案门类分类标识")
    private String classType;

    @ApiModelProperty("档案门类整理方式")
    private String archiveTypeFilingType;

    @ApiModelProperty("统计档案门类表")
    private String storageLocate;

    @ApiModelProperty("档案门类表对应的父表：storageLocate是卷内，parentStorageLocate为案卷")
    private String parentStorageLocate;

}
