
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.constant.ConfigConstant;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSortMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveSortMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveConfigManage;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveSort;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantMenu;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantMenuService;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.ArchiveConstants;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.constants.TypedefEnum;
import com.cescloud.saas.archive.common.util.DynamicTableUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchiveSortMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveSortService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
 * 档案排序配置
 *
 * @author liudong1
 * @date 2019-04-18 21:18:05
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "archive-sort")
public class ArchiveSortServiceImpl extends ServiceImpl<ArchiveSortMapper, ArchiveSort> implements ArchiveSortService {

	/**
	 * 用于元数据标签的排序字段的 STORAGE_LOCATE
	 * 固定格式为 metadata_storage_locate_{tenantId}
	 * 王谷华 2021-11-01
	 */
	private static final String METADATA_STORAGE_LOCATE="metadata_storage_locate_";

	@Autowired
	private ArchiveTableService archiveTableService;
	@Autowired
	private MetadataService metadataService;
	@Autowired(required = false)
	private RemoteTenantTemplateService remoteTenantTemplateService;
	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;
	@Autowired(required = false)
	private RemoteTenantMenuService remoteTenantMenuService;

	@Override
	@Cacheable(key = "'archive-app-management:archive-sort-service:defined' + #storageLocate + ':' + #moduleId + ':' + #userId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<DefinedSortMetadata> listOfDefined(String storageLocate, Long moduleId, Long userId) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseDefined(storageLocate);
		} else {
			List<DefinedSortMetadata> definedSortMetadata = baseMapper.listOfDefined(storageLocate, moduleId, userId);
			if(CollectionUtil.isEmpty(definedSortMetadata)) {
				definedSortMetadata = baseMapper.listOfDefined(storageLocate, moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
			}
			if (CollectionUtil.isEmpty(definedSortMetadata)) {
				definedSortMetadata = baseMapper.listOfDefined(storageLocate, ArchiveConstants.PUBLIC_MODULE_FLAG, ArchiveConstants.PUBLIC_USER_FLAG);
			}
			return definedSortMetadata;
		}
	}

	@Override
	@Cacheable(key = "'archive-app-management:archive-sort-service:defined' + #typeCode + ':'+ #templateTableId + ':' + #moduleId + ':' + #userId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<DefinedSortMetadata> listBusinessOfDefined(Long templateTableId, String typeCode, Long moduleId, Long userId) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		return listOfDefined(storageLocate, moduleId, userId);
	}

