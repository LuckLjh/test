/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.controller</p>
 * <p>文件名:AppStatsController.java</p>
 * <p>创建时间:2020年10月9日 上午9:35:24</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.controller;

import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveStatsService;
import com.cescloud.saas.archive.service.modular.stats.service.FilingStatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author qiucs
 * @version 1.0.0 2020年10月9日
 */
@Api(value = "stats", tags = "统计")
@RestController
@RequestMapping("/stats")
public class AppStatsController {

    @Autowired
    @Qualifier("archiveStatsService")
    private ArchiveStatsService archiveStatsService;

    @Autowired
    @Qualifier("filingStatsService")
    private FilingStatsService filingStatsService;

    @ApiOperation(value = "馆（室）藏统计（小图全部数据汇总）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
            @ApiImplicitParam(name = "filingType", value = "档案类型整理方式：1、以卷整理；2、以件整理；3、单套制", paramType = "int")
    })
    @GetMapping("/collection/total")
    public R<?> collectionTotal(String fondsCodes) {
        return new R<>(archiveStatsService.getCollectionTotalStats(fondsCodes));
    }

    @ApiOperation(value = "馆（室）藏统计（大图按保管期限汇总）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "filingType", value = "档案类型整理方式：1、以卷整理；2、以件整理；3、单套制", paramType = "int")
    })
    @GetMapping("/collection/retention-period")
    public R<?> collectionByRetentionPeriod(String fondsCodes, Integer filingType) {
        return new R<>(
            archiveStatsService.getCollectionFeildStats(FieldConstants.RETENTION_PERIOD, fondsCodes, filingType));
    }

    @ApiOperation(value = "馆（室）藏统计（大图年度汇总）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "filingType", value = "档案类型整理方式：1、以卷整理；2、以件整理；3、单套制", paramType = "int")
    })
    @GetMapping("/collection/year-code")
    public R<?> collectionByYearCode(String fondsCodes, Integer filingType) {
        return new R<>(archiveStatsService.getCollectionFeildStats(FieldConstants.YEAR_CODE, fondsCodes, filingType));
    }

    @ApiOperation(value = "馆（室）藏统计（表格汇总）（按照保管期限）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "filingType", value = "档案类型整理方式：1、以卷整理；2、以件整理；3、单套制", paramType = "int")
    })
    @GetMapping("/collection/table")
    public R<?> collectionTableByRetentionPeriod(String fondsCodes, Integer filingType) {
        return new R<>(archiveStatsService.getCollectionTableStats(fondsCodes, filingType));
    }

	@ApiOperation(value = "馆（室）藏统计（表格汇总）（按照年度）")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
			@ApiImplicitParam(name = "filingType", value = "档案类型整理方式：1、以卷整理；2、以件整理；3、单套制", paramType = "int")
	})
	@GetMapping("/collection/table/year")
	public R<?> collectionTableByYearCode(String fondsCodes, Integer filingType) {
		return new R<>(archiveStatsService.getCollectionTableStatsByYearCode(fondsCodes, filingType));
	}

    @ApiOperation(value = "归档数量统计（按档案门类）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "yearCode", value = "年度", paramType = "string")
    })
    @GetMapping("/filing/archive-type-code")
    public R<?> filingByArchiveTypeCode(String fondsCodes, String yearCode) {
        return new R<>(filingStatsService.getStatsDataGroupByArchiveType(fondsCodes, yearCode));
    }

    @ApiOperation(value = "归档数量统计（按归档部门）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "yearCode", value = "年度", paramType = "string")
    })
    @GetMapping("/filing/filing-dept")
    public R<?> filingByFilingDept(String fondsCodes, String yearCode) {
        return new R<>(filingStatsService.getStatsDataGroupByFilingDept(fondsCodes, yearCode));
    }

    @ApiOperation(value = "数字化统计（表格按页数）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "filingType", value = "整理方式", paramType = "string"),
        @ApiImplicitParam(name = "yearCode", value = "年度", paramType = "string")
    })
    @GetMapping("/digit/table-page")
    public R<?> digitTableByPage(String fondsCodes, Integer filingType, String yearCode) {
        return new R<>(archiveStatsService.getDigitTablePageStats(fondsCodes, filingType, yearCode));
    }

    @ApiOperation(value = "数字化统计（表格按目录）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "filingType", value = "整理方式", paramType = "string"),
        @ApiImplicitParam(name = "yearCode", value = "年度", paramType = "string")
    })
    @GetMapping("/digit/table-folder")
    public R<?> digitTableByFolder(String fondsCodes, Integer filingType, String yearCode) {
        return new R<>(archiveStatsService.getDigitTableFolderStats(fondsCodes, filingType, yearCode));
    }

    @ApiOperation(value = "数字化统计（图表按页数）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "filingType", value = "整理方式", paramType = "string"),
        @ApiImplicitParam(name = "yearCode", value = "年度", paramType = "string"),
        @ApiImplicitParam(name = "retentionPeriod", value = "保管期限", paramType = "string")
    })
    @GetMapping("/digit/chart-page")
    public R<?> digitChartByPage(String fondsCodes, Integer filingType, String yearCode,
        String retentionPeriod) {
        return new R<>(archiveStatsService.getDigitChartPageStats(fondsCodes, filingType, yearCode, retentionPeriod));
    }

    @ApiOperation(value = "数字化统计（图表按目录）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "filingType", value = "整理方式", paramType = "string"),
        @ApiImplicitParam(name = "yearCode", value = "年度", paramType = "string"),
        @ApiImplicitParam(name = "retentionPeriod", value = "保管期限", paramType = "string")
    })
    @GetMapping("/digit/chart-folder")
    public R<?> digitChartByFolder(String fondsCodes, Integer filingType, String yearCode,
        String retentionPeriod) {
        return new R<>(archiveStatsService.getDigitChartFolderStats(fondsCodes, filingType, yearCode, retentionPeriod));
    }

    @ApiOperation(value = "销毁统计（表格）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string")
    })
    @GetMapping("/destroy/table")
    @SysLog("销毁统计（表格）")
    public R<?> destroyTable(String fondsCodes) {
        return new R<>(archiveStatsService.getDestroyTableStats(fondsCodes));
    }

    @ApiOperation(value = "销毁统计（图表）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string")
    })
    @GetMapping("/destroy/chart")
    @SysLog("销毁统计（图表）")
    public R<?> destroyChart(String fondsCodes) {
        return new R<>(archiveStatsService.getDestroyChartStats(fondsCodes));
    }

    @ApiOperation(value = "年报统计（表格）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
        @ApiImplicitParam(name = "yearCode", value = "年度", paramType = "string")
    })
    @GetMapping("/year/table")
    @SysLog("年报统计")
    public R<?> yearTable(String fondsCodes, String yearCode) {
        return new R<>(archiveStatsService.getYearStats(fondsCodes, yearCode));
    }

    @ApiOperation(value = "档案窗口统计数量（不含库房）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string")
    })
    @GetMapping("/total/count")
    public R<?> totalCount(String fondsCodes) {
        return new R<>(archiveStatsService.getTotalCountStatsData(fondsCodes));
    }

	@ApiOperation(value = "档案统计导出")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "fondsCodes", value = "全宗号", paramType = "string"),
			@ApiImplicitParam(name = "yearCode", value = "年度", paramType = "string"),
			@ApiImplicitParam(name = "statsType", value = "统计方式 见 StatsTypeEnum ", paramType = "Integer")
	})
	@GetMapping("/export/{statsType}")
	public void statsExportExcel(@PathVariable("statsType") Integer statsType,HttpServletResponse response, String fondsCodes,Integer filingType, String yearCode) throws ArchiveBusinessException {
		filingStatsService.statsExportExcel(fondsCodes, statsType, yearCode, response,filingType);
	}


}
