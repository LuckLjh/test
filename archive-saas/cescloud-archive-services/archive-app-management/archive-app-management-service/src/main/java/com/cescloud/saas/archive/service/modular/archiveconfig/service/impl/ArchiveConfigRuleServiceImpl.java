package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.*;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveColumnRule;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEditForm;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchivePageModeConfig;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataSource;
import com.cescloud.saas.archive.api.modular.datasource.dto.DdlDTO;
import com.cescloud.saas.archive.api.modular.datasource.dto.DmlDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveInnerService;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveService;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.report.dto.ReportDTO;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchiveColumnRuleMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.*;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.InnerRelationService;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataAutovalueService;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataSourceService;
import com.cescloud.saas.archive.service.modular.archivetype.service.rulecompute.ArchiveRuleService;
import com.cescloud.saas.archive.service.modular.archivetype.service.rulecompute.ArchiveRuleServiceContext;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import com.cescloud.saas.archive.service.modular.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 档案配置规则
 *
 * @author apple
 */
@Service
@Slf4j
public class ArchiveConfigRuleServiceImpl extends ServiceImpl<ArchiveColumnRuleMapper, ArchiveColumnRule> implements ArchiveConfigRuleService {

	private static final String USED = "该元数据已经被使用，操作失败！";

	@Autowired
	private MetadataService metadataService;

	@Autowired
	private InnerRelationService innerRelationService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ArchiveTableService archiveTableService;

	@Autowired
	private ArchiveListService archiveListService;
	@Autowired
	private ArchiveOperateService archiveOperateService;
	@Autowired
	private ArchiveEditService archiveEditService;
	@Autowired
	private ArchiveEditFormService archiveEditFormService;
	@Autowired
	private ArchiveSortService archiveSortService;
	@Autowired
	private ArchiveSearchService archiveSearchService;
	@Autowired
	private ArchiveColumnRuleService archiveColumnRuleService;
	@Autowired
	private MetadataCheckrepeatService metadataCheckrepeatService;
	@Autowired
	private LinkLayerService linkLayerService;
	@Autowired
	private LinkColumnRuleService linkColumnRuleService;
	@Autowired
	private MetadataAutovalueService metadataAutovalueService;
	@Autowired
	private MetadataSourceService metadataSourceService;
	@Autowired
	private TagListService tagListService;
	@Autowired
	private TagSearchService tagSearchService;
	@Autowired
	private DictItemService dictItemService;
	@Autowired
	private ArchiveRuleServiceContext archiveRuleServiceContext;
	@Autowired
	private ArchivePageModeConfigService archivePageModeConfigService;

	@Autowired
	private RemoteArchiveInnerService remoteArchiveInnerService;

	@Autowired
	private RemoteArchiveService remoteArchiveService;
	/**
	 * @return java.util.List<com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationDTO>
	 * @Author xieanzhu
	 * @Description //根据表名 获取关联信息
	 * @Date 16:35 2019/5/14
	 * @Param [storageLocate, type]
	 **/
	@Override
	public List<InnerRelationDTO> getRelationByStorageLocate(String srcStorageLocate, String tarStorageLocate, int isRelation) {
		log.debug("根据表名<{}>,<{}>获取关联信息" + srcStorageLocate, tarStorageLocate);
		QueryWrapper<InnerRelation> queryWrapper = new QueryWrapper<InnerRelation>();
		queryWrapper.eq("source_storage_locate", srcStorageLocate);
		queryWrapper.eq("target_storage_locate", tarStorageLocate);
		if (isRelation == 1) {//isRelation = 1时 只返回勾选了是否配置的规则 否则返回所有
			queryWrapper.eq("is_relation", 1);
		}
		List<InnerRelation> innerRelationList = innerRelationService.getBaseMapper().selectList(queryWrapper);
		List<InnerRelationDTO> innerRelationDTOList = new ArrayList<>();
		innerRelationList.stream().forEach(innerRelation -> {
			Long sourceMetadataId = innerRelation.getSourceMetadataId();
			Long targetMetadataId = innerRelation.getTargetMetadataId();
			Metadata sourceMetadata = metadataService.getMetadataById(sourceMetadataId);
			Metadata targetMetadata = metadataService.getMetadataById(targetMetadataId);
			InnerRelationDTO innerRelationDTO = new InnerRelationDTO();
			innerRelationDTO.setSourceStorageLocate(innerRelation.getSourceStorageLocate() == null ? "" : innerRelation.getSourceStorageLocate());//设置关联元数据表名
			innerRelationDTO.setSourceMetadataId(sourceMetadataId);//设置关联元数据Id
			innerRelationDTO.setSourceMetadata(sourceMetadata == null ? "" : sourceMetadata.getMetadataEnglish());//设置关联元数据英文名称
			innerRelationDTO.setTargetStorageLocate(innerRelation.getTargetStorageLocate());//设置被关联元数据表名
			innerRelationDTO.setTargetMetadataId(targetMetadataId);//设置被关联元数据Id
			innerRelationDTO.setTargetMetadata(targetMetadata.getMetadataEnglish());//设置被关联元数据英文名称
			innerRelationDTO.setTargetMetadataChinese(targetMetadata == null ? "" : targetMetadata.getMetadataChinese());//设置被关联元数据中文名称
			innerRelationDTO.setRelationType(innerRelation.getRelationType());//设置关联方式
			innerRelationDTOList.add(innerRelationDTO);
		});
		return innerRelationDTOList;
	}

