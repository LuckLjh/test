/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service</p>
 * <p>文件名:StatsExecutor.java</p>
 * <p>创建时间:2020年9月25日 上午10:15:53</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service;

import com.cescloud.saas.archive.api.modular.stats.dto.ArchiveTypeStatsDTO;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月25日
 */
public interface StatsExecutor {

    /**
     * 执行统计
     */
    void execute();

    /**
     * 执行统计
     * 
     * @param statsDTO
     * @param statsField
     */
    void execute(ArchiveTypeStatsDTO statsDTO, String statsField);

}
