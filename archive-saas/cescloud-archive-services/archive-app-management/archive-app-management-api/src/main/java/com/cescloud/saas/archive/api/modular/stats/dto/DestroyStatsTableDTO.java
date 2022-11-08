/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.stats.dto</p>
 * <p>文件名:DestroyStatsTableDTO.java</p>
 * <p>创建时间:2020年11月16日 下午3:40:36</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.stats.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月16日
 */
@Api("销毁统计（表格）")
@Data
public class DestroyStatsTableDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 615661404807998741L;

    @ApiModelProperty(name = "年度")
    private String statsTitle;

    @ApiModelProperty(name = "档案门类统计维度集合，key为档案门类编码，value为档案门类统计值")
    private Map<String, Integer> data = new HashMap();

    public void putStatsData(String statsTitle, Integer statsValue) {
        if (data.containsKey(statsTitle)) {
            data.put(statsTitle, data.get(statsTitle) + statsValue);
        } else {
            data.put(statsTitle, statsValue);
        }
    }

}
