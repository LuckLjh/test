/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.convert</p>
 * <p>文件名:StatsEntityConverter.java</p>
 * <p>创建时间:2020年9月25日 下午1:17:13</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.convert;

import java.util.Map;

import com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月25日
 */
public abstract class StatsEntityConverter<Entity> {

    /**
     * 转化为统计对象
     *
     * @param statsTaskDTO
     * @param data
     * @return
     */
    public abstract Entity convert(StatsTaskDTO statsTaskDTO, Map<String, Object> data);

    public String noneIfNull(Object data) {
        if (null == data) {
            return "none";
        }
        return data.toString();
    }

    public Integer zeroIfNull(Object data) {
        if (null == data) {
            return 0;
        }
        return Integer.parseInt(data.toString());
    }

    public Long longZeroIfNull(Object data) {
        if (null == data) {
            return 0L;
        }
        return Long.parseLong(data.toString());
    }
}
