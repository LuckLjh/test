/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.convert</p>
 * <p>文件名:FilingStatsConverter.java</p>
 * <p>创建时间:2020年10月14日 下午3:31:11</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.convert;

import com.cescloud.saas.archive.api.modular.stats.constant.StatsConstants;
import com.cescloud.saas.archive.api.modular.stats.dto.FilingTypeStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.FilingStats;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月14日
 */
@Component("filingStatsConverter")
public class FilingStatsConverter extends StatsEntityConverter<FilingStats> {

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.convert.StatsEntityConverter#convert(com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO,
     *      java.util.Map)
     */
    @Override
    public FilingStats convert(StatsTaskDTO statsTaskDTO, Map<String, Object> data) {
        final FilingStats entity = new FilingStats();
        final FilingTypeStatsDTO filingTypeStatsDTO = statsTaskDTO.getFilingTypeStatsDTO();
        entity.setArchiveTypeCode(filingTypeStatsDTO.getArchiveTypeCode());
		entity.setArchiveTypeName(filingTypeStatsDTO.getArchiveTypeName());
        entity.setTenantId(statsTaskDTO.getTenantId());
        entity.setUpdatedTime(LocalDateTime.now());
        entity.setFilingDeptId(longZeroIfNull(data.get(StatsConstants.STATS_TITLE)));
        entity.setStatsAmount(zeroIfNull(data.get(StatsConstants.STATS_AMOUNT)));
        entity.setFondsCode(noneIfNull(data.get(FieldConstants.FONDS_CODE)));
        entity.setYearCode(noneIfNull(data.get(FieldConstants.YEAR_CODE)));
		entity.setRetentionPeriod(noneIfNull(data.get(FieldConstants.RETENTION_PERIOD)));
        return entity;
    }

}
