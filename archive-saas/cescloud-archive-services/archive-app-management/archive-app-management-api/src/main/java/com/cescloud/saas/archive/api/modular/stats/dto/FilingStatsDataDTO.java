/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:FilingStatsDataDTO.java</p>
 * <p>创建时间:2020年11月12日 下午4:51:32</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月12日
 */
@Data
@Builder
public class FilingStatsDataDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7685805156103260849L;

    @ApiModelProperty(name = "档案门类或归档部门")
    private String statsTitle;

    @ApiModelProperty(name = "数量")
    private Integer statsAmount;

}
