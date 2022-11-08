/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:CollectionChartQueryDTO.java</p>
 * <p>创建时间:2020年11月12日 下午3:00:22</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月12日
 */
@Data
public class CollectionChartQueryDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7274756376224363196L;

    @ApiModelProperty(name = "档案类型编码")
    private String archiveTypeCode;

	@ApiModelProperty(name = "档案类型名称")
	private String archiveTypeName;

    @ApiModelProperty(name = "档案类型整理方式：1、以卷整理；2、以件整理；3、单套制")
    private Integer filingType;

    @ApiModelProperty(name = "统计标题")
    private String statsTitle;

    @ApiModelProperty(name = "统计数量")
    private Integer statsAmount;

    @ApiModelProperty(name = "卷内文件数量")
    private Integer fileAmount;

}
