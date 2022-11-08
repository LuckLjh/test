/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:DestroyStatsChartDTO.java</p>
 * <p>创建时间:2020年11月16日 下午3:36:37</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月16日
 */
@Data
public class DestroyStatsChartDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3367646543998933006L;

    @ApiModelProperty(name = "档案门类编码")
    private String statsTitle;

    @ApiModelProperty(name = "数量")
    private Integer statsAmount;

}
