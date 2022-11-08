/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:DigitChartStatsDTO.java</p>
 * <p>创建时间:2020年11月19日 上午9:39:03</p>
 * <p>作者: qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月19日
 */
@Api("数字化统计（图表）")
@Data
public class DigitChartStatsDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8699048386895951267L;

    @ApiModelProperty(name = "档案门类编码")
    private String statsTitle;

    @ApiModelProperty(name = "应数字化数量")
    private Integer totalAmount;

    @ApiModelProperty(name = "已数字化数量")
    private Integer digitedAmount;

}
