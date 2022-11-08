
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveSearchTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.TagSearch;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.FormConstant;
import com.cescloud.saas.archive.common.constants.MetadataTypeEnum;
import com.cescloud.saas.archive.common.constants.TagConstants;
import com.cescloud.saas.archive.common.constants.TagConstants.UndefinedListTag;
import com.cescloud.saas.archive.common.constants.TagConstants.UndefinedSearchTag;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.search.OperatorKey;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.TagSearchMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveSearchService;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.TagSearchService;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 标签检索配置
 *
 * @author liudong1
 * @date 2019-05-27 15:55:01
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "tag-search")
public class TagSearchServiceImpl extends ServiceImpl<TagSearchMapper, TagSearch> implements TagSearchService {


	@Autowired
	@Lazy
	private MetadataTagService metadataTagService;
	@Autowired
	private RemoteTenantTemplateService remoteTenantTemplateService;
	@Autowired
	private ArchiveSearchService archiveSearchService;
	@Autowired
	private DictItemService dictItemService;


	@Override
	@Cacheable(
			key = "'archive-app-management:archive-tag-search:list-of-defined:'+#tenantId",
			unless = "#result == null "
	)
	public List<DefinedSearchTag> listOfDefined() {
		return getBaseMapper().listOfDefined();
	}

