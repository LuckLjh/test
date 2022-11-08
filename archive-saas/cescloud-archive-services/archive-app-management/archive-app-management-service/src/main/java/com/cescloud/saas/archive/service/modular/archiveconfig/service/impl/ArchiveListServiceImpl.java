
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.constant.ConfigConstant;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveListMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveConfigManage;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveList;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchivePageModeConfig;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.constant.ListAlignmentEnum;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantMenu;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantMenuService;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.util.DynamicTableUtil;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchiveListMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveListService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchivePageModeConfigService;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 档案列表配置
 *
 * @author liudong1
 * @date 2019-04-18 21:12:08
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "archive-list")
public class ArchiveListServiceImpl extends ServiceImpl<ArchiveListMapper, ArchiveList> implements ArchiveListService {

	@Autowired
	private ArchiveTableService archiveTableService;
	@Autowired
	private MetadataService metadataService;
	@Autowired
	private RemoteTenantTemplateService remoteTenantTemplateService;
	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;
	@Autowired
	private RemoteTenantMenuService remoteTenantMenuService;
	@Autowired
	private ArchivePageModeConfigService archivePageModeConfigService;

	@Override
	@Cacheable(key = "'archive-app-management:archive-list-service:defined' + #storageLocate + ':' + #moduleId + ':' + #userId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<DefinedListMetadata> listOfDefined(String storageLocate, Long moduleId, Long userId) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseDefined(storageLocate);
		} else {
			List<DefinedListMetadata> definedListMetadata = baseMapper.listOfDefined(storageLocate, moduleId, userId);
			if (CollectionUtil.isEmpty(definedListMetadata)) {
				definedListMetadata = baseMapper.listOfDefined(storageLocate, moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
			}
			return definedListMetadata;
		}
	}

