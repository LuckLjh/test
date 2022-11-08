package com.cescloud.saas.archive.service.modular.stats.service.helper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.archivetype.feign.RemoteArchiveTableService;
import com.cescloud.saas.archive.api.modular.datasource.dto.DynamicArchiveDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveInnerService;
import com.cescloud.saas.archive.api.modular.stats.dto.YearStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.YearStats;
import com.cescloud.saas.archive.api.modular.stats.entity.YearStatsCondition;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.common.constants.FilingTypeEnum;
import com.cescloud.saas.archive.common.constants.InfoTypeConstants;
import com.cescloud.saas.archive.common.message.publisher.InfoPublisher;
import com.cescloud.saas.archive.common.search.CriteriaCondition;
import com.cescloud.saas.archive.common.search.parser.DatabaseSearchParser;
import com.cescloud.saas.archive.common.util.ArchiveTableUtil;
import com.cescloud.saas.archive.common.util.ArchiveUtil;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.businessconfig.async.AsyncUpdateFieldConfiguration;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.stats.service.YearStatsConditionService;
import com.cescloud.saas.archive.service.modular.stats.service.YearStatsService;
import com.cescloud.saas.archive.service.modular.stats.util.CriteriaConditionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class YearStatsHelper {

	private static final String REDIS_STATS_NAME = "global:stats:";

	@Autowired
	private YearStatsConditionService yearStatsConditionService;

	@Autowired
	private RemoteArchiveInnerService remoteArchiveService;

	@Autowired
	private ArchiveUtil archiveUtil;

	@Autowired
	private RemoteArchiveTableService remoteArchiveTableService;

	@Autowired
	private InfoPublisher infoPublisher;

	@Autowired
	private ArchiveTypeService archiveTypeService;

	@Autowired
	private ArchiveTableService archiveTableService;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private YearStatsService yearStatsService;

	@Async(AsyncUpdateFieldConfiguration.ASYNC_EXECUTOR_NAME)
	public void runAsync(YearStats yearStats, String fondsCode){
		Long yearStatsId = yearStats.getId();
		Long userId = SecurityUtils.getUser().getId();
		Long tenantId  = SecurityUtils.getUser().getTenantId();
		List<ArchiveTable> archiveTableList = archiveUtil.getAllArchiveTables();
		try {
			redisTemplate.opsForValue().set(REDIS_STATS_NAME + yearStatsId, yearStatsId, 60, TimeUnit.MINUTES);
			//固定项统计
			YearStatsDTO yearStatsDTO = yearStatsService.toDTO(yearStats);
			fixedValueStats(yearStatsDTO);
			AtomicReference<Boolean> sucess = new AtomicReference<>(true);
			//根据年报id获取所有设置参数行的配置
			Map<String, List<YearStatsCondition>> yearStatsConditionMap = yearStatsConditionService.getYearStatsConditionMapByYearStatsId(fondsCode, yearStatsId);
			Map<String, Integer> resultMap = yearStatsDTO.getLineValueMap();
			yearStatsConditionMap.forEach((k, v) -> {
				AtomicInteger count = new AtomicInteger();
				v.forEach(yearStatsCondition -> {
					String storageLocate = yearStatsCondition.getStorageLocate();//获取业务表名
					Optional<String> conditionSql = Optional.of(Optional.ofNullable(yearStatsCondition.getBackCondition()).map(o -> {
						CriteriaCondition backCondition = CriteriaConditionUtil.toCriteriaConditionFromDbBackCondition(o);
						String backSql = DatabaseSearchParser.parseCondition(backCondition);
						return backSql;
					}).orElse(""));
					ArchiveTable curArchiveTable = archiveTableList.stream().filter(archive -> ObjectUtil.equal(archive.getStorageLocate(),storageLocate)).findFirst().orElse(null);
					if(ObjectUtil.isNull(curArchiveTable)){
						log.info("获取表信息出错:{}" +storageLocate);
						sucess.set(false);
						return;
					}
					int countArchive = 0;
					if (StrUtil.equals(ArchiveLayerEnum.DOCUMENT.getCode(), Optional.ofNullable(curArchiveTable).map(ArchiveTable::getArchiveLayer).orElse(""))) {//如果是电子文件层
						R<ArchiveTable> r = remoteArchiveTableService.getInnerUpTableByStorageLocate(storageLocate, SecurityConstants.FROM_IN);
						if (!CommonConstants.SUCCESS.equals(r.getCode())) {
							log.info("年报统计获取上级表名失败:" + r.getMsg());
							sucess.set(false);
							return;
						}
						ArchiveTable archiveTable = r.getData();
						String curAlias = ArchiveTableUtil.getAliasByTableName(curArchiveTable.getStorageLocate());
						String upAlias = ArchiveTableUtil.getAliasByTableName(archiveTable.getStorageLocate());
						String where = conditionSql.get() + " " + upAlias + "." + FieldConstants.FONDS_CODE + "='" + fondsCode + "'";
						DynamicArchiveDTO dynamicArchiveDTO = new DynamicArchiveDTO();
						dynamicArchiveDTO.setAlias(curAlias);
						dynamicArchiveDTO.setTableName(curArchiveTable.getStorageLocate());
						dynamicArchiveDTO.setWhere(where);
						dynamicArchiveDTO.setJoin(" LEFT JOIN " + archiveTable.getStorageLocate() + " " + upAlias + "  ON " + curAlias + "." + FieldConstants.OWNER_ID + " = " + upAlias + "." + FieldConstants.ID);
						R<Integer> result = remoteArchiveService.getCountByCondition(dynamicArchiveDTO, SecurityConstants.FROM_IN);
						if (result.getCode() == CommonConstants.FAIL) {
							log.info("年报统计查询档案失败:{}>>>>>{}", archiveTable.getStorageLocate(), result.getMsg());
							sucess.set(false);
							return;
						} else {
							countArchive = result.getData();
						}
					} else {
						DynamicArchiveDTO dynamicArchiveDTO = DynamicArchiveDTO.builder().tableName(storageLocate).fondsCode(fondsCode).where(StrUtil.toString(conditionSql.get())).build();
						R<Integer> result = remoteArchiveService.getCountByCondition(dynamicArchiveDTO, SecurityConstants.FROM_IN);
						if (result.getCode() == CommonConstants.FAIL) {
							log.info("年报统计查询档案失败:" + result.getMsg());
							sucess.set(false);
							return;
						} else {
							countArchive = result.getData();
						}
					}
					count.set(count.get() + countArchive);
					log.info("年报统计>>>>>{}>>>>>{}>>>>>{}", k, storageLocate, countArchive);
				});
				log.info("年报统计>>>>>{}总数>>>>>{}", k, count.get());
				resultMap.put(k, count.get());
			});
			yearStatsService.updateById(YearStats.builder().id(yearStatsId).lineValueJson(JSON.toJSONString(resultMap)).build());
			redisTemplate.delete(REDIS_STATS_NAME + yearStatsId);
			String msg = "年度为"+yearStats.getYearCode()+"的年报统计成功!";
			if(!sucess.get()){
				msg = msg + "统计过程中发生过错误,可能导致数据不准,请重新统计或联系管理员!";
			}
			infoPublisher.send("年度为"+yearStats.getYearCode()+"的年报统计完成", userId, tenantId,
					msg, InfoTypeConstants.INFO, String.valueOf(RandomUtil.randomInt(Integer.MAX_VALUE)), InfoTypeConstants.SUCCESS_ICON);
		} catch (Exception e) {
			redisTemplate.delete(REDIS_STATS_NAME + yearStatsId);//统计出错还原
			infoPublisher.send("年度为"+yearStats.getYearCode()+"的年报统计失败", userId, tenantId,
					"年度为"+yearStats.getYearCode()+"的年报统计失败!", InfoTypeConstants.INFO, String.valueOf(RandomUtil.randomInt(Integer.MAX_VALUE)), InfoTypeConstants.SUCCESS_ICON);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 固定统计项
	 * @param yearStatsDTO 统计条件
	 */
	private void fixedValueStats(YearStatsDTO yearStatsDTO){
		String yearCode = yearStatsDTO.getYearCode();
		String fondsCode = yearStatsDTO.getFondsCode();
		Integer item28 = 0;  //档案类型为案卷的数量 所有状态数据	总量
		Integer item29 = 0;  //档案类型为一文一件的数量 包含（单套制） 所有状态数据 总量
		Integer item32 = 0;  //档案类型为案卷、保管期限为永久、30年（长期）的档案	总量	所有状态
		Integer item33 = 0;  //档案类型为一文一件、保管期限为永久、30年（长期）的档案	总量	所有状态
		Integer item34 = 0;  //档案类型为案卷、保管期限为永久的档案	总量	所有状态
		Integer item35 = 0;  //档案类型为一文一件、保管期限为永久的档案	总量	所有状态
		Integer item57 = 0;  //本年度档案类型为案卷的数量	当年
		Integer item58 = 0;  //本年度档案类型为一文一件的数量	当年

		List<ArchiveType> archiveTypes = archiveTypeService.getArchiveTypes();
		for (ArchiveType archiveType : archiveTypes) {
			String filingType = archiveType.getFilingType();
			FilingTypeEnum filingTypeEnum = FilingTypeEnum.getEnum(filingType);
			List<ArchiveTable> tableList = archiveTableService.getTableListByTypeCode(archiveType.getTypeCode());
			String storageLocate = "";
			List<Integer> statsList;
			switch (filingTypeEnum)
			{
				case PROJECT:
					for (ArchiveTable archiveTable : tableList) {
						if (ArchiveLayerEnum.FOLDER.getCode().equals(archiveTable.getArchiveLayer())){
							storageLocate = archiveTable.getStorageLocate();
						}
					}
					statsList = statsTable(yearCode, fondsCode, storageLocate);
					item28 += statsList.get(0);
					item32 += statsList.get(1);
					item34 += statsList.get(2);
					item57 += statsList.get(3);
					break;
				case FOLDER:
					for (ArchiveTable archiveTable : tableList) {
						if (ArchiveLayerEnum.FOLDER.getCode().equals(archiveTable.getArchiveLayer())){
							storageLocate = archiveTable.getStorageLocate();
						}
					}
					statsList = statsTable(yearCode, fondsCode, storageLocate);
					item28 += statsList.get(0);
					item32 += statsList.get(1);
					item34 += statsList.get(2);
					item57 += statsList.get(3);
					break;
				case ONE:
					for (ArchiveTable archiveTable : tableList) {
						if (ArchiveLayerEnum.ONE.getCode().equals(archiveTable.getArchiveLayer())){
							storageLocate = archiveTable.getStorageLocate();
						}
					}
					statsList = statsTable(yearCode, fondsCode, storageLocate);
					item29 += statsList.get(0);
					item33 += statsList.get(1);
					item35 += statsList.get(2);
					item58 += statsList.get(3);
					break;
				case SINGLE:
					for (ArchiveTable archiveTable : tableList) {
						if (ArchiveLayerEnum.SINGLE.getCode().equals(archiveTable.getArchiveLayer())){
							storageLocate = archiveTable.getStorageLocate();
						}
					}
					statsList = statsTable(yearCode, fondsCode, storageLocate);
					item29 += statsList.get(0);
					item33 += statsList.get(1);
					item35 += statsList.get(2);
					item58 += statsList.get(3);
					break;
				default:
			}
		}
		Map<String, Integer> valueMap = yearStatsDTO.getLineValueMap();
		valueMap.put("28",item28);
		valueMap.put("29",item29);
		valueMap.put("32",item32);
		valueMap.put("33",item33);
		valueMap.put("34",item34);
		valueMap.put("35",item35);
		valueMap.put("57",item57);
		valueMap.put("58",item58);
	}

	/**
	 * 每个表的固定项统计
	 * @param yearCode
	 * @param fondsCode
	 * @param storageLocate
	 * @return
	 */
	private List<Integer> statsTable(String yearCode, String fondsCode, String storageLocate){
		List<Integer> ret = CollectionUtil.<Integer>newArrayList();
		String conditionSql = FieldConstants.IS_DELETE + "= 0" ;

		//所有状态数据-总量 所有状态
		DynamicArchiveDTO dynamicArchiveDTO = DynamicArchiveDTO.builder().tableName(storageLocate).fondsCode(fondsCode).where(conditionSql).build();
		Integer t = archiveUtil.getCountByCondition(storageLocate, "t", fondsCode, conditionSql, false);
		ret.add(Optional.ofNullable(t).orElse(0));

		//保管期限为永久、30年（长期）的档案 -总量	所有状态
		conditionSql = FieldConstants.IS_DELETE + "= 0 AND (" +FieldConstants.RETENTION_PERIOD +" = '永久' or " + FieldConstants.RETENTION_PERIOD +" = '30年')";
		dynamicArchiveDTO = DynamicArchiveDTO.builder().tableName(storageLocate).fondsCode(fondsCode).where(conditionSql).build();
		t = archiveUtil.getCountByCondition(storageLocate, "t", fondsCode, conditionSql, false);
		ret.add(Optional.ofNullable(t).orElse(0));

		//保管期限为永久的档案 -总量所有状态
		conditionSql = FieldConstants.IS_DELETE + "= 0 AND " +FieldConstants.RETENTION_PERIOD +" = '永久'" ;
		dynamicArchiveDTO = DynamicArchiveDTO.builder().tableName(storageLocate).fondsCode(fondsCode).where(conditionSql).build();
		t = archiveUtil.getCountByCondition(storageLocate, "t", fondsCode, conditionSql, false);
		ret.add(Optional.ofNullable(t).orElse(0));

		//本年度档案 -总量所有状态
		conditionSql = FieldConstants.IS_DELETE + "= 0 AND " +FieldConstants.YEAR_CODE + " = "+ yearCode;
		dynamicArchiveDTO = DynamicArchiveDTO.builder().tableName(storageLocate).fondsCode(fondsCode).where(conditionSql).build();
		t = archiveUtil.getCountByCondition(storageLocate, "t", fondsCode, conditionSql, false);
		ret.add(Optional.ofNullable(t).orElse(0));
		return ret;
	}
}
