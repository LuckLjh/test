/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.convert</p>
 * <p>文件名:ArchiveStatsConverter.java</p>
 * <p>创建时间:2020年11月5日 下午3:57:55</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.convert;

import com.cescloud.saas.archive.api.modular.stats.constant.StatsConstants;
import com.cescloud.saas.archive.api.modular.stats.dto.FilingTypeStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveStats;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月5日
 */
@Component("archiveStatsConverter")
public class ArchiveStatsConverter extends StatsEntityConverter<ArchiveStats> {

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.convert.StatsEntityConverter#convert(com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO,
     *      java.util.Map)
     */
    @Override
    public ArchiveStats convert(StatsTaskDTO statsTaskDTO, Map<String, Object> data) {
        final ArchiveStats entity = new ArchiveStats();
        final FilingTypeStatsDTO filingTypeStatsDTO = statsTaskDTO.getFilingTypeStatsDTO();
        entity.setArchiveClassType(filingTypeStatsDTO.getClassType());
        entity.setArchiveTypeCode(filingTypeStatsDTO.getArchiveTypeCode());
		entity.setArchiveTypeName(filingTypeStatsDTO.getArchiveTypeName());
        entity.setFilingType(statsTaskDTO.getStatsFilingTypeEnum().getCode());

        entity.setStatus(zeroIfNull(data.get(FieldConstants.STATUS)));
        entity.setFondsCode(noneIfNull(data.get(FieldConstants.FONDS_CODE)));
        entity.setYearCode(noneIfNull(data.get(FieldConstants.YEAR_CODE)));
        entity.setRetentionPeriod(noneIfNull(data.get(FieldConstants.RETENTION_PERIOD)));

        entity.setStatsAmount(zeroIfNull(data.get(StatsConstants.STATS_AMOUNT)));
        entity.setFileAmount(zeroIfNull(data.get(StatsConstants.FILE_AMOUNT)));
        entity.setPageAmount(zeroIfNull(data.get(StatsConstants.PAGE_AMOUNT)));
        entity.setDigitedPageAmount(longZeroIfNull(data.get(StatsConstants.DIGITED_PAGE_AMOUNT)));
        entity.setDigitedAmount(longZeroIfNull(data.get(StatsConstants.DIGITED_AMOUNT)));

        entity.setTenantId(statsTaskDTO.getTenantId());
        entity.setUpdatedTime(LocalDateTime.now());
        return entity;
    }

}