	@Override
	public Map<String,Object> listOfDefinedAndPageNode(String storageLocate, Long moduleId, Long userId) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		List<DefinedListMetadata> definedListMetadata;
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			definedListMetadata = baseMapper.listOfBaseDefined(storageLocate);
		} else {
			definedListMetadata = baseMapper.listOfDefined(storageLocate, moduleId, userId);
			if (CollectionUtil.isEmpty(definedListMetadata)) {
				definedListMetadata = baseMapper.listOfDefined(storageLocate, moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
			}
		}
		ArchivePageModeConfig archivePageModeConfig = archivePageModeConfigService.getArchivePageModeConfigByStorageLocate(storageLocate, moduleId);
		return MapUtil.<String,Object>builder().put("list",definedListMetadata)
				.put("pageMode", ObjectUtil.isNotNull(archivePageModeConfig)?archivePageModeConfig.getPageMode():ConfigConstant.SHOW_TOTAL_OF_PAGES).build();
	}

	@Override
	@Cacheable(key = "'archive-app-management:archive-list-service:defined' + #typeCode + ':'+ #templateTableId + ':' + #moduleId + ':' + #userId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<DefinedListMetadata> listBusinessOfDefined(Long templateTableId, String typeCode, Long moduleId, Long userId) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		List<DefinedListMetadata> definedListMetadata = listOfDefined(storageLocate, moduleId, userId);
		if (CollectionUtil.isEmpty(definedListMetadata)) {
			definedListMetadata = listOfDefined(storageLocate, ArchiveConstants.PUBLIC_MODULE_FLAG, ArchiveConstants.PUBLIC_USER_FLAG);
		}
		return definedListMetadata;
	}

	@Override
	@Cacheable(key = "'archive-app-management:archive-list-service:undefined' + #storageLocate + ':' + #moduleId + ':' + #userId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<DefinedListMetadata> listOfUnDefined(String storageLocate, Long moduleId, Long userId) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseUnDefined(storageLocate);
		} else {
			List<DefinedListMetadata> definedListMetadata = baseMapper.listOfDefined(storageLocate, moduleId, userId);
			if (CollectionUtil.isNotEmpty(definedListMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, moduleId, userId);
			}
			return baseMapper.listOfUnDefined(storageLocate, moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
		}
	}

	@Override
	@Cacheable(key = "'archive-app-management:archive-list-service:undefined' + #typeCode + ':'+ #templateTableId + ':' + #moduleId + ':' + #userId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<DefinedListMetadata> listBusinessOfUnDefined(Long templateTableId, String typeCode, Long moduleId, Long userId) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseUnDefined(storageLocate);
		} else {
			List<DefinedListMetadata> definedListMetadata = baseMapper.listOfDefined(storageLocate, moduleId, userId);
			if (CollectionUtil.isNotEmpty(definedListMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, moduleId, userId);
			}
			definedListMetadata = baseMapper.listOfDefined(storageLocate, moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
			if (CollectionUtil.isNotEmpty(definedListMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
			}
			definedListMetadata = baseMapper.listOfDefined(storageLocate, ArchiveConstants.PUBLIC_MODULE_FLAG, ArchiveConstants.PUBLIC_USER_FLAG);
			if (CollectionUtil.isNotEmpty(definedListMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, ArchiveConstants.PUBLIC_MODULE_FLAG, ArchiveConstants.PUBLIC_USER_FLAG);
			}
			return baseMapper.listOfUnDefined(storageLocate, moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
		}
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "archive-list", allEntries = true),
			@CacheEvict(cacheNames = "archive-config-manage", allEntries = true),
			@CacheEvict(cacheNames = "archive-page-mode",allEntries = true)
	})
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveListDefined(SaveListMetadata saveListMetadata) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(saveListMetadata.getTypeCode(), saveListMetadata.getTemplateTableId());
		if (CollectionUtil.isEmpty(saveListMetadata.getData())) {
			log.error("列表配置规则数据集为空！");
			return new R<>().fail(null, "列表配置项为空！");
		}
		//保存列表配置
		saveListDefinedByStorageLocate(saveListMetadata, storageLocate);
		//保存分页方式配置
		archivePageModeConfigService.saveArchivePageModeConfig(saveListMetadata.getPageMode(),saveListMetadata.getModuleId(),storageLocate);
		return new R<>().success(null, "保存成功！");
	}

	/**
	 * 为了将原save方法 与专题save兼容，提炼出 storageLocate
	 *
	 * @param saveListMetadata
	 * @param storageLocate
	 * @return
	 */
	private void saveListDefinedByStorageLocate(SaveListMetadata saveListMetadata, String storageLocate) {
		Long userId = SecurityUtils.getUser().getId();
		//公共配置 用户id为 -1  菜单id为-1
		if (Boolean.FALSE.equals(saveListMetadata.getTagging())) {
			userId = ArchiveConstants.PUBLIC_MODULE_FLAG;
		}
		if (log.isDebugEnabled()){
			log.debug("删除原来列表[{}]的配置", storageLocate);
		}
		//删除原来的配置
		deleteByStorageLocate(storageLocate, userId, saveListMetadata.getModuleId());
		//批量插入配置
		Long finalUserId = userId;
		List<ArchiveList> archiveLists = IntStream.range(0, saveListMetadata.getData().size())
				.mapToObj(i -> {
					ArchiveList archiveList = new ArchiveList();
					archiveList.setStorageLocate(storageLocate);
					archiveList.setMetadataId(saveListMetadata.getData().get(i).getMetadataId());
					archiveList.setAlign(saveListMetadata.getData().get(i).getAlign());
					archiveList.setSortNo(i);
					archiveList.setModuleId(saveListMetadata.getModuleId());
					archiveList.setUserId(finalUserId);
					archiveList.setWidth(saveListMetadata.getData().get(i).getWidth());
					return archiveList;
				}).collect(Collectors.toList());
		if (log.isDebugEnabled()){
			log.debug("批量插入列表定义规则：{}", archiveLists);
		}
		this.saveBatch(archiveLists);
		if (ArchiveConstants.PUBLIC_USER_FLAG.equals(userId)) {
			archiveConfigManageService.save(storageLocate, saveListMetadata.getModuleId(), TypedefEnum.LIST.getValue());
		}
	}

	/**
	 * 删除规则
	 *
	 * @param storageLocate
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean deleteByStorageLocate(String storageLocate, Long userId, Long moduleId) {
		LambdaQueryWrapper<ArchiveList> wrapper = Wrappers.<ArchiveList>query().lambda()
				.eq(ArchiveList::getStorageLocate, storageLocate);
		if (ObjectUtil.isNotNull(moduleId)) {
			wrapper.eq(ArchiveList::getModuleId, moduleId);
		}
		if (ObjectUtil.isNotEmpty(userId)) {
			wrapper.eq(ArchiveList::getUserId, userId);
		}
		return this.remove(wrapper);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
		InputStream inputStream = getDefaultTemplateStream(templateId);
		if (ObjectUtil.isNull(inputStream)) {
			return new R<>().fail("", "获取初始化文件异常");
		}
		@Cleanup ExcelReader excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.LIST_DEFINE_NAME, true);
		List<List<Object>> read = excel.read();
		final List<ArchiveList> archiveLists = CollectionUtil.newArrayList();
		//获取档案类型
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理门类信息
		Map<String, String> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageName, ArchiveTable::getStorageLocate));
		//获取字段信息
		final List<Metadata> metadatas = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
		final Map<String, Long> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuName, TenantMenu::getMenuId));
		menuMaps.put("全部", -1L);
		final List<ArchiveConfigManage> archiveConfigManages = CollectionUtil.newArrayList();

		IntStream.range(1, read.size()).forEach(i -> {
			List<Object> objectList = read.get(i);
			//获取门类信息
			String archiveName = StrUtil.toString(objectList.get(0));
			//字段名称
			String fieldName = StrUtil.toString(objectList.get(1));
			//对齐方式
			String alignment = StrUtil.toString(objectList.get(2));
			//模块
			String module = StrUtil.toString(objectList.get(3));
			//宽度
			Integer width = InitializeUtil.toInteger(InitializeUtil.checkListVal(objectList, 4));
			if (width == 0) {
				width = null;
			}            //过滤 门类英文 名称
			String storageLocate = archiveTableMap.get(archiveName);
			//过滤字段id
			Metadata metadata1 = metadatas.parallelStream().filter(metadata -> metadata.getStorageLocate().equals(storageLocate) && metadata.getMetadataChinese().equals(fieldName)).findAny().orElseGet(()->new Metadata());
			ArchiveList archiveList = ArchiveList.builder().sortNo(i).tenantId(tenantId).metadataId(metadata1.getId()).storageLocate(storageLocate).width(width)
					.align(ListAlignmentEnum.getEnumByName(alignment).getCode()).userId(ArchiveConstants.PUBLIC_USER_FLAG).moduleId(menuMaps.get(module)).build();
			archiveLists.add(archiveList);
		});

		Boolean batch = Boolean.FALSE;
		if (CollUtil.isNotEmpty(archiveLists)) {
			batch = this.saveBatch(archiveLists);
			archiveLists.parallelStream().collect(Collectors.groupingBy(archiveList -> archiveList.getStorageLocate() + archiveList.getModuleId())).
					forEach((storageLocate, list) -> {
						ArchiveList archiveList = list.get(0);
						ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().storageLocate(archiveList.getStorageLocate()).moduleId(archiveList.getModuleId()).tenantId(tenantId).typedef(TypedefEnum.LIST.getValue()).isDefine(BoolEnum.YES.getCode()).build();
						archiveConfigManages.add(archiveConfigManage);
					});
		}
		if (CollectionUtil.isNotEmpty(archiveConfigManages)) {
			archiveConfigManageService.saveBatch(archiveConfigManages);
		}
		return batch ? new R("", "初始化列表定义成功") : new R().fail(null, "初始化列表定义失败");
	}

	private List<TenantMenu> getArchiveTypeMenuByTenantId(Long tenantId) throws ArchiveBusinessException {
		final R<List<TenantMenu>> archiveTypeMenu = remoteTenantMenuService.getArchiveTypeMenu(tenantId);
		if (archiveTypeMenu.getCode() != CommonConstants.SUCCESS) {
			throw new ArchiveBusinessException("获取租户绑定门类菜单失败");
		}
		return archiveTypeMenu.getData();
	}

	@Override
	public List<ArrayList<String>> getListDefinitionInfo(Long tenantId) throws ArchiveBusinessException {
		//获取档案门类信息
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理门类信息 StorageLocate，archiveTable
		final Map<String, ArchiveTable> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, archiveTable -> archiveTable));
		//获取字段信息
		final List<Metadata> metadata = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
		//处理字段信息 id,MetadataChinese
		final Map<Long, String> metadataMap = metadata.stream().collect(Collectors.toMap(Metadata::getId, Metadata::getMetadataChinese));
		//获取列表定义
		final List<ArchiveList> archiveLists = this.list(Wrappers.<ArchiveList>lambdaQuery().eq(ArchiveList::getTenantId, tenantId));
		Map<Long, String> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuId, TenantMenu::getMenuName));
		menuMaps.put(-1L, "全部");
		//门类名称	字段名称	对齐方式 宽度
		List<ArrayList<String>> collect = archiveLists.stream().map(archiveList -> CollectionUtil.newArrayList(archiveTableMap.get(archiveList.getStorageLocate()).getStorageName(),
				metadataMap.get(archiveList.getMetadataId()),
				ListAlignmentEnum.getEnum(archiveList.getAlign()).getName(),
				menuMaps.get(archiveList.getModuleId()),
				String.valueOf(archiveList.getWidth())
		)).collect(Collectors.toList());
		return collect;
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "archive-list", allEntries = true),
			@CacheEvict(cacheNames = "archive-config-manage", allEntries = true),
			@CacheEvict(cacheNames = "archive-page-mode",allEntries = true)
	})
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeByModuleId(String storageLocate, Long moduleId) {
		boolean remove = this.remove(Wrappers.<ArchiveList>lambdaQuery().eq(ArchiveList::getModuleId, moduleId).eq(ArchiveList::getStorageLocate, storageLocate));
		archivePageModeConfigService.remove(Wrappers.<ArchivePageModeConfig>lambdaQuery().eq(ArchivePageModeConfig::getModuleId,moduleId).eq(ArchivePageModeConfig::getStorageLocate,storageLocate));
		archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.LIST.getValue(), BoolEnum.NO.getCode());
		return remove;
	}

	@Override
	@Caching(evict = {
			@CacheEvict(cacheNames = "archive-list", allEntries = true),
			@CacheEvict(cacheNames = "archive-config-manage", allEntries = true),
			@CacheEvict(cacheNames = "archive-page-mode",allEntries = true)
	})
	@Transactional(rollbackFor = Exception.class)
	public R copy(CopyPostDTO copyPostDTO) {
		final Long sourceModuleId = copyPostDTO.getSourceModuleId();
		final String storageLocate = copyPostDTO.getStorageLocate();
		final List<Long> targetModuleIds = copyPostDTO.getTargetModuleIds();
		if (CollUtil.isNotEmpty(targetModuleIds)) {
			this.remove(Wrappers.<ArchiveList>lambdaQuery().in(ArchiveList::getModuleId, targetModuleIds).eq(ArchiveList::getStorageLocate, storageLocate).eq(ArchiveList::getUserId, ArchiveConstants.PUBLIC_MODULE_FLAG));
			archivePageModeConfigService.remove(Wrappers.<ArchivePageModeConfig>lambdaQuery().in(ArchivePageModeConfig::getModuleId, targetModuleIds));
		}
		final List<ArchiveList> archiveLists = this.list(Wrappers.<ArchiveList>lambdaQuery().eq(ArchiveList::getModuleId, sourceModuleId).eq(ArchiveList::getStorageLocate, storageLocate).eq(ArchiveList::getUserId, ArchiveConstants.PUBLIC_MODULE_FLAG));
		List<ArchivePageModeConfig> archivePageModeConfigs = archivePageModeConfigService.list(Wrappers.<ArchivePageModeConfig>lambdaQuery().eq(ArchivePageModeConfig::getModuleId, sourceModuleId).eq(ArchivePageModeConfig::getStorageLocate, storageLocate));
		if (CollUtil.isEmpty(archiveLists)) {
			return new R<>().fail(null, "当前模块无信息可复制，请先配置当前模块信息。");
		}
		List<ArchiveList> targetArchiveLists = CollUtil.newArrayList();
		List<ArchivePageModeConfig> archivePageConfigs = CollUtil.newArrayList();
		targetModuleIds.forEach(moduleId -> {
			archiveLists.forEach(archiveList -> {
				ArchiveList archiveList1 = new ArchiveList();
				BeanUtil.copyProperties(archiveList, archiveList1);
				archiveList1.setId(null);
				archiveList1.setModuleId(moduleId);
				targetArchiveLists.add(archiveList1);
			});
			archivePageModeConfigs.forEach(e -> {
				ArchivePageModeConfig archivePageModeConfig = ArchivePageModeConfig.builder().pageMode(e.getPageMode()).moduleId(moduleId).storageLocate(e.getStorageLocate()).build();
				archivePageConfigs.add(archivePageModeConfig);
			});
		});
		if (CollUtil.isNotEmpty(targetArchiveLists)) {
			this.saveBatch(targetArchiveLists);
		}
		if (CollUtil.isNotEmpty(archivePageConfigs)) {
			archivePageModeConfigService.saveBatch(archivePageConfigs);
		}
		archiveConfigManageService.saveBatchByModuleIds(storageLocate, targetModuleIds, TypedefEnum.LIST.getValue());
		return new R(null, "复制成功！");
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public Boolean clearListConfiguration(Long templateTableId, String typeCode, Long moduleId) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		CesCloudUser user = SecurityUtils.getUser();
		boolean result = this.remove(Wrappers.<ArchiveList>lambdaQuery().eq(ArchiveList::getUserId, user.getId()).eq(ArchiveList::getStorageLocate, storageLocate).eq(ArchiveList::getModuleId, moduleId));
		return result;
	}

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap) {
		List<ArchiveList> list = this.list(Wrappers.<ArchiveList>lambdaQuery().eq(ArchiveList::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(archiveList -> {
				archiveList.setId(null);
				archiveList.setStorageLocate(destStorageLocate);
				archiveList.setMetadataId(srcDestMetadataMap.get(archiveList.getMetadataId()));
			});
			this.saveBatch(list);
		}
	}

	@Override
	public Long isPublicUserId(Boolean tagging) {
		CesCloudUser user = SecurityUtils.getUser();
		Long userId = user.getId();
		if (Boolean.FALSE.equals(tagging)) {
			userId = -1L;
		}
		return userId;
	}

	@Override
	public List<DefinedListMetadata> listSpecialOfDefined(String storageLocate, Long specialId, Long moduleId, Long userId) {
		/**
		 * 需要组装专题的表名
		 * 比较坑爹哟啊在不影响原表结构的情况下只能转换下表名，原表上加字段字段会影响 ArchiveConfigManageService 修改太多
		 */

		List<DefinedListMetadata> definedListMetadata = baseMapper.listSpecialOfDefined(DynamicTableUtil.getSpecialTableNameWhithSpecialId(storageLocate, specialId), moduleId, userId);
		if (CollectionUtil.isEmpty(definedListMetadata)) {
			definedListMetadata = baseMapper.listSpecialOfDefined(DynamicTableUtil.getSpecialTableNameWhithSpecialId(storageLocate, specialId), moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
		}
		// 去重
		definedListMetadata = definedListMetadata.stream().filter(distinctByKey((p) -> (p.getMetadataEnglish()))).collect(Collectors.toList());
		return definedListMetadata.stream().filter(item -> !TagConstants.UndefinedListTag.contains(item.getMetadataEnglish())).collect(Collectors.toList());
	}
	static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object,Boolean> seen = new ConcurrentHashMap<>();
		//putIfAbsent方法添加键值对，如果map集合中没有该key对应的值，则直接添加，并返回null，如果已经存在对应的值，则依旧为原来的值。
		//如果返回null表示添加数据成功(不重复)，不重复(null==null :TRUE)
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
	@Override
	public List<DefinedListMetadata> listSpecialOfUnDefined(String storageLocate, Long specialId, Long moduleId, Long userId,
	                                                        Integer moduleType, String moduleCode) {
		List<DefinedListMetadata> unDefinedListMetadata = baseMapper.listSpecialOfUnDefined(DynamicTableUtil.getSpecialTableNameWhithSpecialId(storageLocate, specialId), moduleId, userId, moduleType, moduleCode);
		List<DefinedListMetadata> definedListMetadata = baseMapper.listSpecialOfDefined(DynamicTableUtil.getSpecialTableNameWhithSpecialId(storageLocate, specialId), moduleId, userId);
		if (CollectionUtil.isEmpty(definedListMetadata)) {
			definedListMetadata = baseMapper.listSpecialOfDefined(DynamicTableUtil.getSpecialTableNameWhithSpecialId(storageLocate, specialId), moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
			List<Long> idList = definedListMetadata.stream().map(DefinedListMetadata::getId).collect(Collectors.toList());
			unDefinedListMetadata = unDefinedListMetadata.stream().filter(metadata -> idList.contains(metadata.getId())).collect(Collectors.toList());
		}
		return unDefinedListMetadata;
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = "archive-list", allEntries = true),
			@CacheEvict(cacheNames = "archive-config-manage", allEntries = true)
	})
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveSpecialDefined(SaveListMetadata saveListMetadata) {
		String storageLocate = DynamicTableUtil.getSpecialTableNameWhithSpecialId(SecurityUtils.getUser().getTenantId(), saveListMetadata.getTypeCode(), saveListMetadata.getSpecialId());
		if (CollectionUtil.isEmpty(saveListMetadata.getData())) {
			log.error("列表配置规则数据集为空！");
			return new R<>().fail(null, "列表配置项为空！");
		}
		saveListDefinedByStorageLocate(saveListMetadata, storageLocate);
		return new R<>().success(null, "保存成功！");
	}


	/**
	 * 获取 初始化模板文件流
	 *
	 * @param templateId 模板id
	 * @return
	 */
	private InputStream getDefaultTemplateStream(Long templateId) {
		TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
		byte[] bytes = tenantTemplate.getTemplateContent();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		return inputStream;
	}

	@Override
	public R saveDefaultSpecialDefined(String storageLocate, Long specialId, String moduleCode) {

		List<DefinedListMetadata> definedListMetadatas = this.baseMapper.listDefaultSpecialToSave(storageLocate, ModelTypeEnum.SPECIAL.getValue(), moduleCode);
		if (CollUtil.isEmpty(definedListMetadatas)) {
			return new R<>(false);
		}
		SaveListMetadata saveListMetadata = new SaveListMetadata();
		saveListMetadata.setTypeCode(moduleCode);
		saveListMetadata.setTemplateTableId(-1L);
		saveListMetadata.setModuleId(ArchiveModuleEnum.ARCHIVE_SPECIAL.getModuleId());
		saveListMetadata.setTagging(false);
		saveListMetadata.setSpecialId(specialId);
		List<DefinedListMetadata> data = new ArrayList<>();
		for (DefinedListMetadata definedListMetadata : definedListMetadatas) {
			DefinedListMetadata definedConfig = new DefinedListMetadata();

			definedConfig.setMetadataId(definedListMetadata.getMetadataId());
			definedConfig.setAlign(definedListMetadata.getAlign());

			definedConfig.setModuleId(ArchiveModuleEnum.ARCHIVE_SPECIAL.getModuleId());
			definedConfig.setUserId(-1L);
			definedConfig.setWidth(definedListMetadata.getWidth());
			data.add(definedConfig);
		}
		saveListMetadata.setData(data);

		return saveSpecialDefined(saveListMetadata);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean removeSpecialDefinedBySpecialId(String storageLocate) {
		LambdaQueryWrapper<ArchiveList> wrapper = Wrappers.<ArchiveList>query().lambda()
				.eq(ArchiveList::getStorageLocate, storageLocate).notIn(ArchiveList::getUserId,ArchiveConstants.PUBLIC_USER_FLAG);

		//TODO 王谷华 删除配置爱信息
		// ArchiveConfigManageService
		return this.remove(wrapper);
	}


}
