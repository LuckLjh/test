
package com.cescloud.saas.archive.service.modular.stats.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.datasource.dto.DynamicArchiveDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveInnerService;
import com.cescloud.saas.archive.api.modular.stats.constant.StatsConstants;
import com.cescloud.saas.archive.api.modular.stats.constant.StatsFilingTypeEnum;
import com.cescloud.saas.archive.api.modular.stats.dto.*;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveDeckNew;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.common.util.DataSourceUtil;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.stats.mapper.ArchiveDeckNewMapper;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveDeckNewService;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveStatsService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 驾驶舱新增量
 *
 * @author 刘冬1
 * @date 2021-04-29 17:39:59
 */


@Slf4j
@Service("archiveDockNewService")
@RequiredArgsConstructor
public class ArchiveDeckNewServiceImpl extends ServiceImpl<ArchiveDeckNewMapper, ArchiveDeckNew> implements ArchiveDeckNewService {
	@Autowired
	@Qualifier("archiveStatsService")
	private ArchiveStatsService archiveStatsService;
	/**
	 * 档案门类service
	 *
	 * @return
	 */
	protected final ArchiveTypeService getArchiveTypeService;

	/**
	 * 档案门类表service
	 *
	 * @return
	 */
	protected final ArchiveTableService getArchiveTableService;

	/**
	 * （档案数据）feign调用接口
	 *
	 * @return
	 */
	protected final RemoteArchiveInnerService getRemoteArchiveInnerService;

	/**
	 * 模板表service
	 *
	 * @return
	 */
	protected final TemplateTableService getTemplateTableService;

