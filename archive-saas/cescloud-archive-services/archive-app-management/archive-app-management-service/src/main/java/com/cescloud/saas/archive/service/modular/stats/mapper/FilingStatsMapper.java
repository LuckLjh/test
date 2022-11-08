/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.mapper</p>
 * <p>文件名:FilingStatsMapper.java</p>
 * <p>创建时间:2020年10月14日 下午2:32:56</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.stats.dto.FilingStatsDataDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.FilingStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月14日
 */
public interface FilingStatsMapper extends BaseMapper<FilingStats> {

    /**
     * 获取统计结果
     *
     * @param statsField
     *            统计维度字段：档案门类、归档部门（使用FieldConstants常量）
     * @param fondsCodes
     *            全宗号，如果为null，则表示统计所有全宗
     * @param yearCode
     *            年度
     * @return
     */
    List<FilingStatsDataDTO> getStatsData(@Param("statsField") String statsField, @Param("fondsCodes") List<String> fondsCodes,
        @Param("yearCode") String yearCode);

    /**
     * 根据租户id 清除租户信息
     * @param tenantId
     * @return
     */
    Boolean removeByTenantId(@Param("tenantId") Long tenantId);
}
