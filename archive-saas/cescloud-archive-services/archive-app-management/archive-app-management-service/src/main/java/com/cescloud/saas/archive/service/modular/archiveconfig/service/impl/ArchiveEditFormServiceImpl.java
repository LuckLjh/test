/*
 *    Copyright (c) 2018-2025, cesgroup All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: cesgroup
 */
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.*;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.ArchiveEditForm;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SourceDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.api.modular.datasource.dto.DmlDTO;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveService;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.metadata.cachesupport.MetadataCacheHolder;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantMenu;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantMenuService;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.util.*;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.ArchiveEditFormMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveColumnRuleService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigRuleService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveEditFormService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveEditService;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataAutovalueService;
import com.cescloud.saas.archive.service.modular.archivetype.service.MetadataSourceService;
import com.cescloud.saas.archive.service.modular.archivetype.service.rulecompute.ArchiveRuleService;
import com.cescloud.saas.archive.service.modular.archivetype.service.rulecompute.ArchiveRuleServiceContext;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.enums.ColumnComputeRuleEnum;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeService;
import com.cescloud.saas.archive.service.modular.fonds.service.FondsService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 档案表单定义内容
 *
 * @author liudong1
 * @date 2019-04-22 19:56:41
 */
@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "archive-edit-form")
public class ArchiveEditFormServiceImpl extends ServiceImpl<ArchiveEditFormMapper, ArchiveEditForm> implements ArchiveEditFormService {

	@Autowired
	private FondsService fondsService;
	@Autowired
	private ArchiveEditService archiveEditService;
	@Autowired
	private ArchiveConfigRuleService archiveConfigRuleService;
	@Autowired
	private ArchiveColumnRuleService archiveColumnRuleService;
	@Autowired
	private FilingScopeService filingScopeService;
	@Autowired
	private MetadataService metadataService;
	@Autowired
	private DictItemService dictItemService;
	@Autowired
	private ArchiveTableService archiveTableService;
	@Autowired
	private MetadataAutovalueService metadataAutovalueService;
	@Autowired
	private MetadataSourceService metadataSourceService;
	@Autowired
	private ArchiveRuleServiceContext archiveRuleServiceContext;
	@Autowired(required = false)
	private RemoteTenantTemplateService remoteTenantTemplateService;

	private final RemoteTenantMenuService remoteTenantMenuService;

	private final RemoteArchiveService remoteArchiveService;

	private final MetadataCacheHolder metadataCacheHolder;


	@Override
	@Cacheable(key = "'archive-app-management:archive-edit-form-service:' + #storageLocate + ':' + #moduleId",
			unless = "#result == null"
	)
	public ArchiveEditForm getEditFormByStorageLocate(String storageLocate, Long moduleId) {
		ArchiveEditForm editForm = this.getOne(Wrappers.<ArchiveEditForm>query().lambda()
				.eq(ArchiveEditForm::getStorageLocate, storageLocate).eq(ArchiveEditForm::getModuleId, moduleId));
		if (ObjectUtil.isNull(editForm)) {
			editForm = new ArchiveEditForm();
			editForm.setStorageLocate(storageLocate);
		} else {
			log.debug("将表[{}]的配置从blob转为map对象", storageLocate);
			byte[] bytes = CesBlobUtil.objConvertToByte(editForm.getFormContent());
			LinkedHashMap map = ObjectUtil.deserialize(bytes);
			editForm.setFormContent(map);
		}
		return editForm;
	}

	@Override
	public List<String> getEditFormColumnByStorageLocate(String storageLocate, Long moduleId) {
		final List<String> columnList = CollectionUtil.newArrayList();
		ArchiveEditForm editForm = this.getOne(Wrappers.<ArchiveEditForm>query().lambda()
				.eq(ArchiveEditForm::getStorageLocate, storageLocate).eq(ArchiveEditForm::getModuleId, moduleId));
		if (ObjectUtil.isNull(editForm)) {
			return columnList;
		} else {
			log.debug("将表[{}]的配置从blob转为map对象", storageLocate);
			byte[] bytes = CesBlobUtil.objConvertToByte(editForm.getFormContent());
			LinkedHashMap map = ObjectUtil.deserialize(bytes);
			//list结构
			List list = (List) map.get("list");
			for (int i = 0, length = list.size(); i < length; i++) {
				HashMap mapOfList = (HashMap) list.get(i);
				String metadataEnglish = (String) mapOfList.get(FormConstant.MODEL);
				columnList.add(metadataEnglish);
			}
		}
		return columnList;
	}

