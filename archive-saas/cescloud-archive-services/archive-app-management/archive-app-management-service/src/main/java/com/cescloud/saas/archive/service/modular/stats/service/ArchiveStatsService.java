/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service</p>
 * <p>文件名:ArchiveStatsService.java</p>
 * <p>创建时间:2020年11月5日 下午3:35:40</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service;

import com.cescloud.saas.archive.api.modular.mainpage.dto.ToDoDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.*;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveStats;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月5日
 */
public interface ArchiveStatsService extends StatsExecutor {

    /**
     * 馆（室）藏统计（小图汇总统计数据，按档案门类和保管期限）
     *
     * @return
     */
    List<CollectionChartQueryDTO> getCollectionTotalStats(String fondsCodes);

    /**
     * 馆（室）藏统计（大图汇总统计数据，按档案门类和指定字段（保管期限，年度））
     *
     * @param statsField
     *            指定字段
     * @param fondsCodes
     *            全宗
     * @param filingType
     *            整理方式
     * @return
     */
    CollectionChartStatsDTO getCollectionFeildStats(String statsField, String fondsCodes, Integer filingType);

    /**
     * 馆（室）藏统计（表格汇总，按档案门类和保管期限）
     *
     * @param fondsCodes
     *            全宗
     * @param filingType
     *            整理方式
     * @return
     */
    TableStatsDataDTO<CollectionFilingTypeDataDTO> getCollectionTableStats(String fondsCodes, Integer filingType);

	/**
	 * @param fondsCodes 全宗
	 * @param filingType 整理方式
	 * @return
	 */
	TableStatsDataDTO<CollectionFilingTypeDataDTO> getCollectionTableStatsByYearCode(String fondsCodes, Integer filingType);

    /**
     * 销毁统计（图表汇总，按档案门类）
     *
     * @param fondsCodes
     *            全宗
     * @return
     */
    List<DestroyStatsChartDTO> getDestroyChartStats(String fondsCodes);

    /**
     * 销毁统计（表格汇总，按年度）
     *
     * @param fondsCodes
     *            全宗
     * @return
     */
    List<DestroyStatsTableDTO> getDestroyTableStats(String fondsCodes);

    /**
     * 数字化统计（目录表格汇总）
     *
     * @param fondsCodes
     * @param filingType
     * @param yearCode
     * @return
     */
    TableStatsDataDTO<DigitTableFilingTypeDataDTO> getDigitTableFolderStats(String fondsCodes, Integer filingType,
                                                                            String yearCode);

    /**
     * 数字化统计（页数表格汇总）
     *
     * @param fondsCodes
     * @param filingType
     * @param yearCode
     * @return
     */
    TableStatsDataDTO<DigitTableFilingTypeDataDTO> getDigitTablePageStats(String fondsCodes, Integer filingType,
                                                                          String yearCode);

    /**
     * 数字化统计（目录图表汇总）
     *
     * @param fondsCodes
     * @param filingType
     * @param yearCode
     * @param retentionPeriod
     * @return
     */
    List<DigitChartStatsDTO> getDigitChartFolderStats(String fondsCodes, Integer filingType, String yearCode,
                                                      String retentionPeriod);

    /**
     * 数字化统计（页数图表表格汇总）
     *
     * @param fondsCodes
     * @param filingType
     * @param yearCode
     * @param retentionPeriod
     * @return
     */
    List<DigitChartStatsDTO> getDigitChartPageStats(String fondsCodes, Integer filingType, String yearCode,
                                                    String retentionPeriod);

    /**
     * 年报统计
     *
     * @param fondsCodes
     * @param yearCode
     * @return
     */
    Map<String, Integer> getYearStats(String fondsCodes, String yearCode);

    /**
     * 档案窗口统计数量
     *
     * @param fondsCodes
     * @return
     */
    TotalCountStatsDTO getTotalCountStatsData(String fondsCodes);

    /**
     * 根据租户id 清除租户信息
     * @param tenantId
     * @return
     */
    Boolean removeByTenantId(Long tenantId);

	/**
	 * 获取归档数量
	 * @param fondsCodes
	 * @return
	 */
	int getAuditedAmount(String fondsCodes);

	/**
	 * 档案状态为待归档、待接收、待整理、已整理这四种状态的所有档案数量
	 * @param fondsCodes
	 * @return
	 */
	List<ToDoDTO> getStatusAmount(String fondsCodes) throws ArchiveBusinessException, InterruptedException;
}
