/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service.impl</p>
 * <p>文件名:FilingStatsServiceImpl.java</p>
 * <p>创建时间:2020年10月14日 下午2:56:01</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.datasource.dto.DynamicArchiveDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveInnerService;
import com.cescloud.saas.archive.api.modular.dept.feign.RemoteDeptService;
import com.cescloud.saas.archive.api.modular.stats.constant.ExcelConstants;
import com.cescloud.saas.archive.api.modular.stats.constant.StatsConstants;
import com.cescloud.saas.archive.api.modular.stats.constant.StatsFilingTypeEnum;
import com.cescloud.saas.archive.api.modular.stats.constant.StatsTypeEnum;
import com.cescloud.saas.archive.api.modular.stats.dto.CollectionFilingTypeDataDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.DeckNewTotalDataDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.DeckTotalStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.DestroyStatsTableDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.DigitTableFilingTypeDataDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.FilingStatsDataDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.FilingTypeStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.StatsDataDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.StatsTaskDTO;
import com.cescloud.saas.archive.api.modular.stats.dto.TableStatsDataDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.FilingStats;
import com.cescloud.saas.archive.common.constants.ArchiveStatusEnum;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.common.constants.FilingTypeEnum;
import com.cescloud.saas.archive.common.util.ArchiveTableUtil;
import com.cescloud.saas.archive.common.util.CesFileUtil;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.easyexcel.ExcelFillCellMergeStrategy;
import com.cescloud.saas.archive.service.modular.common.easyexcel.ExcelUtils;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.stats.convert.StatsEntityConverter;
import com.cescloud.saas.archive.service.modular.stats.mapper.FilingStatsMapper;
import com.cescloud.saas.archive.service.modular.stats.service.AbstractAppStatsExecutor;
import com.cescloud.saas.archive.service.modular.stats.service.ArchiveStatsService;
import com.cescloud.saas.archive.service.modular.stats.service.FilingStatsService;
import com.cescloud.saas.archive.service.modular.stats.service.StatsExecutorService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author qiucs
 * @version 1.0.0 2020年10月14日
 */