	@Override
	public List<ReportDTO> getReportList(String typeCode, Long templateTableId, Long moduleId) {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		List<ReportDTO> reportDTOS = reportService.listByStorageLocate(archiveTable.getStorageLocate(), moduleId);
		if (CollectionUtil.isEmpty(reportDTOS)) {
			reportDTOS = reportService.listByStorageLocate(archiveTable.getStorageLocate(), ArchiveConstants.PUBLIC_MODULE_FLAG);
		}
		return reportDTOS.stream().filter(e -> CommonConstants.REPORT_MODEL_EXIST.equals(e.getReportModel())).collect(Collectors.toList());
	}

	@Override
	public Map<String, Object> getConfigArchiveList(String typeCode, Long templateTableId, Long moduleId) {
		List<DefinedListMetadata> definedListMetadataList = archiveListService.listBusinessOfDefined(templateTableId, typeCode, moduleId, SecurityUtils.getUser().getId());
		List<Map<String, Object>> collect = definedListMetadataList.stream().map(m -> {
			String dictCode = m.getDictCode();
			Map<String, Object> beanMap = BeanUtil.beanToMap(m);
			if (StrUtil.isNotEmpty(dictCode)) {
				List<DictItem> dictItemList = dictItemService.getItemListByDictCode(dictCode);
				List<Map<String, String>> options = dictItemList.stream().map(dictItem -> {
					Map<String, String> map = new HashMap<>(2);
					map.put("label", dictItem.getItemLabel());
					map.put("value", dictItem.getItemCode());
					return map;
				}).collect(Collectors.toList());
				beanMap.put("options", options);
			}
			return beanMap;
		}).collect(Collectors.toList());
		ArchivePageModeConfig archivePageModeConfig = archivePageModeConfigService.getArchivePageModeConfig(templateTableId, typeCode, moduleId);
		return MapUtil.<String, Object>builder().put("list", collect).put("pageMode", archivePageModeConfig.getPageMode()).build();
	}

	@Override
	public List<DefinedListTag> getConfigTagList() {
		List<DefinedListTag> definedListMetadataList = tagListService.listOfDefined();
		return definedListMetadataList;
	}

	@Override
	public List<DefinedSearchTag> getConfigTagSearch() {
		List<DefinedSearchTag> definedSearchMetadataList = tagSearchService.listOfDefined();
		return definedSearchMetadataList;
	}

	@Override
	public ConfigTagDTO getConfigTag() {
		return ConfigTagDTO.builder()
				.tagList(tagListService.listOfDefined())
				.tagSearch(tagSearchService.listOfDefined())
				.build();
	}

	@Override
	public List<DefinedListMetadata> getConfigList(String typeCode, Long templateTableId, Long moduleId) {
		return archiveListService.listBusinessOfDefined(templateTableId, typeCode, moduleId, SecurityUtils.getUser().getId());
	}

