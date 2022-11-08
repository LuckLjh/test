/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service.impl</p>
 * <p>文件名:ArchiveStatsServiceImpl.java</p>
 * <p>创建时间:2020年11月5日 下午3:37:11</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.beust.jcommander.internal.Lists;
import com.cescloud.saas.archive.api.modular.archivemaintain.feign.RemoteMaintainService;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.datasource.dto.DynamicArchiveDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveInnerService;
import com.cescloud.saas.archive.api.modular.mainpage.dto.ToDoDTO;
import com.cescloud.saas.archive.api.modular.mainpage.feign.RemoteConstService;
import com.cescloud.saas.archive.api.modular.stats.constant.StatsConstants;
import com.cescloud.saas.archive.api.modular.stats.constant.StatsFilingTypeEnum;
import com.cescloud.saas.archive.api.modular.stats.constant.StatsTypeEnum;
import com.cescloud.saas.archive.api.modular.stats.dto.*;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveStats;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.stats.convert.StatsEntityConverter;
import com.cescloud.saas.archive.service.modular.stats.mapper.ArchiveStatsMapper;
import com.cescloud.saas.archive.service.modular.stats.service.AbstractAppStatsExecutor;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveStatsService;
import com.cescloud.saas.archive.service.modular.stats.service.StatsExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年11月5日
 */
