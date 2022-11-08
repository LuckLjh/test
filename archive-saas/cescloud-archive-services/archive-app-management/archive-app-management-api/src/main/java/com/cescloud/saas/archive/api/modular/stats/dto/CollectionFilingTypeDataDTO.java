/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:CollectionsFilingTypeDataDTO.java</p>
 * <p>创建时间:2020年10月14日 下午2:06:08</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月14日
 */
@Data
public class CollectionFilingTypeDataDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1770531405649115511L;

    @ApiModelProperty(name = "统计维度集合，如保管期限，则key为保管期限值（如永久），value为保管期限统计值（如永久的统计值30件或卷）")
    private Map<String, Integer> data = new HashMap<String, Integer>();

    @ApiModelProperty(name = "总页数/总时长")
    private Integer totalPage = null;

    @ApiModelProperty(name = "卷内文件总数")
    private Integer totalFile = null;

    @ApiModelProperty(name = "总数量")
    private Integer totalStatsAmount = new Integer(0);

    public void addPageAmount(Integer pageAmount) {
        if (null == this.totalPage) {
            this.totalPage = pageAmount;
        } else {
            this.totalPage += pageAmount;
        }
    }

    public void addFileAmount(Integer fileAmount) {
        if (null == this.totalFile) {
            this.totalFile = fileAmount;
        } else {
            this.totalFile += fileAmount;
        }
    }

    public void putStatsData(String statsTitle, Integer statsValue) {
        if (data.containsKey(statsTitle)) {
            data.put(statsTitle, data.get(statsTitle) + statsValue);
        } else {
            data.put(statsTitle, statsValue);
        }

        totalStatsAmount += statsValue;
    }

}
