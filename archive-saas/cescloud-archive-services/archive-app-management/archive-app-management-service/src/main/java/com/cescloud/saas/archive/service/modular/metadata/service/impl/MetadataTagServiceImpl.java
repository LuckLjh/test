
package com.cescloud.saas.archive.service.modular.metadata.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archiveconfig.constant.ConfigConstant;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.LayerService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.metadata.mapper.MetadataTagMapper;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 档案层次的元数据标签
 *
 * @author liudong1
 * @date 2019-05-23 15:37:43
 */
@Service
@AllArgsConstructor
public class MetadataTagServiceImpl extends ServiceImpl<MetadataTagMapper, MetadataTag> implements MetadataTagService {

	@Autowired
	private LayerService layerService;

	private final RemoteTenantTemplateService remoteTenantTemplateService;

	@Autowired
	private ArchiveTypeService archiveTypeService;
	@Autowired
	private MetadataService metaDataService;

	@Override
	public List<MetadataTag> listByTenantId(Long tenantId) {
		if (ObjectUtil.isNotNull(tenantId)) {
			return this.list(Wrappers.<MetadataTag>query().lambda()
					.eq(MetadataTag::getTenantId, tenantId));
		} else {
			return this.list();
		}
	}

	@Override
	public IPage<MetadataTag> page(IPage<MetadataTag> page, String keyword) {
		final LambdaQueryWrapper<MetadataTag> lambdaQuery = Wrappers.<MetadataTag> lambdaQuery();
		if (StrUtil.isNotEmpty(keyword)) {
			lambdaQuery.or().like(MetadataTag::getTagChinese, keyword).or().like(MetadataTag::getTagEnglish, keyword.toUpperCase());
		}
		return page(page, lambdaQuery);
	}

	@Override
	public List<Layer> getLayerCommonTree() {
		Layer commonLayer = ConfigConstant.COMMON_LAYER;
		List<Layer> list = layerService.list(Wrappers.<Layer>lambdaQuery().orderByAsc(Layer::getSortNo));
		list.add(0, commonLayer);
		return list;
	}

	@Override
	public List<ArrayList<String>> getMetadataTagsInfo(Long tenantId) {
		//查询元数据信息
		final List<MetadataTag> metadataTags = this.list(Wrappers.<MetadataTag>lambdaQuery().eq(MetadataTag::getTenantId, tenantId));
		//列名 标签名称	标签编码	标签类型	绑定编码
		List<ArrayList<String>> collect = metadataTags.stream().map(metadataTag -> CollectionUtil.newArrayList(InitializeUtil.toString(metadataTag.getTagChinese()), InitializeUtil.toString(metadataTag.getTagEnglish()), InitializeUtil.toString(metadataTag.getTagType()), InitializeUtil.toString(metadataTag.getDictCode()))).collect(Collectors.toList());
		return collect;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R initializeMetadata(Long templateId, Long tenantId) {
		ExcelReader excel = null;
		try {
			InputStream inputStream = getDefaultTemplateStream(templateId);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.METADATA_TAG_LAYER_NAME, true);
			List<List<Object>> read = excel.read();
			List<MetadataTag> metadataTags = new ArrayList<>();
			// 行循环
			for (int i = 1, length = read.size(); i < length; i++) {
				if(ObjectUtil.isNull(read.get(i))){
					continue;
				}
				//列名 标签名称	标签编码	标签类型	绑定编码
				//获取标签名称
				String metadataName = InitializeUtil.checkListVal(read.get(i),0);
				//获取标签编码
				String metadataCode = InitializeUtil.checkListVal(read.get(i),1);
				//获取标签类型
				String type = InitializeUtil.checkListVal(read.get(i),2);
				//获取绑定编码
				String dictCode = InitializeUtil.checkListVal(read.get(i),3);
				MetadataTag metadataTag = MetadataTag.builder().tagChinese(metadataName).tagEnglish(metadataCode).dictCode(dictCode).tagType(type).tenantId(tenantId).build();
				metadataTags.add(metadataTag);
			}
			boolean batch = Boolean.FALSE;
			if (CollectionUtil.isNotEmpty(metadataTags)) {
				batch = this.saveBatch(metadataTags);
			}
			return batch ? new R("", " 初始化元数据成功") : new R().fail(null, " 初始化元数据失败！！");

		} finally {
			IoUtil.close(excel);
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

	/**
	 *  判断下有没有绑定过绑定过字段绑定了不让删除
	 *
	 * @param id
	 * @return
	 */
	@Override
	public boolean removeMetadataTagById(Long id){
		MetadataTag metadataTag = this.getById(id);
		List<Metadata> list = metaDataService.list(
				Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTagEnglish,metadataTag.getTagEnglish()).eq(Metadata::getTenantId, SecurityUtils.getUser().getTenantId()));
		if(list.size()>0){
			StringBuffer sb = new StringBuffer();
			list.forEach(e ->{
				sb.append(e.getTableId()).append(",");
			});
			String value = sb.toString();
			value = value.substring(0 , value.length()-1);
			List<ArchiveType> typeLists = archiveTypeService.getTypeNameByTableIds(SecurityUtils.getUser().getTenantId(),value);
			StringBuffer sb1 = new StringBuffer();
			typeLists.forEach(e ->{
				sb1.append(" ").append(e.getTypeName()).append(" ").append(",");
			});
			String value1 = sb1.toString();
			value1 = value1.substring(0 , value1.length()-1);
			throw new ArchiveRuntimeException(String.format("以下类型[%s]已绑定此标签，无法删除",value1));
		}
		return this.removeById(id);
	}
}
