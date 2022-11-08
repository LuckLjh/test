/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.controller</p>
 * <p>文件名:AppStatsTimingController.java</p>
 * <p>创建时间:2020年9月28日 下午2:05:37</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.controller;

import cn.hutool.core.date.DateUtil;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveDeckNewService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveStatsService;
import com.cescloud.saas.archive.service.modular.stats.service.FilingStatsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年9月28日
 */
@Api(value = "stats-timing", tags = "统计定时任务", hidden = true)
@RestController
@Slf4j
@RequestMapping("/stats-timing")
@AllArgsConstructor
public class AppStatsTimingController {

    @Autowired
    @Qualifier("archiveStatsService")
    private ArchiveStatsService archiveStatsService;

    @Autowired
    @Qualifier("filingStatsService")
    private FilingStatsService filingStatsService;

	private final ArchiveDeckNewService archiveDeckNewService;
	@ApiOperation(value = "档案统计驾驶舱档案增量统计", hidden = true)
	@PostMapping("/archive-deck-new")
	@Inner
	public R<?> archiveDeckNew() {

		try {
			log.info("档案统计驾驶舱档案增量统计，开始执行--------------目前时间：{}", DateUtil.formatChineseDate(new Date(),false,true));
			archiveDeckNewService.execute();
			log.info("档案统计驾驶舱档案增量统计，执行完成--------------目前时间：{}", DateUtil.formatChineseDate(new Date(),false,true));
		} catch (final Exception e) {
			log.error("档案统计失败：{}", e.getCause(),e);
			return new R<>().fail(false, e.getMessage());
		}

		return new R<>().success(true, null);
	}

    @ApiOperation(value = "档案统计", hidden = true)
    @PostMapping("/archive-data")
    @Inner
    public R<?> archiveData() {

        try {
            archiveStatsService.execute();
        } catch (final Exception e) {
            log.error("档案统计失败：{}", e.getMessage());
            return new R<>().fail(false, e.getMessage());
        }

        return new R<>().success(true, null);
    }

    @ApiOperation(value = "归档数量统计", hidden = true)
    @PostMapping("/filing")
    @Inner
    public R<?> filing() {

        try {
            filingStatsService.execute();
        } catch (final Exception e) {
            log.error("归档数量统计失败：{}", e.getMessage());
            return new R<>().fail(false, e.getMessage());
        }

        return new R<>().success(true, null);
    }
}