	@Override
	public List<DefinedEditMetadata> getConfigArchiveEdit(String typeCode, Long templateTableId, Long moduleId) {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		List<DefinedEditMetadata> definedEditMetadata = archiveEditService.listOfDefined(archiveTable.getStorageLocate(), moduleId);
		if (CollectionUtil.isEmpty(definedEditMetadata)) {
			definedEditMetadata = archiveEditService.listOfDefined(archiveTable.getStorageLocate(), ArchiveConstants.PUBLIC_MODULE_FLAG);
		}
		return definedEditMetadata;
	}

	@Override
	public List<DefinedSortMetadata> getConfigArchiveSort(String typeCode, Long templateTableId, Long moduleId) throws ArchiveBusinessException {
		return archiveSortService.listBusinessOfDefined(templateTableId, typeCode, moduleId, SecurityUtils.getUser().getId());
	}

	@Override
	public List<DefinedColumnRuleMetadata> getConfigArchiveColumnRule(Long metadataSourceId) {
		return archiveColumnRuleService.listOfDefined(metadataSourceId);
	}

	@Override
	public List<DefinedRepeatMetadata> getConfigCheckRepeat(String typeCode, Long templateTableId)  {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		return metadataCheckrepeatService.listOfDefined(archiveTable.getStorageLocate());
	}

	@Override
	public List<LinkLayer> getConfigLinkRule(String typeCode, Long templateTableId, Long moduleId) {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		return linkLayerService.getLinkRule(archiveTable.getStorageLocate(), moduleId);
	}

	@Override
	public LinkLayer getConfigFileNameLinkRule(String typeCode, Long templateTableId, Long moduleId) {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		return linkLayerService.getFileNameLinkRule(archiveTable.getStorageLocate(), moduleId);
	}

	@Override
	public LinkLayer getConfigDocNameLinkRule(String typeCode, Long templateTableId, Long moduleId) {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		return linkLayerService.getDocNameLinkRule(archiveTable.getStorageLocate(), moduleId);
	}


