/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.mapper</p>
 * <p>文件名:ArchiveStatsMapper.java</p>
 * <p>创建时间:2020年11月5日 下午3:09:46</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.stats.dto.*;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月5日
 */
public interface ArchiveStatsMapper extends BaseMapper<ArchiveStats> {

    /**
     * 馆（室）藏统计（小图汇总统计数据，按档案门类和保管期限）
     *
     * @return
     */
    List<CollectionChartQueryDTO> getCollectionTotalStats(@Param("fondsCodes") List<String> fondsCodes);

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
    List<CollectionChartQueryDTO> getCollectionFeildStats(@Param("statsField") String statsField,
        @Param("fondsCodes") List<String> fondsCodes, @Param("filingType") Integer filingType);

    /**
     * 馆（室）藏统计（表格汇总，按档案门类和保管期限）
     *
     * @param fondsCodes
     *            全宗
     * @param filingType
     *            整理方式
     * @return
     */
    List<ArchiveStats> getCollectionTableStats(@Param("fondsCodes") List<String> fondsCodes,
        @Param("filingType") Integer filingType);

	/**
	 * 馆（室）藏统计（表格汇总，按档案门类和年度）
	 * @param fondsCodeList 全宗
	 * @param filingType 整理方式
	 * @return 统计数据
	 */
	List<ArchiveStats> getCollectionTableStatsByYearCode(@Param("fondsCodes") List<String> fondsCodeList, @Param("filingType") Integer filingType);

    /**
     * 销毁统计（图表汇总，按档案门类）
     *
     * @param fondsCodes
     *            全宗
     * @return
     */
    List<DestroyStatsChartDTO> getDestroyChartStats(@Param("fondsCodes") List<String> fondsCodes);

    /**
     * 销毁统计（表格汇总，按年度）
     *
     * @param fondsCodes
     *            全宗
     * @return
     */
    List<ArchiveStats> getDestroyTableStats(@Param("fondsCodes") List<String> fondsCodes);

    /**
     * 数字化统计（目录表格汇总）
     *
     * @param fondsCodes
     * @param filingType
     * @param yearCode
     * @return
     */
    List<ArchiveStats> getDigitTableFolderStats(@Param("fondsCodes") List<String> fondsCodes,
        @Param("filingType") Integer filingType, @Param("yearCode") String yearCode);

    /**
     * 数字化统计（页数表格汇总）
     *
     * @param fondsCodes
     * @param filingType
     * @param yearCode
     * @return
     */
    List<ArchiveStats> getDigitTablePageStats(@Param("fondsCodes") List<String> fondsCodes,
        @Param("filingType") Integer filingType, @Param("yearCode") String yearCode);

    /**
     * 数字化统计（目录图表汇总）
     *
     * @param fondsCodes
     * @param filingType
     * @param yearCode
     * @param retentionPeriod
     * @return
     */
    List<DigitChartStatsDTO> getDigitChartFolderStats(@Param("fondsCodes") List<String> fondsCodes,
        @Param("filingType") Integer filingType, @Param("yearCode") String yearCode,
        @Param("retentionPeriod") String retentionPeriod);

    /**
     * 数字化统计（页数图表表格汇总）
     *
     * @param fondsCodes
     * @param filingType
     * @param yearCode
     * @param retentionPeriod
     * @return
     */
    List<DigitChartStatsDTO> getDigitChartPageStats(@Param("fondsCodes") List<String> fondsCodes,
        @Param("filingType") Integer filingType, @Param("yearCode") String yearCode,
        @Param("retentionPeriod") String retentionPeriod);

    /**
     * 年报统计（全宗下所有数据）
     *
     * @param fondsCodes
     * @return
     */
    List<YearStatsQueryDTO> getYearStatsTotal(@Param("fondsCodes") List<String> fondsCodes);

    /**
     * 年报统计（全宗下指定年度数据）
     *
     * @param fondsCodes
     * @param yearCode
     * @return
     */
    List<YearStatsQueryDTO> getYearStatsYear(@Param("fondsCodes") List<String> fondsCodes, @Param("yearCode") String yearCode);

    /**
     * 档案统计窗口数量统计
     *
     * @param fondsCodes
     * @return
     */
    TotalCountStatsDTO getTotalCountStats(@Param("fondsCodes") List<String> fondsCodes);

    /**
     * 根据租户id 清除租户信息
     * @param tenantId
     * @return
     */
    Boolean removeByTenantId(@Param("tenantId") Long tenantId);


}