	/**
	 * 初始化页面
	 * 得到form的json结构
	 * 里面包含字段拼接规则
	 * 下拉框的选择数据
	 * 不包含字段的初始值
	 */
	@Override
	public ArchiveEditForm initForm(String typeCode, Long templateTableId, Long moduleId) throws ArchiveBusinessException {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		String storageLocate = archiveTable.getStorageLocate();
		ArchiveEditForm editForm = this.getOne(Wrappers.<ArchiveEditForm>query().lambda()
				.eq(ArchiveEditForm::getStorageLocate, storageLocate).eq(ArchiveEditForm::getModuleId, moduleId));
		if (ObjectUtil.isNull(editForm)) {
			editForm = this.getOne(Wrappers.<ArchiveEditForm>query().lambda()
					.eq(ArchiveEditForm::getStorageLocate, storageLocate).eq(ArchiveEditForm::getModuleId, ArchiveConstants.PUBLIC_MODULE_FLAG));
		}
		if (ObjectUtil.isNull(editForm)) {
			editForm = new ArchiveEditForm();
			editForm.setStorageLocate(storageLocate);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("将表[{}]的配置从blob转为map对象", storageLocate);
			}
			//获取不允许重复字段
			final List<Metadata> metadataList = metadataService.getRepeatMetadatasByStorageLocate(storageLocate);
			final List<String> isRepeats = metadataList.stream().map(metadata -> metadata.getMetadataEnglish()).collect(Collectors.toList());
			byte[] bytes = CesBlobUtil.objConvertToByte(editForm.getFormContent());
			LinkedHashMap map = ObjectUtil.deserialize(bytes);
			// list结构
			List list = (List) map.get(FormConstant.LIST);
			//重新处理 返回的是否必填语句
			for (int i = 0; i < list.size(); i++) {
				Map m = ((Map) list.get(i));
				if (((List) m.get("rules")).size() != 0) {
					List l = (List) ((Map) list.get(i)).get("rules");
					for (int j = 0; j < l.size(); j++) {
						if (((String) ((Map) l.get(j)).get("message")).contains("填写")) {
							((Map) l.get(j)).put("message", ((Map) list.get(i)).get("name") + "必须填写");
						}
						if (((String) ((Map) l.get(j)).get("message")).contains("不正确")) {
							((Map) l.get(j)).put("message", ((Map) list.get(i)).get("name") + "格式不正确");
						}
					}
				}
			}
			// 为下拉字段设置options选项
			setOptionsForSelectType(list, storageLocate, typeCode, isRepeats);
			// 判断有无归档人或归档部门
			geneHiddenIdByfilingUserOrDept(list);
			editForm.setFormContent(map);
		}
		return editForm;
	}


	/**
	 * 为下拉字段设置options选项
	 */
	private void setOptionsForSelectType(List<Object> list, String storageLocate, String archiveTypeCode, List<String> isRepeats) {
		// 从缓存中获取所有的字段长度
		Map<String, Integer> metadataLengthMap = metadataCacheHolder.getMetadataLengthMapByStorage(storageLocate);
		Map<String, String> metadataTypeMap = metadataCacheHolder.getMetadataTypeMapByStorage(storageLocate);
		list.stream().forEach(obj -> {
			HashMap mapOfList = (HashMap) obj;
			mapOfList.put("hidden", false);
			String metadataEnglish = (String) mapOfList.get(FormConstant.MODEL);
			if (isRepeats.contains(metadataEnglish)) {
				mapOfList.put("isRepeat", Boolean.TRUE);
				addrules(mapOfList);
			} else {
				mapOfList.put("isRepeat", Boolean.FALSE);
			}
			Map<String, Object> options = (HashMap) mapOfList.get(FormConstant.OPTIONS);
			//设置字段长度
			setLength(options, metadataTypeMap, metadataLengthMap, metadataEnglish);
			//获取数据模型类型
			String type = (String) mapOfList.get(FormConstant.TYPE);
			//对于下拉框, 赋值下拉框
			if (FormConstant.TYPE_SELECT.equals(type)) {
				setFormSelectOption(storageLocate, archiveTypeCode, metadataEnglish, options);
			}
		});
	}

	private void addrules(HashMap mapOfList) {
		List<Map<String, Object>> rules = (List<Map<String, Object>>) mapOfList.get(FormConstant.RULES);
		Map<String, Object> map = CollectionUtil.newHashMap();
		map.put("validator", "repeat");
		map.put("trigger", "blur");
		rules.add(map);
	}

	/**
	 * 如果有归档人或归档部门，则生成filing_user_id或filing_dept_id隐藏字段
	 */
	private void geneHiddenIdByfilingUserOrDept(List<Object> list) {
		// 获取归档人或归档部门字段
		final List<Object> elements = list.stream().filter(obj -> {
			HashMap mapOfList = (HashMap) obj;
			String metadataEnglish = (String) mapOfList.get(FormConstant.MODEL);
			return StrUtil.equals(metadataEnglish, FieldConstants.FILING_USER)
					|| StrUtil.equals(metadataEnglish, FieldConstants.FILING_DEPT);
		}).collect(Collectors.toList());
		if (CollectionUtil.isEmpty(elements)) {
			return;
		}
		// 生成对应的隐藏字段
		final List<Map<String, Object>> collect = elements.stream().map(obj -> {
			Map<String, Object> map = CollectionUtil.newHashMap();
			HashMap mapOfList = (HashMap) obj;
			String metadataEnglish = (String) mapOfList.get(FormConstant.MODEL);
			map.put("hidden", true);
			map.put(FormConstant.TYPE, "input");
			map.put(FormConstant.KEY, IdGenerator.getId());
			if (StrUtil.equals(metadataEnglish, FieldConstants.FILING_USER)) {
				map.put(FormConstant.NAME, "归档人id");
				map.put(FormConstant.MODEL, FieldConstants.FILING_USER_ID);
			} else if (StrUtil.equals(metadataEnglish, FieldConstants.FILING_DEPT)) {
				map.put(FormConstant.NAME, "归档部门id");
				map.put(FormConstant.MODEL, FieldConstants.FILING_DEPT_ID);
			}
			Map<String, Object> optionsCopy = CollectionUtil.newHashMap();
			optionsCopy.put(FormConstant.DEFAULTVALUE, "");
			map.put(FormConstant.OPTIONS, optionsCopy);
			map.put(FormConstant.RULES, CollectionUtil.newArrayList());
			return map;
		}).collect(Collectors.toList());
		list.addAll(collect);
	}

	/**
	 * 设置字段长度
	 *
	 * @param options
	 * @param metadataTypeMap   字段名 -> 字段类型
	 * @param metadataLengthMap 字段名  ->  字段长度
	 * @param metadataEnglish
	 */
	private void setLength(Map<String, Object> options, Map<String, String> metadataTypeMap, Map<String, Integer> metadataLengthMap, String metadataEnglish) {
		String metadataType = metadataTypeMap.get(metadataEnglish);
		if (!MetadataTypeEnum.VARCHAR.getValue().equals(metadataType)) {
			return;
		}
		Integer length = metadataLengthMap.get(metadataEnglish);
		if (ObjectUtil.isNull(length)) {
			return;
		}
		options.put(FormConstant.MAX, length.intValue());
	}

	/**
	 * 给下拉框赋值
	 *
	 * @param storageLocate
	 * @param metadataEnglish
	 * @param options
	 */
	private void setFormSelectOption(String storageLocate, String archiveTypeCode, String metadataEnglish, Map<String, Object> options) {
		try {
			List<Map<String, String>> editFormSelectOption = metadataService.getEditFormSelectOption(storageLocate, archiveTypeCode, metadataEnglish);
			if (CollectionUtil.isNotEmpty(editFormSelectOption)) {
				options.put(FormConstant.OPTIONS, editFormSelectOption);
			}
		} catch (ArchiveBusinessException e) {
			log.error("字典查询失败");
		}
	}

	@Override
	@CacheEvict(key = "'archive-app-management:archive-edit-form-service:' + #editForm.storageLocate + ':' + #editForm.moduleId")
	@Transactional(rollbackFor = Exception.class)
	public ArchiveEditForm saveEditForm(ArchiveEditForm editForm) throws ArchiveBusinessException {
		log.debug("检查下表单里面是否有重复的列", editForm.getFormContent());
		boolean status =  checkForDuplicate(editForm.getFormContent());
		if(!status){
			log.debug("表单中有重复的列，请检查！", editForm.getFormContent());
			throw new ArchiveBusinessException("表单中有重复的列，请检查！");
		}
		log.debug("删除原来表<{}>的表单定义配置", editForm.getStorageLocate());
		this.remove(Wrappers.<ArchiveEditForm>query().lambda()
				.eq(ArchiveEditForm::getStorageLocate, editForm.getStorageLocate()).eq(ArchiveEditForm::getModuleId, editForm.getModuleId()));
//		log.debug("校验表单中的字段是否齐全（表单已定义的字段）");
//		validatorColumn(editForm);
		log.debug("重新保存表<{}>的表单定义配置", editForm.getStorageLocate());
		LinkedHashMap formContent = (LinkedHashMap) editForm.getFormContent();
		byte[] bytes = ObjectUtil.serialize(formContent);
		editForm.setFormContent(bytes);
		this.save(editForm);
		return editForm;
	}

	/**
	 * add by hyq
	 * 检查下表单的配置列中是否有重复的配置
	 * @param formContent
	 * @return
	 */
	private boolean checkForDuplicate(Object formContent) {
		if(formContent == null){
			return true;
		}
		Map<String, Object> beanMap = BeanUtil.beanToMap(formContent);
		List<Map<String,Object>> list = (List) beanMap.get("list");
		// 对比 list  和 set 的长度判断是否有重复
		List newList  = new ArrayList();
		list.stream().forEach(
				e -> newList.add(e.get("name"))
		);
		Set set = new HashSet(newList);
		if(list.size() == set.size()){
			return true ;
		}else{
			return false ;
		}

	}

	public void validatorColumn(ArchiveEditForm editForm) throws ArchiveBusinessException {
		LinkedHashMap map = (LinkedHashMap) editForm.getFormContent();
		List list = (List) map.get("list");
		if (CollectionUtil.isEmpty(list)) {
			log.error("表单为空，不能保存");
			throw new ArchiveBusinessException("表单为空，不能保存");
		}
		List<String> columns = (List<String>) list.parallelStream().map(obj -> {
			HashMap mapOfList = (HashMap) obj;
			String metadataEnglish = (String) mapOfList.get(FormConstant.MODEL);
			return metadataEnglish;
		}).collect(Collectors.toList());
		//获取表单定义中已经定义的字段
		List<DefinedEditMetadata> definedEditMetadataList = archiveEditService.listOfDefined(editForm.getStorageLocate(), editForm.getModuleId());
		List<String> defColumns = definedEditMetadataList.parallelStream().map(m -> m.getMetadataEnglish()).collect(Collectors.toList());
		if (CollectionUtil.isEmpty(defColumns)) {
			log.error("表单定义中未定义表单字段，请先定义表单字段");
			throw new ArchiveBusinessException("表单定义中未定义表单字段，请先定义表单字段");
		}
		//校验表单中有无没有定义的字段
		List<String> undefColumn = defColumns.parallelStream().filter(column -> !columns.contains(column)).collect(Collectors.toList());
		if (CollectionUtil.isNotEmpty(undefColumn)) {
			log.error("表单中还有未定义的字段：" + undefColumn.stream().collect(Collectors.joining(",")));
			throw new ArchiveBusinessException("表单中还有未定义的字段：" + undefColumn.stream().collect(Collectors.joining(",")));
		}
	}

	@Override
	public AutovalueRuleDTO getRuleColumn(String storageLocate, Long moduleId, Integer compose) {
		final AutovalueRuleDTO autovalueRuleDTO = new AutovalueRuleDTO();
		//1、获取数据规则定义中的拼接规则
		List<MetadataAutovalue> definedAutovalues = metadataAutovalueService.getDefinedAutovalues(storageLocate, moduleId);
		//null 的话赋值 -1
		compose = ObjectUtil.isNull(compose) ? -1 : compose;
		//0代表 未组卷 （此处是处理未组卷下的新增）
		if (compose != 0){
			final List<AutovalueRuleDTO.Rules> splicing = getSplicingRuleColumn(definedAutovalues);
			autovalueRuleDTO.setSplicing(splicing);
		}
		//2、获取累加规则
		final List<AutovalueRuleDTO.Rules> flowno = getFlownoRuleColumn(definedAutovalues);
		autovalueRuleDTO.setFlowno(flowno);
		//3、获取默认值日期
		final List<AutovalueRuleDTO.Rules> nowDate = getNowDateColumn(definedAutovalues);
		autovalueRuleDTO.setNowDate(nowDate);
		//4、获取页数页号的联动规则
		final List<AutovalueRuleDTO.Rules> pagesOrPageNoRule = getPagesOrPageNoRule(definedAutovalues);
		autovalueRuleDTO.setPagesOrPageNo(pagesOrPageNoRule);
		return autovalueRuleDTO;
	}

	private List<AutovalueRuleDTO.Rules> getPagesOrPageNoRule(List<MetadataAutovalue> definedAutovalues) {
		final List<AutovalueRuleDTO.Rules> pagesOrPageNoRule = definedAutovalues.stream().filter(metadataAutovalue ->
				metadataAutovalue.getType().equals(AutovalueTypeEnum.PAGESORPAGENO.getValue()))
				.map(autovalue -> {
					AutovalueRuleDTO.Rules rules = new AutovalueRuleDTO.Rules();
					final Metadata metadata = metadataService.getMetadataById(autovalue.getMetadataId());
					rules.setName(metadata.getMetadataEnglish());
					List<Map<String, Object>> list = new ArrayList<>();
					Map<String, Object> map = new HashMap<>();
					map.put("PageOrPageNoRule",autovalue.getPageOrPageNoRule());
					list.add(map);
					rules.setColumnRules(list);
					return rules;
				}).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
		return pagesOrPageNoRule;
	}

	/**
	 * 获取当前日期的字段,4位字段只取年度
	 * @param definedAutovalues
	 * @return
	 */
	private List<AutovalueRuleDTO.Rules> getNowDateColumn(List<MetadataAutovalue> definedAutovalues) {
		final List<AutovalueRuleDTO.Rules> nowDate = definedAutovalues.stream().filter(metadataAutovalue -> metadataAutovalue.getType().equals(AutovalueTypeEnum.NOW_DATE.getValue()))
				.map(autovalue -> {
					AutovalueRuleDTO.Rules rules = new AutovalueRuleDTO.Rules();
					//2021-10-18
					Metadata metadata = metadataService.getMetadataById(autovalue.getMetadataId());
					String value = "";
					List<Map<String, Object>> list = new ArrayList<>();
					Map<String, Object> map = new HashMap<>();
					if (metadata.getMetadataType().equals(MetadataTypeEnum.DATE.getValue())) {
						value = LocalDateTime.now(ZoneOffset.of("+8")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					} else if (metadata.getMetadataType().equals(MetadataTypeEnum.DATETIME.getValue())) {
						value = LocalDateTime.now(ZoneOffset.of("+8")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
					} else if (metadata.getMetadataType().equals(MetadataTypeEnum.VARCHAR.getValue()) && metadata.getMetadataLength() >= 10) {
						value = LocalDateTime.now(ZoneOffset.of("+8")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					} else if (metadata.getMetadataLength() == 4) {
						value = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
					}
					rules.setName(metadata.getMetadataEnglish());
					map.put(metadata.getMetadataEnglish(),value);
					list.add(map);
					rules.setColumnRules(list);
					return rules;
				}).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
		return nowDate;
	}

	private List<AutovalueRuleDTO.Rules> getFlownoRuleColumn(List<MetadataAutovalue> definedAutovalues) {
		final List<AutovalueRuleDTO.Rules> flowno = definedAutovalues.stream().filter(metadataAutovalue -> metadataAutovalue.getType().equals(AutovalueTypeEnum.FLOWNO.getValue()))
				.map(autovalue -> {
					AutovalueRuleDTO.Rules rules = null;
					final List<SourceDTO> metaDataSourceList = metadataSourceService.getMetadataSourcesByStorageAndTargetId(autovalue.getStorageLocate(), autovalue.getId());
					if (CollectionUtil.isNotEmpty(metaDataSourceList)) {
						rules = new AutovalueRuleDTO.Rules();
						final List<Map<String, Object>> columnRules = metaDataSourceList.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
						final Metadata metadata = metadataService.getMetadataById(autovalue.getMetadataId());
						rules.setName(metadata.getMetadataEnglish());
						rules.setColumnRules(columnRules);
					}
					return rules;
				}).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
		return flowno;
	}


	private List<AutovalueRuleDTO.Rules> getSplicingRuleColumn(List<MetadataAutovalue> definedAutovalues) {
		final List<AutovalueRuleDTO.Rules> splicing = definedAutovalues.stream().filter(metadataAutovalue -> metadataAutovalue.getType().equals(AutovalueTypeEnum.SPLICING.getValue()))
				.map(autovalue -> {
					AutovalueRuleDTO.Rules rules = null;
					List<DefinedColumnRuleMetadata> definedColumnRuleMetadata = archiveColumnRuleService.listOfDefined(autovalue.getId());
					if (CollectionUtil.isNotEmpty(definedColumnRuleMetadata)) {
						rules = new AutovalueRuleDTO.Rules();
						//将数据字典值组装到definedColumnRuleMetadata中
						List<Map<String, Object>> columnRules = definedColumnRuleMetadata.stream().map(m -> {
							String dictCode = m.getDictCode();
							Map<String, Object> beanMap = BeanUtil.beanToMap(m);
							if (StrUtil.isNotEmpty(dictCode)) {
								List<DictItem> dictItemList = dictItemService.getItemListByDictCode(dictCode);
								List<Map<String, String>> options = dictItemList.stream().map(dictItem -> {
									Map<String, String> dicMap = CollectionUtil.newHashMap(2);
									dicMap.put("label", dictItem.getItemLabel());
									dicMap.put("value", dictItem.getItemCode());
									return dicMap;
								}).collect(Collectors.toList());
								beanMap.put("options", options);
							}
							return beanMap;
						}).collect(Collectors.toList());
						final Metadata metadata = metadataService.getMetadataById(autovalue.getMetadataId());
						rules.setName(metadata.getMetadataEnglish());
						rules.setColumnRules(columnRules);
					}
					return rules;
				}).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
		return splicing;
	}

	@Override
	public Map<String, Object> getFlownoValue(ComputeFlowNoDTO computeFlowNoDTO) throws ArchiveBusinessException {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(computeFlowNoDTO.getTypeCode(),
				computeFlowNoDTO.getTemplateTableId());
		if (ObjectUtil.isNull(archiveTable)) {
			return null;
		}
		//获取规则,并计算规则字段值
		Map<String, Object> result = getMetadataSourceRuleColumnValue(computeFlowNoDTO.getModuleId(), computeFlowNoDTO.getData(),
				archiveTable.getStorageLocate(), computeFlowNoDTO.getOwnerId());
		return result;
	}

	@Override
	public Map<String, Object> initFormData(ArchiveInitFormDataDTO archiveInitFromDataDTO) throws ArchiveBusinessException {
		String type = archiveInitFromDataDTO.getType();
		String fondsCode = archiveInitFromDataDTO.getFondsCode();
		String filter = archiveInitFromDataDTO.getFilter();
		String path = archiveInitFromDataDTO.getPath();
		String ids = archiveInitFromDataDTO.getIds();
		Long ownerId = archiveInitFromDataDTO.getOwnerId();
		Long moduleId = archiveInitFromDataDTO.getModuleId();
		Long templateTableId = archiveInitFromDataDTO.getTemplateTableId();
		Long folderTemplateId = archiveInitFromDataDTO.getFolderTemplateId();
		String className = archiveInitFromDataDTO.getClassName();
		if ("compose".equals(type)) {
			templateTableId = folderTemplateId;
		}

		//获取表名
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(archiveInitFromDataDTO.getTypeCode(), templateTableId);
		String storageLocate = archiveTable.getStorageLocate();

		FormStatusEnum formStatusEnum = "add".equalsIgnoreCase(type) ? FormStatusEnum.ADD : FormStatusEnum.COMPOSE;
		final Map<String, Object> data = CollectionUtil.newHashMap();
		if (StrUtil.isNotBlank(filter)) {
			String[] conditions = filter.split(";");
			for (String condition : conditions) {
				if (condition.contains(FieldConstants.DEPT_PATH)) {
					continue;
				}
				String[] strings = condition.split("=");
				data.put(strings[0], strings[1].replaceAll("'", ""));
			}
		}
		// 填充分类号
		if (StrUtil.isNotEmpty(path)) {
			String[] pathArr = path.split(",");
			if (pathArr.length > 0) {
				String seriesCode = pathArr[pathArr.length - 1];
				data.put(FieldConstants.SERIES_CODE, seriesCode);
				//填充分类名称
				data.put(FieldConstants.CATALOGUE_NAME, className);
			}
		}
		if (StrUtil.isNotEmpty(fondsCode)) {
			data.put(FieldConstants.FONDS_CODE, fondsCode);
			Fonds fonds = fondsService.getFondsByCode(fondsCode);
			data.put(FieldConstants.FONDS_NAME, fonds.getFondsName());
		}
		//归档人归档部门填充
		final CesCloudUser user = SecurityUtils.getUser();
		data.put(FieldConstants.FILING_USER, user.getChineseName());
		data.put(FieldConstants.FILING_DEPT, user.getDeptName());
		data.put(FieldConstants.FILING_USER_ID, user.getId());
		data.put(FieldConstants.FILING_DEPT_ID, user.getDeptId());
		List<MetadataAutovalue> definedAutovalues = metadataAutovalueService.getDefinedAutovalues(storageLocate, moduleId);
		final List<AutovalueRuleDTO.Rules> nowDate = getNowDateColumn(definedAutovalues);
        //年度、文件日期 填充为当前时间
        for (AutovalueRuleDTO.Rules rules : nowDate) {
            if (rules.getName().equals(FieldConstants.YEAR_CODE)) {
                data.put(FieldConstants.YEAR_CODE, rules.getColumnRules().get(0).get(FieldConstants.YEAR_CODE));
            }
            if (rules.getName().equals(FieldConstants.File.DATE_OF_CREATION)){
                data.put(FieldConstants.File.DATE_OF_CREATION, rules.getColumnRules().get(0).get(FieldConstants.File.DATE_OF_CREATION));
            }
        }
		//未组卷新增时，不获取关联关系和 流水号默认值
		if(!(FormStatusEnum.ADD.equals(formStatusEnum) && ObjectUtil.isNotNull(archiveInitFromDataDTO.getCompose()) && archiveInitFromDataDTO.getCompose() == 0)){
			//一、关联关系规则字段默认值设置
			Map<String, Object> relationMap = getInnerRelationRuleColumnValue(moduleId, storageLocate, formStatusEnum, ownerId, ids);
			data.putAll(relationMap);
			//二、累加规则字段默认值计算
			// 1、没有分组字段即大流水，计算流水号默认值；
			// 2、有分组字段：
			// 	1)分组字段默认值有空值，则流水号字段不设置值；
			//	2)分组字段都有默认值，则计算流水号默认值。
			Map<String, Object> flowNoMap = getMetadataSourceRuleColumnValue(moduleId, data, storageLocate, ownerId);
			data.putAll(flowNoMap);
		}
		return data;
	}


	private Map<String, Object> getInnerRelationRuleColumnValue(Long moduleId, String storageLocate, FormStatusEnum formStatusEnum, Long ownerId, String ids) {
		final Map<String, Object> result = CollectionUtil.newHashMap();
		List<ColumnComputeRuleDTO> filterColumnComputeRuleDTO = null;
		try {
			filterColumnComputeRuleDTO = archiveConfigRuleService.getComputeRuleByStorageLocate(moduleId, storageLocate, formStatusEnum);
		} catch (ArchiveBusinessException e) {
			log.error("查询关联关系规则失败");
		}
		if (CollectionUtil.isEmpty(filterColumnComputeRuleDTO)) {
			return result;
		}
		filterColumnComputeRuleDTO.stream().forEach(columnComputeRuleDTO -> {
			if (formStatusEnum.equals(FormStatusEnum.ADD)) {
				// 新增表单初始化
				if (ObjectUtil.isNotNull(ownerId)) {
					//ids为 父档案的 id
					if (ColumnComputeRuleEnum.EQUAL.getValue().equals(columnComputeRuleDTO.getRelationType())) {
						// 如果是 相等 规则（继承父类相等规则字段值）
						columnComputeRuleDTO.setWhere("t." + FieldConstants.ID + " = " + ownerId);
					}
				}
			} else if (formStatusEnum.equals(FormStatusEnum.COMPOSE)) {
				//组卷 表单 初始化
				if (StrUtil.isNotEmpty(ids)) {
					// ids 为 勾选的待组卷的id集合
					// （（规则向下）。根据设置的相等、求和、计数、求起止值、最大值、最小值、平均值规则，计算案卷的字段值）
					columnComputeRuleDTO.setWhere("t." + FieldConstants.ID + " in (" + ids + ")");
				}
			}
			String metadataType = metadataCacheHolder.getMetadataType(storageLocate, columnComputeRuleDTO.getColumn());
			columnComputeRuleDTO.setMetadataType(metadataType);
			String sql = columnComputeRuleDTO.getSql(DataSourceUtil.getCurrentDbType());
			log.info("计算规则sql:" + sql);
			String value = getRemoteFieldValue(columnComputeRuleDTO, sql);
			if (!MetadataTypeEnum.VARCHAR.getValue().equals(columnComputeRuleDTO.getMetadataType())){
				if (StrUtil.isBlank(value)){
					value = null;
				}
			}
			result.put(columnComputeRuleDTO.getMetadataEnglish(), value);
		});
		return result;
	}

	private Map<String, Object> getMetadataSourceRuleColumnValue(Long moduleId, Map<String, Object> data, String storageLocate, Long ownerId) throws ArchiveBusinessException {
		final String layer = ArchiveTableUtil.getArchiveLayerByStorageLocate(storageLocate);
		final Map<String, Object> result = CollectionUtil.newHashMap();
		List<ColumnComputeRuleDTO> metadataSourceRule = null;
		try {
			metadataSourceRule = metadataAutovalueService.getComputeRuleByStorageLocate(moduleId, storageLocate);
		} catch (ArchiveBusinessException e) {
			log.error("查询累加规则失败");
		}
		if (CollectionUtil.isEmpty(metadataSourceRule)) {
			return result;
		}
		try {
			metadataSourceRule.stream().forEach(columnComputeRuleDTO -> {
				String group = columnComputeRuleDTO.getGroup();
				StrBuilder where = new StrBuilder().append("t.").append(FieldConstants.IS_DELETE).append(" = 0 ");
				if (ArchiveLayerEnum.PRE.getCode().equals(layer) || ArchiveLayerEnum.SINGLE.getCode().equals(layer) || ArchiveLayerEnum.ONE.getCode().equals(layer)) {
					where.append(" and t.").append(FieldConstants.STATUS).append(" <> ").append(ArchiveStatusEnum.COMPONENT.getValue());
				}
				if (ObjectUtil.isNotNull(ownerId)) {
					where.append(" and t.").append(FieldConstants.OWNER_ID).append(" = ").append(ownerId);
				}
				if (StrUtil.isNotBlank(group)) {
					Map<String, String> columnValueMap = Arrays.stream(group.split(",")).collect(Collectors.toMap(Function.identity(), column -> ObjectUtil.isNull(data.get(column)) ? "" : StrUtil.toString(data.get(column))));
					columnValueMap.forEach((k, v) -> {
						if (StrUtil.isBlank(v)) {
							where.append(" and t.").append(k).append(" is null ");
						} else {
							where.append(" and t.").append(k).append(" = '").append(v).append("' ");
						}
					});
					columnComputeRuleDTO.setGroup(null);
				}
				columnComputeRuleDTO.setWhere(where.toString());
				String metadataType = metadataCacheHolder.getMetadataType(storageLocate, columnComputeRuleDTO.getColumn());
				columnComputeRuleDTO.setMetadataType(metadataType);
				String sql = columnComputeRuleDTO.getSql(DataSourceUtil.getCurrentDbType());
				log.info("计算累加规则字段SQL:[{}]", sql);
				String value = getRemoteFieldValue(columnComputeRuleDTO, sql);
				/*
				 * 增加 "null".equalsIgnoreCase(value) 是因为海量数据库在 select max(t.file_no) 后返回的是 [null] 而字段类型明明是  numeric
				 * 字段对应关系待改
				 * TODO 王谷华 2021-09-23
				 *
				 * */
				//判断如果是字符型字段，是否补零
				if (StrUtil.isBlank(value) || StrUtil.isNullOrUndefined(value)) {
					value = "1";
				} else {
					value = StrUtil.toString(Integer.parseInt(value) + 1);
				}
				if (MetadataTypeEnum.VARCHAR.getValue().equals(columnComputeRuleDTO.getMetadataType())) {
					if (columnComputeRuleDTO.getFlagZero() > 0) {
						value = StrUtil.fillBefore(value, '0', columnComputeRuleDTO.getFlagZero());
					}
					result.put(columnComputeRuleDTO.getMetadataEnglish(), value);
				} else if (MetadataTypeEnum.INT.getValue().equals(columnComputeRuleDTO.getMetadataType())) {
					result.put(columnComputeRuleDTO.getMetadataEnglish(), Integer.parseInt(value));
				}
			});
		} catch (NumberFormatException e) {
			log.error("流水号数值转换异常，所有流水号数据中可能含有不能转换为数值型的字符", e);
			throw new ArchiveBusinessException("获取最大流水号异常，请检查所有流水号数据中是否存在不能转换为数值型的字符");
		}
		return result;
	}

	@Override
	public Map<String, List<ColumnComputeRuleDTO>> getColumnRuleByType(Long moduleId, String storageLocate, String type) throws ArchiveBusinessException {
		FormStatusEnum formStatusEnum = FormStatusEnum.getEnum(type);
		ArchiveRuleService archiveRuleService = archiveRuleServiceContext.getArchiveRuleService(formStatusEnum);
		Map<String, List<ColumnComputeRuleDTO>> rulesMap = archiveRuleService.handleComputeRule(storageLocate, moduleId);
		return rulesMap;
	}

	private String getRemoteFieldValue(ColumnComputeRuleDTO columnComputeRuleDTO, String sql) {
		String value = "";
		DmlDTO dmlDTO = new DmlDTO();
		dmlDTO.setSql(sql);
		R<List<Map<String, Object>>> result = remoteArchiveService.executeQuery(dmlDTO);
		if (result.getCode() == CommonConstants.SUCCESS) {
			List<Map<String, Object>> data = result.getData();
			if (CollectionUtil.isNotEmpty(data)) {
				Map<String, Object> map = data.get(0);
				if (columnComputeRuleDTO.getRelationType().equals(ColumnComputeRuleEnum.START_END.getValue())) {
					//求起止值
					String min = StrUtil.toString(map.get("MIN"));
					String max = StrUtil.toString(map.get("MAX"));
					value = min + " ~ " + max;
				} else {
					value = StrUtil.toString(map.get("DD"));
				}
			}
		}
		return value;
	}

	/**
	 * 根据表单状态过滤不需要的计算条件，减少查询时间
	 *
	 * @param columnComputeRuleList
	 * @param formStatusEnum
	 * @return
	 */
	private List<ColumnComputeRuleDTO> filterComputeRule(List<ColumnComputeRuleDTO> columnComputeRuleList, FormStatusEnum formStatusEnum) {
		List<ColumnComputeRuleDTO> filterColumnComputeRuleDTO = new ArrayList<>();
		//录入表单初始化时，只取“等于”和“自动累加”的大流水关系，因为还没有下级关联表的数据，取其他关系 也是为空
		if (formStatusEnum.equals(FormStatusEnum.ADD)) {
			filterColumnComputeRuleDTO = columnComputeRuleList.stream().filter(
					columnComputeRuleDTO ->
							columnComputeRuleDTO.getRelationType().equals(ColumnComputeRuleEnum.EQUAL.getValue()) ||
									columnComputeRuleDTO.getRelationType().equals(ColumnComputeRuleEnum.AUTO_VALUE.getValue())
			).collect(Collectors.toList());
		} else { //保存和组卷的时候，应该需要所有的 规则
			filterColumnComputeRuleDTO = columnComputeRuleList;
		}
		return filterColumnComputeRuleDTO;
	}

	/**
	 * 删除规则
	 *
	 * @param storageLocate
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteByStorageLocate(String storageLocate) {
		return this.remove(Wrappers.<ArchiveEditForm>query().lambda()
				.eq(ArchiveEditForm::getStorageLocate, storageLocate));
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
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.FORM_DEFINE_NAME, true);
			List<List<Object>> read = excel.read();
			final List<ArchiveEditForm> archiveEditForms = CollectionUtil.newArrayList();
			//获取档案类型
			final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
			//处理门类信息
			final Map<String, String> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageName, ArchiveTable::getStorageLocate));
			final Map<String, Long> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuName, TenantMenu::getMenuId));
			menuMaps.put("全部", -1L);
			//循环行
			for (int i = 1, length = read.size(); i < length; i++) {
				//门类名称
				String archiveName = StrUtil.toString(read.get(i).get(0));
				//表单信息
				LinkedHashMap linkedHashMap = JsonUtil.toBean(StrUtil.toString(read.get(i).get(1)), LinkedHashMap.class);
				//模块
				String module = StrUtil.toString(read.get(i).get(2));
				//过滤 门类英文 名称
				String storageLocate = archiveTableMap.get(archiveName);
				ArchiveEditForm archiveEditForm = ArchiveEditForm.builder().tenantId(tenantId).formContent(ObjectUtil.serialize(linkedHashMap)).storageLocate(storageLocate).moduleId(menuMaps.get(module)).build();
				archiveEditForms.add(archiveEditForm);
			}
			boolean batch = Boolean.FALSE;
			if (CollectionUtil.isNotEmpty(archiveEditForms)) {
				batch = this.saveBatch(archiveEditForms);
			}
			return batch ? new R("", "初始化表单信息成功") : new R().fail(null, "初始化表单信息失败");
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
	public List<ArrayList<String>> getFormDefinitionInfo(Long tenantId) throws ArchiveBusinessException {
		//获取档案类型
		final List<ArchiveTable> archiveTables = archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
		//处理档案类型数据
		final Map<String, ArchiveTable> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, archiveTable -> archiveTable));
		//获取表单定义
		final List<ArchiveEditForm> archiveEditForms = this.list(Wrappers.<ArchiveEditForm>lambdaQuery().eq(ArchiveEditForm::getTenantId, tenantId));
		Map<Long, String> menuMaps = getArchiveTypeMenuByTenantId(tenantId).stream().collect(Collectors.toMap(TenantMenu::getMenuId, TenantMenu::getMenuName));
		menuMaps.put(-1L, "全部");
		//门类名称 JSON 模块
		List<ArrayList<String>> results = archiveEditForms.stream().map(archiveEditForm -> CollectionUtil.newArrayList(archiveTableMap.get(archiveEditForm.getStorageLocate()).getStorageName(), processingFormInformation(archiveEditForm.getFormContent()), menuMaps.get(archiveEditForm.getModuleId()))).collect(Collectors.toList());
		return results;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copyByStorageLocate(String srcStorageLocate, String destStorageLocate) {
		List<ArchiveEditForm> list = this.list(Wrappers.<ArchiveEditForm>lambdaQuery().eq(ArchiveEditForm::getStorageLocate, srcStorageLocate));
		if (CollectionUtil.isNotEmpty(list)) {
			list.stream().forEach(archiveEditForm -> {
				archiveEditForm.setId(null);
				archiveEditForm.setStorageLocate(destStorageLocate);
			});
			this.saveBatch(list);
		}
	}

	@Override
	public List<ArchiveTreeResultDTO> getTreeByTreeType(Integer treeType, ArchiveTreeQueryDTO archiveTreeQueryDTO) throws ArchiveBusinessException {
		if (ArchiveTreeQueryDTO.CLASS_NO_TREE.equals(treeType)) {
			List<FilingScope> filingScopes = filingScopeService.getFilingScopeByParentId(archiveTreeQueryDTO);
			//去重
			filingScopes = distinctList(filingScopes);
/*			if(StrUtil.isBlank(archiveTreeQueryDTO.getPath())){
				 throw new ArchiveBusinessException("无法获取分类号,请确认当前节点是否为归档范围节点");
			}*/
			//项目情况下，案卷与卷内只显示当前所在的层级,案卷情况下卷内只显示当前所在的层级
			if(checkType(filingScopes,archiveTreeQueryDTO)){
				boolean contian = filingScopes.stream().anyMatch(m -> {
					String x = m.getPath() == null?"":m.getPath();
					if (x.equals(archiveTreeQueryDTO.getPath())){
						return true;
					}else{
						return false;
					}
				});
				if (contian){
					filingScopes = filingScopes.stream().filter(
							e ->e.getPath().equals(archiveTreeQueryDTO.getPath())
					).collect(Collectors.toList());
				}
			}
			return filingScopes.stream().map(e -> {
				ArchiveTreeResultDTO treeResultDTO = ArchiveTreeResultDTO.builder().name(e.getClassName())
						.id(e.getId()).parentId(e.getParentClassId())
						.path(e.getPath()).title(e.getClassNo()).value(e.getClassNo()).build();
				treeResultDTO.setDisabled(ArchiveConstants.TREE_ROOT_NODE_VALUE.equals(e.getParentClassId()));
				return treeResultDTO;
			}).collect(Collectors.toList());
		}
		return null;
	}

	/**
	 * 去重list
	 * @param filingScopes
	 * @return
	 */
	private List<FilingScope> distinctList(List<FilingScope> filingScopes) {

		return filingScopes.stream().filter(distinctByKey((p) -> (p.getId()))).collect(Collectors.toList());

	}
	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
	private boolean checkType(List<FilingScope> filingScopes, ArchiveTreeQueryDTO archiveTreeQueryDTO) {
		if (ArchiveLayerEnum.PROJECT.getCode().equals(archiveTreeQueryDTO.getFilingType())
				&& (ArchiveLayerEnum.FOLDER.getCode().equals(archiveTreeQueryDTO.getArchiveLayer())
				|| ArchiveLayerEnum.FILE.getCode().equals(archiveTreeQueryDTO.getArchiveLayer()))){
			return true;
		}
		if (ArchiveLayerEnum.FOLDER.getCode().equals(archiveTreeQueryDTO.getFilingType())
				&& ArchiveLayerEnum.FILE.getCode().equals(archiveTreeQueryDTO.getArchiveLayer())
		){
			return true;
		}
		return false;
	}

	/**
	 * 获取最大页号 页号为（1-9） 这种格式
	 * @param tableName
	 * @param ownerId
	 * @return
	 */
	@Override
	public Map<String, Object> getMaxPageNoByOwnerId (String typeCode,Long templateTableId, Long ownerId) throws ArchiveBusinessException {
		ArchiveTable archiveTable = archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
		String sql = "select "+FieldConstants.File.PAGE_NO+ " from "+archiveTable.getStorageLocate()+" where "+FieldConstants.OWNER_ID+" ="+ownerId+
				" and "+FieldConstants.IS_DELETE+"=0  and "+FieldConstants.File.PAGE_NO+" IS NOT NULL ";
		DmlDTO dmlDTO = new DmlDTO();
		dmlDTO.setSql(sql);
		R<List<Map<String, Object>>> result = remoteArchiveService.executeQuery(dmlDTO);
		if (result == null || result.getCode() != CommonConstants.SUCCESS) {
			log.error("根据sql[{}]查询档案表数据失败，{}", sql, result == null ? "" : result.getMsg());
			throw new ArchiveBusinessException("根据sql["+sql+"]查询档案表数据失败");
		}
		List<Map<String, Object>> dataList = result.getData();
		Long value = getMaxPageNo(dataList);
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put(FieldConstants.File.PAGE_NO,value);
		return dataMap;
	}

	private long getMaxPageNo(List<Map<String, Object>> dataList) {
		if(dataList.size()>0){
			long num = 0L;
			for(int i=0;i<dataList.size();i++){
				String value = dataList.get(i).get(FieldConstants.File.PAGE_NO).toString();
				String[] value1 = value.split("-");
				if(value1.length>1){
					long nowNum = parseLong(value1[1],0L);
					if (nowNum >= num ){
						num = nowNum;
					}
				}
			}
			return num;
		}else{
			return 0L;
		}
	}
	public static Long parseLong(String s, Long defaultValue) {
		if (StrUtil.isBlank(s)) {
			return defaultValue;
		}
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException x) {
			return defaultValue;
		}
	}
	/**
	 * 处理表单数据
	 *
	 * @param formContent
	 * @return
	 */
	private String processingFormInformation(Object formContent) {
		byte[] bytes = CesBlobUtil.objConvertToByte(formContent);
		LinkedHashMap map = ObjectUtil.deserialize(bytes);
		return JsonUtil.bean2json(map);
	}

}