	/**
	 * 判断字段是否已经被配置使用
	 * 使用缓存，即使所有规则都验证一遍，也只耗时个位毫秒数
	 *
	 * @param metadataId
	 * @param storageLocate
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@Override
	public Boolean checkUsed(Long metadataId, String storageLocate) throws ArchiveBusinessException {
		//数据规则定义校验
		List<MetadataAutovalue> definedAutovalues = metadataAutovalueService.getAllDefinedAutovalues(storageLocate);
		checkAutovalueUsed(definedAutovalues, metadataId, "在数据规则定义中");
		List<MetadataSource> defiendSource = metadataSourceService.getMetaDataSourceByStorageLocate(storageLocate);
		checkAutovalueUsed(defiendSource, metadataId, "在数据规则定义中");
		//判断字段组成规则是否被使用
		final List<MetadataAutovalue> columnRuleAutovalues = definedAutovalues.parallelStream().filter(metadataAutovalue -> {
			if (BoolEnum.YES.getCode().equals(metadataAutovalue.getType()) && metadataAutovalue.getMetadataId().equals(metadataId)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}).collect(Collectors.toList());
		if (CollectionUtil.isNotEmpty(columnRuleAutovalues)) {
			throw new ArchiveBusinessException("在字段组成规则中" + USED);
		}
//		List<DefinedColumnRuleMetadata> columnRules = archiveColumnRuleService.listOfDefined(storageLocate, metadataId);
//		if (CollectionUtil.isNotEmpty(columnRules)) {
//			throw new ArchiveBusinessException("在字段组成规则中" + USED);
//		}
//		checkUsed(columnRules, metadataId, "在字段组成规则中");
		//判断表单字段是否被使用
		List<DefinedEditMetadata> edits = archiveEditService.listOfDefined(storageLocate, null);
		checkUsed(edits, metadataId, "在表单定义中");
		//判断列表配置是否被使用
		List<DefinedListMetadata> lists = archiveListService.listOfDefined(storageLocate, null, SecurityUtils.getUser().getId());
		checkUsed(lists, metadataId, "在列表定义中");
		//判断排序配置是否被使用
		List<DefinedSortMetadata> sorts = archiveSortService.listOfDefined(storageLocate, null, SecurityUtils.getUser().getId());
		checkUsed(sorts, metadataId, "在排序定义中");
		//判断重复校验配置是否被使用
		List<DefinedRepeatMetadata> repeats = metadataCheckrepeatService.listOfDefined(storageLocate);
		checkUsed(repeats, metadataId, "在查重配置中");
		//判断挂接规则是否被使用
		List<LinkLayer> linkLayerList = linkLayerService.getLinkRule(storageLocate, null);
		checkUsed(linkLayerList, metadataId);
		//判断关联关系配置是否被使用
		List<InnerRelation> innerRelationList = innerRelationService.listByStorageLocate(storageLocate, null);
		checkInnerRelationUsed(innerRelationList, metadataId);
		return Boolean.FALSE;
	}

	@Override
	public List<MetadataAutovalue> getAutoValueRule(String storageLocate) {
		List<MetadataAutovalue> metadataAutovalues = metadataAutovalueService.list(Wrappers.<MetadataAutovalue>query()
				.lambda().eq(MetadataAutovalue::getStorageLocate, storageLocate));
		for (MetadataAutovalue metadataAutovalue : metadataAutovalues) {
			if (metadataAutovalue.getType() == 0) {
				if (log.isDebugEnabled()){
					log.debug("获取此拼接规则下的分组规则");
				}
				List<MetadataSource> metadataSources = metadataSourceService.list(Wrappers.<MetadataSource>query()
						.lambda()
						.eq(MetadataSource::getStorageLocate, storageLocate)
						.eq(MetadataSource::getMetadataTargetId, metadataAutovalue.getMetadataId())
						.orderByAsc(MetadataSource::getSortNo));
				metadataSources.stream().forEach(s -> s.setMetadataSourceEnglish(metadataService.getMetadataById(s.getMetadataSourceId()).getMetadataEnglish()));
				metadataAutovalue.setMetadataSources(metadataSources);
			} else {
				if (log.isDebugEnabled()){
					log.debug("获取此拼接规则下的组成规则");
				}
				List<ArchiveColumnRule> archiveColumnRules = archiveColumnRuleService.list(Wrappers.<ArchiveColumnRule>query()
						.lambda()
						.eq(ArchiveColumnRule::getStorageLocate, storageLocate)
						.eq(ArchiveColumnRule::getMetadataSourceId, metadataAutovalue.getMetadataId())
						.orderByAsc(ArchiveColumnRule::getSortNo));
				archiveColumnRules.stream().forEach(r -> {
					if (StrUtil.equals(r.getConnectSign(), ConnectSignEnum.METADATA.getCode())) {
						Metadata m = metadataService.getMetadataById(r.getMetadataId());
						r.setMetadataEnglish(m.getMetadataEnglish());
						r.setMetadataChinese(m.getMetadataChinese());
						//0、存KEY  1、存值
						Integer dictKeyValue = r.getDictKeyValue();
						if (dictKeyValue == 0 && StrUtil.isNotBlank(m.getDictCode())) {
							r.setDictCode(m.getDictCode());
						}
					}
				});
				metadataAutovalue.setArchiveColumnRules(archiveColumnRules);
			}
		}
		return metadataAutovalues;
	}

	@Override
	public MetadataAutovalue getAutoValueRule(Long autoValueId, Long metadataId) {
		MetadataAutovalue metadataAutovalue = metadataAutovalueService.getById(autoValueId);
		if (ObjectUtil.isNotNull(metadataAutovalue)) {
			metadataAutovalue.setMetadataEnglish(metadataService.getMetadataById(metadataId).getMetadataEnglish());
			if (metadataAutovalue.getType() == 0) {
				log.debug("获取此累加规则下的分组规则");
				List<MetadataSource> metadataSources = metadataSourceService.list(Wrappers.<MetadataSource>query()
						.lambda()
						.eq(MetadataSource::getMetadataTargetId, autoValueId)
						.orderByAsc(MetadataSource::getSortNo));
				metadataSources.stream().forEach(s -> s.setMetadataSourceEnglish(metadataService.getMetadataById(s.getMetadataSourceId()).getMetadataEnglish()));
				metadataAutovalue.setMetadataSources(metadataSources);
			} else {
				log.debug("获取此拼接规则下的组成规则");
				List<ArchiveColumnRule> archiveColumnRules = archiveColumnRuleService.list(Wrappers.<ArchiveColumnRule>query()
						.lambda()
						.eq(ArchiveColumnRule::getMetadataSourceId, autoValueId)
						.orderByAsc(ArchiveColumnRule::getSortNo));
				archiveColumnRules.stream().forEach(r -> {
					if (StrUtil.equals(r.getConnectSign(), ConnectSignEnum.METADATA.getCode())) {
						Metadata m = metadataService.getMetadataById(r.getMetadataId());
						r.setMetadataEnglish(m.getMetadataEnglish());
						r.setMetadataChinese(m.getMetadataChinese());
						//0、存KEY  1、存值
						Integer dictKeyValue = r.getDictKeyValue();
						if (dictKeyValue == 0 && StrUtil.isNotBlank(m.getDictCode())) {
							r.setDictCode(m.getDictCode());
						}
					}
				});
				metadataAutovalue.setArchiveColumnRules(archiveColumnRules);
			}
		}
		return metadataAutovalue;
	}

	/**
	 * ColumnBindingRuleCode与metadataAutovalueType相差10
	 *
	 * @param metadataAutovalueType
	 * @return
	 */
	private Integer toColumnBindingRuleCode(Integer metadataAutovalueType) {
		return metadataAutovalueType + 10;
	}

