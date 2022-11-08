/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service</p>
 * <p>文件名:StatsExecutorService.java</p>
 * <p>创建时间:2020年9月27日 上午11:21:36</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service;

import java.util.List;
import java.util.Map;

import com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月27日
 */
public interface StatsExecutorService {

    /**
     * 获取统计数据
     *
     * @param statsTaskDTO
     * @return
     */
    List<Map<String, Object>> getStatsData(StatsTaskDTO statsTaskDTO);

    /**
     * 保存统计数据
     *
     * @param statsTaskDTO
     * @param statsData
     * @return
     */
    boolean saveStatsData(StatsTaskDTO statsTaskDTO, List<Map<String, Object>> statsData);

}