	@Override
	public void execute() {
		if (log.isInfoEnabled()) {
//			log.info("执行[" + statsName() + "]统计");
		}
		final ArchiveTypeStatsDTO statsDTO = getStatsDTO();
		if (null == statsDTO) {
			return;
		}
		final String[] statsFields = getStatsFields();
		for (final String statsField : statsFields) {
			execute(statsDTO, statsField);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void execute(ArchiveTypeStatsDTO statsDTO, String statsField) {
//		log.info("最终的结果是:{}", JSONUtil.toJsonStr(statsDTO));
//		log.info("最终的比较域是:{}", statsField);

		QueryWrapper<ArchiveDeckNew> queryWrapper = new QueryWrapper <>();
		queryWrapper.select("max(created_time) createdTime");
		List<ArchiveDeckNew> archiveDeckNewListMax = this.baseMapper.selectList(queryWrapper);
		LocalDateTime createdTimeFrom = null;
		LocalDateTime createdTimeTo = null;
		if(archiveDeckNewListMax.size()>0 && archiveDeckNewListMax.get(0)!=null){
			createdTimeFrom = archiveDeckNewListMax.get(0).getCreatedTime();//查询日期的 00:00:00
			createdTimeFrom = LocalDateTimeUtil.endOfDay(createdTimeFrom);//查询日期 到  23:59:59
			createdTimeTo = LocalDateTimeUtil.offset(LocalDateTimeUtil.beginOfDay(LocalDateTime.now()) ,-1, ChronoUnit.DAYS);
			createdTimeTo = LocalDateTimeUtil.endOfDay(createdTimeTo);//查询日期 到  23:59:59
			if(createdTimeFrom.compareTo(createdTimeTo)>=0){
				log.info("停止无法执行：{} -- {}",createdTimeFrom,createdTimeTo);
				return;
			}

		}
		else{
			createdTimeTo = LocalDateTimeUtil.offset(LocalDateTimeUtil.beginOfDay(LocalDateTime.now()) ,-1, ChronoUnit.DAYS);
			createdTimeTo = LocalDateTimeUtil.endOfDay(createdTimeTo);//查询日期 到  23:59:59
		}
		log.info("执行日期：{} -- {}",createdTimeFrom,createdTimeTo);



		List<StatsTaskDTO> statsTaskDTOList = Lists.newArrayList();
		List<ArchiveDeckNew> archiveDeckNewList = Lists.newArrayList();
		statsTaskDTOList.addAll(submitTask(statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.PROJECT, statsDTO.getProjectArchiveTypeList()));
		statsTaskDTOList.addAll(submitTask(statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.FOLDER, statsDTO.getFolderArchiveTypeList()));
		statsTaskDTOList.addAll(submitTask(statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.ONE, statsDTO.getFileArchiveTypeList()));
		statsTaskDTOList.addAll(submitTask(statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.SINGAL, statsDTO.getSingleArchiveTypeList()));
		statsTaskDTOList.addAll(submitTask(statsDTO.getTenantId(), statsField, StatsFilingTypeEnum.DOCUMENT, statsDTO.getDocumentArchiveTypeList()));
		for (StatsTaskDTO statsTaskDTO : statsTaskDTOList) {
			List<Map<String, Object>> listDBResult = getStatsData(statsTaskDTO,createdTimeFrom,createdTimeTo);
			archiveDeckNewList.addAll(getArchiveDeckNew(statsTaskDTO, listDBResult));
		}
//		statsTaskDTOList.stream().forEach(statsTaskDTO -> {
//			List<Map<String, Object>> listDBResult = getStatsData(statsTaskDTO,createdTimeFrom,createdTimeTo);
//			archiveDeckNewList.addAll(getArchiveDeckNew(statsTaskDTO, listDBResult));
//		});
		log.info("将要插入的条目数量：{}", archiveDeckNewList.size());
		//批量保存
		this.saveBatch(archiveDeckNewList);


//
//		StatsTaskDTO firstTest = statsTaskDTOList.stream().filter(t->t.getTenantId().longValue()!=161).findFirst().orElse(null);
//		List<Map<String, Object>> getFirstStatDateInfo =  getStatsData(firstTest);
//		log.info("第一个查询结果的信息为:{}",JSONUtil.toJsonStr( getFirstStatDateInfo ));
//		List<ArchiveDeckNew> resultList =  this.list();
//		log.info("第一个查询结果的信息为:{}",JSONUtil.toJsonStr( resultList ));
	}

	private List<ArchiveDeckNew> getArchiveDeckNew(StatsTaskDTO statsTaskDTO, List<Map<String, Object>> getFirstStatDateInfo) {
		List<ArchiveDeckNew> list = Lists.newArrayList();

//		Map<String, Object> storageLocateMap = MapUtil.newHashMap();
//		storageLocateMap.put("storageLocate",statsTaskDTO.getFilingTypeStatsDTO().getStorageLocate());
//		remoteData.getData().add(storageLocateMap);

		//		//t_175_ws_ztdaywyj_13782_o
		//t_232_ws_zb_ws_784_o
		String storageLocate = statsTaskDTO.getFilingTypeStatsDTO().getStorageLocate();
		log.info("存储地址为 storageLocate :{}",storageLocate);
		List<String> storageList = StrSpliter.split(storageLocate, '_', 0, true, true);
//		log.info("存储地址为 storageList :{}",JSONUtil.toJsonStr(storageList));
		ArchiveDeckNew archiveDeckNew = new ArchiveDeckNew();
		archiveDeckNew.setUpdatedTime(LocalDateTime.now());
		archiveDeckNew.setStatus(0);
		archiveDeckNew.setTenantId(NumberUtil.parseLong(storageList.get(1)));
		archiveDeckNew.setArchiveClassType(storageList.get(2));
		archiveDeckNew.setArchiveTypeCode(storageList.get(3));
		if(storageList.size()>6){
			archiveDeckNew.setTemplateTypeId(NumberUtil.parseLong(storageList.get(storageList.size()-2)));
			archiveDeckNew.setLayerCode(storageList.get(storageList.size()-1));
		}
		else{
			archiveDeckNew.setTemplateTypeId(NumberUtil.parseLong(storageList.get(4)));
			archiveDeckNew.setLayerCode(storageList.get(5));
		}

		for (Map<String, Object> item : getFirstStatDateInfo) {
			ArchiveDeckNew deckNew = new ArchiveDeckNew();
			deckNew.setUpdatedTime(archiveDeckNew.getUpdatedTime());
			deckNew.setStatus(archiveDeckNew.getStatus());
			deckNew.setTenantId(archiveDeckNew.getTenantId());
			deckNew.setArchiveClassType(archiveDeckNew.getArchiveClassType());
			deckNew.setArchiveTypeCode(archiveDeckNew.getArchiveTypeCode());
			deckNew.setTemplateTypeId(archiveDeckNew.getTemplateTypeId());
			deckNew.setLayerCode(archiveDeckNew.getLayerCode());


			Object dayInfo = item.get("dd");
			if (dayInfo != null) {
				LocalDateTime localDateTime = ArchiveDeckNewServiceImpl.strToLocalDateTime(StrUtil.toString(dayInfo));
				deckNew.setCreatedTime(localDateTime);

				deckNew.setCreatedYear(localDateTime.getYear());
				deckNew.setCreatedMonth(localDateTime.getMonthValue());
				deckNew.setCreatedDay(localDateTime.getDayOfMonth());
				deckNew.setCreatedWeek(localDateTime.getDayOfWeek().getValue());
				deckNew.setCreatedYearWeek(DateUtil.weekOfYear(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
			}
			Object page_amount = item.get("page_amount");
			if (page_amount != null) {
				deckNew.setPageAmount(NumberUtil.parseInt(page_amount.toString()));
			}
			Object stats_amount = item.get("stats_amount");
			if (stats_amount != null) {
				deckNew.setStatsAmount(NumberUtil.parseInt(stats_amount.toString()));
			}
			Object fonds_code = item.get("fonds_code");
			if (fonds_code != null) {
				deckNew.setFondsCode(fonds_code.toString());
			}
			list.add(deckNew);
		}
		return list;
	}

	public static LocalDateTime strToLocalDateTime(String str) {

		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime ldt = LocalDateTime.parse(str + " 00:00:00", df);
		return ldt;
	}

	private List<StatsTaskDTO> submitTask(Long tenantId, String statsField, StatsFilingTypeEnum statsFilingTypeEnum, List<FilingTypeStatsDTO> folderList) {
		List<StatsTaskDTO> statsTaskDTOList = Lists.newArrayList();
		for (final FilingTypeStatsDTO filingTypeStatsDTO : folderList) {
			String storageLocate = filingTypeStatsDTO.getStorageLocate();
			List<String> storageInfo = StrUtil.splitTrim(storageLocate, "_");
			if (storageInfo != null && storageInfo.size() > 0) {
				tenantId = NumberUtil.parseLong(storageInfo.get(1));
			}
			StatsTaskDTO statsTaskDTO = toStatsTaskDTO(tenantId, filingTypeStatsDTO, statsFilingTypeEnum, statsField);
			statsTaskDTOList.add(statsTaskDTO);
		}
//		log.info("最终的查询域列表是:{}", JSONUtil.toJsonStr(statsTaskDTOList));
		return statsTaskDTOList;
	}

	private StatsTaskDTO toStatsTaskDTO(Long tenantId, FilingTypeStatsDTO filingTypeStatsDTO,
                                        StatsFilingTypeEnum statsFilingTypeEnum, String statsField) {
		return new StatsTaskDTO(tenantId, statsFilingTypeEnum, statsField, filingTypeStatsDTO);
	}


	public List<Map<String, Object>> getStatsData(StatsTaskDTO statsTaskDTO, LocalDateTime createdTimeFrom, LocalDateTime createdTimeTo) {
		final R<List<Map<String, Object>>> remoteData = getRemoteArchiveInnerService.getListByCondition(getDynamicArchiveVo(statsTaskDTO,createdTimeFrom,createdTimeTo), SecurityConstants.FROM_IN);

		if (!CommonConstants.SUCCESS.equals(remoteData.getCode())) {
			if (log.isErrorEnabled()) {
				log.error("返回为空但继续执行！！ sharding查询[{}]出错：{}", statsTaskDTO.getFilingTypeStatsDTO().getStorageLocate(),
						remoteData.getMsg());
			}
			return Collections.emptyList();
		}

		if (null == remoteData.getData()) {
			if (log.isWarnEnabled()) {
				log.warn("sharding查询[{}]数据为空", statsTaskDTO.getFilingTypeStatsDTO().getStorageLocate());
			}
			return Collections.emptyList();
		}
		return remoteData.getData();
	}


	// 统计每天的增加量
	protected DynamicArchiveDTO getDynamicArchiveVo(StatsTaskDTO statsTaskDTO, LocalDateTime createdTimeFrom, LocalDateTime createdTimeTo) {
		final DynamicArchiveDTO dynamicArchiveDTO = new DynamicArchiveDTO();
		dynamicArchiveDTO.setTableName(statsTaskDTO.getFilingTypeStatsDTO().getStorageLocate());
		final DbType dbType = DataSourceUtil.getCurrentDbType();
		final List<String> selectColumnList = new ArrayList<>(8);
		switch (dbType) {
			case MYSQL:
				if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
					selectColumnList.add("date_format(d.created_time,'%Y-%m-%d')  as dd");
				} else {
					selectColumnList.add("date_format(created_time,'%Y-%m-%d')  as dd");
				}
				break;
			case ORACLE:
			case ORACLE_12C:
			case DM:
				if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
					selectColumnList.add("to_char(d.created_time,'yyyy-MM-dd')  as dd");
				} else {
					selectColumnList.add("to_char(created_time,'yyyy-MM-dd')  as dd");
				}
				break;
			case SQL_SERVER:
				if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
					selectColumnList.add("convert(varchar(10),d.created_time,23)  as dd");
				} else {
					selectColumnList.add("convert(varchar(10),created_time,23)  as dd");
				}
				break;
			default:
				if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
					selectColumnList.add("date_format(d.created_time,'%Y-%m-%d')  as dd");
				} else {
					selectColumnList.add("date_format(created_time,'%Y-%m-%d')  as dd");
				}
				break;
		}
		if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.PROJECT) {
			// 项目只按全宗统计
			selectColumnList.add(FieldConstants.FONDS_CODE);
			selectColumnList.add("sum(1) as " + StatsConstants.STATS_AMOUNT);
		} else if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
			dynamicArchiveDTO.setAlias("d");
			dynamicArchiveDTO.setJoin("join " + statsTaskDTO.getFilingTypeStatsDTO().getParentStorageLocate()
					+ " p on p." + FieldConstants.ID + "=d." + FieldConstants.OWNER_ID + " and p."
					+ FieldConstants.IS_DELETE + "=" + BoolEnum.NO.getCode());
			// 电子全文只按全宗统计
			selectColumnList.add("p." + FieldConstants.FONDS_CODE);
			selectColumnList.add("sum(1) as " + StatsConstants.STATS_AMOUNT);
			// StatsConstants.DIGITED_AMOUNT：电子文件大小
			selectColumnList
					.add("sum(d." + FieldConstants.Document.FILE_SIZE + ") as " + StatsConstants.DIGITED_AMOUNT);
		} else {
			selectColumnList.add(FieldConstants.FONDS_CODE);
			selectColumnList.add("sum(1) as " + StatsConstants.STATS_AMOUNT);
			if (StatsFilingTypeEnum.FOLDER == statsTaskDTO.getStatsFilingTypeEnum()) {
				// 卷内总件数
				selectColumnList.add("sum(" + FieldConstants.ITEM_COUNT + ") as " + StatsConstants.FILE_AMOUNT);
			}
			// amount_of_pages: 案卷/文件为页数，音视频为时长
			selectColumnList.add("sum(" + FieldConstants.AMOUNT_OF_PAGES + ") as " + StatsConstants.PAGE_AMOUNT);
//			// 已数字化页数
//			selectColumnList.add("sum(" + FieldConstants.PDF_PAGE + ") as " + StatsConstants.DIGITED_PAGE_AMOUNT);
//			// 已数字化数量
//			selectColumnList.add("sum(case when " + FieldConstants.PDF_PAGE + ">0 then 1 else 0 end) as " + StatsConstants.DIGITED_AMOUNT);
		}
		dynamicArchiveDTO.setFilterColumn(selectColumnList);