	private void checkUsed(List<LinkLayer> linkLayerList, Long metadataId) throws ArchiveBusinessException {
		for (LinkLayer linkLayer : linkLayerList) {
			for (DefinedColumnRuleMetadata linkColumnRule : linkLayer.getLinkColumnRule()) {
				if (linkColumnRule.getMetadataId() != null && linkColumnRule.getMetadataId().equals(metadataId)) {
					throw new ArchiveBusinessException("在挂接规则中" + USED);
				}
			}
		}

	}

	private <T extends DefinedMetadata> void checkUsed(List<T> definedList, Long metadataId, String message) throws ArchiveBusinessException {
		for (T t : definedList) {
			if (t.getMetadataId() != null && t.getMetadataId().equals(metadataId)) {
				throw new ArchiveBusinessException(message + USED);
			}
		}
	}

	private <T extends Object> void checkAutovalueUsed(List<T> rules, Long metadataId, String message) throws ArchiveBusinessException {
		for (T e : rules) {
			if (e instanceof MetadataAutovalue) {
				if (((MetadataAutovalue) e).getMetadataId() != null
						&& ((MetadataAutovalue) e).getMetadataId().equals(metadataId)) {
					throw new ArchiveBusinessException(message + USED);
				}
			} else if (e instanceof MetadataSource) {
				if (((MetadataSource) e).getMetadataSourceId() != null
						&& ((MetadataSource) e).getMetadataSourceId().equals(metadataId)) {
					throw new ArchiveBusinessException(message + USED);
				}
			}
		}
	}

	private void checkInnerRelationUsed(List<InnerRelation> innerRelationList, Long metadataId) throws ArchiveBusinessException {
		for (InnerRelation innerRelation : innerRelationList) {
			if (innerRelation.getSourceMetadataId().equals(metadataId) || innerRelation.getTargetMetadataId().equals(metadataId)) {
				throw new ArchiveBusinessException("在关联关系定义中" + USED);
			}
		}

	}

