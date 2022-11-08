
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
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedEditMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveEditMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveConfigManage;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEdit;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEditForm;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantMenu;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantMenuService;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.constants.TypedefEnum;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchiveEditMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveEditFormService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveEditService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 档案录入配置
 *
 * @author liudong1
 * @date 2019-04-18 16:06:51
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "archive-edit")
public class ArchiveEditServiceImpl extends ServiceImpl<ArchiveEditMapper, ArchiveEdit> implements ArchiveEditService {

	@Autowired
	private ArchiveTableService archiveTableService;
	@Autowired
	private ArchiveEditFormService archiveEditFormService;
	@Autowired
	private MetadataService metadataService;
	@Autowired(required = false)
	private RemoteTenantTemplateService remoteTenantTemplateService;
	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;
	@Autowired(required = false)
	private RemoteTenantMenuService remoteTenantMenuService;

	@Cacheable(
			key = "'archive-app-management:archive-edit:defined:'+#storageLocate+':'+#moduleId",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<DefinedEditMetadata> listOfDefined(String storageLocate, Long moduleId) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseDefined(storageLocate);
		} else {
			return baseMapper.listOfDefined(storageLocate, moduleId);
		}
	}

	@Cacheable(
			key = "'archive-app-management:archive-edit:undefined:'+#storageLocate+':'+#moduleId",
			unless = "#result == null || #result.size() == 0"
	)
	@Override
	public List<DefinedEditMetadata> listOfUnDefined(String storageLocate, Long moduleId) {
		Long tenantId = SecurityUtils.getUser().getTenantId();
		if (tenantId.equals(ConfigConstant.ADMIN_TENANT_ID)) {
			return baseMapper.listOfBaseUnDefined(storageLocate);
		} else {
			return baseMapper.listOfUnDefined(storageLocate, moduleId);
		}
	}

	@CacheEvict(cacheNames = {"archive-edit", "archive-column-rule"}, allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveEditDefined(SaveEditMetadata saveEditMetadata) {
		List<DefinedEditMetadata> definedEditMetadataList = saveEditMetadata.getData();
		boolean empty = CollectionUtil.isEmpty(definedEditMetadataList);
		if (empty) {
			log.error("表单定义字段规则数据集为空！");
			return new R().fail(null, "录入配置项为空！");
		}

		//判断是否被表单设计器引用
		List<String> editFormColumnList = archiveEditFormService.getEditFormColumnByStorageLocate(saveEditMetadata.getStorageLocate(), saveEditMetadata.getModuleId());
		for (String editFormColumn : editFormColumnList) {
			boolean flag = definedEditMetadataList.stream()
					.anyMatch(definedEditMetadata -> editFormColumn.equals(definedEditMetadata.getMetadataEnglish()));
			if (!flag) {
				try {
					Metadata metadata = metadataService.getByStorageLocateAndMetadataEnglish(saveEditMetadata.getStorageLocate(), editFormColumn);
					return new R().fail(null, "字段：" + metadata.getMetadataChinese() + "已经被表单设计使用，不能能删除！");
				} catch (ArchiveBusinessException e) {
					log.error("元数据查询失败", e);
					return new R().fail(null, "字段：" + editFormColumn + "已经被表单设计使用，不能能删除！");
				}
			}
		}

		log.debug("删除原来表单定义字段<{}>的配置", saveEditMetadata.getStorageLocate());
		//删除原来的配置
		deleteByStorageLocate(saveEditMetadata.getStorageLocate(), saveEditMetadata.getModuleId());
		//批量插入配置
		List<ArchiveEdit> archiveEdits = IntStream.range(0, definedEditMetadataList.size())
				.mapToObj(i -> {
					ArchiveEdit archiveEdit = new ArchiveEdit();
					archiveEdit.setStorageLocate(saveEditMetadata.getStorageLocate());
					archiveEdit.setMetadataId(definedEditMetadataList.get(i).getMetadataId());
					archiveEdit.setSortNo(i);
					archiveEdit.setModuleId(saveEditMetadata.getModuleId());
					return archiveEdit;
				}).collect(Collectors.toList());
		log.debug("批量插入表单定义字段规则：{}", archiveEdits.toString());
		this.saveBatch(archiveEdits);
		archiveConfigManageService.save(saveEditMetadata.getStorageLocate(), saveEditMetadata.getModuleId(), TypedefEnum.FROM.getValue());
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
	public boolean deleteByStorageLocate(String storageLocate, Long moduleId) {
		LambdaQueryWrapper<ArchiveEdit> wrapper = Wrappers.<ArchiveEdit>query().lambda()
				.eq(ArchiveEdit::getStorageLocate, storageLocate);
		if (ObjectUtil.isNotNull(moduleId)) {
			wrapper.eq(ArchiveEdit::getModuleId, moduleId);
		}
		return this.remove(wrapper);
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
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.FORM_FIELD_NAME, true);
			final List<List<Object>> read = excel.read();
			final List<ArchiveEdit> archiveEditList = CollectionUtil.newArrayList();
			//获取档案类型
			final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
			//处理门类信息
			final Map<String, String> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageName, ArchiveTable::getStorageLocate));
			//获取字段信息
			final List<Metadata> metadatas = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
			final Map<String, Long> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuName, TenantMenu::getMenuId));
			menuMaps.put("全部", -1L);
			final List<ArchiveConfigManage> archiveConfigManages = CollectionUtil.newArrayList();

			IntStream.range(1,read.size()).forEach(i->{
				List<Object> objectList = read.get(i);
				//门类名称
				String archiveName = StrUtil.toString(objectList.get(0));
				//字段
				String field = StrUtil.toString(objectList.get(1));
				//模块
				String module = StrUtil.toString(read.get(i).get(2));
				//过滤 门类英文 名称
				String storageLocate = archiveTableMap.get(archiveName);
				//过滤字段id
				Metadata metadata1 = metadatas.parallelStream().filter(metadata -> metadata.getStorageLocate().equals(storageLocate) && metadata.getMetadataChinese().equals(field)).findAny().orElseGet(()->new Metadata());
				ArchiveEdit archiveEdit = ArchiveEdit.builder().metadataId(metadata1.getId()).storageLocate(storageLocate).sortNo(i).moduleId(menuMaps.get(module)).tenantId(tenantId).build();
				archiveEditList.add(archiveEdit);
			});

			boolean result = Boolean.FALSE;
			if (CollUtil.isNotEmpty(archiveEditList)) {
				result = this.saveBatch(archiveEditList);
				archiveEditList.parallelStream().collect(Collectors.groupingBy(archiveEditForm -> archiveEditForm.getStorageLocate() + archiveEditForm.getModuleId())).
						forEach((storageLocate, list) -> {
							ArchiveEdit archiveEdit = list.get(0);
							ArchiveConfigManage archiveConfigManage = ArchiveConfigManage.builder().storageLocate(archiveEdit.getStorageLocate()).tenantId(tenantId).moduleId(archiveEdit.getModuleId()).typedef(TypedefEnum.FROM.getValue()).isDefine(BoolEnum.YES.getCode()).build();
							archiveConfigManages.add(archiveConfigManage);
						});
			}
			if (CollectionUtil.isNotEmpty(archiveConfigManages)) {
				archiveConfigManageService.saveBatch(archiveConfigManages);
			}
			return result ? new R("", "初始化表单字段信息成功") : new R().fail(null, "初始化表单字段信息失败！！！");
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
	public List<ArrayList<String>> getFormFieldInfo(Long tenantId) throws ArchiveBusinessException {
		//获取字段信息
		final List<Metadata> metadata = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
		//处理字段信息 id,chinese
		final Map<Long, String> metadatamap = metadata.stream().collect(Collectors.toMap(Metadata::getId, Metadata::getMetadataChinese));
		//获取档案类型
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理档案类型 StorageLocate，archiveLayer
		final Map<String, String> archiveLayermap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, ArchiveTable::getArchiveLayer));
		//处理档案类型 StorageLocate，storageName
		final Map<String, String> archiveNamemap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, ArchiveTable::getStorageName));
		//获取绑定的表单字段
		final List<ArchiveEdit> archiveEditList = this.list(Wrappers.<ArchiveEdit>lambdaQuery().eq(ArchiveEdit::getTenantId, tenantId));
		Map<Long, String> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuId, TenantMenu::getMenuName));
		menuMaps.put(-1L,"全部");
		//门类名称	层级	字段名称 模块
		List<ArrayList<String>> collect = archiveEditList.stream().map(archiveEdit -> CollectionUtil.newArrayList(archiveNamemap.get(archiveEdit.getStorageLocate()), metadatamap.get(archiveEdit.getMetadataId()),menuMaps.get(archiveEdit.getModuleId()))).collect(Collectors.toList());
		return collect;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(allEntries = true)
	public Boolean removeByModuleId(String storageLocate, Long moduleId) {
		//清除 表单配置
		archiveEditFormService.remove(Wrappers.<ArchiveEditForm>lambdaQuery().eq(ArchiveEditForm::getStorageLocate,storageLocate).eq(ArchiveEditForm::getModuleId,moduleId));
		//清除 表单列表配置
		boolean remove = this.remove(Wrappers.<ArchiveEdit>lambdaQuery().eq(ArchiveEdit::getStorageLocate, storageLocate).eq(ArchiveEdit::getModuleId, moduleId));
		archiveConfigManageService.update(storageLocate,moduleId,TypedefEnum.FROM.getValue(),BoolEnum.NO.getCode());
		return remove;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R copy(CopyPostDTO copyPostDTO) {
		final Long sourceModuleId = copyPostDTO.getSourceModuleId();
		final String storageLocate = copyPostDTO.getStorageLocate();
		final List<Long> targetModuleIds = copyPostDTO.getTargetModuleIds();
		if(CollectionUtil.isNotEmpty(targetModuleIds)){
			this.remove(Wrappers.<ArchiveEdit>lambdaQuery().eq(ArchiveEdit::getStorageLocate,storageLocate).in(ArchiveEdit::getModuleId,targetModuleIds));
			archiveEditFormService.remove(Wrappers.<ArchiveEditForm>lambdaQuery().in(ArchiveEditForm::getModuleId,targetModuleIds).eq(ArchiveEditForm::getStorageLocate,storageLocate));
		}
		final List<ArchiveEdit> archiveEdits = this.list(Wrappers.<ArchiveEdit>lambdaQuery().eq(ArchiveEdit::getModuleId, sourceModuleId).eq(ArchiveEdit::getStorageLocate, storageLocate));
		if(CollectionUtil.isEmpty(archiveEdits)){
			return new R().fail(null,"当前模块无信息可复制，请先配置当前模块信息。");
		}
		ArchiveEditForm archiveEditForm = archiveEditFormService.getOne(Wrappers.<ArchiveEditForm>lambdaQuery().eq(ArchiveEditForm::getModuleId, sourceModuleId).eq(ArchiveEditForm::getStorageLocate, storageLocate));

		List<ArchiveEdit> archiveEditList =CollectionUtil.newArrayList();
		List<ArchiveEditForm> archiveEditFormList =CollectionUtil.newArrayList();
		targetModuleIds.forEach(moduleId->{
			archiveEdits.forEach(archiveEdit -> {
				ArchiveEdit archiveEdit1 = new ArchiveEdit();
				BeanUtil.copyProperties(archiveEdit,archiveEdit1);
				archiveEdit1.setId(null);
				archiveEdit1.setModuleId(moduleId);
				archiveEditList.add(archiveEdit1);
			});
			if(ObjectUtil.isNotEmpty(archiveEditForm)){
				ArchiveEditForm archiveEditForm1 =new ArchiveEditForm();
				BeanUtil.copyProperties(archiveEditForm,archiveEditForm1);
				archiveEditForm1.setId(null);
				archiveEditForm1.setModuleId(moduleId);
				archiveEditFormList.add(archiveEditForm1);
			}
		});
		if(CollectionUtil.isNotEmpty(archiveEditList)){
			this.saveBatch(archiveEditList);
		}
		if(CollectionUtil.isNotEmpty(archiveEditFormList)){
			archiveEditFormService.saveBatch(archiveEditFormList);
		}
		archiveConfigManageService.saveBatchByModuleIds(storageLocate,targetModuleIds,TypedefEnum.FROM.getValue());
		return new R(null,"复制成功！");
	}

	@Override
	@CacheEvict(cacheNames = {"archive-edit", "archive-column-rule"}, allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate, Map<Long, Long> srcDestMetadataMap) {
		List<ArchiveEdit> list = this.list(Wrappers.<ArchiveEdit>lambdaQuery().eq(ArchiveEdit::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(archiveEdit -> {
				archiveEdit.setId(null);
				archiveEdit.setStorageLocate(destStorageLocate);
				archiveEdit.setMetadataId(srcDestMetadataMap.get(archiveEdit.getMetadataId()));
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
}
