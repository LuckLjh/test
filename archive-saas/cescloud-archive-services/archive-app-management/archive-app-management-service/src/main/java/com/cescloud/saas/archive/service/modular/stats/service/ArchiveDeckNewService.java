
package com.cescloud.saas.archive.service.modular.stats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.stats.dto.DeckNewDataDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.DeckNewTotalDataDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.DeckStatDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.DeckTotalStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveDeckNew;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;


/**
 * 驾驶舱新增量
 *
 * @author 刘冬1
 * @date 2021-04-29 17:39:59
 */
public interface ArchiveDeckNewService extends IService<ArchiveDeckNew>, StatsExecutor {

	/**
	 * 统计新增数量，根据传递的当前日期 判断出（ state:100 本年 10 本月 1本周 )
	 * @param archiveDeckNew
	 * @return
	 */
	DeckTotalStatsDTO<DeckNewTotalDataDTO, List<DeckNewDataDTO>> deckNewStat(ArchiveDeckNew archiveDeckNew) throws ArchiveBusinessException;

	/**
	 * 档案窗口统计数量
	 *
	 * @param fondsCodes
	 * @return
	 */
	DeckStatDTO getTotalCountStatsData(String fondsCodes);
}
