/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:CollectionChartStatsDTO.java</p>
 * <p>创建时间:2020年11月19日 下午3:17:57</p>
 * <p>作者: qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月19日
 */
@Api("馆（室）藏统计（小图汇总统计数据，按档案门类和保管期限）")
@Data
public class CollectionChartStatsDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3179052672800378122L;

    @ApiModelProperty("数据集合")
    private List<CollectionTotalDTO> data;

    @ApiModelProperty("保管期限集合")
    private Set<String> keys = new HashSet<String>();

}