	@Override
	public List<DefinedSearchTag> listOfUnDefined() {
        List<DefinedSearchTag> list = getBaseMapper().listOfUnDefined();
        // 剔除ID类字段
        list = list.stream().filter(tag -> !UndefinedSearchTag.contains(tag.getTagEnglish())).collect(Collectors.toList());
        return list;
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
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.METADATA_TAG_LAYER_SEARCH_NAME, true);
			List<List<Object>> read = excel.read();
			//获取元数据标签
			final List<MetadataTag> metadataTags = metadataTagService.list(Wrappers.<MetadataTag>lambdaQuery().eq(MetadataTag::getTenantId, tenantId));
			//处理元数据
			final Map<String, String> metadataTagMap = metadataTags.stream().collect(Collectors.toMap(MetadataTag::getTagChinese, MetadataTag::getTagEnglish));
			List<TagSearch> tagSearchList = new ArrayList<>();
			//循环行
			for (int i = 1, length = read.size(); i < length; i++) {
				//字段
				//获取字段
				String field = StrUtil.toString(read.get(i).get(0));
				TagSearch tagSearch = TagSearch.builder().sortNo(i).tagEnglish(metadataTagMap.get(field)).conditionType("=").tenantId(tenantId).build();
				tagSearchList.add(tagSearch);
			}
			boolean batch = Boolean.FALSE;
			if (CollUtil.isNotEmpty(tagSearchList)) {
				batch = this.saveBatch(tagSearchList);
			}
			return batch ? new R("", "初始化元数据检索字段配置成功") : new R().fail(null, "初始化元数据检索字段配置失败！！");
		} finally {
			IoUtil.close(excel);
		}
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveSearchDefined(SaveSearchTag saveSearchTag) {
		//删除原来的配置
		this.remove(Wrappers.<TagSearch>query().lambda());

		if (CollectionUtil.isNotEmpty(saveSearchTag.getData())) {
			//批量插入配置
			List<TagSearch> tagSearchs = IntStream.range(0, saveSearchTag.getData().size())
					.mapToObj(i -> {
						TagSearch tagSearch = new TagSearch();
						tagSearch.setTagEnglish(saveSearchTag.getData().get(i).getTagEnglish());
						tagSearch.setConditionType(saveSearchTag.getData().get(i).getConditionType());
						tagSearch.setSortNo(i);

						return tagSearch;
					}).collect(Collectors.toList());
			log.debug("批量插入标签检索定义规则：{}", tagSearchs.toString());
			this.saveBatch(tagSearchs);
		}
		return new R().success(null, "保存成功！");
	}

	@Override
	public List<ArrayList<String>> getMetadataTagsSearchFieldInfo(Long tenantId) {
		//获取信息
		final List<TagSearch> tagSearches = this.list(Wrappers.<TagSearch>lambdaQuery().eq(TagSearch::getTenantId, tenantId));
		//获取字段信息
		final List<MetadataTag> metadataTags = metadataTagService.list(Wrappers.<MetadataTag>lambdaQuery().eq(MetadataTag::getTenantId, tenantId));
		final Map<String, String> metadataTagMap = metadataTags.stream().collect(Collectors.toMap(MetadataTag::getTagEnglish, MetadataTag::getTagChinese));
		//	字段
		List<ArrayList<String>> collect = tagSearches.stream().map(tagSearch -> CollectionUtil.newArrayList(metadataTagMap.get(tagSearch.getTagEnglish()))).collect(Collectors.toList());
		return collect;
	}

	/**
	 * 获取标签的查询配置来展现表单
	 *
	 * @param tenantId
	 * @return
	 */
	@Override
	@Cacheable(
			key = "'archive-app-management:archive-tag-search:search-form:'+#tenantId",
			unless = "#result == null "
	)
	public Object getBasicRetrievalForm(Long tenantId) {
		List<DefinedSearchTag> definedSearchTagList = this.listOfDefined();
		//初始化表单
		LinkedHashMap linkedHashMap = new LinkedHashMap(2);
		//添加表单配置
		linkedHashMap.put(FormConstant.CONFIG, archiveSearchService.configConfiguration());
		//添加 字段信息
		linkedHashMap.put(FormConstant.LIST, listConfiguration(definedSearchTagList));

		return JSONArray.toJSON(linkedHashMap);
	}

	private List listConfiguration(List<DefinedSearchTag> searchTagList) {
		List<Map> list = searchTagList.stream().map(definedSearchTag -> {
			final String tagType = definedSearchTag.getTagType();
			final String name = definedSearchTag.getTagChinese();
			final String model = definedSearchTag.getTagEnglish();
			Map map = null;
			// 特殊处理字段，都是下拉框，由前端处理类型
            if (TagConstants.TYPE_CODE.equals(model) || TagConstants.STATUS.equals(model)
                || TagConstants.ARCHIVE_LAYER.equals(model) || TagConstants.IS_BORROWED.equals(model)) {
                map = archiveSearchService.fieldPublicProperties(map, "select", name, "icon-select",
                    selectConfiguration(definedSearchTag.getDictCode(), definedSearchTag), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
                return map;
            }
            if (MetadataTypeEnum.VARCHAR.getValue().equals(tagType) && StrUtil.isBlank(definedSearchTag.getDictCode())) {
				map = archiveSearchService.fieldPublicProperties(map, "input", name, "el-icon-edit",
						inputConfiguration(definedSearchTag), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
			} else if (MetadataTypeEnum.VARCHAR.getValue().equals(tagType) && ObjectUtil.isNotNull(definedSearchTag.getDictCode())) {
				map = archiveSearchService.fieldPublicProperties(map, "select", name, "icon-select",
						selectConfiguration(definedSearchTag.getDictCode(), definedSearchTag), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
			} else if (MetadataTypeEnum.DATETIME.getValue().equals(tagType) || MetadataTypeEnum.DATE.getValue().equals(tagType)) {
				map = archiveSearchService.fieldPublicProperties(map, "date", name, "el-icon-date",
						dateConfiguration(definedSearchTag), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
			} else if (MetadataTypeEnum.INT.getValue().equals(tagType)) {
				Map map1 = CollectionUtil.newHashMap();
				map1.put("pattern", "/^\\d+$|^\\d+[.]?\\d+$/");
				map1.put("message", "请输入自然数");
				map = archiveSearchService.fieldPublicProperties(map, "input", name, "el-icon-d-caret",
						inputConfiguration(definedSearchTag), RandomUtil.randomLong(), model, CollectionUtil.newArrayList(map1));
			} else {
				map = archiveSearchService.fieldPublicProperties(map, "input", name, "el-icon-edit",
						inputConfiguration(definedSearchTag), RandomUtil.randomLong(), model, CollectionUtil.newArrayList());
			}
			return map;
		}).collect(Collectors.toList());
		return list;
	}

	public Map inputConfiguration(DefinedSearchTag definedSearchTag) {
		Map map = CollectionUtil.newHashMap();
		map.put(FormConstant.WIDTH, 24);
		map.put(FormConstant.DEFAULTVALUE, "");
		map.put(FormConstant.REQUIRED, Boolean.FALSE);
		map.put(FormConstant.DATATYPE, "String");
		map.put(FormConstant.PATTERN, "");
		map.put(FormConstant.PLACEHOLDER, "");
		map.put(FormConstant.REMOTEFUNC, "");
		map.put(FormConstant.CONDITION_TYPE, StrUtil.isBlank(definedSearchTag.getConditionType())
				? OperatorKey.LIKE.getValue() : definedSearchTag.getConditionType());
		return map;
	}

	/**
	 * date 配置信息
	 *
	 * @param definedSearchTag
	 * @return
	 */
	private Map dateConfiguration(DefinedSearchTag definedSearchTag) {
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
		map.put(FormConstant.CONDITION_TYPE, StrUtil.isBlank(definedSearchTag.getConditionType())
				? OperatorKey.LIKE.getValue() : definedSearchTag.getConditionType());
		return map;
	}

	/**
	 * select 配置信息
	 *
	 * @param dictCode
	 * @param definedSearchTag
	 * @return
	 */
	private Map selectConfiguration(String dictCode, DefinedSearchTag definedSearchTag) {
		Map map = CollectionUtil.newHashMap();
		map.put(FormConstant.DEFAULTVALUE, "");
		map.put(FormConstant.DISABLED, Boolean.FALSE);
		map.put(FormConstant.CLEARABLE, Boolean.FALSE);
		map.put(FormConstant.PLACEHOLDER, "");
		map.put(FormConstant.REQUIRED, Boolean.TRUE);
		map.put(FormConstant.SHOWLABEL, Boolean.FALSE);
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
				map1.put("value", dictItem.getItemLabel());
				return map1;
			}).collect(Collectors.toList());
			map.put(FormConstant.OPTIONS, options);
		}
		Map propsMap = CollectionUtil.newHashMap(2);
		propsMap.put("value", "value");
		propsMap.put("label", "label");
		map.put(FormConstant.PROPS, propsMap);
		map.put(FormConstant.MULTIPLE, OperatorKey.IN.getValue().equals(definedSearchTag.getConditionType()));
		map.put(FormConstant.CONDITION_TYPE, StrUtil.isBlank(definedSearchTag.getConditionType())
				? OperatorKey.LIKE.getValue() : definedSearchTag.getConditionType());
		return map;
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
