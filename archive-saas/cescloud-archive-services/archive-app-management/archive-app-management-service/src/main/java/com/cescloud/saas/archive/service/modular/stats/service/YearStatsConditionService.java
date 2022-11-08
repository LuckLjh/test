
package com.cescloud.saas.archive.service.modular.stats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.stats.dto.YearStatsConditionSaveDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.YearStatsCondition;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;


/*
 * Description: 年报统计条件
 * @auther: qianbaocheng
 * @date: 2021-6-29 13:47
 */
public interface YearStatsConditionService extends IService<YearStatsCondition> {

	YearStatsCondition saveYearStatsCondition(YearStatsConditionSaveDTO yearStatsConditionSaveDTO) throws ArchiveBusinessException;

	Boolean removeByNumberLine(YearStatsConditionSaveDTO yearStatsConditionSaveDTO) throws ArchiveBusinessException;

	YearStatsCondition getCondition(  Long yearStatsId, Integer numberLine, String storageLocate) throws ArchiveBusinessException;

	String correspondence(List<YearStatsConditionSaveDTO> yearStatsConditionSaveDTO) throws ArchiveBusinessException;

	Map<String,Object> getYearStatsConditionMap(Long yearStatsId);

	List<YearStatsConditionSaveDTO> getArchiveTypeConditionList(Long yearStatsId , Integer numberLine);

	Map<String,List<YearStatsCondition>> getYearStatsConditionMapByYearStatsId(String fondsCode,Long yearStatsId);


}
