/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:CollectionTotalDTO.java</p>
 * <p>创建时间:2020年11月12日 上午11:35:44</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月12日
 */
@Api("馆（室）藏统计（小图汇总统计数据，按档案门类和保管期限）")
@Data
public class CollectionTotalDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -333986963670077214L;

    @ApiModelProperty(name = "档案类型编码/年度")
    private String showTitle;

    @ApiModelProperty(name = "统计维度集合，第一层为层级（案卷、卷内、一文一件），第二层key为保管期限值（如永久），value为保管期限统计值（如永久的统计值30件或卷）")
    private Map<String, Map<String, Integer>> data = new HashMap<String, Map<String, Integer>>();

    public void putStatsData(String filingType, String statsTitle, Integer statsValue) {
        if (!data.containsKey(filingType)) {
            data.put(filingType, new HashMap<>(8));
        }
        final Map<String, Integer> statsMap = data.get(filingType);
        if (StringUtils.isEmpty(statsTitle)) {
			statsTitle = "none";
		}
        if (statsMap.containsKey(statsTitle)) {
            statsMap.put(statsTitle, statsMap.get(statsTitle) + statsValue);
        } else {
            statsMap.put(statsTitle, statsValue);
        }
    }
}
