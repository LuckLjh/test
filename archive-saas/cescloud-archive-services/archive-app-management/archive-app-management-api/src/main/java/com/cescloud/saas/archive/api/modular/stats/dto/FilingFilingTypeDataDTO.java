/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:FilingFilingTypeDataDTO.java</p>
 * <p>创建时间:2020年10月14日 下午2:52:27</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月14日
 */
public class FilingFilingTypeDataDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1770531405649115511L;

    @ApiModelProperty(name = "保管期限统计维度集合，key为保管期限值（如永久），value为保管期限统计值（如永久的统计值30件或卷）")
    private final Map<String, Integer> data = new HashMap<String, Integer>();

    @ApiModelProperty(name = "总归档数")
    private Integer totalFiling = null;

    public void addFilingAmount(Integer filingAmount) {
        if (null == this.totalFiling) {
            this.totalFiling = filingAmount;
        } else {
            this.totalFiling += filingAmount;
        }
    }

    public void putStatsData(String statsTitle, Integer statsValue) {
        if (data.containsKey(statsTitle)) {
            data.put(statsTitle, data.get(statsTitle) + statsValue);
        } else {
            data.put(statsTitle, statsValue);
        }
    }
}