//LocalDateTime createdTimeFrom,LocalDateTime createdTimeTo
		// 条件
		if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
			StringBuilder sb = new StringBuilder();
			sb.append("d." + FieldConstants.IS_DELETE + "=" + BoolEnum.NO.getCode()+" and d.created_time is not null and p.fonds_code is not null ");
			if(createdTimeFrom!=null){
				sb.append(" and d.created_time ").append(">").append("'").append(LocalDateTimeUtil.format(createdTimeFrom, DatePattern.NORM_DATETIME_PATTERN)).append("' ");
			}
			if(createdTimeTo!=null){
				sb.append(" and d.created_time ").append("<=").append("'").append(LocalDateTimeUtil.format(createdTimeTo, DatePattern.NORM_DATETIME_PATTERN)).append("' ");
			}
			dynamicArchiveDTO.setWhere(sb.toString());

		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(FieldConstants.IS_DELETE + "=" + BoolEnum.NO.getCode()+" and created_time is not null and fonds_code is not null");
			if(createdTimeFrom!=null){
				sb.append(" and created_time ").append(">").append("'").append(LocalDateTimeUtil.format(createdTimeFrom, DatePattern.NORM_DATETIME_PATTERN)).append("' ");
			}
			if(createdTimeTo!=null){
				sb.append(" and created_time ").append("<=").append("'").append(LocalDateTimeUtil.format(createdTimeTo, DatePattern.NORM_DATETIME_PATTERN)).append("' ");
			}
			dynamicArchiveDTO.setWhere(sb.toString());
		}


		// GROUP BY 字段
		final List<String> groupColumnList = new ArrayList<>(4);
		switch (dbType) {
			case MYSQL:
				if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
					groupColumnList.add("date_format(d.created_time,'%Y-%m-%d')");
				} else {
					groupColumnList.add("date_format(created_time,'%Y-%m-%d')");
				}
				break;
			case ORACLE:
			case ORACLE_12C:
			case DM:
				if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
					groupColumnList.add("to_char(d.created_time,'yyyy-MM-dd')");
				} else {
					groupColumnList.add("to_char(created_time,'yyyy-MM-dd')");
				}
				break;
			case SQL_SERVER:
				if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
					groupColumnList.add("convert(varchar(10),d.created_time,23)");
				} else {
					groupColumnList.add("convert(varchar(10),created_time,23)");
				}

				break;
			default:
				if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
					groupColumnList.add("date_format(d.created_time,'%Y-%m-%d')");
				} else {
					groupColumnList.add("date_format(created_time,'%Y-%m-%d')");
				}
				break;
		}
		groupColumnList.add(FieldConstants.FONDS_CODE);

		if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.PROJECT) {
		} else if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
		} else {
		}
		dynamicArchiveDTO.setGroups(groupColumnList);