	@Override
	public List<DefinedSortMetadata> listOfUnDefined(String storageLocate, Long moduleId, Boolean tagging) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (ConfigConstant.ADMIN_TENANT_ID.equals(tenantId)) {
			return baseMapper.listOfBaseUnDefined(storageLocate);
		} else {
			CesCloudUser user = SecurityUtils.getUser();
			Long userId = user.getId();
			if (Boolean.FALSE.equals(tagging)) {
				userId = -1L;
			}
			List<DefinedSortMetadata> definedSortMetadata = baseMapper.listOfDefined(storageLocate, moduleId, userId);
			if(CollectionUtil.isEmpty(definedSortMetadata)){
				return baseMapper.listOfUnDefined(storageLocate, moduleId,ArchiveConstants.PUBLIC_MODULE_FLAG);
			}
			return baseMapper.listOfUnDefined(storageLocate, moduleId,userId);
		}
	}

	@Override
	@Cacheable(key = "'archive-app-management:archive-sort-service:undefined' + #typeCode + ':'+ #templateTableId + ':' + #moduleId + ':' + #userId",
			unless = "#result == null || #result.size() == 0"
	)
	public List<DefinedSortMetadata> listBusinessOfUnDefined(Long templateTableId, String typeCode, Long moduleId, Long userId) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (ConfigConstant.ADMIN_TENANT_ID.equals(tenantId)) {
			return baseMapper.listOfBaseUnDefined(storageLocate);
		} else {
			List<DefinedSortMetadata> definedSortMetadata = baseMapper.listOfDefined(storageLocate, moduleId, userId);
			if (CollUtil.isNotEmpty(definedSortMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, moduleId, userId);
			}
			definedSortMetadata = baseMapper.listOfDefined(storageLocate, moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
			if (CollUtil.isNotEmpty(definedSortMetadata)) {
				return baseMapper.listOfUnDefined(storageLocate, moduleId, ArchiveConstants.PUBLIC_USER_FLAG);
			}
			return baseMapper.listOfUnDefined(storageLocate, ArchiveConstants.PUBLIC_MODULE_FLAG, ArchiveConstants.PUBLIC_USER_FLAG);
		}
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveSortDefined(SaveSortMetadata saveSortMetadata) {
		boolean empty = CollectionUtil.isEmpty(saveSortMetadata.getData());
		if (empty) {
			log.error("排序配置规则数据集为空！");
			return new R().fail(null, "排序配置项为空！");
		}
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(saveSortMetadata.getTypeCode(), saveSortMetadata.getTemplateTableId());
		Long userId = SecurityUtils.getUser().getId();
		//公共配置 用户id为 -1  菜单id为-1
		if (Boolean.FALSE.equals(saveSortMetadata.getTagging())) {
			userId = ArchiveConstants.PUBLIC_MODULE_FLAG;
		}
		log.debug("删除原来排序<{}>的配置", storageLocate);
		//删除原来的配置
		deleteByStorageLocate(storageLocate, saveSortMetadata.getModuleId(),userId);
		//批量插入配置
		Long finalUserId = userId;
		List<ArchiveSort> archiveSorts = IntStream.range(0, saveSortMetadata.getData().size())
				.mapToObj(i -> {
					ArchiveSort archiveSort = new ArchiveSort();
					archiveSort.setStorageLocate(storageLocate);
					archiveSort.setMetadataId(saveSortMetadata.getData().get(i).getMetadataId());
					archiveSort.setSortSign(saveSortMetadata.getData().get(i).getSortSign());
					archiveSort.setSortNo(i);
					archiveSort.setModuleId(saveSortMetadata.getModuleId());
					archiveSort.setUserId(finalUserId);
					return archiveSort;
				}).collect(Collectors.toList());
		log.debug("批量插入排序定义规则：{}", archiveSorts.toString());
		this.saveBatch(archiveSorts);
		if(ArchiveConstants.PUBLIC_USER_FLAG.equals(userId)){
			archiveConfigManageService.save(storageLocate, saveSortMetadata.getModuleId(), TypedefEnum.SORT.getValue());
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
	public boolean deleteByStorageLocate(String storageLocate, Long moduleId,Long userId) {
		LambdaQueryWrapper<ArchiveSort> lambdaQueryWrapper = Wrappers.<ArchiveSort>query().lambda()
				.eq(ArchiveSort::getStorageLocate, storageLocate);
		if (ObjectUtil.isNotNull(moduleId)) {
			lambdaQueryWrapper.eq(ArchiveSort::getModuleId, moduleId);
		}
		if(ObjectUtil.isNotEmpty(userId)){
			lambdaQueryWrapper.eq(ArchiveSort::getUserId, userId);
		}
		return this.remove(lambdaQueryWrapper);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
		ExcelReader excel = null;
		try {
			InputStream inputStream = getDefaultTemplateStream(templateId);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.SORT_DEFINE_NAME, true);
			final List<List<Object>> read = excel.read();
			final List<ArchiveSort> sortList = CollectionUtil.newArrayList();
			//获取档案门类
			final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
			//处理门类信息
			final Map<String, String> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageName, ArchiveTable::getStorageLocate));
			//获取字段信息
			final List<Metadata> metadatas = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
			final Map<String, Long> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuName, TenantMenu::getMenuId));
			menuMaps.put("全部", -1L);
			final List<ArchiveConfigManage> archiveConfigManages = CollectionUtil.newArrayList();
			IntStream.range(1, read.size()).forEach(i -> {
				List<Object> objectList = read.get(i);
				//门类名称
				String archiveName = StrUtil.toString(objectList.get(0));
				//字段
				String field = StrUtil.toString(objectList.get(1));
				//排序方式
				String sort = StrUtil.toString(objectList.get(2));
				//模块
				String module = StrUtil.toString(read.get(i).get(3));
				sort = "升序".equals(sort) ? "ASC" : "DESC";
				//过滤 门类英文 名称
				String storageLocate = archiveTableMap.get(archiveName);
				//过滤字段id
				Metadata metadata1 = metadatas.parallelStream().filter(metadata -> metadata.getStorageLocate().equals(storageLocate) && metadata.getMetadataChinese().equals(field)).findAny().orElseGet(()->new Metadata());
				ArchiveSort archiveSort = ArchiveSort.builder().sortNo(i).metadataId(metadata1.getId()).tenantId(tenantId).storageLocate(storageLocate).sortSign(sort).userId(ArchiveConstants.PUBLIC_USER_FLAG).moduleId(menuMaps.get(module)).build();
				sortList.add(archiveSort);
			});

			boolean batch = Boolean.FALSE;
			if (CollUtil.isNotEmpty(sortList)) {
				batch = this.saveBatch(sortList);
				sortList.parallelStream().collect(Collectors.groupingBy(archiveSort -> archiveSort.getStorageLocate() + archiveSort.getModuleId())).
						forEach((storageLocate, list) -> {
							ArchiveSort archiveSort = list.get(0);
							ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().tenantId(tenantId).storageLocate(archiveSort.getStorageLocate()).moduleId(archiveSort.getModuleId()).typedef(TypedefEnum.SORT.getValue()).isDefine(BoolEnum.YES.getCode()).build();
							archiveConfigManages.add(archiveConfigManage);
						});
			}
			if (CollectionUtil.isNotEmpty(archiveConfigManages)) {
				archiveConfigManageService.saveBatch(archiveConfigManages);
			}
			return batch ? new R("", "初始化排序定义成功") : new R().fail(null, "初始化排序定义失败！！");
		} finally {
			IoUtil.close(excel);
		}
	}

	private List<TenantMenu> getArchiveTypeMenuByTenantId(Long tenantId) throws ArchiveBusinessException {
		final R<List<TenantMenu>> archiveTypeMenu = remoteTenantMenuService.getArchiveTypeMenu(tenantId);
		if (archiveTypeMenu.getCode() != CommonConstants.SUCCESS) {
			throw new ArchiveBusinessException("获取租户绑定门类菜单失败");
		}
		return archiveTypeMenu.getData();
	}

	@Override
	public List<ArrayList<String>> getSortDefinitionInfo(Long tenantId) throws ArchiveBusinessException {
		//获取档案门类信息
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理门类信息 StorageLocate，archiveTable
		final Map<String, ArchiveTable> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, archiveTable -> archiveTable));
		//获取字段信息
		final List<Metadata> metadata = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
		//处理字段信息 id,MetadataChinese
		final Map<Long, String> metadataMap = metadata.stream().collect(Collectors.toMap(Metadata::getId, Metadata::getMetadataChinese));
		//获取排序字段信息
		final List<ArchiveSort> archiveSorts = this.list(Wrappers.<ArchiveSort>lambdaQuery().eq(ArchiveSort::getTenantId, tenantId).orderByAsc(ArchiveSort::getSortNo));
		Map<Long, String> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuId, TenantMenu::getMenuName));
		menuMaps.put(-1L, "全部");
		//门类名称	字段名称	排序方式
		List<ArrayList<String>> collect = archiveSorts.stream().map(archiveSort ->
				CollectionUtil.newArrayList(ObjectUtil.isNotNull(archiveTableMap.get(archiveSort.getStorageLocate())) ? archiveTableMap.get(archiveSort.getStorageLocate()).getStorageName() : "",
						metadataMap.get(archiveSort.getMetadataId()),
						"ASC".equals(archiveSort.getSortSign()) ? "升序" : "降序",
						menuMaps.get(archiveSort.getModuleId())
				)).collect(Collectors.toList());
		return collect;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public Boolean removeByModuleId(String storageLocate, Long moduleId) {
		boolean remove = this.remove(Wrappers.<ArchiveSort>lambdaQuery().eq(ArchiveSort::getModuleId, moduleId).eq(ArchiveSort::getStorageLocate, storageLocate));
		archiveConfigManageService.update(storageLocate, moduleId, TypedefEnum.SORT.getValue(), BoolEnum.NO.getCode());
		return remove;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R copy(CopyPostDTO copyPostDTO) {
		final Long sourceModuleId = copyPostDTO.getSourceModuleId();
		final String storageLocate = copyPostDTO.getStorageLocate();
		final List<Long> targetModuleIds = copyPostDTO.getTargetModuleIds();
		if(CollectionUtil.isNotEmpty(targetModuleIds)){
			this.remove(Wrappers.<ArchiveSort>lambdaQuery().eq(ArchiveSort::getStorageLocate, storageLocate).in(ArchiveSort::getModuleId, targetModuleIds).eq(ArchiveSort::getUserId,ArchiveConstants.PUBLIC_MODULE_FLAG));
		}
		List<ArchiveSort> archiveSorts = this.list(Wrappers.<ArchiveSort>lambdaQuery().eq(ArchiveSort::getStorageLocate, storageLocate).eq(ArchiveSort::getModuleId, sourceModuleId).eq(ArchiveSort::getUserId,ArchiveConstants.PUBLIC_MODULE_FLAG));
		if (CollectionUtil.isEmpty(archiveSorts)) {
			return new R().fail(null, "当前模块无信息可复制，请先配置当前模块信息。");
		}
		List<ArchiveSort> archiveSortList = CollectionUtil.newArrayList();
		targetModuleIds.forEach(moduleId->{
			archiveSorts.forEach(archiveSort -> {
				ArchiveSort archiveSort1 =new ArchiveSort();
				BeanUtil.copyProperties(archiveSort,archiveSort1);
				archiveSort1.setId(null);
				archiveSort1.setModuleId(moduleId);
				archiveSortList.add(archiveSort1);
			});
		});
		if(CollectionUtil.isNotEmpty(archiveSortList)){
			this.saveBatch(archiveSortList);
		}
		archiveConfigManageService.saveBatchByModuleIds(storageLocate, targetModuleIds, TypedefEnum.SORT.getValue());
		return new R(null, "复制成功！");
	}

	@Override
	public List<DefinedSortMetadata> getArchiveSortDefList(String storageLocate, Long moduleId) {
		return listOfDefined(storageLocate, moduleId, SecurityUtils.getUser().getId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public Boolean clearListConfiguration(Long templateTableId,String  typeCode, Long moduleId) {
		String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		CesCloudUser user = SecurityUtils.getUser();
		boolean result = this.remove(Wrappers.<ArchiveSort>lambdaQuery().eq(ArchiveSort::getUserId, user.getId()).eq(ArchiveSort::getStorageLocate, storageLocate).eq(ArchiveSort::getModuleId, moduleId));
		return result;
	}


	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long,Long> srcDestMetadataMap) {
		List<ArchiveSort> list = this.list(Wrappers.<ArchiveSort>lambdaQuery().eq(ArchiveSort::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(archiveSort -> {
				archiveSort.setId(null);
				archiveSort.setStorageLocate(destStorageLocate);
				archiveSort.setMetadataId(srcDestMetadataMap.get(archiveSort.getMetadataId()));
			});
			this.saveBatch(list);
		}
	}

	/**
	 * 获取 初始化模板文件流
	 *
	 * @param templateId 模板id
	 * @return
	 */
	private InputStream getDefaultTemplateStream(Long templateId) {
		TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
		byte[] bytes = (byte[]) tenantTemplate.getTemplateContent();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		return inputStream;
	}


	@Override
	public List<DefinedSortMetadata> listSpecialOfDefined(String storageLocate, Long specialId, Long moduleId, Boolean tagging) {
		// 专题的排序不定义不需要特别强制

		CesCloudUser user = SecurityUtils.getUser();
		Long userId = user.getId();
		List<DefinedSortMetadata> definedSortMetadata;
		if (Boolean.FALSE.equals(tagging)) {
			userId = -1L;
			definedSortMetadata =this.baseMapper.listSpecialOfDefined(DynamicTableUtil.getSpecialTableNameWhithSpecialId(storageLocate,specialId), moduleId, userId);
		}else {
			definedSortMetadata =this.baseMapper.listSpecialOfDefined(DynamicTableUtil.getSpecialTableNameWhithSpecialId(storageLocate,specialId), moduleId, userId);
			if(CollectionUtil.isEmpty(definedSortMetadata)) {
				definedSortMetadata = baseMapper.listSpecialOfDefined(DynamicTableUtil.getSpecialTableNameWhithSpecialId(storageLocate, specialId), moduleId, -1L);
			}

		}
		return definedSortMetadata;
	}

	@Override
	public List<DefinedSortMetadata> listSpecialOfUnDefined(String storageLocate, Long specialId,Long moduleId, Boolean tagging,
															Integer moduleType, String moduleCode) {
		// 专题的排序不定义不需要特别强制
		List<DefinedSortMetadata> definedSortMetadata = this.baseMapper.listSpecialOfUnDefined(  DynamicTableUtil.getSpecialTableNameWhithSpecialId(storageLocate,specialId),
				  moduleId,  tagging?SecurityUtils.getUser().getId():ArchiveConstants.PUBLIC_USER_FLAG,  moduleType,  moduleCode);
		// 去重
		definedSortMetadata = definedSortMetadata.stream().filter(distinctByKey((p) -> (p.getMetadataEnglish()))).collect(Collectors.toList());
		return definedSortMetadata;
	}
	static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object,Boolean> seen = new ConcurrentHashMap<>();
		//putIfAbsent方法添加键值对，如果map集合中没有该key对应的值，则直接添加，并返回null，如果已经存在对应的值，则依旧为原来的值。
		//如果返回null表示添加数据成功(不重复)，不重复(null==null :TRUE)
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
	@Override
	public R saveSpecialSortDefined(SaveSortMetadata saveSortMetadata) {
		boolean empty = CollectionUtil.isEmpty(saveSortMetadata.getData());
		if (empty) {
			log.error("排序配置规则数据集为空！");
			return new R().fail(null, "排序配置项为空！");
		}
//		String storageLocate = DynamicTableUtil.getSpecialTableNameWhithSpecialId(archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(saveSortMetadata.getTypeCode(), saveSortMetadata.getTemplateTableId()),saveSortMetadata.getSpecialId());
		String storageLocate = DynamicTableUtil.getSpecialTableNameWhithSpecialId(SecurityUtils.getUser().getTenantId(), saveSortMetadata.getTypeCode(),saveSortMetadata.getSpecialId());
		Long userId = SecurityUtils.getUser().getId();
		//公共配置 用户id为 -1  菜单id为-1
		if (Boolean.FALSE.equals(saveSortMetadata.getTagging())) {
			userId = ArchiveConstants.PUBLIC_MODULE_FLAG;
		}
		log.debug("删除原来排序<{}>的配置", storageLocate);
		//删除原来的配置
		deleteByStorageLocate(storageLocate, saveSortMetadata.getModuleId(),userId);
		//批量插入配置
		Long finalUserId = userId;
		List<ArchiveSort> archiveSorts = IntStream.range(0, saveSortMetadata.getData().size())
				.mapToObj(i -> {
					ArchiveSort archiveSort = new ArchiveSort();
					archiveSort.setStorageLocate(storageLocate);
					archiveSort.setMetadataId(saveSortMetadata.getData().get(i).getMetadataId());
					archiveSort.setSortSign(saveSortMetadata.getData().get(i).getSortSign());
					archiveSort.setSortNo(i);
					archiveSort.setModuleId(saveSortMetadata.getModuleId());
					archiveSort.setUserId(finalUserId);
					return archiveSort;
				}).collect(Collectors.toList());
		log.debug("批量插入排序定义规则：{}", archiveSorts.toString());
		this.saveBatch(archiveSorts);
		if(ArchiveConstants.PUBLIC_USER_FLAG.equals(userId)){
			archiveConfigManageService.save(storageLocate, saveSortMetadata.getModuleId(), TypedefEnum.SORT.getValue());
		}
		return new R().success(null, "保存成功！");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public boolean removeSpecialDefinedBySpecialId(String storageLocate) {
		LambdaQueryWrapper<ArchiveSort> wrapper = new LambdaQueryWrapper();

		wrapper.eq(ArchiveSort::getStorageLocate, storageLocate);

		//TODO 王谷华 删除配置爱信息
		// ArchiveConfigManageService
		return this.remove(wrapper);

	}


	/**
	 * 元数据的排序字段，一个租户全局通用
	 * 故 moduleId userId 固定为 -1
	 *
	 * 因 唯一参数 tenantId 在方法内获取，不添加 @CacheEvict(allEntries = true) 缓存
	 *
	 * @return
	 */
	@Override
	public List<DefinedSortMetadata> listMetadataOfDefined() {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		List<DefinedSortMetadata> definedSortMetadata = baseMapper.listMetadataOfDefined(getMetadataStorageLocate(tenantId), ArchiveConstants.PUBLIC_MODULE_FLAG, ArchiveConstants.PUBLIC_USER_FLAG);
		return definedSortMetadata;
	}

	/**
	 * 同上 未使用的的元数据排序字段
	 * @return
	 */
	@Override
	public List<DefinedSortMetadata> listMetadataOfUnDefined() {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		List<DefinedSortMetadata> definedSortMetadata = baseMapper.listMetadataOfUnDefined(ArchiveConstants.PUBLIC_MODULE_FLAG, ArchiveConstants.PUBLIC_USER_FLAG);
		return definedSortMetadata;

	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public R savetMetadataSortDefined(SaveSortMetadata saveSortMetadata) {
		boolean empty = CollectionUtil.isEmpty(saveSortMetadata.getData());

		Long tenantId = SecurityUtils.getUser().getTenantId();
		String storageLocate = getMetadataStorageLocate(tenantId);
		if(log.isDebugEnabled()){
			log.debug("删除原来排序<{}>的配置", storageLocate);
		}

		//删除原来的配置
		deleteByStorageLocate(storageLocate, ArchiveConstants.PUBLIC_MODULE_FLAG, ArchiveConstants.PUBLIC_USER_FLAG);
		//批量插入配置
		if (empty) {

			return new R().success(null, "排序配置删除成功！");
		}
		List<ArchiveSort> archiveSorts = IntStream.range(0, saveSortMetadata.getData().size())
				.mapToObj(i -> {
					ArchiveSort archiveSort = new ArchiveSort();
					archiveSort.setStorageLocate(storageLocate);
					archiveSort.setMetadataId(saveSortMetadata.getData().get(i).getMetadataId());
					archiveSort.setSortSign(saveSortMetadata.getData().get(i).getSortSign());
					archiveSort.setSortNo(i);
					archiveSort.setModuleId(ArchiveConstants.PUBLIC_MODULE_FLAG);
					archiveSort.setUserId(ArchiveConstants.PUBLIC_USER_FLAG);
					return archiveSort;
				}).collect(Collectors.toList());
		if(log.isDebugEnabled()) {
			log.debug("批量插入排序定义规则：{}", archiveSorts.toString());
		}
		this.saveBatch(archiveSorts);

		return new R().success(null, "保存成功！");

	}

	/**
	 * 用于元数据标签的排序字段
	 * 固定格式为 metadata_storage_locate_{tenantId}
	 * 王谷华 2021-11-01
	 * @param tenantId
	 * @return
	 */
	private String getMetadataStorageLocate(Long tenantId){
		return METADATA_STORAGE_LOCATE+ tenantId;
	}

}
