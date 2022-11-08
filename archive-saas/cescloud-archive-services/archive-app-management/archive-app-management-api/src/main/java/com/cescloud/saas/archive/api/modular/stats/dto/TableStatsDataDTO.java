/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:TableStatsDataDTO.java</p>
 * <p>创建时间:2020年11月19日 下午3:35:11</p>
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
@Api("表格统计数据集")
@Data
public class TableStatsDataDTO<FilingTypeDataDTO> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4900324361442171933L;

    @ApiModelProperty(name = "数据集合")
    private List<StatsDataDTO<FilingTypeDataDTO>> data;

    @ApiModelProperty(name = "统计维度集合中所有key集合（按卷整理）")
    private Set<String> folderKeys = new HashSet<String>();

    @ApiModelProperty(name = "统计维度集合中所有key集合（按件整理）")
    private Set<String> fileKeys = new HashSet<String>();

}
