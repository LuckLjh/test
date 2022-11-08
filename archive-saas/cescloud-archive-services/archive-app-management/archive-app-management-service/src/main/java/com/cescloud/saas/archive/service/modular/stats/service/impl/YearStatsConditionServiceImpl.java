
package com.cescloud.saas.archive.service.modular.stats.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.authority.dto.DataConditionDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.YearStatsConditionSaveDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.YearStatsCondition;
import com.cescloud.saas.archive.common.search.CriteriaCondition;
import com.cescloud.saas.archive.service.modular.stats.mapper.YearStatsConditionMapper;
import com.cescloud.saas.archive.service.modular.stats.service.YearStatsConditionService;
import com.cescloud.saas.archive.service.modular.stats.util.CriteriaConditionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/*
 * Description: 年报统计条件
 * @auther: qianbaocheng
 * @date: 2021-6-29 11:08
 */
@Service
@Slf4j
public class YearStatsConditionServiceImpl extends ServiceImpl<YearStatsConditionMapper, YearStatsCondition> implements YearStatsConditionService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public YearStatsCondition saveYearStatsCondition(YearStatsConditionSaveDTO yearStatsConditionSaveDTO) {
		YearStatsCondition yearStatsCondition = new YearStatsCondition();
		BeanUtils.copyProperties(yearStatsConditionSaveDTO, yearStatsCondition);
		if(ObjectUtil.isNotEmpty(yearStatsConditionSaveDTO.getDataConditionDTOList())){
			CriteriaCondition backCondition = CriteriaConditionUtil.toCriteriaCondition(yearStatsConditionSaveDTO.getDataConditionDTOList());
			yearStatsCondition.setBackCondition(ObjectUtil.serialize(backCondition));
			yearStatsCondition.setPageCondition(ObjectUtil.serialize(convertPageCondition(yearStatsConditionSaveDTO.getDataConditionDTOList())));
		}
		this.saveOrUpdate(yearStatsCondition);
		return yearStatsCondition;
	}
    /**
     * Description: 转换成前台显示条件
     * @param dataConditionDTOList  false
     * @return: List<DataConditionDTO>
     * @auther: qianbaocheng
     * @date: 2021-7-1 15:38
     */
	private List<DataConditionDTO> convertPageCondition(List<DataConditionDTO> dataConditionDTOList) {
		if (CollectionUtil.isEmpty(dataConditionDTOList)) {
			return null;
		}
		List<DataConditionDTO> dataConditionList = new LinkedList<>(dataConditionDTOList);
		dataConditionList.stream().forEach(dataConditionDTO -> {
			if (CollectionUtil.isNotEmpty(dataConditionDTO.getChildren())) {
				dataConditionDTO.setChildren(convertPageCondition(dataConditionDTO.getChildren()));
			}
		});
		return dataConditionList;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeByNumberLine(YearStatsConditionSaveDTO yearStatsConditionSaveDTO){
			return this.remove(Wrappers.<YearStatsCondition>lambdaQuery()
					.eq(YearStatsCondition::getNumberLine, yearStatsConditionSaveDTO.getNumberLine())
					.eq(YearStatsCondition::getFondsCode, yearStatsConditionSaveDTO.getFondsCode())
					.eq(YearStatsCondition::getYearStatsId,yearStatsConditionSaveDTO.getYearStatsId()));

	}

	@Override
	public YearStatsCondition getCondition(Long yearStatsId , Integer numberLine , String storageLocate) {
		Assert.notNull(yearStatsId, "缺少年报统计表id");
		Assert.notNull(numberLine, "缺少年报统计表对应行数");
		Assert.notNull(storageLocate, "缺少年报统计表对应表名");
		YearStatsCondition yearStatsCondition = this.getOne(Wrappers.<YearStatsCondition>lambdaQuery()
				.eq(YearStatsCondition::getNumberLine,numberLine)
				.eq(YearStatsCondition::getYearStatsId,yearStatsId)
		        .eq(YearStatsCondition::getStorageLocate ,storageLocate));
		return Optional.ofNullable(yearStatsCondition).map(condition -> {
			condition.setPageCondition(CriteriaConditionUtil.unserialize(condition.getPageCondition()));
			condition.setBackCondition(CriteriaConditionUtil.unserialize(condition.getBackCondition()));
			return condition;
		}).orElse(null);
	}


	@Override
	public String correspondence(List<YearStatsConditionSaveDTO> yearStatsConditionSaveDTOList) {
		Assert.notEmpty(yearStatsConditionSaveDTOList,"条件设置不能为空!");
		if(ObjectUtil.isNotEmpty(yearStatsConditionSaveDTOList)){
			removeByNumberLine(yearStatsConditionSaveDTOList.get(0));
		}
		yearStatsConditionSaveDTOList.stream().forEach(yearStatsConditionSaveDTO -> {
			saveYearStatsCondition(yearStatsConditionSaveDTO);
		});
		return ObjectUtil.isNotEmpty(yearStatsConditionSaveDTOList)&&yearStatsConditionSaveDTOList.get(0)!=null ?
				getArchiveListStr(yearStatsConditionSaveDTOList.get(0)) : StrUtil.EMPTY;
	}

	@Override
	public Map<String, Object> getYearStatsConditionMap(Long yearStatsId) {
		Assert.notNull(yearStatsId, "缺少年报统计表id");
		List<YearStatsCondition> yearStatsConditionList = this.list(Wrappers.<YearStatsCondition>lambdaQuery().eq(YearStatsCondition::getYearStatsId, yearStatsId));;
		return yearStatsConditionList.stream().collect(Collectors.toMap(yearStatsCondition -> String.valueOf(yearStatsCondition.getNumberLine()),
				yearStatsCondition ->
						getArchiveListStr(YearStatsConditionSaveDTO.builder()
								.fondsCode(yearStatsCondition.getFondsCode())
								.numberLine(yearStatsCondition.getNumberLine())
								.build()), (item1, item2) -> item1));
	}

	@Override
	public Map<String, List<YearStatsCondition>> getYearStatsConditionMapByYearStatsId(String fondsCode, Long yearStatsId) {
		Assert.notNull(yearStatsId, "缺少年报统计表id");
		Assert.notEmpty(fondsCode, "缺少年报统计表全宗");
		Map<String, List<YearStatsCondition>> result =  new HashMap<>();
		List<YearStatsCondition> yearStatsConditionList = this.list(Wrappers.<YearStatsCondition>lambdaQuery()
				.eq(YearStatsCondition::getYearStatsId, yearStatsId)
		        .eq(YearStatsCondition::getFondsCode,fondsCode));;
		yearStatsConditionList.stream().forEach(yearStatsCondition ->{
			if(result.containsKey(StrUtil.toString(yearStatsCondition.getNumberLine()))){
				result.get(StrUtil.toString(yearStatsCondition.getNumberLine())).add(yearStatsCondition);
			}else{
				List<YearStatsCondition> list = new ArrayList<>();
				list.add(yearStatsCondition);
				result.put(StrUtil.toString(yearStatsCondition.getNumberLine()),list);
			}
		});
		return result;
	}

	@Override
	public List<YearStatsConditionSaveDTO> getArchiveTypeConditionList(Long yearStatsId , Integer numberLine) {
		Assert.notNull(yearStatsId, "缺少年报统计表id");
		Assert.notNull(numberLine, "缺少年报统计表对应行数");
		List<YearStatsCondition> yearStatsConditionList = this.list(Wrappers.<YearStatsCondition>lambdaQuery().eq(YearStatsCondition::getYearStatsId, yearStatsId).eq(YearStatsCondition::getNumberLine,numberLine));;

		return yearStatsConditionList.stream().map(yearStatsCondition -> {
			YearStatsConditionSaveDTO yearStatsConditionSaveDTO = new YearStatsConditionSaveDTO();
			BeanUtils.copyProperties(yearStatsCondition, yearStatsConditionSaveDTO);
			return yearStatsConditionSaveDTO;
		}).collect(Collectors.toList());
	}


	/*
	 * Description: 获取对应关系的前台显示
	 * @return: String
	 * @auther: qianbaocheng
	 * @date: 2021-7-1 14:58
	 */
	public String getArchiveListStr(YearStatsConditionSaveDTO yearStatsConditionSaveDTO) {
		if(ObjectUtil.isEmpty(yearStatsConditionSaveDTO.getFondsCode())||ObjectUtil.isNull(yearStatsConditionSaveDTO.getNumberLine())){
			return StrUtil.EMPTY;
		}
		Assert.notEmpty(yearStatsConditionSaveDTO.getFondsCode(),"缺少全宗条件");
		Assert.notNull(yearStatsConditionSaveDTO.getNumberLine(),"缺少行数条件");
		AtomicReference<String> name = new AtomicReference<>("");
		List<YearStatsCondition> yearStatsConditionList = this.list(Wrappers.<YearStatsCondition>lambdaQuery()
				.eq(YearStatsCondition::getFondsCode, yearStatsConditionSaveDTO.getFondsCode())
				.eq(YearStatsCondition::getNumberLine, yearStatsConditionSaveDTO.getNumberLine())
				.select(YearStatsCondition::getArchiveTypeName));
		yearStatsConditionList.stream().forEach(yearStatsCondition ->{
			yearStatsCondition.setBackCondition(CriteriaConditionUtil.unserialize(yearStatsCondition.getBackCondition()));
			yearStatsCondition.setPageCondition(CriteriaConditionUtil.unserialize(yearStatsCondition.getPageCondition()));
			name.set(name.get()+"/"+yearStatsCondition.getArchiveTypeName());
		});
		return ObjectUtil.isNotEmpty(name.get()) ? name.get().substring(1) : StrUtil.EMPTY;
	}

}