//		log.info("bbp查询条件为：{}", JSONUtil.toJsonStr(dynamicArchiveDTO));
		return dynamicArchiveDTO;
	}


/*

	final List<Map<String, Object>> statsData = statsExecutorService.getStatsData(statsTaskDTO);
            if (null != statsData && !statsData.isEmpty()) {
		statsExecutorService.saveStatsData(statsTaskDTO, statsData);
	}

       */


	/**
	 * 表示按哪个字段进行统计，多个字段表示统计多次，默认按保管期限字段进行统计
	 *
	 * @return
	 */
	protected String[] getStatsFields() {
		return new String[]{FieldConstants.RETENTION_PERIOD};
	}

	protected ArchiveTypeStatsDTO getStatsDTO() {

		final List<ArchiveType> typeList = getArchiveTypeService.list();
		if (null == typeList || typeList.isEmpty()) {
			if (log.isWarnEnabled()) {
				log.warn("获取档案门类信息为空");
			}
			return null;
		}

		final List<ArchiveTable> tableList = getArchiveTableService.list();
		if (null == typeList || typeList.isEmpty()) {
			if (log.isWarnEnabled()) {
				log.warn("获取档案门类表信息为空");
			}
			return null;
		}

		final List<TemplateTable> templateTableList = getTemplateTableService.list();

		return toStatsDTO(typeList, tableList, templateTableList);
	}


	/**
	 * 转化为档案门类统计DTO
	 *
	 * @param typeList
	 * @param tableList
	 * @param templateTableList
	 * @return
	 */
	protected ArchiveTypeStatsDTO toStatsDTO(List<ArchiveType> typeList, List<ArchiveTable> tableList,
                                             List<TemplateTable> templateTableList) {

		final ArchiveTypeStatsDTO statsDTO = new ArchiveTypeStatsDTO();

		statsDTO.setTenantId(typeList.get(0).getTenantId());

		final Map<String, ArchiveType> typeMap = toTypeMap(typeList);

		final Map<Long, Long> templateParentMap = toTemplateParentMap(templateTableList);

		final Map<String, ArchiveTable> templateTableMap = toTemplateTableMap(tableList);

		for (final ArchiveTable table : tableList) {
			if (typeMap.get(table.getArchiveTypeCode()) == null) {
				continue;
			}
			if (ArchiveLayerEnum.PROJECT.getCode().equals(table.getArchiveLayer())) {
				statsDTO.getProjectArchiveTypeList()
						.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
								templateTableMap));
			} else if (ArchiveLayerEnum.FOLDER.getCode().equals(table.getArchiveLayer())) {
				statsDTO.getFolderArchiveTypeList()
						.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
								templateTableMap));
			} else if (ArchiveLayerEnum.ONE.getCode().equals(table.getArchiveLayer())) {
				statsDTO.getFileArchiveTypeList()
						.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
								templateTableMap));
			} else if (ArchiveLayerEnum.SINGLE.getCode().equals(table.getArchiveLayer())) {
				statsDTO.getSingleArchiveTypeList()
						.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
								templateTableMap));
			} else if (ArchiveLayerEnum.DOCUMENT.getCode().equals(table.getArchiveLayer())) {
				statsDTO.getDocumentArchiveTypeList()
						.add(toFilingTypeStatsDTO(typeMap.get(table.getArchiveTypeCode()), table, templateParentMap,
								templateTableMap));
			}
		}

		return statsDTO;
	}

	private FilingTypeStatsDTO toFilingTypeStatsDTO(ArchiveType type, ArchiveTable table, Map<Long, Long> parentMap,
                                                    Map<String, ArchiveTable> templateTableMap) {
		final FilingTypeStatsDTO filingTypeStatsDTO = new FilingTypeStatsDTO();
		filingTypeStatsDTO.setArchiveTypeCode(table.getArchiveTypeCode());
		if (type == null) {
			log.info("将会出现空指针异常的表信息：{}", table);
		}
		filingTypeStatsDTO.setClassType(type.getClassType());
		filingTypeStatsDTO.setArchiveTypeFilingType(type.getFilingType());
		filingTypeStatsDTO.setStorageLocate(table.getStorageLocate());
		final Long templateTableId = table.getTemplateTableId();
		final Long parentTemplateTableId = parentMap.get(templateTableId);
		if (null != parentTemplateTableId) {
			final ArchiveTable parentTable = templateTableMap.get(parentTemplateTableId + "-" + table.getArchiveTypeCode());
			if (null != parentTable) {
				filingTypeStatsDTO.setParentStorageLocate(parentTable.getStorageLocate());
			}
		}
		return filingTypeStatsDTO;
	}

	private Map<String, ArchiveType> toTypeMap(List<ArchiveType> typeList) {
		final Map<String, ArchiveType> typeMap = new HashMap<String, ArchiveType>(typeList.size());

		for (final ArchiveType type : typeList) {
			typeMap.put(type.getTypeCode(), type);
		}

		return typeMap;
	}

	private Map<Long, Long> toTemplateParentMap(List<TemplateTable> templateTableList) {
		final Map<Long, Long> parentMap = new HashMap<Long, Long>(templateTableList.size());

		for (final TemplateTable tt : templateTableList) {
			parentMap.put(tt.getId(), tt.getParentId());
		}

		return parentMap;
	}

	private Map<String, ArchiveTable> toTemplateTableMap(List<ArchiveTable> tableList) {
		final Map<String, ArchiveTable> templateTableMap = new HashMap<String, ArchiveTable>(tableList.size());

		for (final ArchiveTable table : tableList) {
			templateTableMap.put(table.getTemplateTableId() + "-" + table.getArchiveTypeCode(), table);
		}

		return templateTableMap;
	}

	/**
	 * 判断 false 抛出业务异常
	 * @param expression
	 * @param message
	 */
	@SneakyThrows
	public void isTrue(boolean expression, String message) throws ArchiveBusinessException {
		if (!expression) {
			throw new ArchiveBusinessException(message);
		}
	}
	@Override
	public DeckTotalStatsDTO<DeckNewTotalDataDTO, List<DeckNewDataDTO>> deckNewStat(ArchiveDeckNew archiveDeckNew) throws ArchiveBusinessException {

		this.isTrue(archiveDeckNew.getCreatedTime()!=null,"当前日期需要传递");
		DeckTotalStatsDTO<DeckNewTotalDataDTO, List<DeckNewDataDTO>> result = new DeckTotalStatsDTO<DeckNewTotalDataDTO, List<DeckNewDataDTO>>();
		int queryYear = archiveDeckNew.getCreatedTime().getYear();
		int queryMonth = archiveDeckNew.getCreatedTime().getMonthValue();
		Date date = Date.from(archiveDeckNew.getCreatedTime().atZone(ZoneId.systemDefault()).toInstant());
		int queryYearWeek =  DateUtil.weekOfYear(date);

		QueryWrapper<ArchiveDeckNew> queryWrapper = new QueryWrapper<>();
		if(StrUtil.isNotBlank(archiveDeckNew.getFondsCode())){
			queryWrapper.eq("fonds_code",archiveDeckNew.getFondsCode());
		}
		if(archiveDeckNew.getTenantId()!=null){
			queryWrapper.eq("tenant_id",archiveDeckNew.getTenantId());
		}
		switch (archiveDeckNew.getStatus()){
			case 100://每月
				queryWrapper.eq("created_year",queryYear);
				queryWrapper.groupBy("created_month");
				queryWrapper.orderByAsc("created_month");
				queryWrapper.select("created_month status","sum(stats_amount) statsAmount","sum(page_amount) pageAmount","sum(digited_amount) digitedAmount");
				break;
			case 10://每周
				queryWrapper.eq("created_year",queryYear);
				queryWrapper.eq("created_month",queryMonth);
				queryWrapper.groupBy("created_week");
				queryWrapper.orderByAsc("created_week");
				queryWrapper.select("created_week status","sum(stats_amount) statsAmount","sum(page_amount) pageAmount","sum(digited_amount) digitedAmount");
				break;
			case 1://每日
				queryWrapper.eq("created_year",queryYear);
				queryWrapper.eq("created_month",queryMonth);
				queryWrapper.eq("created_year_week",queryYearWeek);
				queryWrapper.groupBy("created_day");
				queryWrapper.orderByAsc("created_day");
				queryWrapper.select("created_day status","sum(stats_amount) statsAmount","sum(page_amount) pageAmount","sum(digited_amount) digitedAmount");
				break;
			default:
				queryWrapper.eq("created_year",queryYear);
				queryWrapper.groupBy("created_month");
				queryWrapper.orderByAsc("created_month");
				queryWrapper.select("created_month status","sum(stats_amount) statsAmount","sum(page_amount) pageAmount","sum(digited_amount) digitedAmount");
		}
		List<ArchiveDeckNew> resultArchiveDeckNewList = this.baseMapper.selectList(queryWrapper);
		log.info("结果是:{}", JSONUtil.toJsonStr(resultArchiveDeckNewList));

		DeckNewTotalDataDTO totalDataDTO = new DeckNewTotalDataDTO();
		totalDataDTO.setNewDigitedAmountUnit("KB全文");
		totalDataDTO.setNewPageAmountUnit("件");
		totalDataDTO.setNewStatsAmountUnit("卷/件");
		List<DeckNewDataDTO> itemList = resultArchiveDeckNewList.stream().map(statN ->{
			DeckNewDataDTO deckNewDataDTO = new DeckNewDataDTO();
			deckNewDataDTO.setGroupValue(statN.getStatus().intValue());
			deckNewDataDTO.setNewDigitedAmount(statN.getDigitedAmount());
			deckNewDataDTO.setNewPageAmount(statN.getPageAmount());
			deckNewDataDTO.setNewStatsAmount(statN.getStatsAmount());
			switch (archiveDeckNew.getStatus()){
				case 100://每月
					deckNewDataDTO.setGroupTitle(deckNewDataDTO.getGroupValue()+"月");
					 break;
				case 10://每周
					deckNewDataDTO.setGroupTitle("第"+deckNewDataDTO.getGroupValue()+"周");
					break;
				case 1://每日
					deckNewDataDTO.setGroupTitle(weekToChinese(deckNewDataDTO.getGroupValue()));
					break;
				default://每月
					deckNewDataDTO.setGroupTitle(deckNewDataDTO.getGroupValue()+"月");
			}
			totalDataDTO.setNewDigitedAmount(totalDataDTO.getNewDigitedAmount()+deckNewDataDTO.getNewDigitedAmount());
			totalDataDTO.setNewPageAmount(totalDataDTO.getNewPageAmount()+deckNewDataDTO.getNewPageAmount());
			totalDataDTO.setNewStatsAmount(totalDataDTO.getNewStatsAmount()+deckNewDataDTO.getNewStatsAmount());
			return deckNewDataDTO;
		}).collect(Collectors.toList());
		List<DeckNewDataDTO> resultItemList = Lists.newArrayList();
		switch (archiveDeckNew.getStatus()){
			case 100://每月
				final List<Integer> monthList = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12);
				for (Integer month:monthList) {
					DeckNewDataDTO deckNewDataDTO = itemList.stream().filter(t->t.getGroupValue()==month.intValue()).findFirst().orElse(null);
					if(deckNewDataDTO==null){
						deckNewDataDTO = new DeckNewDataDTO();
						deckNewDataDTO.setGroupTitle(month+"月");
						deckNewDataDTO.setGroupValue(month);
					}
					resultItemList.add(deckNewDataDTO);
				}
				break;
			case 10://每周
				final List<Integer> weekList = Arrays.asList(1,2,3,4,5);
				for (Integer week:weekList) {
					DeckNewDataDTO deckNewDataDTO = itemList.stream().filter(t->t.getGroupValue()==week.intValue()).findFirst().orElse(null);
					if(deckNewDataDTO==null){
						deckNewDataDTO = new DeckNewDataDTO();
						deckNewDataDTO.setGroupTitle("第"+week+"周");
						deckNewDataDTO.setGroupValue(week);
					}
					resultItemList.add(deckNewDataDTO);
				}
				break;
			case 1://每日
				final List<Integer> dayList = Arrays.asList(1,2,3,4,5,6,7);
				for (Integer day:dayList) {
					DeckNewDataDTO deckNewDataDTO = itemList.stream().filter(t->t.getGroupValue()==day.intValue()).findFirst().orElse(null);
					if(deckNewDataDTO==null){
						deckNewDataDTO = new DeckNewDataDTO();
						deckNewDataDTO.setGroupTitle(weekToChinese(day));
						deckNewDataDTO.setGroupValue(day);
					}
					resultItemList.add(deckNewDataDTO);
				}
				break;
			default://每月
		}
		result.setTotal(totalDataDTO);
		result.setList(resultItemList);
		log.info("结果：{}",JSONUtil.toJsonStr(result));
		return result;
	}
	/**
	 * 周 转换成中文周
	 * @param week
	 * @return
	 */
	public String weekToChinese(int week) {
		switch (week) {
			case 7:
				return "周日";
			case 1:
				return "周一";
			case 2:
				return "周二";
			case 3:
				return "周三";
			case 4:
				return "周四";
			case 5:
				return "周五";
			case 6:
				return "周六";
			default:
				return null;
		}
	}
	@Override
	public DeckStatDTO getTotalCountStatsData(String fondsCodes) {
		DeckStatDTO deckStatDTO = new DeckStatDTO();
		//这边统计了项目数量，案卷数量，卷内数量，文件数量，电子文件数量，文件大小  ， 和已整理>80的pdf 页数，与 已整理>80已数字化的pdf 页数
		TotalCountStatsDTO totalCountStatsDTO = Optional.ofNullable(archiveStatsService.getTotalCountStatsData(fondsCodes))
				.orElseGet(this::emptyTotalCountStatsDTO);
		BeanUtil.copyProperties(totalCountStatsDTO,deckStatDTO,true);
		// 设置归档数量 IF(ISNULL(sum(stats_amount)) ,0, sum(stats_amount)) statsAmount  这边的总数是去除30：待归档的数量
		deckStatDTO.setAuditedAmount( archiveStatsService.getAuditedAmount(fondsCodes) );
		deckStatDTO.showTitle();
		//重新计算数字化率 = 所有电子文件手动输入的页数总和 / 所有文件层条目的输入页数总和；
		log.info("对应的导航驾驶舱结果是:{}",JSONUtil.toJsonStr(deckStatDTO));
		return deckStatDTO;
	}

	private TotalCountStatsDTO emptyTotalCountStatsDTO() {
		TotalCountStatsDTO dto = new TotalCountStatsDTO();
		dto.setProjectAmount(0);
		dto.setFolderAmount(0);
		dto.setFileAmount(0);
		dto.setOneSingleAmount(0);
		dto.setDocumentAmount(0);
		dto.setFileSize(BigInteger.valueOf(0L));
		dto.setPageAmount(0);
		dto.setDigitedPageAmount(0);
		dto.setTotalArchive(0);
		return dto;
	}
}
