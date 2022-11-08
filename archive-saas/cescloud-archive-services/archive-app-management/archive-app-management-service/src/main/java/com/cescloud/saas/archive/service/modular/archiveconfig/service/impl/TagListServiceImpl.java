
package com.cescloud.saas.archive.service.modular.archiveconfig.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveListTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.entity.TagList;
import com.cescloud.saas.archive.api.modular.archivetype.constant.ListAlignmentEnum;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.TagConstants.UndefinedListTag;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.mapper.TagListMapper;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.TagListService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagService;
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
 * 标签列表配置
 *
 * @author liudong1
 * @date 2019-05-27 15:20:07
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "tag-list")
public class TagListServiceImpl extends ServiceImpl<TagListMapper, TagList> implements TagListService {

	@Autowired
	private MetadataTagService metadataTagService;

	@Autowired
	private RemoteTenantTemplateService remoteTenantTemplateService;


	@Override
	@Cacheable(key = "'archive-app-management:tag-list:defined:all'", unless = "#result == null || #result.size() == 0")
	public List<DefinedListTag> listOfDefined() {
		return getBaseMapper().listOfDefined();
	}

	@Override
	public List<DefinedListTag> listOfUnDefined() {
        List<DefinedListTag> list = getBaseMapper().listOfUnDefined();
        // 剔除ID类字段
        list = list.stream().filter(tag -> !UndefinedListTag.contains(tag.getTagEnglish())).collect(Collectors.toList());
        return list;
	}

	@CacheEvict(allEntries = true)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveListDefined(SaveListTag saveListTag) {
		//删除原来的配置
		this.remove(Wrappers.<TagList>query().lambda());

		if (CollectionUtil.isNotEmpty(saveListTag.getData())) {
			//批量插入配置
			List<TagList> tagLists = IntStream.range(0, saveListTag.getData().size())
					.mapToObj(i -> {
						List<DefinedListTag> definedTag = saveListTag.getData();
						TagList tagList = new TagList();
						tagList.setTagEnglish(definedTag.get(i).getTagEnglish());
						tagList.setShowAlias(definedTag.get(i).getTagChinese());
						tagList.setAlign(definedTag.get(i).getAlign());
						tagList.setWidth(definedTag.get(i).getWidth());
						tagList.setSortNo(i);

						return tagList;
					}).collect(Collectors.toList());
			log.debug("批量插入标签列表定义规则：{}", tagLists.toString());
			this.saveBatch(tagLists);
		}
		return new R().success(null, "保存成功！");
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
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.METADATA_TAG_LAYER_LIST_NAME, true);
			List<List<Object>> read = excel.read();
			List<TagList> tagLists = CollectionUtil.newArrayList();
			//获取元数据标签
			final List<MetadataTag> metadataTags = metadataTagService.list(Wrappers.<MetadataTag>lambdaQuery().eq(MetadataTag::getTenantId, tenantId));
			//处理元数据
			final Map<String, String> metadataTagMap = metadataTags.stream().collect(Collectors.toMap(MetadataTag::getTagChinese, MetadataTag::getTagEnglish));
			//循环行
			for (int i = 1, length = read.size(); i < length; i++) {
				//	字段	对齐方式
				// 获取字段
				String field = StrUtil.toString(read.get(i).get(0));
				//获取 对齐方式
				String alignment = StrUtil.toString(read.get(i).get(1));
				//获取 宽度
				Integer width = InitializeUtil.toInteger(InitializeUtil.checkListVal(read.get(i), 2));
				if (width == 0) {
					width = null;
				}
				TagList tagList = TagList.builder().sortNo(i).tenantId(tenantId).tagEnglish(metadataTagMap.get(field))
						.width(width).showAlias(field).align(ListAlignmentEnum.getEnumByName(alignment).getCode()).build();
				tagLists.add(tagList);
			}
			Boolean batch = Boolean.FALSE;
			if (CollUtil.isNotEmpty(tagLists)) {
				batch = this.saveBatch(tagLists);
			}
			return batch ? new R("", " 初始化元数据检索列表配置成功") : new R().fail(null, " 初始化元数据检索列表配置失败！！");
		} finally {
			IoUtil.close(excel);
		}

	}

	@Override
	public List<ArrayList<String>> getMetadataTagsSearchListInfo(Long tenantId) {
		final List<TagList> tagLists = this.list(Wrappers.<TagList>lambdaQuery().eq(TagList::getTenantId, tenantId));
		//层次	字段	对齐方式 宽度
		List<ArrayList<String>> collect = tagLists.stream().map(tagList -> CollectionUtil.newArrayList(StrUtil.toString(tagList.getShowAlias()),
				ObjectUtil.isNotNull(tagList.getAlign()) ? ListAlignmentEnum.getEnum(StrUtil.toString(tagList.getAlign())).getName() : StrUtil.EMPTY,
				String.valueOf(tagList.getWidth()))).collect(Collectors.toList());
		return collect;
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
