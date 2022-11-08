/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.controller</p>
 * <p>文件名:YearStatsController.java</p>
 * <p>创建时间:2020年10月23日 下午2:01:29</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.controller;

import cn.hutool.core.io.IoUtil;
import com.cescloud.saas.archive.api.modular.stats.dto.YearStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.YearStats;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.stats.service.YearStatsService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月23日
 */
@Api(value = "stats", tags = "年报统计")
@RestController
@RequestMapping(value = { "/year-stats", "/busi-stats" })
@Slf4j
public class YearStatsController {

    @Autowired
    private YearStatsService yearStatsService;

    @ApiOperation(value = "通过id查询档案树定义")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "树节点ID", required = true, dataType = "int", paramType = "query"),
    })
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") Long id) {
        return new R<>(yearStatsService.toDTO(yearStatsService.getById(id)));
    }

    @ApiOperation(value = "新增年报统计")
    @SysLog("新增年报统计")
    @PostMapping
    public R save(
        @RequestBody @ApiParam(name = "entityDTO", value = "年报统计对象", required = true) YearStatsDTO entityDTO) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增年报统计-统计年度【%s】-单位名称【%s】",entityDTO.getYearCode(),entityDTO.getUnitName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(yearStatsService.save(entityDTO));
    }

    @ApiOperation(value = "修改年报统计")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "entityDTO", value = "年报统计对象", required = true, dataType = "YearStatsDTO")
    })
    @SysLog("修改年报统计")
    @PutMapping
    public R updateById(
        @RequestBody @ApiParam(name = "entityDTO", value = "年报统计对象", required = true) YearStatsDTO entityDTO) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改年报统计-统计年度【%s】-单位名称【%s】",entityDTO.getYearCode(),entityDTO.getUnitName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(yearStatsService.updateById(entityDTO));
    }

    @ApiOperation(value = "删除年报统计")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "entityDTO", value = "年报统计对象", required = true, dataType = "YearStatsDTO")
    })
    @SysLog("删除年报统计")
    @DeleteMapping("/{id}")
    public R deleteById(@ApiParam(name = "id", value = "年报ID", required = true) @PathVariable("id") Long id) {
		try {
			YearStats byId = yearStatsService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除年报统计-统计年度【%s】-单位名称【%s】",byId.getYearCode(),byId.getUnitName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(yearStatsService.removeById(id));
    }

    @ApiOperation(value = "获取年度下拉框")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "fondsCode", value = "全宗号", required = true, dataType = "string")
    })
    @GetMapping("/combo")
    public R getYearCombo(String fondsCode) {
        return new R<>(yearStatsService.getYearCodeByFondsCode(fondsCode));
    }

    @ApiOperation(value = "年报导出")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "long"),
    })
    @GetMapping("/export/{id}")
    @SysLog("年报导出")
    public void export(HttpServletResponse response, @PathVariable("id") Long id) {
        ServletOutputStream outputStream = null;
        try (HSSFWorkbook workbook = yearStatsService.export(id)) {
            outputStream = response.getOutputStream();
            final String encodeName = URLEncoder.encode("年度统计_" + workbook.getSheetName(0),
                StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodeName + ".xls");
            workbook.write(outputStream);
        } catch (final IOException e) {
            log.error("年报导出出错：{}", e.getMessage());
        } finally {
            IoUtil.close(outputStream);
        }
    }

	@GetMapping("/year-stats-count/{fondsCode}/{yearStatsId}")
	@SysLog("年报统计")
	public R yearStatsCount(@PathVariable("fondsCode") String fondsCode,@PathVariable("yearStatsId") Long yearStatsId) {
		try {
			YearStats byId = yearStatsService.getById(yearStatsId);
			SysLogContextHolder.setLogTitle(String.format("年报统计-统计年度【%s】-单位名称【%s】",byId.getYearCode(),byId.getUnitName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return yearStatsService.yearStatsCount(fondsCode,yearStatsId);
	}

}