@Component("filingStatsService")
@Slf4j
public class FilingStatsServiceImpl extends AbstractAppStatsExecutor<FilingStatsMapper, FilingStats>
		implements FilingStatsService, StatsExecutorService {

	@Autowired
	@Qualifier("filingStatsConverter")
	private StatsEntityConverter<FilingStats> converter;

	@Autowired
	private ArchiveTableService archiveTableService;

	@Autowired
	private ArchiveTypeService archiveTypeService;

	@Autowired
	private TemplateTableService templateTableService;

	@Autowired
	private RemoteArchiveInnerService remoteArchiveInnerService;

	@Autowired
	private RemoteDeptService remoteDeptService;

	@Autowired
	private ArchiveStatsService archiveStatsService;

	/**
     * @see FilingStatsService#getStatsDataGroupByArchiveType(String,
     * String)
	 */
	@Override
	public List<FilingStatsDataDTO> getStatsDataGroupByArchiveType(String fondsCodes, String yearCode) {
		List<String> fondsCodeList = CollectionUtil.newArrayList();
		if (StrUtil.isNotBlank(fondsCodes)) {
			fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
		}
		return getBaseMapper().getStatsData(StatsConstants.ARCHIVE_TYPE_NAME, fondsCodeList, yearCode);
	}

	/**
     * @see FilingStatsService#getStatsDataGroupByFilingDept(String,
     * String)
	 */
	@Override
	public List<FilingStatsDataDTO> getStatsDataGroupByFilingDept(String fondsCodes, String yearCode) {
		List<String> fondsCodeList = CollectionUtil.newArrayList();
		if (StrUtil.isNotBlank(fondsCodes)) {
			fondsCodeList = Arrays.asList(fondsCodes.split(",")).stream().collect(Collectors.toList());
		}
		final List<FilingStatsDataDTO> statsData = getBaseMapper().getStatsData(FieldConstants.FILING_DEPT_ID,
				fondsCodeList, yearCode);
		processStats(statsData);
		return statsData;
	}

	private void processStats(List<FilingStatsDataDTO> list) {
		final StringBuilder sb = new StringBuilder();
		list.forEach(data -> {
			sb.append(data.getStatsTitle()).append(",");
		});
		if (StrUtil.isNotBlank(sb.toString())) {
			sb.setLength(sb.length() - 1);
		}
		final R<Map<Long, String>> remoteData = remoteDeptService.getDeptIdNameMap(sb.toString());
		if (!CommonConstants.SUCCESS.equals(remoteData.getCode())) {
			if (log.isErrorEnabled()) {
				log.error("获取部门名称出错：{}", remoteData.getMsg());
			}
			throw new ArchiveRuntimeException("获取部门名称出错");
		}
		final Map<Long, String> idNameMap = remoteData.getData();
		list.forEach(data -> {
			final Long deptId = Long.valueOf(data.getStatsTitle());
			if (0 == deptId) {
				data.setStatsTitle(StatsFilingTypeEnum.NO_DEPT.getName());
			} else {
				if (idNameMap.containsKey(deptId)) {
					data.setStatsTitle(idNameMap.get(deptId));
				} else {
					data.setStatsTitle("部门不存在（" + deptId + "）");
				}
			}
		});
	}

	@Override
	protected DynamicArchiveDTO getDynamicArchiveVo(StatsTaskDTO statsTaskDTO) {
		final DynamicArchiveDTO dynamicArchiveDTO = new DynamicArchiveDTO();
		final String storageLocate = statsTaskDTO.getFilingTypeStatsDTO().getStorageLocate();
		if (ArchiveTableUtil.isArchiveDocumentTable(storageLocate)) {
			return dynamicArchiveDTO;
		}
		dynamicArchiveDTO.setTableName(storageLocate);
		final List<String> selectColumnList = new ArrayList<>(8);
		selectColumnList.add(FieldConstants.FONDS_CODE);
		selectColumnList.add(FieldConstants.YEAR_CODE);
		selectColumnList.add(statsTaskDTO.getStatsField() + " as " + StatsConstants.STATS_TITLE);
		selectColumnList.add("sum(1) as " + StatsConstants.STATS_AMOUNT);
		selectColumnList.add(FieldConstants.RETENTION_PERIOD);
		dynamicArchiveDTO.setFilterColumn(selectColumnList);

		// filing_amount: 已归档（status >= 50）
		dynamicArchiveDTO.setWhere(
				FieldConstants.IS_DELETE + "=" + BoolEnum.NO.getCode() + " and " + FieldConstants.STATUS + ">="
						+ ArchiveStatusEnum.RECEIVING.getValue());

		final List<String> groupColumnList = new ArrayList<>(4);
		groupColumnList.add(FieldConstants.FONDS_CODE);
		groupColumnList.add(FieldConstants.YEAR_CODE);
		groupColumnList.add(statsTaskDTO.getStatsField());
		dynamicArchiveDTO.setGroups(groupColumnList);
		return dynamicArchiveDTO;
	}

	/**
	 * @see com.cescloud.saas.archive.service.modular.stats.service.AbstractStatsExecutor#getStatsFields()
	 */
	@Override
	protected String[] getStatsFields() {
		return new String[]{FieldConstants.FILING_DEPT_ID};
	}

	/**
	 * @see com.cescloud.saas.archive.service.modular.stats.service.AbstractStatsExecutor#statsName()
	 */
	@Override
	protected String statsName() {
		return "filing";
	}

	/**
     * @see StatsExecutorService#saveStatsData(StatsTaskDTO,
     * List)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
	public boolean saveStatsData(StatsTaskDTO statsTaskDTO, List<Map<String, Object>> statsData) {
		final FilingTypeStatsDTO filingTypeStatsDTO = statsTaskDTO.getFilingTypeStatsDTO();
		final String archiveTypeCode = filingTypeStatsDTO.getArchiveTypeCode();
		final List<FilingStats> list = list(
				Wrappers.<FilingStats>lambdaQuery().eq(FilingStats::getTenantId, statsTaskDTO.getTenantId())
						.eq(FilingStats::getArchiveTypeCode, archiveTypeCode));
		List<Long> ids =  new ArrayList<>();
		list.forEach(filingStats -> ids.add(filingStats.getId()));
		if (!ids.isEmpty()) {//删除老数据
			this.removeByIds(ids);
		}
		final List<FilingStats> insertList = new ArrayList<FilingStats>(statsData.size());
		FilingStats entity = null;
		for (final Map<String, Object> data : statsData) {
			entity = converter.convert(statsTaskDTO, data);
				insertList.add(entity);
		}
		if (!insertList.isEmpty()) {
			saveBatch(insertList);
		}
		return true;
	}

	private Map<Long, String> idMap(List<FilingStats> list) {
		final Map<Long, String> idMap = new HashMap<>(list.size());
		for (final FilingStats entity : list) {
			idMap.put( entity.getId(),toKey(entity));
		}
		return idMap;
	}

	private String toKey(FilingStats entity) {
		return converter.noneIfNull(entity.getFondsCode()) + ":"
				+ converter.noneIfNull(entity.getYearCode() + ":" + converter.longZeroIfNull(entity.getFilingDeptId()));
	}

	/**
     * @see AbstractAppStatsExecutor#getArchiveTypeService()
	 */
	@Override
	public ArchiveTypeService getArchiveTypeService() {
		return archiveTypeService;
	}

	/**
     * @see AbstractAppStatsExecutor#getArchiveTableService()
	 */
	@Override
	public ArchiveTableService getArchiveTableService() {
		return archiveTableService;
	}

	/**
     * @see AbstractAppStatsExecutor#getRemoteArchiveInnerService()
	 */
	@Override
	public RemoteArchiveInnerService getRemoteArchiveInnerService() {
		return remoteArchiveInnerService;
	}

	/**
     * @see com.cescloud.saas.archive.service.modular.stats.service.AbstractStatsExecutor#check(FilingTypeStatsDTO)
	 */
	@Override
	protected boolean check(FilingTypeStatsDTO filingTypeStatsDTO) {
		// 过滤掉 单套制 档案门类
		return !FilingTypeEnum.SINGLE.getCode().equals(filingTypeStatsDTO.getArchiveTypeFilingType());
	}

	/**
     * @see AbstractAppStatsExecutor#getTemplateTableService()
	 */
	@Override
	protected TemplateTableService getTemplateTableService() {
		return this.templateTableService;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeByTenantId(Long tenantId) {
		return getBaseMapper().removeByTenantId(tenantId);
	}

	@Override
    public List<FilingStats> filingStatsGroupByTypeCodeWithFonds(String fondsCode){
		LambdaQueryWrapper<FilingStats> lambdaQueryWrapper = Wrappers.<FilingStats>query().lambda();
		if(StrUtil.isNotBlank(fondsCode)){
			lambdaQueryWrapper.eq(FilingStats::getFondsCode,fondsCode);
		}
		lambdaQueryWrapper.groupBy(FilingStats::getArchiveTypeCode);
		lambdaQueryWrapper.orderByAsc(FilingStats::getArchiveTypeCode);
		return this.list(lambdaQueryWrapper);
	}

	@Override
	public Map<String, Object> deckShowTypeCodeForDept(int pageSize, int pageNumber, String fondsCode,String code){
//		int pageSize = 4;// 每次最多查询阿里 100条
		Map<String, Object> mapResult = Maps.newHashMap();
		pageNumber -= 1;
		List<FilingStats> groupByTypeCodeList = this.filingStatsGroupByTypeCodeWithFonds(fondsCode);
		List<String> typeCodeList = Lists.newArrayList();
		int total = groupByTypeCodeList.size();
		int totalPage = PageUtil.totalPage(total, pageSize);//总页码
		if(pageNumber+1>=totalPage){
			log.warn("********超出页码限制！！！*******");
		}
		int[] startEnd = PageUtil.transToStartEnd(pageNumber, pageSize);//[10, 20];
		for (int index = startEnd[0] ; index< startEnd[1];index++) {
			if(index+1<=total){
				FilingStats currentTypeCodeInfo = groupByTypeCodeList.get(index);
				typeCodeList.add(currentTypeCodeInfo.getArchiveTypeCode());
			}
		}
		List<ArchiveType> archiveTypeList = this.archiveTypeService.getArchiveTypes();
		if(code.equals("dept") || code.equals("")){
			mapResult = deckShowTypeCodeGroupByDept(archiveTypeList,fondsCode,typeCodeList,total,totalPage);
		}else if (code.equals("year")){
			mapResult = deckShowTypeCodeGroupByYearCode(archiveTypeList,fondsCode,typeCodeList,total,totalPage);
		}else if (code.equals("retentionPeriod")){
			mapResult = deckShowTypeCodeGroupByRetentionPeriod(archiveTypeList,fondsCode,typeCodeList,total,totalPage);
		}
		return  mapResult;
	}

	private Map<String, Object> deckShowTypeCodeGroupByDept(List<ArchiveType> archiveTypeList, String fondsCode, List<String> typeCodeList, int total, int totalPage) {
		List<DeckTotalStatsDTO<DeckNewTotalDataDTO, List<FilingStats>>> result = Lists.newArrayList();
    	Map<String, Object> mapResult = Maps.newHashMap();
    	QueryWrapper<FilingStats> queryWrapper = new QueryWrapper<>();
		if(StrUtil.isNotBlank(fondsCode)){
			queryWrapper.eq("fonds_code",fondsCode);
		}
		if(CollUtil.isNotEmpty(typeCodeList)){
			queryWrapper.in("archive_type_code",typeCodeList);
		}
		queryWrapper.groupBy("filing_dept_id","archive_type_code");
		queryWrapper.orderByAsc("filing_dept_id","archive_type_code");
		queryWrapper.select("filing_dept_id filingDeptId","archive_type_code archiveTypeCode","sum(stats_amount) statsAmount");
		List<FilingStats> allStatsInfoResultList = this.baseMapper.selectList(queryWrapper);
		//部门Id集合
		final Set<Long> deptIds = new HashSet<Long>();
		allStatsInfoResultList.forEach(statInfo -> {
			deptIds.add(statInfo.getFilingDeptId());
		});
		final R<Map<Long, String>> remoteData = remoteDeptService.getDeptIdNameMap(CollUtil.join(deptIds, ","));
		if (!CommonConstants.SUCCESS.equals(remoteData.getCode())) {
			if (log.isErrorEnabled()) {
				log.error("获取部门名称出错：{}", remoteData.getMsg());
			}
			throw new ArchiveRuntimeException("获取部门名称出错");
		}
		//id 部门名字信息
		final Map<Long, String> deptIdNameMap = remoteData.getData();

		for(String typeCode:typeCodeList){
			DeckTotalStatsDTO<DeckNewTotalDataDTO, List<FilingStats>> dockTotalByTypeCode = new DeckTotalStatsDTO<>();
			DeckNewTotalDataDTO totalDataDTO = new DeckNewTotalDataDTO();
			totalDataDTO.setNewPageAmount(totalPage);//总页码
			ArchiveType archiveType = archiveTypeList.stream().filter(t->StrUtil.equalsAnyIgnoreCase(t.getTypeCode(),typeCode) ).findFirst().orElse(null);
			totalDataDTO.setNewStatsAmountTitle(archiveType.getTypeName());
			totalDataDTO.setNewPageAmountTitle(unitInfo(archiveType));
			List<FilingStats> showList = allStatsInfoResultList.stream().filter(t->StrUtil.equalsAnyIgnoreCase(typeCode,t.getArchiveTypeCode())).map(typeCodeMap->{
				totalDataDTO.setNewStatsAmount(totalDataDTO.getNewStatsAmount()+typeCodeMap.getStatsAmount().intValue());
				return typeCodeMap;
			}).collect(Collectors.toList());
			showList.stream().forEach(sl->{
				sl.setArchiveTypeCode(sl.getFilingDeptId()==null?"部门名字无效":deptIdNameMap.get(sl.getFilingDeptId()));//填部门名字
			});
			dockTotalByTypeCode.setTotal(totalDataDTO);
			dockTotalByTypeCode.setList(showList);
			result.add(dockTotalByTypeCode);
		}
		mapResult.put("total",total);
		mapResult.put("records",result);
		return mapResult;
	}
	private Map<String, Object> deckShowTypeCodeGroupByYearCode(List<ArchiveType> archiveTypeList, String fondsCode,List<String> typeCodeList,int total,int totalPage) {
		List<DeckTotalStatsDTO<DeckNewTotalDataDTO, List<FilingStats>>> result = Lists.newArrayList();
		Map<String, Object> mapResult = Maps.newHashMap();
		QueryWrapper<FilingStats> queryWrapper = new QueryWrapper<>();
		if(StrUtil.isNotBlank(fondsCode)){
			queryWrapper.eq("fonds_code",fondsCode);
		}
		if(CollUtil.isNotEmpty(typeCodeList)){
			queryWrapper.in("archive_type_code",typeCodeList);
		}
		queryWrapper.groupBy("year_code","archive_type_code");
		queryWrapper.orderByAsc("year_code","archive_type_code");
		queryWrapper.select("year_code yearCode","archive_type_code archiveTypeCode","sum(stats_amount) statsAmount");
		List<FilingStats> allStatsInfoResultList = this.baseMapper.selectList(queryWrapper);
		//部门Id集合
		final Set<String> yearCodes = new HashSet<String>();
		allStatsInfoResultList.forEach(statInfo -> {
			yearCodes.add(statInfo.getYearCode());
		});
		for(String typeCode:typeCodeList){
			DeckTotalStatsDTO<DeckNewTotalDataDTO, List<FilingStats>> dockTotalByTypeCode = new DeckTotalStatsDTO<>();
			DeckNewTotalDataDTO totalDataDTO = new DeckNewTotalDataDTO();
			totalDataDTO.setNewPageAmount(totalPage);//总页码
			ArchiveType archiveType = archiveTypeList.stream().filter(t->StrUtil.equalsAnyIgnoreCase(t.getTypeCode(),typeCode) ).findFirst().orElse(null);
			totalDataDTO.setNewStatsAmountTitle(archiveType.getTypeName());
			totalDataDTO.setNewPageAmountTitle(unitInfo(archiveType));
			List<FilingStats> showList = allStatsInfoResultList.stream().filter(t->StrUtil.equalsAnyIgnoreCase(typeCode,t.getArchiveTypeCode())).map(typeCodeMap->{
				totalDataDTO.setNewStatsAmount(totalDataDTO.getNewStatsAmount()+typeCodeMap.getStatsAmount().intValue());
				return typeCodeMap;
			}).collect(Collectors.toList());
			showList.stream().forEach(sl->{
				sl.setYearCode(sl.getYearCode());
			});
			dockTotalByTypeCode.setTotal(totalDataDTO);
			dockTotalByTypeCode.setList(showList);
			result.add(dockTotalByTypeCode);
		}
		mapResult.put("total",total);
		mapResult.put("records",result);
		return mapResult;
	}

	private Map<String, Object> deckShowTypeCodeGroupByRetentionPeriod(List<ArchiveType> archiveTypeList, String fondsCode,List<String> typeCodeList,int total,int totalPage) {
		List<DeckTotalStatsDTO<DeckNewTotalDataDTO, List<FilingStats>>> result = Lists.newArrayList();
		Map<String, Object> mapResult = Maps.newHashMap();
		QueryWrapper<FilingStats> queryWrapper = new QueryWrapper<>();
		if(StrUtil.isNotBlank(fondsCode)){
			queryWrapper.eq("fonds_code",fondsCode);
		}
		if(CollUtil.isNotEmpty(typeCodeList)){
			queryWrapper.in("archive_type_code",typeCodeList);
		}
		queryWrapper.groupBy("retention_period","archive_type_code");
		queryWrapper.orderByAsc("retention_period","archive_type_code");
		queryWrapper.select("retention_period retentionPeriod","archive_type_code archiveTypeCode","sum(stats_amount) statsAmount");
		List<FilingStats> allStatsInfoResultList = this.baseMapper.selectList(queryWrapper);
		//部门Id集合
		final Set<String> retentionPeriods = new HashSet<String>();
		allStatsInfoResultList.forEach(statInfo -> {
			retentionPeriods.add(statInfo.getRetentionPeriod());
		});
		for(String typeCode:typeCodeList){
			DeckTotalStatsDTO<DeckNewTotalDataDTO, List<FilingStats>> dockTotalByTypeCode = new DeckTotalStatsDTO<>();
			DeckNewTotalDataDTO totalDataDTO = new DeckNewTotalDataDTO();
			totalDataDTO.setNewPageAmount(totalPage);//总页码
			ArchiveType archiveType = archiveTypeList.stream().filter(t->StrUtil.equalsAnyIgnoreCase(t.getTypeCode(),typeCode) ).findFirst().orElse(null);
			totalDataDTO.setNewStatsAmountTitle(archiveType.getTypeName());
			totalDataDTO.setNewPageAmountTitle(unitInfo(archiveType));
			List<FilingStats> showList = allStatsInfoResultList.stream().filter(t->StrUtil.equalsAnyIgnoreCase(typeCode,t.getArchiveTypeCode())).map(typeCodeMap->{
				totalDataDTO.setNewStatsAmount(totalDataDTO.getNewStatsAmount()+typeCodeMap.getStatsAmount().intValue());
				return typeCodeMap;
			}).collect(Collectors.toList());
			showList.stream().forEach(sl->{
				sl.setRetentionPeriod(sl.getRetentionPeriod());
			});
			dockTotalByTypeCode.setTotal(totalDataDTO);
			dockTotalByTypeCode.setList(showList);
			result.add(dockTotalByTypeCode);
		}
		mapResult.put("total",total);
		mapResult.put("records",result);
		return mapResult;
	}
	private String unitInfo(ArchiveType archiveType){
    	String info = "";
		//项目 P
		//案卷 V
		//卷内 F
		//一文一件 O
		//单套制 S
		//预归档文件 R
		//电子全文 D
		//过程信息 I
		//签名签章 G
    	switch (archiveType.getFilingType()){
			case "P":
				info = "(卷/件)";
				break;
			case "V":
				info = "(卷/件)";
				break;
			case "F":
				info = "(件)";
				break;
			case "O":
				info = "(件)";
				break;
			case "S":
				info = "(件)";
				break;
			case "R":
				info = "(件)";
				break;
			case "D":
				info = "(件)";
				break;
			case "I":
				info = "(件)";
				break;
			case "G":
				info = "(件)";
				break;

		}
		return info;
	}


	@Override
	public void statsExportExcel(String fondsCodes, Integer statsType, String yearCode, HttpServletResponse response, Integer filingType) throws ArchiveBusinessException {
		List<String> fondsCodeList = Arrays.stream(fondsCodes.split(StrUtil.COMMA)).distinct().collect(Collectors.toList());
		//导出归档数量统计(按照门类统计)
		if (StatsTypeEnum.FILING_STATS_BY_TYPE_CODE.getCode().equals(statsType)) {
			writeExcelFilingStatsByCode(statsType, yearCode, response, fondsCodeList);
		}
		//导出归档数量统计(按照归档部门统计)
		if (StatsTypeEnum.FILING_STATS_BY_DEPT.getCode().equals(statsType)) {
			writeExcelFilingStatsByDept(statsType, yearCode, response, fondsCodeList);
		}
		//导出档案汇总统计(按照年度统计)
		if (StatsTypeEnum.COLLECTION_STATS_BY_YEAR.getCode().equals(statsType)) {
			writeExcelCollectionStats(fondsCodes, statsType, response);
		}
		//导出档案汇总统计(按照保管期限统计)
		if (StatsTypeEnum.COLLECTION_STATS_BY_RETENTION.getCode().equals(statsType)) {
			writeExcelCollectionStats(fondsCodes, statsType, response);
		}
		//导出档案数字化率统计(按照目录统计)
		if (StatsTypeEnum.DIGITIZATION_RATE_BY_CATALOG.getCode().equals(statsType)) {
			writeExcelDigitizationRateByCatalog(fondsCodes, filingType, yearCode, statsType, response);
		}
		//导出档案数字化率统计(按照页数统计)
		if (StatsTypeEnum.DIGITIZATION_RATE_BY_PAGE_NUMBER.getCode().equals(statsType)) {
			writeExcelDigitizationRateByPageNumber(fondsCodes, filingType, yearCode, statsType, response);
		}
		//导出档案销毁统计
		if (StatsTypeEnum.DESTROY_STATS.getCode().equals(statsType)) {
			writeExcelDestroy(fondsCodes, statsType, response);
		}

	}



	private void writeExcelDestroy(String fondsCodes, Integer statsType, HttpServletResponse response) throws ArchiveBusinessException {
		//数据
		List<DestroyStatsTableDTO> destroyTableStats = archiveStatsService.getDestroyTableStats(fondsCodes);
		Set<String> keys = CollUtil.newHashSet();
		destroyTableStats.forEach(e -> {
			Map<String, Integer> data = e.getData();
			keys.addAll(data.keySet());
		});
		List<List<Object>> data = new ArrayList<>();
		destroyTableStats.forEach(statsData -> {
			List<Object> objects = new ArrayList<>();
			objects.add(statsData.getStatsTitle());
			keys.forEach(e -> objects.add(statsData.getData().get(e)));
			data.add(objects);
		});
		int objectSize = keys.size() + 1;
		//合计
		List<Object> notesObject = new ArrayList<>();
		notesObject.add(ExcelConstants.TOTAL);
		for (int i = 1; i < objectSize; i++) {
			int sum = 0;
			for (List<Object> e : data) {
				sum = sum + ((e.get(i) instanceof Integer) ? Integer.parseInt(e.get(i).toString()) : 0);
			}
			notesObject.add(sum);
		}
		data.add(notesObject);
		//表头
		List<List<String>> head = new ArrayList<>();
		head.add(CollUtil.newArrayList(ExcelConstants.DESTROY_STATS, ExcelConstants.YEAR, ExcelConstants.YEAR));
		keys.forEach(key -> {
			head.add(CollUtil.newArrayList(ExcelConstants.DESTROY_STATS, ExcelConstants.TYPE_CODE, key));
		});
		writeExcel(statsType, response, head, data, null);
	}

	private void writeExcelDigitizationRateByPageNumber(String fondsCodes, Integer filingType, String yearCode, Integer statsType, HttpServletResponse response) throws ArchiveBusinessException {
		//数据
		TableStatsDataDTO<DigitTableFilingTypeDataDTO> digitTableFolderStats = archiveStatsService.getDigitTablePageStats(fondsCodes, filingType, yearCode);
		Set<String> fileKeys = digitTableFolderStats.getFileKeys();
		Set<String> folderKeys = digitTableFolderStats.getFolderKeys();
		List<StatsDataDTO<DigitTableFilingTypeDataDTO>> statsDataList = digitTableFolderStats.getData();
		List<List<Object>> data = new ArrayList<>();
		statsDataList.forEach(statsData -> {
			boolean isFile = ObjectUtil.isNull(statsData.getFolderData());
			List<Object> objects = new ArrayList<>();
			objects.add(statsData.getShowTitle());
			folderKeys.forEach(e -> objects.add(isFile ? "" : statsData.getFolderData().getData().get(e)));
			objects.add(isFile ? "" : statsData.getFolderData().getTotalFileAmount());
			objects.add(isFile ? "" : statsData.getFolderData().getTotalPageAmonut());
			objects.add(isFile ? "" : statsData.getFolderData().getTotalDigitedAmount());
			fileKeys.forEach(e -> objects.add(isFile ? statsData.getFileData().getData().get(e) : ""));
			objects.add(isFile ? statsData.getFileData().getTotalFileAmount() : "");
			objects.add(isFile ? statsData.getFileData().getTotalPageAmonut() : "");
			objects.add(isFile ? statsData.getFileData().getTotalDigitedAmount() : "");
			data.add(objects);
		});
		int objectSize = fileKeys.size() + folderKeys.size() + 7;
		//小计
		List<Object> notesObject = new ArrayList<>(objectSize);
		notesObject.add(ExcelConstants.NOTES);
		for (int i = 1; i < objectSize; i++) {
			int sum = 0;
			for (List<Object> e : data) {
				sum = sum + ((e.get(i) instanceof Integer) ? Integer.parseInt(e.get(i).toString()) : 0);
			}
			notesObject.add(sum);
		}
		data.add(notesObject);
		//表头
		List<List<String>> head = new ArrayList<>();
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.TYPE_CODE, ExcelConstants.TYPE_CODE));
		folderKeys.forEach(folderKey -> {
			List<String> list = CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.FOLDER_MANAGEMENT, folderKey);
			head.add(list);
		});
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.FOLDER_MANAGEMENT, ExcelConstants.FOLDER_FILE_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.FOLDER_MANAGEMENT, ExcelConstants.PAGE_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.FOLDER_MANAGEMENT, ExcelConstants.DIGITIZED_PAGE_COUNT));
		fileKeys.forEach(fileKey -> {
			List<String> list = CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.MANAGEMENT_BY_FILE, fileKey);
			head.add(list);
		});
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.MANAGEMENT_BY_FILE, ExcelConstants.FILE_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.MANAGEMENT_BY_FILE, ExcelConstants.PAGE_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.MANAGEMENT_BY_FILE, ExcelConstants.DIGITIZED_PAGE_COUNT));
		writeExcel(statsType, response, head, data, null);
	}

	private void writeExcelDigitizationRateByCatalog(String fondsCodes, Integer filingType, String yearCode, Integer statsType, HttpServletResponse response) throws ArchiveBusinessException {
		//数据
		TableStatsDataDTO<DigitTableFilingTypeDataDTO> digitTableFolderStats = archiveStatsService.getDigitTableFolderStats(fondsCodes, filingType, yearCode);
		Set<String> fileKeys = digitTableFolderStats.getFileKeys();
		Set<String> folderKeys = digitTableFolderStats.getFolderKeys();
		List<StatsDataDTO<DigitTableFilingTypeDataDTO>> statsDataList = digitTableFolderStats.getData();
		List<List<Object>> data = new ArrayList<>();
		statsDataList.forEach(statsData -> {
			boolean isFile = ObjectUtil.isNull(statsData.getFolderData());
			List<Object> objects = new ArrayList<>();
			objects.add(statsData.getShowTitle());
			folderKeys.forEach(e -> objects.add(isFile ? "" : statsData.getFolderData().getData().get(e)));
			objects.add(isFile ? "" : statsData.getFolderData().getTotalAmount());
			objects.add(isFile ? "" : statsData.getFolderData().getTotalDigitedAmount());
			objects.add(isFile ? "" : NumberUtil.formatPercent(NumberUtil.div(statsData.getFolderData().getTotalDigitedAmount().doubleValue(), statsData.getFolderData().getTotalAmount().doubleValue()), 2));
			fileKeys.forEach(e -> objects.add(isFile ? statsData.getFileData().getData().get(e) : ""));
			objects.add(isFile ? statsData.getFileData().getTotalAmount() : "");
			objects.add(isFile ? statsData.getFileData().getTotalDigitedAmount() : "");
			objects.add(isFile ? NumberUtil.formatPercent(NumberUtil.div(statsData.getFileData().getTotalDigitedAmount().doubleValue(), statsData.getFileData().getTotalAmount().doubleValue()), 2) : "");
			data.add(objects);
		});
		int objectSize = fileKeys.size() + folderKeys.size() + 7;
		//小计
		List<Object> notesObject = new ArrayList<>(objectSize);
		notesObject.add(ExcelConstants.NOTES);
		for (int i = 1; i < objectSize; i++) {
			int sum = 0;
			boolean isRate = false;
			double rate = 0;
			for (List<Object> e : data) {
				if (e.get(i) instanceof Integer) {
					sum = sum + Integer.parseInt(e.get(i).toString());
				}
				if (e.get(i) instanceof String) {
					if (e.get(i).toString().contains("%")) {
						isRate = true;
						rate = rate + Double.parseDouble(e.get(i).toString().replace("%", ""));
					}
				}
			}
			if (isRate) {
				notesObject.add(rate + "%");
			} else {
				notesObject.add(sum);
			}
		}
		data.add(notesObject);
		//表头
		List<List<String>> head = new ArrayList<>();
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.TYPE_CODE, ExcelConstants.TYPE_CODE));
		folderKeys.forEach(folderKey -> {
			List<String> list = CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.FOLDER_MANAGEMENT, folderKey);
			head.add(list);
		});
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.FOLDER_MANAGEMENT, ExcelConstants.DIGITIZATION_FOLDER_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.FOLDER_MANAGEMENT, ExcelConstants.DIGITIZED_FOLDER_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.FOLDER_MANAGEMENT, ExcelConstants.DIGITIZATION_RATE));
		fileKeys.forEach(fileKey -> {
			List<String> list = CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.MANAGEMENT_BY_FILE, fileKey);
			head.add(list);
		});
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.MANAGEMENT_BY_FILE, ExcelConstants.DIGITIZATION_FILE_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.MANAGEMENT_BY_FILE, ExcelConstants.DIGITIZED_FILE_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.STATISTICS_COLLECTION_ARCHIVES, ExcelConstants.MANAGEMENT_BY_FILE, ExcelConstants.DIGITIZATION_RATE));
		writeExcel(statsType, response, head, data, null);
	}

	private void writeExcelCollectionStats(String fondsCodes, Integer statsType, HttpServletResponse response) throws
			ArchiveBusinessException {
		TableStatsDataDTO<CollectionFilingTypeDataDTO> collectionTableStats;
		//数据
		if (StatsTypeEnum.COLLECTION_STATS_BY_YEAR.getCode().equals(statsType)) {
			collectionTableStats = archiveStatsService.getCollectionTableStatsByYearCode(fondsCodes, null);
		} else {
			collectionTableStats = archiveStatsService.getCollectionTableStats(fondsCodes, null);
		}
		List<StatsDataDTO<CollectionFilingTypeDataDTO>> statsDataList = collectionTableStats.getData();
		Set<String> folderKeys = collectionTableStats.getFolderKeys();
		Set<String> fileKeys = collectionTableStats.getFileKeys();
		List<List<Object>> data = new ArrayList<>();
		statsDataList.forEach(statsData -> {
			boolean isFile = ObjectUtil.isNull(statsData.getFolderData());
			List<Object> objects = new ArrayList<>();
			objects.add(statsData.getShowTitle());
			folderKeys.forEach(e -> objects.add(isFile ? "" : statsData.getFolderData().getData().get(e)));
			objects.add(isFile ? "" : statsData.getFolderData().getTotalFile());
			objects.add(isFile ? "" : statsData.getFolderData().getTotalPage());
			objects.add(isFile ? "" : statsData.getFolderData().getTotalStatsAmount());
			fileKeys.forEach(e -> objects.add(isFile ? statsData.getFileData().getData().get(e) : ""));
			objects.add(isFile ? statsData.getFileData().getTotalStatsAmount() : "");
			objects.add(isFile ? statsData.getFileData().getTotalPage() : "");
			objects.add(isFile ? statsData.getFileData().getTotalStatsAmount() : statsData.getFolderData().getTotalStatsAmount());
			objects.add(isFile ? statsData.getFileData().getTotalPage() : statsData.getFolderData().getTotalPage());
			data.add(objects);
		});
		int objectSize = fileKeys.size() + folderKeys.size() + 8;
		//小计
		List<Object> notesObject = new ArrayList<>(objectSize);
		notesObject.add(ExcelConstants.NOTES);
		for (int i = 1; i < objectSize; i++) {
			int sum = 0;
			for (List<Object> e : data) {
				sum = sum + ((e.get(i) instanceof Integer) ? Integer.parseInt(e.get(i).toString()) : 0);
			}
			notesObject.add(sum);
		}
		data.add(notesObject);
		//合计
		List<Object> totalObject = new ArrayList<>(objectSize);
		totalObject.add(ExcelConstants.TOTAL);
		//总卷数
		int folderSize = statsDataList.stream().filter(e -> ObjectUtil.isNotNull(e.getFolderData()))
				.mapToInt(e -> ObjectUtil.isNotNull(e.getFolderData().getTotalFile()) ? e.getFolderData().getTotalFile() : 0).sum();
		//总件数
		int fileSize = statsDataList.stream().filter(e -> ObjectUtil.isNotNull(e.getFileData()))
				.mapToInt(e -> ObjectUtil.isNotNull(e.getFileData().getTotalStatsAmount()) ? e.getFileData().getTotalStatsAmount() : 0).sum();
		for (int i = 1; i < objectSize; i++) {
			if (i <= folderKeys.size() + 3) {
				totalObject.add(ExcelConstants.MANAGEMENT_BY_FOLDER + ":" + folderSize);
			} else if (i > folderKeys.size() + 3 && i <= objectSize - 3) {
				totalObject.add(ExcelConstants.MANAGEMENT_BY_FILE + ":" + fileSize);
			} else {
				totalObject.add("");
			}
		}
		data.add(totalObject);

		//总计
		List<Object> totalizeObject = new ArrayList<>(objectSize);
		totalizeObject.add(ExcelConstants.TOTALIZE);
		for (int i = 1; i < objectSize; i++) {
			totalizeObject.add(folderSize + fileSize + "卷(件)");
		}
		data.add(totalizeObject);
		//表头
		List<List<String>> head = new ArrayList<>();
		head.add(CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.TYPE_CODE, ExcelConstants.TYPE_CODE));
		folderKeys.forEach(folderKey -> {
			List<String> list = CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.MANAGEMENT_BY_FOLDER, folderKey);
			head.add(list);
		});
		head.add(CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.MANAGEMENT_BY_FOLDER, ExcelConstants.FOLDER_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.MANAGEMENT_BY_FOLDER, ExcelConstants.PAGE_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.MANAGEMENT_BY_FOLDER, ExcelConstants.FOLDER_FILE_COUNT));
		fileKeys.forEach(fileKey -> {
			List<String> list = CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.MANAGEMENT_BY_FILE, fileKey);
			head.add(list);
		});
		head.add(CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.MANAGEMENT_BY_FILE, ExcelConstants.FILE_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.MANAGEMENT_BY_FILE, ExcelConstants.PAGE_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.TOTAL, ExcelConstants.FILE_CATALOG_COUNT));
		head.add(CollUtil.newArrayList(ExcelConstants.ARCHIVE_STATS, ExcelConstants.TOTAL, ExcelConstants.PAGE_COUNT));

		//合并单元格配置
		ExcelFillCellMergeStrategy excelFillCellMergeStrategy = new ExcelFillCellMergeStrategy(ExcelFillCellMergeStrategy.COLUMN_MERGE, CollUtil.newHashSet(data.size() + 2, data.size() + 1));
		writeExcel(statsType, response, head, data, excelFillCellMergeStrategy);
	}

	private void writeExcelFilingStatsByDept(Integer statsType, String yearCode, HttpServletResponse
			response, List<String> fondsCodeList) throws ArchiveBusinessException {
		//数据
		List<FilingStatsDataDTO> statsData = baseMapper.getStatsData(FieldConstants.FILING_DEPT_ID, fondsCodeList, yearCode);
		processStats(statsData);
		int sum = statsData.stream().mapToInt(FilingStatsDataDTO::getStatsAmount).sum();
		statsData.add(FilingStatsDataDTO.builder().statsTitle(ExcelConstants.TOTALIZE).statsAmount(sum).build());
		//表头
		List<List<String>> head = new ArrayList<>();
		head.add(CollUtil.newArrayList(ExcelConstants.FILING_STATS, ExcelConstants.DEPT));
		head.add(CollUtil.newArrayList(ExcelConstants.FILING_STATS, ExcelConstants.COUNT));
		writeExcel(statsType, response, head, statsData, null);
	}

	private void writeExcelFilingStatsByCode(Integer statsType, String yearCode, HttpServletResponse response, List<String> fondsCodeList) throws ArchiveBusinessException {
		//数据
		List<FilingStatsDataDTO> statsData = baseMapper.getStatsData(StatsConstants.ARCHIVE_TYPE_NAME, fondsCodeList, yearCode);
		int sum = statsData.stream().mapToInt(FilingStatsDataDTO::getStatsAmount).sum();
		statsData.add(FilingStatsDataDTO.builder().statsTitle(ExcelConstants.TOTALIZE).statsAmount(sum).build());
		//表头
		List<List<String>> head = new ArrayList<>();
		head.add(CollUtil.newArrayList(ExcelConstants.FILING_STATS, ExcelConstants.TYPE_CODE));
		head.add(CollUtil.newArrayList(ExcelConstants.FILING_STATS, ExcelConstants.COUNT));
		writeExcel(statsType, response, head, statsData, null);
	}

	private void writeExcel(Integer statsType, HttpServletResponse response, List<List<String>> head, List<?> data, ExcelFillCellMergeStrategy excelFillCellMergeStrategy) throws ArchiveBusinessException {
		try {
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setCharacterEncoding("utf-8");
			String fileName = URLEncoder.encode(StatsTypeEnum.getEnum(statsType).getName(), "UTF-8").replaceAll("\\+", "%20");
			response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." + CesFileUtil.XLSX);
			WriteCellStyle writeCellStyle = new WriteCellStyle();
			writeCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
			EasyExcel.write(response.getOutputStream())
					.autoTrim(true)
					.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
					.registerWriteHandler(ExcelUtils.setConfigure())
					.registerWriteHandler(excelFillCellMergeStrategy)
					.head(head)
					.sheet(StatsTypeEnum.getEnum(statsType).getName())
					.doWrite(data);
		} catch (Exception e) {
			log.error("导出统计数据失败", e);
			throw new ArchiveBusinessException("导出统计数据失败！");
		}
	}


}