	/**
	 * 获取字段关联关系计算规则
	 *
	 * @param storageLocate
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@Override
	public List<ColumnComputeRuleDTO> getComputeRuleByStorageLocate(Long moduleId, String storageLocate, FormStatusEnum formStatusEnum) throws ArchiveBusinessException {
		//1、定义一个规则容器，放置表单初始化规则
		final List<ColumnComputeRuleDTO> list = CollectionUtil.newArrayList();
		//2、获取 新增表单或者组卷表单关联关系规则
		ArchiveRuleService archiveRuleService = archiveRuleServiceContext.getArchiveRuleService(formStatusEnum);
		Map<String, List<ColumnComputeRuleDTO>> storageLocateRuleMap = archiveRuleService.handleComputeRule(storageLocate, moduleId);
		storageLocateRuleMap.forEach((k, v) -> {
			CollectionUtil.addAll(list, v);
		});
		return list;
	}

	/**
	 * 获取表单中字段初始化值的规则
	 *
	 * @param storageLocate
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@Override
	public List<ColumnComputeRuleDTO> getEditFormComputeRule(String storageLocate, Long moduleId) throws ArchiveBusinessException {
		List<String> columnList = archiveEditFormService.getEditFormColumnByStorageLocate(storageLocate, moduleId);
		List<ColumnComputeRuleDTO> columnComputeRuleDTOList = getComputeRuleByStorageLocate(moduleId, storageLocate, FormStatusEnum.ADD);
		List<ColumnComputeRuleDTO> editFormcolumnComputeRuleDTOList = new ArrayList<>();
		columnList.stream().forEach(column -> {
			columnComputeRuleDTOList.stream().forEach(columnComputeRuleDTO -> {
				if (column.equals(columnComputeRuleDTO.getMetadataEnglish())) {
					editFormcolumnComputeRuleDTOList.add(columnComputeRuleDTO);
				}
			});
		});

		return editFormcolumnComputeRuleDTOList;
	}

	/**
	 * 删除所有绑定的规则
	 *
	 * @param storageLocate
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteByStorageLocate(String storageLocate) throws ArchiveBusinessException {
		try {
			archiveEditFormService.deleteByStorageLocate(storageLocate);
			archiveEditService.deleteByStorageLocate(storageLocate, null);
			archiveListService.deleteByStorageLocate(storageLocate, null, null);
			archiveOperateService.deleteByStorageLocate(storageLocate);
			archiveSearchService.deleteByStorageLocate(storageLocate, null, null, null);
			archiveSortService.deleteByStorageLocate(storageLocate, null, null);
			archiveColumnRuleService.deleteByStorageLocate(storageLocate);
			linkLayerService.deleteByStorageLocate(storageLocate);
			linkColumnRuleService.deleteByStorageLocate(storageLocate);
			metadataCheckrepeatService.deleteByStorageLocate(storageLocate);
			metadataAutovalueService.deleteByStorageLocate(storageLocate);
			metadataSourceService.deleteByStorageLocate(storageLocate);
			innerRelationService.deleteByStorageLocate(storageLocate);
		}catch (Exception e){
			throw  new ArchiveBusinessException("删除档案门类相关配置信息失败!");
		}

		return Boolean.TRUE;
	}

	@Override
	public CheckRequiredDTO checkRequired(Long moduleId, String storageLocate, String dataIds) throws ArchiveBusinessException {
		CheckRequiredDTO crDto = new CheckRequiredDTO();
		// 档案类型是否是一文一件，案卷那种
		String archiveLayer = archiveTableService.getTableByStorageLocate(storageLocate).getArchiveLayer();
		List<String> requiredColumn = new ArrayList();
		//查询一下必输字段
		requiredColumn = getReqColByStorageLocateAndModuleId(storageLocate, moduleId);
		//组装查询sql
		R<List<Map<String, Object>>> selectR = getSelectData(requiredColumn,storageLocate,dataIds,FieldConstants.ID);
		//中英文键值对
		List<Metadata> metadataList =  metadataService.listByStorageLocate(storageLocate);
		Map columnMap = metadataList.stream().collect(Collectors.toMap(Metadata::getMetadataEnglish, Metadata::getMetadataChinese));
		if (selectR.getCode() == CommonConstants.FAIL) {
			log.error("查询失败");
			return null;
		}else{
			// 返回组装结果,只要一条检测失败就是false
			Boolean status = true;
			String mess = "";
			List<String > detailMess = new ArrayList<>();
			List<Map<String, Object>> data = selectR.getData();
			for(int x = 0; x<data.size(); x++){
				// 当前条目的检测状态
				Boolean thisDatastatus = true;
				StringBuffer sb = new StringBuffer("以下必输项：");
				for(int y=0;y<requiredColumn.size();y++){
					if (data.get(x).get(requiredColumn.get(y)) == null || data.get(x).get(requiredColumn.get(y)) == ""){
						sb.append(columnMap.get(requiredColumn.get(y))).append(" 值为空； ");
						status = false;
						thisDatastatus = false;
					}
				}
				// 校验失败
				if(!thisDatastatus){
					String sql = "update "+storageLocate+"  set "+FieldConstants.CHECK_MESS+" ='"+sb.toString()+"' where "+FieldConstants.ID+" = "+data.get(x).get(FieldConstants.ID);
					DdlDTO ddlDTO = new DdlDTO();
					ddlDTO.setSql(sql);
					R<Boolean> result = remoteArchiveService.executeDdl(ddlDTO);
				}
				// 就算这一层检查过了，案卷需要检查一下，下面的层级有没有缺少必输项,单套制、待分类、一文一件没有卷内
				if(thisDatastatus && !archiveLayer.equals(ArchiveLayerEnum.ONE.getCode())
						&& !ArchiveLayerEnum.SINGLE.getCode().equals(archiveLayer)
						&& !ArchiveLayerEnum.PRE.getCode().equals(archiveLayer) ){
					// 获取卷内 文件表的表名
					String lowTable = getjnTableName(storageLocate);
					String ownerId = data.get(x).get(FieldConstants.ID).toString();
					//返回结果
					CheckRequiredDTO dtoData = checkRequiredLowtable(moduleId,lowTable,ownerId);
					// 如果是false 说明卷内不合规
					if(!dtoData.getStatus()){
						status = false;
						thisDatastatus = false;
						String sql = "update "+storageLocate+"  set "+FieldConstants.CHECK_MESS+" ='卷内必输项有值为空； "+"' where "+FieldConstants.ID+" = "+data.get(x).get(FieldConstants.ID);
						DdlDTO ddlDTO = new DdlDTO();
						ddlDTO.setSql(sql);
						R<Boolean> result = remoteArchiveService.executeDdl(ddlDTO);
					}
				}
				if (!thisDatastatus){
					detailMess.add(sb.toString());
					mess = "档案条目必填项未补充完整！";
				}else {
					// 将校验正常的checkMess 置空
					String sql = "update "+storageLocate+"  set "+FieldConstants.CHECK_MESS+" ='' where "+FieldConstants.ID+" = "+data.get(x).get(FieldConstants.ID);
					DdlDTO ddlDTO = new DdlDTO();
					ddlDTO.setSql(sql);
					R<Boolean> result = remoteArchiveService.executeDdl(ddlDTO);
				}
			}
			CheckRequiredDTO dto = new CheckRequiredDTO();
			dto.setStatus(status);
			dto.setMessage(mess);
			dto.setDetail(detailMess);
			return dto;
		}



	}

	/**
	 * 处理检查一下卷内的必输项
	 * @param moduleId
	 * @param lowTable
	 * @param ownerId
	 * @return
	 */
	private CheckRequiredDTO checkRequiredLowtable(Long moduleId, String lowTable, String ownerId) throws ArchiveBusinessException {
		// 返回组装结果，有一个卷内条目不行都会成false
		Boolean status = true;
		CheckRequiredDTO dto = new CheckRequiredDTO();
		List<String> requiredColumn = new ArrayList();
		//查询一下必输字段
		requiredColumn = getReqColByStorageLocateAndModuleId(lowTable, moduleId);
		// 查询一下结果
		R<List<Map<String, Object>>> selectR = getSelectData(requiredColumn, lowTable, ownerId, FieldConstants.OWNER_ID);
		//中英文键值对
		List<Metadata> metadataList = metadataService.listByStorageLocate(lowTable);
		Map columnMap = metadataList.stream().collect(Collectors.toMap(Metadata::getMetadataEnglish, Metadata::getMetadataChinese));
		if (selectR.getCode() == CommonConstants.FAIL) {
			log.error("查询卷内信息失败");
			throw new ArchiveBusinessException("查询卷内信息失败!");
		} else {
			List<Map<String, Object>> data = selectR.getData();
			for (int x = 0; x < data.size(); x++) {
				// 这条条目的正确与否
				Boolean thisData = true;
				StringBuffer sb = new StringBuffer("以下必输项：");
				for (int y = 0; y < requiredColumn.size(); y++) {
					String val = data.get(x).get(requiredColumn.get(y))== null?"": data.get(x).get(requiredColumn.get(y)).toString();
					if (StrUtil.isEmpty((val))) {
						sb.append(columnMap.get(requiredColumn.get(y))).append(" 值为空； ");
						status = false;
						thisData = false;
					}
				}
				if (!thisData) {
					String sql = "update " + lowTable + "  set " + FieldConstants.CHECK_MESS + " ='" + sb.toString() + "' where " + FieldConstants.ID + " = " + data.get(x).get(FieldConstants.ID);
					DdlDTO ddlDTO = new DdlDTO();
					ddlDTO.setSql(sql);
					R<Boolean> result = remoteArchiveService.executeDdl(ddlDTO);
				} else {
					String sql = "update " + lowTable + "  set " + FieldConstants.CHECK_MESS + " ='' where " + FieldConstants.ID + " = " + data.get(x).get(FieldConstants.ID);
					DdlDTO ddlDTO = new DdlDTO();
					ddlDTO.setSql(sql);
					R<Boolean> result = remoteArchiveService.executeDdl(ddlDTO);
				}
			}
			dto.setStatus(status);
			return dto;
		}
	}