@Slf4j
@Component("archiveStatsService")
public class ArchiveStatsServiceImpl extends AbstractAppStatsExecutor<ArchiveStatsMapper, ArchiveStats>
    implements ArchiveStatsService, StatsExecutorService {

    @Autowired
    @Qualifier("archiveStatsConverter")
    private StatsEntityConverter<ArchiveStats> converter;

    @Autowired
    private RemoteArchiveInnerService remoteArchiveInnerService;

    @Autowired
    private ArchiveTypeService archiveTypeService;

    @Autowired
    private TemplateTableService templateTableService;

    @Autowired
    private ArchiveTableService archiveTableService;

    @Autowired
	private RemoteConstService remoteConstService;

    @Autowired
	private RemoteMaintainService remoteMaintainService;

    @Override
    protected DynamicArchiveDTO getDynamicArchiveVo(StatsTaskDTO statsTaskDTO) {
        final DynamicArchiveDTO dynamicArchiveDTO = new DynamicArchiveDTO();
        dynamicArchiveDTO.setTableName(statsTaskDTO.getFilingTypeStatsDTO().getStorageLocate());

        final List<String> selectColumnList = new ArrayList<>(8);
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
            selectColumnList.add(FieldConstants.STATUS);
            selectColumnList.add(FieldConstants.FONDS_CODE);
            selectColumnList.add(FieldConstants.YEAR_CODE);
            selectColumnList.add(FieldConstants.RETENTION_PERIOD);
            selectColumnList.add("sum(1) as " + StatsConstants.STATS_AMOUNT);
            if (StatsFilingTypeEnum.FOLDER == statsTaskDTO.getStatsFilingTypeEnum()) {
                // 卷内总件数
                selectColumnList.add("sum(" + FieldConstants.ITEM_COUNT + ") as " + StatsConstants.FILE_AMOUNT);
            }
            // amount_of_pages: 案卷/文件为页数，音视频为时长
            selectColumnList.add("sum(" + FieldConstants.AMOUNT_OF_PAGES + ") as " + StatsConstants.PAGE_AMOUNT);
            // 已数字化页数
            selectColumnList.add("sum(" + FieldConstants.PDF_PAGE + ") as " + StatsConstants.DIGITED_PAGE_AMOUNT);
            // 已数字化数量
            selectColumnList.add(
                "sum(case when " + FieldConstants.PDF_PAGE + ">0 then 1 else 0 end) as "
                    + StatsConstants.DIGITED_AMOUNT);
        }
        dynamicArchiveDTO.setFilterColumn(selectColumnList);

        // 条件
        if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
            dynamicArchiveDTO.setWhere("d." + FieldConstants.IS_DELETE + "=" + BoolEnum.NO.getCode());
        } else {
            dynamicArchiveDTO.setWhere(FieldConstants.IS_DELETE + "=" + BoolEnum.NO.getCode());
        }

        // GROUP BY 字段
        final List<String> groupColumnList = new ArrayList<>(4);
        if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.PROJECT) {
            groupColumnList.add(FieldConstants.FONDS_CODE);
        } else if (statsTaskDTO.getStatsFilingTypeEnum() == StatsFilingTypeEnum.DOCUMENT) {
            groupColumnList.add("p." + FieldConstants.FONDS_CODE);
        } else {
            groupColumnList.add(FieldConstants.STATUS);
            groupColumnList.add(FieldConstants.FONDS_CODE);
            groupColumnList.add(FieldConstants.YEAR_CODE);
            groupColumnList.add(FieldConstants.RETENTION_PERIOD);
        }
        dynamicArchiveDTO.setGroups(groupColumnList);
        return dynamicArchiveDTO;
    }

    /**
     *
     * @see StatsExecutorService#saveStatsData(StatsTaskDTO,
     *      List)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
	public boolean saveStatsData(StatsTaskDTO statsTaskDTO, List<Map<String, Object>> statsData) {
		final FilingTypeStatsDTO filingTypeStatsDTO = statsTaskDTO.getFilingTypeStatsDTO();
		final String archiveTypeCode = filingTypeStatsDTO.getArchiveTypeCode();
		final List<ArchiveStats> list = list(
				Wrappers.<ArchiveStats> lambdaQuery().eq(ArchiveStats::getArchiveTypeCode, archiveTypeCode)
						.eq(ArchiveStats::getFilingType, statsTaskDTO.getStatsFilingTypeEnum().getCode()));

		final List<ArchiveStats> insertList = new ArrayList<ArchiveStats>(statsData.size());
		ArchiveStats entity = null;
		List<Long> ids =  new ArrayList<>();
		list.forEach(archiveStats -> ids.add(archiveStats.getId()));
		if (!ids.isEmpty()) {
			removeByIds(ids);
		}
		for (final Map<String, Object> data : statsData) {
			entity = converter.convert(statsTaskDTO, data);
				insertList.add(entity);
		}
		if (!insertList.isEmpty()) {
			saveBatch(insertList);
		}
		return true;
	}

    private Map<String, Long> idMap(List<ArchiveStats> list) {
        final Map<String, Long> idMap = new HashMap<String, Long>(list.size());
        for (final ArchiveStats entity : list) {
            idMap.put(toKey(entity), entity.getId());
        }

        return idMap;
    }

    private String toKey(ArchiveStats entity) {
        return converter.noneIfNull(entity.getStatus()) + ":"
            + converter.noneIfNull(entity.getFondsCode()) + ":"
            + converter.noneIfNull(entity.getYearCode()) + ":"
            + converter.noneIfNull(entity.getRetentionPeriod());
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.service.AbstractStatsExecutor#check(FilingTypeStatsDTO)
     */
    @Override
    protected boolean check(FilingTypeStatsDTO filingTypeStatsDTO) {

        return true;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.service.AbstractStatsExecutor#statsName()
     */
    @Override
    protected String statsName() {
        return "archive-data";
    }

    /**
     *
     * @see AbstractAppStatsExecutor#getArchiveTypeService()
     */
    @Override
    public ArchiveTypeService getArchiveTypeService() {
        return archiveTypeService;
    }

    /**
     *
     * @see AbstractAppStatsExecutor#getArchiveTableService()
     */
    @Override
    public ArchiveTableService getArchiveTableService() {
        return archiveTableService;
    }

    /**
     *
     * @see AbstractAppStatsExecutor#getTemplateTableService()
     */
    @Override
    protected TemplateTableService getTemplateTableService() {
        return this.templateTableService;
    }

    private TableStatsDataDTO<CollectionFilingTypeDataDTO> processCollectionStats(List<ArchiveStats> list, Integer type) {
        final TableStatsDataDTO<CollectionFilingTypeDataDTO> tableStatsDataDTO = new TableStatsDataDTO<CollectionFilingTypeDataDTO>();
        final List<StatsDataDTO<CollectionFilingTypeDataDTO>> data = new ArrayList<>();
        final Map<String, Integer> posMap = new HashMap<>(); // key为archiveTypeCode, vlaue为data中下标
        StatsDataDTO<CollectionFilingTypeDataDTO> statsDataDTO = null;
        //final StatsDataDTO<CollectionFilingTypeDataDTO> totalStatsDataDTO = new StatsDataDTO<CollectionFilingTypeDataDTO>(); //小计
        //CollectionFilingTypeDataDTO totalFilingTypeDataDTO;
        CollectionFilingTypeDataDTO filingTypeDataDTO;
        Integer pos;
        for (final ArchiveStats entity : list) {
            String statsTitle = StatsFilingTypeEnum.NONE.getName();
            if (StatsTypeEnum.COLLECTION_STATS_BY_RETENTION.getCode().equals(type)){
                statsTitle = entity.getRetentionPeriod();
                if (StatsFilingTypeEnum.NONE.getName().equals(statsTitle)) {
                    statsTitle = StatsFilingTypeEnum.NO_RETENTION_PERIOD.getName();
                }
            } else {
                statsTitle = entity.getYearCode();
                if (StatsFilingTypeEnum.NONE.getName().equals(statsTitle)) {
                    statsTitle = StatsFilingTypeEnum.NO_YEAR_CODE.getName();
                }
            }
            if (posMap.containsKey(entity.getArchiveTypeCode())) {
                pos = posMap.get(entity.getArchiveTypeCode());
                statsDataDTO = data.get(pos);
            } else {
                statsDataDTO = new StatsDataDTO<CollectionFilingTypeDataDTO>();
                posMap.put(entity.getArchiveTypeCode(), data.size());
                data.add(statsDataDTO);
                statsDataDTO.setShowTitle(entity.getArchiveTypeName());
            }
            if (StatsFilingTypeEnum.FOLDER.getCode().equals(entity.getFilingType())) {
                filingTypeDataDTO = statsDataDTO.folder(CollectionFilingTypeDataDTO.class);
                filingTypeDataDTO.addFileAmount(entity.getFileAmount());
                //totalFilingTypeDataDTO = totalStatsDataDTO.folder(CollectionFilingTypeDataDTO.class); // 小计
                //totalFilingTypeDataDTO.addFileAmount(entity.getFileAmount());
                tableStatsDataDTO.getFolderKeys().add(statsTitle);
            } else {
                filingTypeDataDTO = statsDataDTO.file(CollectionFilingTypeDataDTO.class);
                //totalFilingTypeDataDTO = totalStatsDataDTO.file(CollectionFilingTypeDataDTO.class); // 小计
                tableStatsDataDTO.getFileKeys().add(statsTitle);
            }
            filingTypeDataDTO.addPageAmount(entity.getPageAmount());
            filingTypeDataDTO.putStatsData(statsTitle, entity.getStatsAmount());
            // 小计
            //totalFilingTypeDataDTO.addPageAmount(entity.getPageAmount());
            //totalFilingTypeDataDTO.putStatsData(entity.getRetentionPeriod(), entity.getStatsAmount());
        }
        //data.add(totalStatsDataDTO); //小计

        tableStatsDataDTO.setData(sortByArchiveType(data, posMap));

        return tableStatsDataDTO;
    }

    private List<StatsDataDTO<CollectionFilingTypeDataDTO>> sortByArchiveType(
            List<StatsDataDTO<CollectionFilingTypeDataDTO>> data, Map<String, Integer> posMap) {
        final List<StatsDataDTO<CollectionFilingTypeDataDTO>> sortData = new ArrayList<>();

        final LambdaQueryWrapper<ArchiveType> lambdaQueryWrapper = Wrappers.lambdaQuery(ArchiveType.class)
            .select(ArchiveType::getTypeCode, ArchiveType::getSortNo).orderByAsc(ArchiveType::getSortNo);
        final List<ArchiveType> typeList = archiveTypeService.list(lambdaQueryWrapper);

        for (final ArchiveType type : typeList) {
            if (posMap.containsKey(type.getTypeCode())) {
                sortData.add(data.get(posMap.get(type.getTypeCode())));
            }
        }

        return sortData;
    }

    /**
     *
     * @see AbstractAppStatsExecutor#getRemoteArchiveInnerService()
     */
    @Override
    public RemoteArchiveInnerService getRemoteArchiveInnerService() {
        return remoteArchiveInnerService;
    }

    /**
     *
     * @see ArchiveStatsService#getCollectionTotalStats()
     */
    @Override
    public List<CollectionChartQueryDTO> getCollectionTotalStats(String fondsCodes) {
        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }

        return getBaseMapper().getCollectionTotalStats(fondsCodeList);
    }

	private CollectionChartStatsDTO toCollectionTotal(List<CollectionChartQueryDTO> collectionChartQueryDataList) {
		final CollectionChartStatsDTO statsDTO = new CollectionChartStatsDTO();
		final List<CollectionTotalDTO> list = new ArrayList<>();
		final Map<String, Integer> posMap = new HashMap<>();
		for (final CollectionChartQueryDTO stats : collectionChartQueryDataList) {
			if (!posMap.containsKey(stats.getArchiveTypeCode())) {
				posMap.put(stats.getArchiveTypeCode(), list.size());
				final CollectionTotalDTO collectionTotalDTO = new CollectionTotalDTO();
				collectionTotalDTO.setShowTitle(stats.getArchiveTypeName());
				list.add(collectionTotalDTO);
			}
			CollectionTotalDTO collectionTotalDTO = list.get(posMap.get(stats.getArchiveTypeCode()));
			collectionTotalDTO.putStatsData(stats.getArchiveTypeName(), stats.getStatsTitle(), stats.getStatsAmount());
			if (StringUtils.isEmpty(stats.getStatsTitle())){
				statsDTO.getKeys().add("none");
			}else{
				statsDTO.getKeys().add(stats.getStatsTitle());
			}
		}
		statsDTO.setData(list);
		return statsDTO;
	}

	private CollectionChartStatsDTO toCollectionTotalDTOs(List<CollectionChartQueryDTO> collectionChartQueryDataList) {
        final CollectionChartStatsDTO statsDTO = new CollectionChartStatsDTO();
        final List<CollectionTotalDTO> list = new ArrayList<>();
        final Map<String, Integer> posMap = new HashMap<>();
        for (final CollectionChartQueryDTO stats : collectionChartQueryDataList) {
            if (!posMap.containsKey(stats.getArchiveTypeCode())) {
                posMap.put(stats.getArchiveTypeCode(), list.size());
	            final CollectionTotalDTO collectionTotalDTO = new CollectionTotalDTO();
	            collectionTotalDTO.setShowTitle(stats.getArchiveTypeName());
                list.add(collectionTotalDTO);
            }
            if (StatsFilingTypeEnum.FOLDER.getCode().equals(stats.getFilingType())) {
                putFolderCollectionTotalDTO(stats, list.get(posMap.get(stats.getArchiveTypeCode())));
            } else {
                putOneCollectionTotalDTO(stats, list.get(posMap.get(stats.getArchiveTypeCode())));
            }
			if (StringUtils.isEmpty(stats.getStatsTitle())){
				statsDTO.getKeys().add("none");
			}else{
				statsDTO.getKeys().add(stats.getStatsTitle());
			}
        }

        statsDTO.setData(list);

        return statsDTO;
    }


    private CollectionTotalDTO putFolderCollectionTotalDTO(CollectionChartQueryDTO stats,
                                                           CollectionTotalDTO collectionTotalDTO) {
        collectionTotalDTO.putStatsData(ArchiveLayerEnum.FILE.getName(), stats.getStatsTitle(),
            stats.getFileAmount());
        collectionTotalDTO.putStatsData(ArchiveLayerEnum.FOLDER.getName(), stats.getStatsTitle(),
            stats.getStatsAmount());

        return collectionTotalDTO;
    }

    private CollectionTotalDTO putOneCollectionTotalDTO(CollectionChartQueryDTO stats,
                                                        CollectionTotalDTO collectionTotalDTO) {
        collectionTotalDTO.putStatsData(ArchiveLayerEnum.ONE.getName(), stats.getStatsTitle(),
            stats.getStatsAmount());

        return collectionTotalDTO;
    }

    /**
     *
     * @see ArchiveStatsService#getCollectionFeildStats(String,
     *      String, Integer)
     */
    @Override
    public CollectionChartStatsDTO getCollectionFeildStats(String statsField, String fondsCodes, Integer filingType) {
        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }
        final List<CollectionChartQueryDTO> collectionChartQueryDataList = getBaseMapper().getCollectionFeildStats(statsField, fondsCodeList, filingType);
        collectionChartQueryDataList.stream().filter(e -> e.getStatsTitle().equals(StatsFilingTypeEnum.NONE.getName()))
            .forEach(e -> {
                if (FieldConstants.RETENTION_PERIOD.equals(statsField)) {
                    e.setStatsTitle(StatsFilingTypeEnum.NO_RETENTION_PERIOD.getName());
                } else if (FieldConstants.YEAR_CODE.equals(statsField)) {
                    e.setStatsTitle(StatsFilingTypeEnum.NO_YEAR_CODE.getName());
                }
            });

        return toCollectionTotal(collectionChartQueryDataList);
    }

    /**
     *
     * @see ArchiveStatsService#getCollectionTableStats(String,
     *      Integer)
     */
    @Override
    public TableStatsDataDTO<CollectionFilingTypeDataDTO> getCollectionTableStats(String fondsCodes,
                                                                                  Integer filingType) {
        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }
        final List<ArchiveStats> statsList = getBaseMapper().getCollectionTableStats(fondsCodeList, filingType);
        return processCollectionStats(statsList, StatsTypeEnum.COLLECTION_STATS_BY_RETENTION.getCode());
    }

	@Override
	public TableStatsDataDTO<CollectionFilingTypeDataDTO> getCollectionTableStatsByYearCode(String fondsCodes, Integer filingType) {
		List<String> fondsCodeList = CollectionUtil.newArrayList();
		if(StrUtil.isNotBlank(fondsCodes)){
			fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
		}
		final List<ArchiveStats> statsList = getBaseMapper().getCollectionTableStatsByYearCode(fondsCodeList, filingType);
		return processCollectionStats(statsList,StatsTypeEnum.COLLECTION_STATS_BY_YEAR.getCode());
	}

	/**
     *
     * @see ArchiveStatsService#getDestroyChartStats(String)
     */
    @Override
    public List<DestroyStatsChartDTO> getDestroyChartStats(String fondsCodes) {
        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }
        return getBaseMapper().getDestroyChartStats(fondsCodeList);
    }

    /**
     *
     * @see ArchiveStatsService#getDestroyTableStats(String)
     */
    @Override
    public List<DestroyStatsTableDTO> getDestroyTableStats(String fondsCodes) {
        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }
        final List<ArchiveStats> destroyTableStats = getBaseMapper().getDestroyTableStats(fondsCodeList);
        return processDestroyStats(destroyTableStats);
    }

    private List<DestroyStatsTableDTO> processDestroyStats(List<ArchiveStats> list) {
        final List<DestroyStatsTableDTO> statsDataList = new ArrayList<DestroyStatsTableDTO>();

        final Map<String, Integer> posMap = new HashMap<String, Integer>();
        Integer pos;
        for (final ArchiveStats entity : list) {

            if (!posMap.containsKey(entity.getYearCode())) {
                posMap.put(entity.getYearCode(), statsDataList.size());
                statsDataList.add(newDestroyStatsTableDTO(entity));
            }
            pos = posMap.get(entity.getYearCode());

            statsDataList.get(pos).putStatsData(entity.getArchiveTypeName(), entity.getStatsAmount());
        }

        return statsDataList;
    }

    private DestroyStatsTableDTO newDestroyStatsTableDTO(ArchiveStats entity) {
        final DestroyStatsTableDTO destroyStatsTableDTO = new DestroyStatsTableDTO();
        destroyStatsTableDTO.setStatsTitle(entity.getYearCode());
        return destroyStatsTableDTO;
    }

    /**
     *
     * @see ArchiveStatsService#getDigitTableFolderStats(String,
     *      Integer, String)
     */
    @Override
    public TableStatsDataDTO<DigitTableFilingTypeDataDTO> getDigitTableFolderStats(String fondsCodes,
                                                                                   Integer filingType, String yearCode) {
        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }
        final List<ArchiveStats> digitStats = getBaseMapper().getDigitTableFolderStats(fondsCodeList, filingType, yearCode);
        return processDigitFolderStats(digitStats);
    }

    private TableStatsDataDTO<DigitTableFilingTypeDataDTO> processDigitFolderStats(List<ArchiveStats> list) {

        final TableStatsDataDTO<DigitTableFilingTypeDataDTO> tableStatsDataDTO = new TableStatsDataDTO<DigitTableFilingTypeDataDTO>();

        final List<StatsDataDTO<DigitTableFilingTypeDataDTO>> data = new ArrayList<StatsDataDTO<DigitTableFilingTypeDataDTO>>();
        final Map<String, Integer> posMap = new HashMap<>(); // key为archiveTypeCode, vlaue为data中下标
        StatsDataDTO<DigitTableFilingTypeDataDTO> statsDataDTO = null;
        DigitTableFilingTypeDataDTO filingTypeDataDTO;
        Integer pos;
        for (final ArchiveStats entity : list) {
            String statsTitle = entity.getRetentionPeriod();
            if (StatsFilingTypeEnum.NONE.getName().equals(statsTitle)) {
                statsTitle = StatsFilingTypeEnum.NO_RETENTION_PERIOD.getName();
            }
            if (posMap.containsKey(entity.getArchiveTypeCode())) {
                pos = posMap.get(entity.getArchiveTypeCode());
                statsDataDTO = data.get(pos);
            } else {
                statsDataDTO = new StatsDataDTO<DigitTableFilingTypeDataDTO>();
                posMap.put(entity.getArchiveTypeCode(), data.size());
                data.add(statsDataDTO);
                statsDataDTO.setShowTitle(entity.getArchiveTypeName());
            }
            if (StatsFilingTypeEnum.FOLDER.getCode().equals(entity.getFilingType())) {
                filingTypeDataDTO = statsDataDTO.folder(DigitTableFilingTypeDataDTO.class);
                tableStatsDataDTO.getFolderKeys().add(statsTitle);
            } else {
                filingTypeDataDTO = statsDataDTO.file(DigitTableFilingTypeDataDTO.class);
                tableStatsDataDTO.getFileKeys().add(statsTitle);
            }
            filingTypeDataDTO.addDigitAmount(entity.getDigitedAmount());
            filingTypeDataDTO.putStatsData(statsTitle, entity.getStatsAmount());
        }

        tableStatsDataDTO.setData(data);

        return tableStatsDataDTO;
    }

    /**
     *
     * @see ArchiveStatsService#getDigitTablePageStats(String,
     *      Integer, String)
     */
    @Override
    public TableStatsDataDTO<DigitTableFilingTypeDataDTO> getDigitTablePageStats(String fondsCodes, Integer filingType,
                                                                                 String yearCode) {
        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }
        final List<ArchiveStats> digitStats = getBaseMapper().getDigitTablePageStats(fondsCodeList, filingType, yearCode);
        return processDigitPageStats(digitStats);
    }

    private TableStatsDataDTO<DigitTableFilingTypeDataDTO> processDigitPageStats(List<ArchiveStats> list) {

        final TableStatsDataDTO<DigitTableFilingTypeDataDTO> tableStatsDataDTO = new TableStatsDataDTO<DigitTableFilingTypeDataDTO>();

        final List<StatsDataDTO<DigitTableFilingTypeDataDTO>> data = new ArrayList<StatsDataDTO<DigitTableFilingTypeDataDTO>>();
        final Map<String, Integer> posMap = new HashMap<>(); // key为archiveTypeCode, vlaue为data中下标
        StatsDataDTO<DigitTableFilingTypeDataDTO> statsDataDTO = null;
        DigitTableFilingTypeDataDTO filingTypeDataDTO;
        Integer pos;
        for (final ArchiveStats entity : list) {
            String statsTitle = entity.getRetentionPeriod();
            if (StatsFilingTypeEnum.NONE.getName().equals(statsTitle)) {
                statsTitle = StatsFilingTypeEnum.NO_RETENTION_PERIOD.getName();
            }
            if (posMap.containsKey(entity.getArchiveTypeCode())) {
                pos = posMap.get(entity.getArchiveTypeCode());
                statsDataDTO = data.get(pos);
            } else {
                statsDataDTO = new StatsDataDTO<DigitTableFilingTypeDataDTO>();
                posMap.put(entity.getArchiveTypeCode(), data.size());
                data.add(statsDataDTO);
                statsDataDTO.setShowTitle(entity.getArchiveTypeName());
            }
            if (StatsFilingTypeEnum.FOLDER.getCode().equals(entity.getFilingType())) {
                filingTypeDataDTO = statsDataDTO.folder(DigitTableFilingTypeDataDTO.class);
                filingTypeDataDTO.addFileAmount(entity.getFileAmount());
                tableStatsDataDTO.getFolderKeys().add(statsTitle);
            } else {
                filingTypeDataDTO = statsDataDTO.file(DigitTableFilingTypeDataDTO.class);
                tableStatsDataDTO.getFileKeys().add(statsTitle);
            }
            filingTypeDataDTO.addPageAmount(entity.getPageAmount());
            filingTypeDataDTO.addDigitAmount(entity.getDigitedPageAmount());
            filingTypeDataDTO.putStatsData(statsTitle, entity.getStatsAmount());
        }

        tableStatsDataDTO.setData(data);

        return tableStatsDataDTO;
    }

    /**
     *
     * @see ArchiveStatsService#getDigitChartFolderStats(String,
     *      Integer, String, String)
     */
    @Override
    public List<DigitChartStatsDTO> getDigitChartFolderStats(String fondsCodes, Integer filingType, String yearCode,
                                                             String retentionPeriod) {
        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }
        return getBaseMapper().getDigitChartFolderStats(fondsCodeList, filingType, yearCode, retentionPeriod);
    }

    /**
     *
     * @see ArchiveStatsService#getDigitChartPageStats(String,
     *      Integer, String, String)
     */
    @Override
    public List<DigitChartStatsDTO> getDigitChartPageStats(String fondsCodes, Integer filingType, String yearCode,
                                                           String retentionPeriod) {
        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }
        return getBaseMapper().getDigitChartPageStats(fondsCodeList, filingType, yearCode, retentionPeriod);
    }

    /**
     *
     * @see ArchiveStatsService#getYearStats(String,
     *      String)
     */
    @Override
    public Map<String, Integer> getYearStats(String fondsCodes, String yearCode) {

        Assert.hasText(fondsCodes, "全宗号不能为空");
        Assert.hasText(yearCode, "年度不能为空");

        List<String> fondsCodeList = CollectionUtil.newArrayList();
        if(StrUtil.isNotBlank(fondsCodes)){
            fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
        }
        // 统计全宗所有数据
        final Map<String, Integer> yearStatsMap = processYearStats(fondsCodeList);
        // 统计当前年度下数据
        yearStatsMap.putAll(processYearStats(fondsCodeList, yearCode));

        return yearStatsMap;
    }

    private Map<String, Integer> processYearStats(List<String> fondsCodes) {
        /*
         *   名称                  单位          位置     说明
         * 1. 案卷                 卷           28      档案类型为案卷的数量
         * 2. 以件为保管单位档案        件           29      档案类型为一文一件的数量 包含（单套制）
         *
         * 3. 室存永久、30年（长期）档案  卷           32      档案类型为案卷、保管期限为永久、30年（长期）的档案
         * 4.                     件           33      档案类型为一文一件、保管期限为永久、30年（长期）的档案
         *
         * 5. 永久保管               卷           34      档案类型为案卷、保管期限为永久的档案
         * 6.                     件           35      档案类型为一文一件、保管期限为永久的档案
         */
        final List<YearStatsQueryDTO> yearStatsTotal = getBaseMapper().getYearStatsTotal(fondsCodes);
        int amount28 = 0, amount29 = 0;
        int amount32 = 0, amount33 = 0;
        int amount34 = 0, amount35 = 0;
        for (final YearStatsQueryDTO stats : yearStatsTotal) {
            if (StatsFilingTypeEnum.FOLDER.getCode().equals(stats.getFilingType())) { // 卷
                amount28 += stats.getStatsAmount();
                if (RetentionPeriodEnum.YJ.getName().equals(stats.getRetentionPeriod())) {
                    amount32 += stats.getStatsAmount();
                    amount34 += stats.getStatsAmount();
                } else if (RetentionPeriodEnum.CQ.getName().equals(stats.getRetentionPeriod())
                    || RetentionPeriodEnum.SS.getName().equals(stats.getRetentionPeriod())) {
                    amount32 += stats.getStatsAmount();
                }
            } else { // 件
                amount29 += stats.getStatsAmount();
                if (RetentionPeriodEnum.YJ.getName().equals(stats.getRetentionPeriod())) {
                    amount33 += stats.getStatsAmount();
                    amount35 += stats.getStatsAmount();
                } else if (RetentionPeriodEnum.CQ.getName().equals(stats.getRetentionPeriod())
                    || RetentionPeriodEnum.SS.getName().equals(stats.getRetentionPeriod())) {
                    amount33 += stats.getStatsAmount();
                }
            }
        }

        return MapUtil.builder("28", amount28).put("29", amount29)
            .put("32", amount32).put("33", amount33)
            .put("34", amount34).put("35", amount35)
            .build();
    }

    private Map<String, Integer> processYearStats(List<String> fondsCodes, String yearCode) {
        /*
         *   名称                  单位          位置     说明
         * 7. 案卷                 卷           57      本年度档案类型为案卷的数量
         * 8. 以件为保管单位档案        件           58      本年度档案类型为一文一件的数量
         */
        final List<YearStatsQueryDTO> yearStatsYear = getBaseMapper().getYearStatsYear(fondsCodes, yearCode);
        int amount57 = 0, amount58 = 0;
        for (final YearStatsQueryDTO stats : yearStatsYear) {
            if (StatsFilingTypeEnum.FOLDER.getCode().equals(stats.getFilingType())) {
                amount57 += stats.getStatsAmount();
            } else {
                amount58 += stats.getStatsAmount();
            }
        }

        return MapUtil.builder("57", amount57).put("58", amount58)
            .build();
    }

    /**
     *
     * @see ArchiveStatsService#getTotalCountStatsData(String)
     */
    @Override
    public TotalCountStatsDTO getTotalCountStatsData(String fondsCodes) {
		List<String> fondsCodeList = CollectionUtil.newArrayList();
		if (StrUtil.isNotBlank(fondsCodes)) {
			fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
		}
		return getBaseMapper().getTotalCountStats(fondsCodeList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeByTenantId(Long tenantId){
        return getBaseMapper().removeByTenantId(tenantId);
    }


	@Override
	public int getAuditedAmount(String fondsCodes) {
		int auditedAmount = 0 ;
		List<String> fondsCodeList = CollectionUtil.newArrayList();
		if(StrUtil.isNotBlank(fondsCodes)){
			fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
		}
		List<String> statusList = Lists.newArrayList("50","60","61","62","80","81","90","100","110","120","130","140","150","160");
		QueryWrapper<ArchiveStats> queryWrapper = new QueryWrapper<>();
		queryWrapper.in("status",statusList);
		if(CollUtil.isNotEmpty(fondsCodeList)){
			queryWrapper.in("fonds_code",fondsCodeList);
		}
//		if(tenantId!=null){
//			queryWrapper.eq("tenant_id",tenantId);
//		}
		queryWrapper.select("IF(ISNULL(sum(stats_amount)) ,0, sum(stats_amount)) statsAmount");

		List<ArchiveStats> resultArchiveStatsList = this.baseMapper.selectList(queryWrapper);
		log.info("结果是:{}", JSONUtil.toJsonStr(resultArchiveStatsList));
		if(resultArchiveStatsList.size()>0){
			auditedAmount = resultArchiveStatsList.get(0)==null ? 0 : resultArchiveStatsList.get(0).getStatsAmount();
		}

		return auditedAmount;
	}



	@Override
	public List<ToDoDTO> getStatusAmount(String fondsCodes) throws ArchiveBusinessException, InterruptedException {
		cn.hutool.core.lang.Assert.isTrue(StrUtil.isNotBlank(fondsCodes),"全宗编码不可为空!!!");
		List<ToDoDTO> list = Lists.newArrayList();
		/**
		 * 档案维护
		 * 归档接收
		 * 整理归档
		 * 文件管理
		 */
		String menuIds = "52011231,5202391,12032,12031";
		R<List<ToDoDTO>> t = remoteConstService.getToDoList(fondsCodes, menuIds);
		Integer count = 0;
		if (null != t && CollUtil.isNotEmpty(t.getData())){
			list= t.getData().stream().filter(item -> item.getName().equals(ArchiveConstants.STATUS.FILING) ||
					item.getName().equals(ArchiveConstants.STATUS.RECEIVING) ||
					item.getName().equals(ArchiveConstants.STATUS.TIDYING)).collect(Collectors.toList());

			R  r = remoteMaintainService.getCount(fondsCodes);
			if (null != r && null != r.getData()){
				Integer countR =(Integer) r.getData();
				count += countR;
			}
			ToDoDTO dto = ToDoDTO.builder().moduleId(null).name("已整理").number(count).status(null).build();
			list.add(dto);
		}
		return list;
	}
}
