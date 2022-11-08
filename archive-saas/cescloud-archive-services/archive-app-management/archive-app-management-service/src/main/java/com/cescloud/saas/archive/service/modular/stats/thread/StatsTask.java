/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.thread</p>
 * <p>文件名:StatsTask.java</p>
 * <p>创建时间:2020年9月25日 上午11:56:58</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.thread;

import java.util.List;
import java.util.Map;

import com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO;
import com.cescloud.saas.archive.service.modular.stats.service.StatsExecutorService;

import lombok.extern.slf4j.Slf4j;

/**
 * 执行统计接口
 *
 * @author qiucs
 * @version 1.0.0 2020年9月25日
 */
@Slf4j
public class StatsTask implements Runnable {

    private final StatsTaskDTO statsTaskDTO;

    private final StatsExecutorService statsExecutorService;

    public StatsTask(StatsTaskDTO statsTaskDTO, StatsExecutorService statsExecutorService) {
        this.statsTaskDTO = statsTaskDTO;
        this.statsExecutorService = statsExecutorService;
    }

    /**
     *
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public void run() {
        try {
            final List<Map<String, Object>> statsData = statsExecutorService.getStatsData(statsTaskDTO);
            if (null != statsData && !statsData.isEmpty()) {
                statsExecutorService.saveStatsData(statsTaskDTO, statsData);
            }
        } catch (final Exception e) {
            log.error("执行统计任务出错：{}", e.getMessage());
        }
    }

}