	/**
	 * 查询必输项的结果
	 * @param requiredColumn
	 * @param storageLocate
	 * @param dataIds
	 * @param columnName
	 * @return
	 */
	private R<List<Map<String, Object>>> getSelectData(List<String> requiredColumn, String storageLocate, String dataIds, String columnName) {
		// 带入查询sql
		DmlDTO dml = new DmlDTO();
		StringBuilder selectSql = new StringBuilder("select ");
		// 必输项是否包含题名
		//Boolean containTitle = false;
		for(int k =0; k<requiredColumn.size();k++){
/*			if (requiredColumn.get(k).equals(FieldConstants.TITLE_PROPER)){
				containTitle = true;
			}*/
			if(k == 0){
				selectSql.append(requiredColumn.get(k));
			}else{
				selectSql.append(",").append(requiredColumn.get(k));
			}
		}
//		if(!containTitle){
			selectSql.append(",").append(FieldConstants.ID);
//		}
		selectSql.append(" from ");
		selectSql.append(storageLocate).append(" ").append(" where  ")
				.append(columnName).append(" in ( ")
				.append(dataIds).append(" ) and ").append(FieldConstants.IS_DELETE).append(  "='0'");
		dml.setSql(selectSql.toString());
		return remoteArchiveInnerService.executeQuery(dml, SecurityConstants.FROM_IN);
	}

