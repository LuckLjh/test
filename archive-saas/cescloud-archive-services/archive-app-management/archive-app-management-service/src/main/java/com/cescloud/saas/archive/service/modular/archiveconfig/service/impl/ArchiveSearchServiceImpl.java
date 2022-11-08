
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.constant.ConfigConstant;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ArchiveTreeQueryDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveSearchMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SearchListDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveConfigManage;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveSearch;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantMenu;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantMenuService;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.search.OperatorKey;
import com.cescloud.saas.archive.common.util.DynamicTableUtil;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchiveSearchMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveSearchService;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 档案检索配置
 *
 * @author liudong1
 * @date 2019-05-27 16:52:00
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "archive-search")
public class ArchiveSearchServiceImpl extends ServiceImpl<ArchiveSearchMapper, ArchiveSearch> implements ArchiveSearchService {

	@Autowired
	private ArchiveTableService archiveTableService;
	@Autowired
	private MetadataService metadataService;
	@Autowired
	private DictItemService dictItemService;
	@Autowired
	private RemoteTenantTemplateService remoteTenantTemplateService;
	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;
	@Autowired
	private RemoteTenantMenuService remoteTenantMenuService;


	@Override
	public List<DefinedSearchMetadata> listOfDefined(SearchListDTO searchListDTO) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(searchListDTO.getTypeCode(), searchListDTO.getTemplateTableId());
		return listOfDefinedByStorageLocate(storageLocate, searchListDTO.getSearchType(), searchListDTO.getTagging(), searchListDTO.getModuleId());
	}

	@Override
	public List<DefinedSearchMetadata> listBusinessOfDefined(SearchListDTO searchListDTO) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(searchListDTO.getTypeCode(), searchListDTO.getTemplateTableId());
		List<DefinedSearchMetadata> definedSearchMetadata = listOfDefinedByStorageLocate(storageLocate, searchListDTO.getSearchType(), searchListDTO.getTagging(), searchListDTO.getModuleId());
		if (CollectionUtil.isEmpty(definedSearchMetadata)) {
			definedSearchMetadata = listOfDefinedByStorageLocate(storageLocate, searchListDTO.getSearchType(), searchListDTO.getTagging(), ArchiveConstants.PUBLIC_MODULE_FLAG);
		}
		return definedSearchMetadata;
	}

	@Override
	public List<DefinedSearchMetadata> getArchiveListdefList(String storageLocate, Integer searchType, Boolean tagging, Long moduleId) {
		List<DefinedSearchMetadata> definedSearchMetadata = listOfDefinedByStorageLocate(storageLocate, searchType, tagging, moduleId);
		if (CollectionUtil.isEmpty(definedSearchMetadata)) {
			definedSearchMetadata = listOfDefinedByStorageLocate(storageLocate, searchType, tagging, ArchiveConstants.PUBLIC_MODULE_FLAG);
		}
		return definedSearchMetadata;
	}

	@Override
	public List<DefinedSearchMetadata> listOfDefinedByStorageLocate(String storageLocate, Integer searchType, Boolean tagging, Long moduleId) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseDefined(storageLocate);
		} else {
			CesCloudUser user = SecurityUtils.getUser();
			Long userId = user.getId();
			if (Boolean.FALSE.equals(tagging)) {
				userId = -1L;
			}
			List<DefinedSearchMetadata> definedSearchMetadata = baseMapper.listOfDefined(storageLocate, userId, searchType, moduleId);
			if (CollUtil.isEmpty(definedSearchMetadata)) {
				definedSearchMetadata = baseMapper.listOfDefined(storageLocate, ArchiveConstants.PUBLIC_USER_FLAG, null, moduleId);
			}
			return definedSearchMetadata;
		}
	}

	@Override
	public List<DefinedSearchMetadata> listOfUnDefined(SearchListDTO searchListDTO) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(searchListDTO.getTypeCode(), searchListDTO.getTemplateTableId());
		Long tenantId = SecurityUtils.getUser().getTenantId();
		Long moduleId = searchListDTO.getModuleId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseUnDefined(storageLocate);
		} else {
			CesCloudUser user = SecurityUtils.getUser();
			Long userId = user.getId();
			if (Boolean.FALSE.equals(searchListDTO.getTagging())) {
				userId = -1L;
			}
			List<DefinedSearchMetadata> definedSearchMetadata = baseMapper.listOfDefined(storageLocate, userId, searchListDTO.getSearchType(), moduleId);
			if (CollUtil.isNotEmpty(definedSearchMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, userId, searchListDTO.getSearchType(), moduleId);
			}
			return baseMapper.listOfUnDefined(storageLocate, ArchiveConstants.PUBLIC_USER_FLAG, null, moduleId);
		}
	}

	@Override
	public List<DefinedSearchMetadata> listBusinessOfUnDefined(SearchListDTO searchListDTO) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(searchListDTO.getTypeCode(), searchListDTO.getTemplateTableId());
		Long tenantId = SecurityUtils.getUser().getTenantId();
		Long moduleId = searchListDTO.getModuleId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseUnDefined(storageLocate);
		} else {
			CesCloudUser user = SecurityUtils.getUser();
			Long userId = user.getId();
			if (Boolean.FALSE.equals(searchListDTO.getTagging())) {
				userId = ArchiveConstants.PUBLIC_USER_FLAG;
			}
			List<DefinedSearchMetadata> definedSearchMetadata = baseMapper.listOfDefined(storageLocate, userId, searchListDTO.getSearchType(), moduleId);
			if (CollectionUtil.isNotEmpty(definedSearchMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, userId, searchListDTO.getSearchType(), moduleId);
			}
			definedSearchMetadata = baseMapper.listOfDefined(storageLocate, ArchiveConstants.PUBLIC_USER_FLAG, null, moduleId);
			if (CollectionUtil.isNotEmpty(definedSearchMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, ArchiveConstants.PUBLIC_USER_FLAG, null, moduleId);
			}
			definedSearchMetadata = baseMapper.listOfDefined(storageLocate, ArchiveConstants.PUBLIC_USER_FLAG, null, ArchiveConstants.PUBLIC_MODULE_FLAG);
			if (CollUtil.isNotEmpty(definedSearchMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, ArchiveConstants.PUBLIC_USER_FLAG, null, ArchiveConstants.PUBLIC_MODULE_FLAG);
			}
			return baseMapper.listOfUnDefined(storageLocate, userId, searchListDTO.getSearchType(), moduleId);

		}
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveSearchDefined(SaveSearchMetadata saveSearchMetadata) {
		if (CollectionUtil.isEmpty(saveSearchMetadata.getData())) {
			log.error("检索配置规则数据集为空！");
			return new R().fail(null, "列表配置项为空！");
		}
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(saveSearchMetadata.getTypeCode(), saveSearchMetadata.getTemplateTableId());
		log.debug("删除原来检索[{}]的配置", storageLocate);
		Long userId = SecurityUtils.getUser().getId();
		Long moduleId = saveSearchMetadata.getModuleId();
		//公共配置 用户id为 -1  菜单id为-1
		if (Boolean.FALSE.equals(saveSearchMetadata.getTagging())) {
			userId = ArchiveConstants.PUBLIC_MODULE_FLAG;
		}
		//删除原来的配置
		deleteByStorageLocate(storageLocate, userId, saveSearchMetadata.getSearchType(), moduleId);
		//批量插入配置
		Long finalUserId = userId;
		Long finalMeunId = moduleId;
		List<ArchiveSearch> archiveSearchs = IntStream.range(0, saveSearchMetadata.getData().size())
				.mapToObj(i -> {
					ArchiveSearch archiveSearch = new ArchiveSearch();
					archiveSearch.setStorageLocate(storageLocate);
					archiveSearch.setMetadataId(saveSearchMetadata.getData().get(i).getMetadataId());
					archiveSearch.setConditionType(saveSearchMetadata.getData().get(i).getConditionType());
					archiveSearch.setDictKeyValue(saveSearchMetadata.getData().get(i).getDictKeyValue());
					archiveSearch.setSortNo(i);
					archiveSearch.setModuleId(finalMeunId);
					archiveSearch.setUserId(finalUserId);
					archiveSearch.setSearchType(saveSearchMetadata.getSearchType());
					return archiveSearch;
				}).collect(Collectors.toList());
		log.debug("批量插入检索定义规则：{}", archiveSearchs.toString());
		if (CollectionUtil.isNotEmpty(archiveSearchs)) {
			this.saveBatch(archiveSearchs);
		}
		if (Boolean.FALSE.equals(saveSearchMetadata.getTagging())) {
			archiveConfigManageService.save(storageLocate, saveSearchMetadata.getModuleId(), TypedefEnum.SEARCH.getValue());
		}
		return new R().success(null, "保存成功！");
	}

	/**
	 * 删除规则
	 *
	 * @param storageLocate
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean deleteByStorageLocate(String storageLocate, Long userId, Integer searchType, Long moduleId) {
		LambdaQueryWrapper<ArchiveSearch> lambdaQueryWrapper = Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getStorageLocate, storageLocate);
		if (ObjectUtil.isNotNull(userId)) {
			lambdaQueryWrapper.eq(ArchiveSearch::getUserId, userId);
		}
		if (ObjectUtil.isNotNull(searchType)) {
			lambdaQueryWrapper.eq(ArchiveSearch::getSearchType, searchType);
		}
		if (ObjectUtil.isNotNull(moduleId)) {
			lambdaQueryWrapper.eq(ArchiveSearch::getModuleId, moduleId);
		}
		return this.remove(lambdaQueryWrapper);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
		InputStream inputStream = getDefaultTemplateStream(templateId);
		if (ObjectUtil.isNull(inputStream)) {
			return new R<>().fail("", "获取初始化文件异常");
		}
		@Cleanup ExcelReader excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.SEARCH_DEFINE_NAME, true);
		List<List<Object>> read = excel.read();
		final List<ArchiveSearch> searchList = CollectionUtil.<ArchiveSearch>newArrayList();
		//获取档案类型
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理门类信息
		final Map<String, String> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageName, ArchiveTable::getStorageLocate));
		//获取字段信息
		final List<Metadata> metadatas = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
		final Map<String, Long> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuName, TenantMenu::getMenuId));
		menuMaps.put("全部", -1L);
		final List<ArchiveConfigManage> archiveConfigManages = CollectionUtil.newArrayList();
		//循环行
		IntStream.range(1, read.size()).forEach(i -> {
			List<Object> objectList = read.get(i);
			//门类名称
			String archiveName = InitializeUtil.checkListVal(objectList, 0);
			//字段
			String field = InitializeUtil.checkListVal(objectList, 1);
			//条件方式
			String conditionType = InitializeUtil.checkListVal(objectList, 2);
			//模块
			String module = StrUtil.toString(read.get(i).get(3));
			//过滤 门类英文 名称
			String storageLocate = archiveTableMap.get(archiveName);
			//过滤字段id
			Metadata metadata1 = metadatas.parallelStream().filter(metadata -> metadata.getStorageLocate().equals(storageLocate) && metadata.getMetadataChinese().equals(field)).findAny().orElseGet(()-> new Metadata());
			ArchiveSearch archiveSearch = ArchiveSearch.builder().sortNo(i).storageLocate(storageLocate).metadataId(metadata1.getId()).conditionType(OperatorKey.getEnumByName(conditionType).getValue()).userId(-1L).moduleId(menuMaps.get(module)).tenantId(tenantId).build();
			searchList.add(archiveSearch);
		});

		Boolean batch = Boolean.FALSE;
		if (CollUtil.isNotEmpty(searchList)) {
			batch = this.saveBatch(searchList);
			searchList.parallelStream().collect(Collectors.groupingBy(archiveSearch -> archiveSearch.getStorageLocate() + archiveSearch.getModuleId())).
					forEach((storageLocate, list) -> {
						ArchiveSearch archiveSearch = list.get(0);
						ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().tenantId(tenantId).storageLocate(archiveSearch.getStorageLocate()).moduleId(archiveSearch.getModuleId()).typedef(TypedefEnum.SEARCH.getValue()).isDefine(BoolEnum.YES.getCode()).build();
						archiveConfigManages.add(archiveConfigManage);
					});
		}
		if (CollectionUtil.isNotEmpty(archiveConfigManages)) {
			archiveConfigManageService.saveBatch(archiveConfigManages);
		}
		return batch ? new R("", "初始化检索定义成功") : new R().fail(null, "初始化检索定义失败！！！");
	}

	private List<TenantMenu> getArchiveTypeMenuByTenantId(Long tenantId) throws ArchiveBusinessException {
		final R<List<TenantMenu>> archiveTypeMenu = remoteTenantMenuService.getArchiveTypeMenu(tenantId);
		if (archiveTypeMenu.getCode() != CommonConstants.SUCCESS) {
			throw new ArchiveBusinessException("获取租户绑定门类菜单失败");
		}
		return archiveTypeMenu.getData();
	}

	@Override
	public List<ArrayList<String>> getRetrieveDefinitionInfo(Long tenantId) throws ArchiveBusinessException {
		//获取档案门类信息
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理门类信息 StorageLocate，archiveTable
		final Map<String, ArchiveTable> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, archiveTable -> archiveTable));
		//获取字段信息
		final List<Metadata> metadata = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
		//处理字段信息 id,MetadataChinese
		final Map<Long, String> metadataMap = metadata.stream().collect(Collectors.toMap(Metadata::getId, Metadata::getMetadataChinese));
		//获取检索列表
		final List<ArchiveSearch> archiveSearches = this.list(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getTenantId, tenantId));
		Map<Long, String> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuId, TenantMenu::getMenuName));
		menuMaps.put(-1L, "全部");
		//门类名称	字段名称 条件方式 模块
		List<ArrayList<String>> collect = archiveSearches.stream().map(archiveSearch -> CollectionUtil.newArrayList(archiveTableMap.get(archiveSearch.getStorageLocate()).getStorageName(),
				metadataMap.get(archiveSearch.getMetadataId()), OperatorKey.get(archiveSearch.getConditionType()).getName(), menuMaps.get(archiveSearch.getModuleId()))).collect(Collectors.toList());
		return collect;
	}

	@Override
	public Object getBasicRetrievalForm(Long moduleId, String typeCode, Long templateTableId) {
		//获取 表名
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		//过滤当前用户 配置
		CesCloudUser user = SecurityUtils.getUser();
		Long userId = user.getId();
		//获取当前用户配置表单
		List<ArchiveSearch> archiveSearches = this.list(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getStorageLocate, storageLocate).eq(ArchiveSearch::getUserId, userId).eq(ArchiveSearch::getSearchType, SearchTypeEnum.BASIC_SEARCH.getValue()).eq(ArchiveSearch::getModuleId, moduleId).orderByAsc(ArchiveSearch::getSortNo));
		if (CollectionUtil.isEmpty(archiveSearches)) {
			archiveSearches = this.list(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getStorageLocate, storageLocate).eq(ArchiveSearch::getUserId, -1).eq(ArchiveSearch::getModuleId, moduleId).orderByAsc(ArchiveSearch::getSortNo));
		}
		if (CollectionUtil.isEmpty(archiveSearches)) {
			archiveSearches = this.list(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getStorageLocate, storageLocate).eq(ArchiveSearch::getUserId, -1).eq(ArchiveSearch::getModuleId, ArchiveConstants.PUBLIC_MODULE_FLAG).orderByAsc(ArchiveSearch::getSortNo));
		}
		//初始化表单
		LinkedHashMap linkedHashMap = new LinkedHashMap(2);
		//添加表单配置
		linkedHashMap.put(FormConstant.CONFIG, configConfiguration());
		//添加 字段信息
		linkedHashMap.put(FormConstant.LIST, listConfiguration(archiveSearches, storageLocate));

		return JSONArray.toJSON(linkedHashMap);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean clearListConfiguration(SearchListDTO searchListDTO) {
		//获取 表名
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(searchListDTO.getTypeCode(), searchListDTO.getTemplateTableId());
		CesCloudUser user = SecurityUtils.getUser();
		boolean result = this.remove(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getSearchType, searchListDTO.getSearchType()).eq(ArchiveSearch::getUserId, user.getId()).eq(ArchiveSearch::getStorageLocate, storageLocate).eq(ArchiveSearch::getModuleId, searchListDTO.getModuleId()));
		return result;
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeByModuleId(String storageLocate, Long moduleId) {
		boolean remove = this.remove(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getStorageLocate, storageLocate).eq(ArchiveSearch::getModuleId, moduleId));
		archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.SEARCH.getValue(), BoolEnum.NO.getCode());
		return remove;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R copy(CopyPostDTO copyPostDTO) {
		final Long sourceModuleId = copyPostDTO.getSourceModuleId();
		final String storageLocate = copyPostDTO.getStorageLocate();
		final List<Long> targetModuleIds = copyPostDTO.getTargetModuleIds();
		if (CollectionUtil.isNotEmpty(targetModuleIds)) {
			this.remove(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getStorageLocate, storageLocate).in(ArchiveSearch::getModuleId, targetModuleIds));
		}
		List<ArchiveSearch> archiveSearches = this.list(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getStorageLocate, storageLocate).eq(ArchiveSearch::getModuleId, sourceModuleId));
		if (CollectionUtil.isEmpty(archiveSearches)) {
			return new R().fail(null, "当前模块无信息可复制，请先配置当前模块信息。");
		}
		List<ArchiveSearch> archiveSearchList = CollectionUtil.newArrayList();
		targetModuleIds.stream().forEach(moduleId -> {
			archiveSearches.forEach(archiveSearch -> {
				ArchiveSearch archiveSearch1 = new ArchiveSearch();
				BeanUtil.copyProperties(archiveSearch, archiveSearch1);
				archiveSearch1.setId(null);
				archiveSearch1.setModuleId(moduleId);
				archiveSearchList.add(archiveSearch1);
			});
		});
		if (CollectionUtil.isNotEmpty(archiveSearchList)) {
			this.saveBatch(archiveSearchList);
		}
		archiveConfigManageService.saveBatchByModuleIds(storageLocate, targetModuleIds, TypedefEnum.SEARCH.getValue());
		return new R(null, "复制成功！");
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap) {
		List<ArchiveSearch> list = this.list(Wrappers.<ArchiveSearch>lambdaQuery().eq(ArchiveSearch::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(archiveSearch -> {
				archiveSearch.setId(null);
				archiveSearch.setStorageLocate(destStorageLocate);
				archiveSearch.setMetadataId(srcDestMetadataMap.get(archiveSearch.getMetadataId()));
			});
			this.saveBatch(list);
		}
	}

	/**
	 * 拼装 字段 配置
	 *
	 * @param archiveSearches
	 * @return
	 */
	private List listConfiguration(List<ArchiveSearch> archiveSearches, String storageLocate) {
		//获取字段信息
		List<Metadata> metadata = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getStorageLocate, storageLocate));
		Map<Long, Metadata> metadataMap = metadata.stream().collect(Collectors.toMap(Metadata::getId, metadata1 -> metadata1));
		List<Long> metadataIds = archiveSearches.stream().map(archiveSearch -> archiveSearch.getMetadataId()).collect(Collectors.toList());
		metadata = metadataIds.stream().map(integer -> metadataMap.get(integer)).collect(Collectors.toList());
		List list = CollectionUtil.newArrayList();
		metadata.stream().forEach(metadata1 -> {
			ArchiveSearch archiveSearch1 = archiveSearches.parallelStream().filter(archiveSearch -> metadata1.getId().equals(archiveSearch.getMetadataId())).findAny().orElse(null);
			//判断字段类型
			fieldConfigurationProcessing(metadata1, list, archiveSearch1);
		});
		//0是传编码，1是传值
		for (ArchiveSearch archiveSearch : archiveSearches) {
			if (archiveSearch.getDictKeyValue() != null) {
				if (archiveSearch.getDictKeyValue().equals(1)) {
					for (int i = 0; i < list.size(); i++) {
						Map map = (Map) list.get(i);
						if (map.get("model").equals(DictEnum.BGQX.getValue())) {
							List label = (List) ((Map) map.get("options")).get("options");
							for (int j = 0; j < label.size(); j++) {
								Map map2 = (Map) label.get(j);
								map2.put("value", map2.get("label"));
							}
						}
					}
				}
			}
		}
		return list;
	}

	private void fieldConfigurationProcessing(Metadata metadata, List list, ArchiveSearch archiveSearch) {
		String metadataType = metadata.getMetadataType();
		String name = metadata.getMetadataChinese();
		String model = metadata.getMetadataEnglish();
		Map map = null;
		if (MetadataTypeEnum.VARCHAR.getValue().equals(metadataType) && StrUtil.isBlank(metadata.getDictCode())) {
			if (FieldConstants.SERIES_CODE.equals(model)) {
				//如果是分类号,类型为下拉树
				map = fieldPublicProperties(map, FormConstant.TYPE_SELECT_TREE, name, "el-icon-edit", MapUtil.builder().put(FormConstant.SELECT_TREE_TYPE, ArchiveTreeQueryDTO.CLASS_NO_TREE).build(), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
			} else {
				map = fieldPublicProperties(map, "input", name, "el-icon-edit", inputConfiguration(), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
			}
		} else if (MetadataTypeEnum.VARCHAR.getValue().equals(metadataType) && ObjectUtil.isNotNull(metadata.getDictCode())) {
			map = fieldPublicProperties(map, "select", name, "icon-select", selectConfiguration(metadata.getDictCode(), archiveSearch), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
		} else if (MetadataTypeEnum.DATETIME.getValue().equals(metadataType) || MetadataTypeEnum.DATE.getValue().equals(metadataType)) {
			map = fieldPublicProperties(map, "date", name, "el-icon-date", dateConfiguration(archiveSearch), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
		} else if (MetadataTypeEnum.INT.getValue().equals(metadataType)) {
			if (FieldConstants.YEAR_CODE.equals(model)){
				map = fieldPublicProperties(map, "date", name, "el-icon-date", yearDateConfiguration(), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
			}else {
				Map map1 = CollectionUtil.newHashMap();
				map1.put("pattern", "/^\\d+$|^\\d+[.]?\\d+$/");
				map1.put("message", "请输入自然数");
				map = fieldPublicProperties(map, "input", name, "el-icon-d-caret", inputConfiguration(), RandomUtil.randomLong(), model, CollectionUtil.newArrayList(map1));
			}
		}
		list.add(map);
	}

	@Override
	public Map fieldPublicProperties(Map map, String type, String name, String icon, Object options, Long key, String model, List rules) {
		HashMap<String, Object> resultMap = CollectionUtil.newHashMap();
		resultMap.put(FormConstant.TYPE, type);
		resultMap.put(FormConstant.NAME, name);
		resultMap.put(FormConstant.ICON, icon);
		resultMap.put(FormConstant.OPTIONS, options);
		resultMap.put(FormConstant.KEY, key);
		resultMap.put(FormConstant.MODEL, model);
		resultMap.put(FormConstant.RULES, rules);
		return resultMap;
	}

	/**
	 * number 配置
	 *
	 * @return
	 */
	private Map numberConfiguration() {
		Map map = CollectionUtil.newHashMap();
		map.put(FormConstant.REQUIRED, Boolean.FALSE);
		map.put(FormConstant.WIDTH, 24);
		map.put(FormConstant.DEFAULTVALUE, null);
		map.put(FormConstant.MIN, 1);
		map.put(FormConstant.MAX, 9999999);
		map.put(FormConstant.STEP, 1);
		map.put(FormConstant.DISABLED, Boolean.FALSE);
		map.put(FormConstant.CONTROLSPOSITION, Boolean.FALSE);
		map.put(FormConstant.REMOTEFUNC, "");
		return map;
	}

	/**
	 * date 配置信息
	 *
	 * @return
	 */
	private Map dateConfiguration(ArchiveSearch archiveSearch) {
		Map map = CollectionUtil.newHashMap();
		map.put(FormConstant.READONLY, Boolean.FALSE);
		map.put(FormConstant.DISABLED, Boolean.FALSE);
		map.put(FormConstant.EDITABLE, Boolean.TRUE);
		map.put(FormConstant.CLEARABLE, Boolean.TRUE);
		map.put(FormConstant.PLACEHOLDER, "");
		map.put(FormConstant.FORMAT, "yyyy-MM-dd");
		map.put(FormConstant.TIMESTAMP, Boolean.FALSE);
		map.put(FormConstant.REQUIRED, Boolean.FALSE);
		map.put(FormConstant.WIDTH, 24);
		map.put(FormConstant.REMOTEFUNC, "");
		OperatorKey operatorKey = OperatorKey.get(archiveSearch.getConditionType());
		if (OperatorKey.BT.equals(operatorKey)) {
			map.put(FormConstant.TYPE, "daterange");
			map.put(FormConstant.STARTPLACEHOLDER, "开始日期");
			map.put(FormConstant.ENDPLACEHOLDER, "结束日期");
			map.put(FormConstant.RANGESEPARATOR, "至");
		} else {
			map.put(FormConstant.TYPE, "date");
			map.put(FormConstant.STARTPLACEHOLDER, "");
			map.put(FormConstant.ENDPLACEHOLDER, "");
		}
		return map;
	}

	private Map yearDateConfiguration() {
		Map map = CollectionUtil.newHashMap();
		map.put(FormConstant.READONLY, Boolean.FALSE);
		map.put(FormConstant.DISABLED, Boolean.FALSE);
		map.put(FormConstant.EDITABLE, Boolean.TRUE);
		map.put(FormConstant.CLEARABLE, Boolean.TRUE);
		map.put(FormConstant.FORMAT, "yyyy");
		map.put(FormConstant.TIMESTAMP, Boolean.FALSE);
		map.put(FormConstant.REQUIRED, Boolean.FALSE);
		map.put(FormConstant.WIDTH, 24);
		map.put(FormConstant.TYPE, "year");
		map.put(FormConstant.STARTPLACEHOLDER, "");
		map.put(FormConstant.ENDPLACEHOLDER, "");
		return map;
	}

	/**
	 * select 配置信息
	 *
	 * @param dictCode
	 * @return
	 */
	private Map selectConfiguration(String dictCode, ArchiveSearch archiveSearch) {
		Map map = CollectionUtil.newHashMap();
		map.put(FormConstant.DEFAULTVALUE, "");
		OperatorKey operatorKey = OperatorKey.get(archiveSearch.getConditionType());
		if (OperatorKey.IN.equals(operatorKey) || OperatorKey.NIN.equals(operatorKey)) {
			map.put(FormConstant.MULTIPLE, Boolean.TRUE);
		} else {
			map.put(FormConstant.MULTIPLE, Boolean.FALSE);
		}
		map.put(FormConstant.DISABLED, Boolean.FALSE);
		map.put(FormConstant.CLEARABLE, Boolean.FALSE);
		map.put(FormConstant.PLACEHOLDER, "");
		map.put(FormConstant.REQUIRED, Boolean.TRUE);
		map.put(FormConstant.SHOWLABEL, Boolean.TRUE);
		map.put(FormConstant.WIDTH, 24);
		map.put(FormConstant.REMOTE, Boolean.FALSE);
		map.put(FormConstant.FILTERABLE, Boolean.FALSE);
		map.put(FormConstant.REMOTEOPTIONS, CollectionUtil.newArrayList());
		map.put(FormConstant.REMOTEFUNC, "");
		map.put(FormConstant.FORMAT, "");
		if (StrUtil.isNotEmpty(dictCode)) {
			List<DictItem> dictItemList = dictItemService.getItemListByDictCode(dictCode);
			List<Map<String, String>> options = dictItemList.stream().map(dictItem -> {
				Map<String, String> map1 = new HashMap<>();
				map1.put("label", dictItem.getItemLabel());
				map1.put("value", dictItem.getItemCode());
				return map1;
			}).collect(Collectors.toList());
			map.put(FormConstant.OPTIONS, options);
		}
		Map propsMap = CollectionUtil.newHashMap(2);
		propsMap.put("value", "value");
		propsMap.put("label", "label");
		map.put(FormConstant.PROPS, propsMap);
		return map;
	}

	/**
	 * input 配置信息
	 */
	@Override
	public Map inputConfiguration() {
		Map map = CollectionUtil.newHashMap();
		map.put(FormConstant.WIDTH, 24);
		map.put(FormConstant.DEFAULTVALUE, "");
		map.put(FormConstant.REQUIRED, Boolean.FALSE);
		map.put(FormConstant.DATATYPE, "String");
		map.put(FormConstant.PATTERN, "");
		map.put(FormConstant.PLACEHOLDER, "");
		map.put(FormConstant.REMOTEFUNC, "");
		return map;
	}

	/**
	 * 拼装表单 配置
	 *
	 * @return
	 */
	@Override
	public Map configConfiguration() {
		Map map = CollectionUtil.newHashMap(4);
		map.put(FormConstant.LABELWIDTH, 100);
		map.put(FormConstant.LABELPOSITION, "left");
		map.put(FormConstant.SIZE, "medium");
		map.put(FormConstant.SPAN, 24);
		return map;
	}

	@Override
	public List<DefinedSearchMetadata> listOfSpecialDefined(SearchListDTO searchListDTO) {

		log.error("searchListDTO:" + searchListDTO);
		Long tenantId = SecurityUtils.getUser().getTenantId();
		Long moduleId = searchListDTO.getModuleId();
		CesCloudUser user = SecurityUtils.getUser();
		log.error("user" + user);
		Long userId = user.getId();
		if (Boolean.FALSE.equals(searchListDTO.getTagging())) {
			userId = -1L;
		}
		// 专题表名是转义过的
		String storageLocate = DynamicTableUtil.getSpecialTableNameWhithSpecialId(tenantId, searchListDTO.getTypeCode(), searchListDTO.getSpecialId());
		List<DefinedSearchMetadata> definedSearchMetadata = baseMapper.listSpecialOfDefined(storageLocate, userId, searchListDTO.getSearchType(), moduleId);
		if (CollUtil.isEmpty(definedSearchMetadata) && userId > -1L) {
			return baseMapper.listSpecialOfDefined(storageLocate, -1L, searchListDTO.getSearchType(), moduleId);
		}

		return definedSearchMetadata;

	}

	@Override
	public List<DefinedSearchMetadata> listSpecialOfUnDefined(SearchListDTO searchListDTO) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		String storageLocate = DynamicTableUtil.getSpecialTableNameWhithSpecialId(tenantId, searchListDTO.getTypeCode(), searchListDTO.getSpecialId());

		Long moduleId = searchListDTO.getModuleId();
		CesCloudUser user = SecurityUtils.getUser();
		Long userId = user.getId();
		if (Boolean.FALSE.equals(searchListDTO.getTagging())) {
			userId = -1L;
		}
		log.debug("searchListDTO:" + searchListDTO);
		List<DefinedSearchMetadata> definedSearchMetadata = baseMapper.listSpecialOfUnDefined(storageLocate, userId, searchListDTO.getSearchType(), moduleId, searchListDTO.getTypeCode());
		List<DefinedSearchMetadata> list = new ArrayList<>();
		if (CollUtil.isNotEmpty(definedSearchMetadata) && userId > -1L) {
			list = baseMapper.listSpecialOfUnDefined(storageLocate, -1L, searchListDTO.getSearchType(), moduleId, searchListDTO.getTypeCode());
			list = list.stream().filter(distinctByKey((p) -> (p.getMetadataEnglish()))).collect(Collectors.toList());
			return list;
		}
		list = baseMapper.listSpecialOfUnDefined(storageLocate, ArchiveConstants.PUBLIC_USER_FLAG, null, moduleId, searchListDTO.getTypeCode());
		list = list.stream().filter(distinctByKey((p) -> (p.getMetadataEnglish()))).collect(Collectors.toList());
		return list;
	}
	static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object,Boolean> seen = new ConcurrentHashMap<>();
		//putIfAbsent方法添加键值对，如果map集合中没有该key对应的值，则直接添加，并返回null，如果已经存在对应的值，则依旧为原来的值。
		//如果返回null表示添加数据成功(不重复)，不重复(null==null :TRUE)
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
	/**
	 * 获取 初始化模板文件流
	 *
	 * @param templateId 文件ID
	 * @return
	 */
	private InputStream getDefaultTemplateStream(Long templateId) {
		TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
		byte[] bytes = (byte[]) tenantTemplate.getTemplateContent();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		return inputStream;
	}


	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveSpecialSearchDefined(SaveSearchMetadata saveSearchMetadata) {
		Assert.isTrue(!Objects.isNull(saveSearchMetadata.getSpecialId()), "专题ID不能为空");
		if (CollectionUtil.isEmpty(saveSearchMetadata.getData())) {
			log.error("检索配置规则数据集为空！");
			return new R().fail(null, "列表配置项为空！");
		}
		Long tenantId = SecurityUtils.getUser().getTenantId();
		String storageLocate = DynamicTableUtil.getSpecialTableNameWhithSpecialId(tenantId, saveSearchMetadata.getTypeCode(), saveSearchMetadata.getSpecialId());
		log.debug("删除原来检索[{}]的配置", storageLocate);
		Long userId = SecurityUtils.getUser().getId();
		Long moduleId = saveSearchMetadata.getModuleId();
		//公共配置 用户id为 -1  菜单id为-1
		if (Boolean.FALSE.equals(saveSearchMetadata.getTagging())) {
			userId = ArchiveConstants.PUBLIC_MODULE_FLAG;
		}
		//删除原来的配置
		deleteByStorageLocate(storageLocate, userId, saveSearchMetadata.getSearchType(), moduleId);
		//批量插入配置
		Long finalUserId = userId;
		Long finalMeunId = moduleId;
		List<ArchiveSearch> archiveSearchs = IntStream.range(0, saveSearchMetadata.getData().size())
				.mapToObj(i -> {
					ArchiveSearch archiveSearch = new ArchiveSearch();
					archiveSearch.setStorageLocate(storageLocate);
					archiveSearch.setMetadataId(saveSearchMetadata.getData().get(i).getMetadataId());
					archiveSearch.setConditionType(saveSearchMetadata.getData().get(i).getConditionType());
					archiveSearch.setSortNo(i);
					archiveSearch.setModuleId(finalMeunId);
					archiveSearch.setUserId(finalUserId);
					archiveSearch.setSearchType(saveSearchMetadata.getSearchType());
					return archiveSearch;
				}).collect(Collectors.toList());
		log.debug("批量插入检索定义规则：{}", archiveSearchs.toString());
		if (CollectionUtil.isNotEmpty(archiveSearchs)) {
			 this.saveBatch(archiveSearchs);
		}
		if (Boolean.FALSE.equals(saveSearchMetadata.getTagging())) {
			archiveConfigManageService.save(storageLocate, saveSearchMetadata.getModuleId(), TypedefEnum.SEARCH.getValue());
		}
		return new R().success(null, "保存成功！");
	}
}
