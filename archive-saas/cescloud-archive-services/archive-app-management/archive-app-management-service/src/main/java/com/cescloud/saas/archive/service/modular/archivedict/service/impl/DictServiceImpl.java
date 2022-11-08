
package com.cescloud.saas.archive.service.modular.archivedict.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivedict.entity.Dict;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archivedict.mapper.DictMapper;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 数据字典项
 *
 * @author liudong1
 * @date 2019-03-18 17:44:09
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

	@Autowired
	private DictItemService dictItemService;
	@Autowired
	private MetadataService metadataService;
	@Autowired
	private RemoteTenantTemplateService remoteTenantTemplateService;


	/**
	 * 获取数据字典树
	 *
	 * @return
	 */
	@Override
	public List<Dict> getDictTree() {
		List<Dict> dictList = this.list(Wrappers.<Dict>query().lambda()
				.orderByAsc(Dict::getCreatedTime));
		return dictList;
	}

	/**
	 * 保存数据字典项
	 *
	 * @param dict
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Dict saveDict(final Dict dict) throws ArchiveBusinessException {
		Dict selectDict = this.getOne(Wrappers.<Dict>query().lambda().eq(Dict::getDictLabel, dict.getDictLabel()), false);
		if (ObjectUtil.isNotNull(selectDict)) {
			throw new ArchiveBusinessException("存在相同的分类名称");
		}
		Dict archiveDict = new Dict();
		BeanUtils.copyProperties(dict, archiveDict);
		setDictCode(archiveDict);
		if (StrUtil.isBlank(archiveDict.getDictCode())) {
			throw new ArchiveBusinessException("字典编码生成失败，请检查字典名称！");
		}
		this.save(archiveDict);
		return archiveDict;
	}

	/**
	 * 设置code值
	 * 如果不存在，则直接使用label的拼音首字母赋值给code
	 * 如果存在，则code_no+1 拼接到label的拼音首字母，作为code
	 *
	 * @param archiveDict
	 * @return
	 */
	private Dict setDictCode(final Dict archiveDict) {
		//拼音首字母编码
		String dictCode = HanLP.convertToPinyinFirstCharString(archiveDict.getDictLabel(), "", false);
		Integer maxCodeNo = baseMapper.getMaxCodeNoByCode(dictCode);
		//如果已传编码 则返回
		if (StrUtil.isNotEmpty(archiveDict.getDictCode())) {
			return archiveDict;
		}
		//没有重复的code
		if (ObjectUtil.isNull(maxCodeNo)) {
			archiveDict.setDictCode(dictCode);
			archiveDict.setDictCodeHidden(dictCode);
			archiveDict.setDictCodeNo(0);
		} else {
			archiveDict.setDictCode(dictCode + String.valueOf(maxCodeNo + 1));
			archiveDict.setDictCodeHidden(dictCode);
			archiveDict.setDictCodeNo(maxCodeNo + 1);
		}
		return archiveDict;
	}
	/**
	 * 设置code值
	 * 如果不存在，则直接使用label的拼音首字母赋值给code
	 * 如果存在，则code_no+1 拼接到label的拼音首字母，作为code
	 *
	 * @param archiveDict
	 * @param dictCodeAndCodeNoMap  记录每次准备插入的code和对应的codeno(为批量插入提供的setCode方法)
	 * @return
	 */
	@Override
	public Dict setDictCodeWithBatch(final Dict archiveDict, final Map<String,Integer> dictCodeAndCodeNoMap) {
		//拼音首字母编码
		String dictCode = HanLP.convertToPinyinFirstCharString(archiveDict.getDictLabel(), "", false);
		Integer maxCodeNo = 0;
		//若map中存在dictCode 说明上一次已经插入了相同的code
		if(MapUtil.isNotEmpty(dictCodeAndCodeNoMap) && dictCodeAndCodeNoMap.get(dictCode)!=null){
			maxCodeNo = dictCodeAndCodeNoMap.get(dictCode);
		}else{
			maxCodeNo = baseMapper.getMaxCodeNoByCode(dictCode);
		}
		//如果已传编码 则返回
		if (StrUtil.isNotEmpty(archiveDict.getDictCode())) {
			return archiveDict;
		}
		//没有重复的code
		if (ObjectUtil.isNull(maxCodeNo)) {
			archiveDict.setDictCode(dictCode);
			archiveDict.setDictCodeHidden(dictCode);
			archiveDict.setDictCodeNo(0);
		} else {
			archiveDict.setDictCode(dictCode + String.valueOf(maxCodeNo + 1));
			archiveDict.setDictCodeHidden(dictCode);
			archiveDict.setDictCodeNo(maxCodeNo + 1);
		}
		//map中存放上一次设置的信息
		dictCodeAndCodeNoMap.put(dictCode,archiveDict.getDictCodeNo());
		return archiveDict;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeDict(Long id) throws ArchiveBusinessException {
		Dict dict = this.getById(id);
		if (ObjectUtil.isNull(dict)) {
			throw new ArchiveBusinessException("未找到该数据字典！");
		}
		if (BoolEnum.NO.getCode().equals(dict.getDictClass())) {
			throw new ArchiveBusinessException("该数据为系统数据字典，不能删除！");
		}
		//判断元数据 是否绑定 数据字典
		IPage page = new Page(1, 1);
		//该查询会加上租户条件，所以只查该租户的档案元数据表
		IPage metadataPage = metadataService.page(page, Wrappers.<Metadata>query().lambda()
				.eq(Metadata::getDictCode, dict.getDictCode()));
		if (CollectionUtil.isNotEmpty(metadataPage.getRecords())) {
			throw new ArchiveBusinessException("已经被绑定到元数据，不能删除！");
		}

		this.removeById(id);
		return dictItemService.remove(Wrappers.<DictItem>query().lambda()
				.eq(DictItem::getDictCode, dict.getDictCode()));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R updateDictById(Dict dict) throws ArchiveBusinessException {
		Dict selectDict = this.getOne(Wrappers.<Dict>query().lambda().eq(Dict::getDictLabel, dict.getDictLabel()).ne(Dict::getId, dict.getId()), false);
		if (ObjectUtil.isNotNull(selectDict)) {
			throw new ArchiveBusinessException("存在相同的分类名称");
		}
		Dict originalDict = this.getOne(Wrappers.<Dict>query().lambda().eq(Dict::getId, dict.getId()), false);
		if(!ObjectUtil.equal(originalDict.getDictCode(),dict.getDictCode())){
			LambdaUpdateWrapper<DictItem> updateWrapper  =
					Wrappers.<DictItem>lambdaUpdate().set(DictItem::getDictCode, dict.getDictCode())
							.eq(DictItem::getDictCode, originalDict.getDictCode()).eq(DictItem::getTenantId, SecurityUtils.getUser().getTenantId());
			dictItemService.update(updateWrapper);
		}
		return new R<>(this.updateById(dict));
	}

	/**
	 * 接受到初始化的Excel 进行处理
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户ID
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
		ExcelReader excel = null;
		try {
			InputStream inputStream = getDefaultTemplateStream(templateId);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			//closeAfterReader设为true，创建工作簿后会关闭inputStream流
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.DICT_NAME, true);
			List<List<Object>> read = excel.read();
			//对表单表头校验
			Map<Integer, String> map = InitializeUtil.checkHeader(TemplateFieldConstants.DICT_FIELD_LIST, read.get(0));
			if (CollectionUtils.isEmpty(map)) {
				return new R<>().fail("", "模板表列数据不匹配！！！");
			}
			//新增数据
			Boolean aBoolean = insertData(map, read, tenantId);
			if (aBoolean) {
				return new R("", "成功");
			} else {
				return new R().fail(null, "初始化数据字典失败");
			}
		} finally {
			IoUtil.close(excel);
		}
	}

	/**
	 * 初始化数据字典操作
	 *
	 * @param map      处理好的数据集
	 * @param read     excel数据集
	 * @param tenantId 租户ID
	 * @return 成功状态
	 */
	private Boolean insertData(Map<Integer, String> map, List<List<Object>> read, Long tenantId) {
		List<Dict> saveDict = CollectionUtil.<Dict>newArrayList();
		List<DictItem> saveDictItem = CollectionUtil.<DictItem>newArrayList();
		//循环行
		for (int i = 1, length = read.size(); i < length; i++) {
			//数据处理
			Map<String, String> map1 = InitializeUtil.dataTreating(map, TemplateFieldConstants.DICT_FIELD_LIST, read.get(i));
			if (CollectionUtils.isEmpty(map1)) {
				continue;
			}
			//查询分类编码 为空则新增
			String dictLabel = map1.get(TemplateFieldConstants.DICT_FIELD.DICT_LABEL);
			String dictLabelCode = map1.get(TemplateFieldConstants.DICT_FIELD.DICT_LABEL_CODE);
			Dict dict = saveDict.parallelStream().filter(dict1 -> dict1.getDictLabel().equals(dictLabel)).findAny().orElse(null);
			if (ObjectUtil.isNull(dict)) {
				Dict dicts = Dict.builder().dictLabel(dictLabel).dictCode(dictLabelCode).dictCodeHidden(dictLabelCode).dictCodeNo(0).tenantId(tenantId).build();
				saveDict.add(dicts);
				DictItem dictItem = DictItem.builder()
						.dictCode(dicts.getDictCode())
						.itemCode(map1.get(TemplateFieldConstants.DICT_FIELD.ITEM_CODE))
						.itemLabel(map1.get(TemplateFieldConstants.DICT_FIELD.ITEM_LABEL))
						.itemValue(map1.get(TemplateFieldConstants.DICT_FIELD.CONVERSION_VALUE))
						.tenantId(tenantId).build();
				saveDictItem.add(dictItem);
			} else {
				DictItem dictItem = DictItem.builder()
						.dictCode(dict.getDictCode())
						.itemCode(map1.get(TemplateFieldConstants.DICT_FIELD.ITEM_CODE))
						.itemLabel(map1.get(TemplateFieldConstants.DICT_FIELD.ITEM_LABEL))
						.itemValue(map1.get(TemplateFieldConstants.DICT_FIELD.CONVERSION_VALUE))
						.tenantId(tenantId).build();
				saveDictItem.add(dictItem);
			}
		}
		Boolean batch = Boolean.FALSE;
		if (CollUtil.isNotEmpty(saveDict)) {
			batch = this.saveBatch(saveDict);
		}
		if (CollUtil.isNotEmpty(saveDictItem)) {
			batch = dictItemService.saveBatch(saveDictItem);
		}
		return batch;
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