	/**
	 * 取一下卷内表
	 * @param storageLocate
	 * @return
	 */
	private String getjnTableName(String storageLocate) {
		String jnTableNamae = "";
		List<ArchiveTable> lowList = archiveTableService.getDownTableByStorageLocate(storageLocate);
		for(int x = 0; x < lowList.size(); x++ ){
			if (lowList.get(x).getArchiveLayer().equals(ArchiveLayerEnum.FILE.getCode())){
				jnTableNamae = lowList.get(x).getStorageLocate();
			}
		}
		return jnTableNamae;
	}

	/**
	 * 获取必输字段
	 * @param storageLocate
	 * @param moduleId
	 * @return
	 * @throws ArchiveBusinessException
	 */
	private List<String> getReqColByStorageLocateAndModuleId(String storageLocate, Long moduleId) throws ArchiveBusinessException {
		List<String> requiredColumn = new ArrayList<>();
		//检查一下表单必输项是哪些
		ArchiveEditForm aefrom = archiveEditFormService.getEditFormByStorageLocate(storageLocate, moduleId);
		if(aefrom.getId() == null){
			//查询一下全部
			aefrom = archiveEditFormService.getEditFormByStorageLocate(storageLocate, -1L);
			if(aefrom.getId() == null){
				throw  new ArchiveBusinessException("查询表单相关配置信息失败!");
			}
		}

		Object  formContent  = aefrom.getFormContent();
		Map<String, Object> beanMap = BeanUtil.beanToMap(formContent);
		ArrayList<Map<String,Object>> columnList =  (ArrayList)beanMap.get("list");
		if(columnList.size()>0){
			for(int i=0;i<columnList.size();i++){
				Object rules = columnList.get(i).get("rules");
				if(rules != null){
					ArrayList<Map<String,Object>> ruleList = (ArrayList)rules;
					for(int j =0; j<ruleList.size(); j++){
						if(ruleList.get(j).get("required")!= null){
							requiredColumn.add(columnList.get(i).get("model").toString());
						}
					}
				}
			}
		}
		return requiredColumn;
	}
}
