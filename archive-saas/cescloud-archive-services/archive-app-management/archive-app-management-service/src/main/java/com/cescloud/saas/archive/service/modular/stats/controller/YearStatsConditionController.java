
package com.cescloud.saas.archive.service.modular.stats.controller;

import com.cescloud.saas.archive.api.modular.stats.dto.YearStatsConditionSaveDTO;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.stats.service.YearStatsConditionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/*
 * Description: 年报统计条件
 * @auther: qianbaocheng
 * @date: 2021-6-29 10:23
 */
@Api(value = "YearStatsCondition", description = "年报统计条件")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/year-stats-condition")
public class YearStatsConditionController {

	private final YearStatsConditionService yearStatsConditionService;

	/**
	 * 保存年报条件
	 *
	 * @param yearStatsConditionSaveDTO 年报条件DTO
	 * @return R
	 */
	@ApiOperation(value = "保存年报条件")
	@SysLog("保存年报条件")
	@PostMapping
	public R save(@RequestBody @Valid YearStatsConditionSaveDTO yearStatsConditionSaveDTO) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("保存年报条件-档案门类名称【%s】-条件名称【%s】",yearStatsConditionSaveDTO.getArchiveTypeName(),yearStatsConditionSaveDTO.getChineseCondition()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(yearStatsConditionService.saveYearStatsCondition(yearStatsConditionSaveDTO));
	}

	/**
	 * 通过行数删除年报条件
	 *
	 * @param yearStatsConditionSaveDTO 年报条件DTO
	 * @return R
	 */
	@ApiOperation(value = "清除对应行数的条件设置")
	@SysLog("删除年报条件")
	@DeleteMapping("/delete")
	public R removeById(@RequestBody @Valid YearStatsConditionSaveDTO yearStatsConditionSaveDTO) throws ArchiveBusinessException{
		try {
			SysLogContextHolder.setLogTitle(String.format("删除年报条件-档案门类名称【%s】-条件名称【%s】",yearStatsConditionSaveDTO.getArchiveTypeName(),yearStatsConditionSaveDTO.getChineseCondition()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(yearStatsConditionService.removeByNumberLine(yearStatsConditionSaveDTO));
	}

	/**
	 * 查询行数的具体条件设置
	 *
	 * @param yearStatsId 年报统计表id numberLine行数
	 * @return R
	 */
	@ApiOperation(value = "查询行数的具体条件设置")
	@GetMapping("/search/{yearStatsId}/{numberLine}/{storageLocate}")
	public R getCondition(@PathVariable  Long yearStatsId,@PathVariable  Integer numberLine,@PathVariable String storageLocate) throws ArchiveBusinessException{
		return new R<>(yearStatsConditionService.getCondition(yearStatsId,numberLine,storageLocate));
	}

	/**
	 * 设置对应关系
	 *
	 * @param yearStatsConditionSaveDTO 年报条件DTO
	 * @return R
	 */
	@ApiOperation(value = "设置对应关系")
	@SysLog("设置对应关系")
	@PostMapping("/correspondence")
	public R correspondence(@RequestBody @Valid List<YearStatsConditionSaveDTO> yearStatsConditionSaveDTO) throws ArchiveBusinessException{
		return new R<>(yearStatsConditionService.correspondence(yearStatsConditionSaveDTO));
	}

	/**
	 * 获取年报统计报表对应关系
	 *
	 * @param yearStatsId 年报表id
	 * @return R
	 */
	@GetMapping("/year-condition-map/{yearStatsId}")
	@SysLog("获取年报统计报表对应关系")
	public R<?> yearConditionTable(@PathVariable  Long yearStatsId) {
		return new R<>(yearStatsConditionService.getYearStatsConditionMap(yearStatsId));
	}

	/**
	 * 获取年报统计报表行对应的档案类型条件
	 *
	 * @param yearStatsId 年报表id numberLine 年报行数
	 * @return R
	 */
	@GetMapping("/year-condition-list/{yearStatsId}/{numberLine}")
	@SysLog("获取年报统计报表行对应的档案类型条件")
	public R<?> yearConditionTable(@PathVariable  Long yearStatsId,@PathVariable  Integer numberLine) {
		return new R<>(yearStatsConditionService.getArchiveTypeConditionList(yearStatsId , numberLine));
	}

}
