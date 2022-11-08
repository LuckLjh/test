/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.service.impl</p>
 * <p>文件名:TemplateTypeServiceImpl.java</p>
 * <p>创建时间:2020年2月17日 上午9:18:16</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateType;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.FilingTypeEnum;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.tree.TreeNode;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.TemplateTypeMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qiucs
 * @version 1.0.0 2020年2月17日
 */
@Component
public class TemplateTypeServiceImpl extends ServiceImpl<TemplateTypeMapper, TemplateType>
		implements TemplateTypeService {

	@Autowired
	private TemplateTableService templateTableService;

	@Autowired
	private ArchiveTypeService archiveTypeService;
	@Autowired
	private RemoteTenantTemplateService remoteTenantTemplateService;

	/**
	 * 档案门类模板树根节点
	 */
	private final String rootNodeType = "R";

	/**
	 * 档案门类模板树档案门类模板节点
	 */
	private final String typeNodeType = "A";

	/**
	 * 档案门类模板树表模板节点
	 */
	private final String tableNodeType = "T";

	/**
	 * @see com.baomidou.mybatisplus.extension.service.IService#save(java.lang.Object)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean save(TemplateType entity) {
		//
		checkFilingType(entity);
		//
		checkUniqueName(entity);
		// 设置排序号
		processSortNo(entity);
		//
		final boolean success = super.save(entity);
		// 生成预置整理方式表模板
		autoGeneratePresetTemplateTables(entity);

		return success;
	}

	private void autoGeneratePresetTemplateTables(TemplateType entity) {
		if (FilingTypeEnum.CUSTOM.getCode().equals(entity.getFilingType())) {
			return;
		}
		final FilingTypeEnum ft = FilingTypeEnum.getEnum(entity.getFilingType());
		final ArchiveLayerEnum[] layers = ft.getLayers();
		templateTableService.autoGenerateByLayers(entity.getId(), layers);

	}

	private void checkFilingType(TemplateType entity) {
		final FilingTypeEnum ft = FilingTypeEnum.getEnum(entity.getFilingType());
		if (null == ft) {
			throw new ArchiveRuntimeException("整理方式不合法，请检查！");
		}
		if (null == entity.getId()) {
			return;
		}
		final TemplateType dbEntity = getById(entity.getId());
		if (null == dbEntity) {
			throw new ArchiveRuntimeException("该模板不存在或已被删除，请刷新再操作！");
		}
		if (dbEntity.getFilingType().equals(entity.getFilingType())) {
			return;
		}
		// 预置整理方式只能修改成自定义/其他整理方式
		if (!FilingTypeEnum.CUSTOM.getCode().equals(dbEntity.getFilingType())) {
			if (!FilingTypeEnum.CUSTOM.getCode().equals(dbEntity.getFilingType())) {
				throw new ArchiveRuntimeException(String.format("非“%s”整理方式，只能修改成“%s”整理方式！",
						FilingTypeEnum.CUSTOM.getName(), FilingTypeEnum.CUSTOM.getName()));
			}
		}
	}

	private void checkUniqueName(TemplateType entity) {
		final LambdaQueryWrapper<TemplateType> lambdaQuery = Wrappers.lambdaQuery();
		lambdaQuery.eq(TemplateType::getName, entity.getName());
		if (ObjectUtil.isNotNull(entity.getId())) {
			lambdaQuery.ne(TemplateType::getId, entity.getId());
		}
		if (count(lambdaQuery) > 0) {
			throw new ArchiveRuntimeException(String.format("名称[%s]重复！", entity.getName()));
		}
	}

	private void processSortNo(TemplateType entity) {
		if (null != entity.getSortNo()) {
			return;
		}
		Integer maxSortNo = baseMapper.getMaxSortNo();
		if (null == maxSortNo) {
			maxSortNo = 0;
		}
		entity.setSortNo(++maxSortNo);
	}

	/**
	 * @see com.baomidou.mybatisplus.extension.service.impl.ServiceImpl#updateById(java.lang.Object)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateById(TemplateType entity) {
		//
		checkFilingType(entity);
		// 名称唯一性检查
		checkUniqueName(entity);
		// 设置排序号
		processSortNo(entity);
		return super.updateById(entity);
	}

	/**
	 * @see com.baomidou.mybatisplus.extension.service.IService#removeById(java.io.Serializable)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeById(Serializable id) {
		//TODO检查案门类模模板是否被应用了
		if (!archiveTypeService.checkTemplateType((Long) id)) {
			throw new ArchiveRuntimeException("该档案门类模板已被使用，不能删除！");
		}
		//删除所有表模板
		templateTableService.removeByTemplateTypeId((Long) id);
		return super.removeById(id);
	}

	/**
	 * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService#copy(java.lang.Long,
	 * com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateType)
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copy(Long copyId, TemplateType entity) {
		final TemplateType copyEntity = getById(copyId);
		if (null == copyEntity) {
			throw new ArchiveRuntimeException("复制对象不存在或已被删除，请刷新再操作！");
		}
		checkUniqueName(entity);
		entity.setSortNo(null);
		processSortNo(entity);
		entity.setId(null);
		super.save(entity);
		// 复制表模板
		templateTableService.copyByTemplateTypeId(copyId, entity.getId());
	}

	@Override
	public List<ArrayList<String>> getArchivesTypeTemplateInfor(Long tenantId) {
		final List<TemplateType> templateTypes = this
				.list(Wrappers.<TemplateType>lambdaQuery().eq(TemplateType::getTenantId, tenantId));
		final List<ArrayList<String>> collect = templateTypes.stream().map(templateType -> CollUtil
				.newArrayList(templateType.getName(), FilingTypeEnum.getEnum(templateType.getFilingType()).getName()))
				.collect(Collectors.toList());
		return collect;
	}

    /*private String getTemplateTypeName(String name) {
        final LambdaQueryWrapper<TemplateType> lambdaQuery = Wrappers.<TemplateType> lambdaQuery();
        lambdaQuery.eq(TemplateType::getName, name);
        if (count(lambdaQuery) > 0) {
            return getTemplateTypeName(name + "（复制）");
        }
        return name;
    }

    private String[] copyIgnoreProperties() {
        return new String[] { "id", "sortNo", "tenantId", "revision", "createdBy", "createdTime", "updatedBy",
            "updatedTime" };
    }*/

	/**
	 * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService#getByFilingType(java.lang.String)
	 */
	@Override
	public List<TemplateType> getByFilingType(String filingType) {
		final LambdaQueryWrapper<TemplateType> lambdaQuery = Wrappers.lambdaQuery();
		lambdaQuery.eq(TemplateType::getFilingType, filingType);
		lambdaQuery.orderByAsc(TemplateType::getSortNo);
		return list(lambdaQuery);
	}

	/**
	 * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService#getMaxSortNo()
	 */
	@Override
	public Integer getMaxSortNo() {
		return baseMapper.getMaxSortNo();
	}

	/**
	 * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService#getTreeDataList(java.lang.Long,
	 * java.lang.String)
	 */
	@Override
	public List<TreeNode<Long>> getTreeDataList(Long parentId, String type) {
		switch (type) {
			case rootNodeType:
				return toTreeNode();
			case typeNodeType:
				return toTreeNode(parentId);
			default:
				return null;
		}
	}

	private List<TreeNode<Long>> toTreeNode() {
		final LambdaQueryWrapper<TemplateType> lambdaQuery = Wrappers.lambdaQuery();
		lambdaQuery.orderByAsc(TemplateType::getSortNo);
		final List<TemplateType> list = list(lambdaQuery);
		if (null == list || list.isEmpty()) {
			return Lists.newArrayList();
		}
		final List<TreeNode<Long>> nodeList = Lists.newArrayListWithCapacity(list.size());
		list.forEach(entity -> {
			final TreeNode<Long> node = TreeNode.<Long>builder().id(entity.getId()).parentId(-1L)
					.name(entity.getName()).type(typeNodeType).isLeaf(false).data(entity).build();
			nodeList.add(node);
		});

		return nodeList;
	}

	private List<TreeNode<Long>> toTreeNode(Long id) {
		final List<TemplateTable> tableList = templateTableService.getByTemplateTypeId(id);
		if (null == tableList || tableList.isEmpty()) {
			return Lists.newArrayList();
		}
		final List<TreeNode<Long>> nodeList = Lists.newArrayListWithCapacity(tableList.size());
		tableList.forEach(entity -> {
			final TreeNode<Long> node = TreeNode.<Long>builder().id(entity.getId()).parentId(id)
					.name(entity.getName()).type(tableNodeType).isLeaf(true).data(entity).build();
			nodeList.add(node);
		});

		return nodeList;
	}

	/**
	 * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService#page(com.baomidou.mybatisplus.core.metadata.IPage,
	 * java.lang.String)
	 */
	@Override
	public IPage<TemplateType> page(IPage<TemplateType> page, String keyword) {
		return page(page, queryLambdaQuery(keyword));
	}

	private LambdaQueryWrapper<TemplateType> queryLambdaQuery(String keyword) {
		final LambdaQueryWrapper<TemplateType> lambdaQuery = Wrappers.lambdaQuery();
		if (StrUtil.isNotEmpty(keyword)) {
			lambdaQuery.like(TemplateType::getName, keyword);
		}
		lambdaQuery.orderByAsc(TemplateType::getSortNo);
		return lambdaQuery;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R initialArchiveType(Long templateId, Long tenantId) {
		ExcelReader excel = null;
		try {
			InputStream inputStream = getDefaultTemplateStream(templateId);
			if (ObjectUtil.isNull(inputStream)) {
				return new R<>().fail("", "获取初始化文件异常");
			}
			excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.ARCHIVE_TYPE_TEMPLATE, true);
			final List<List<Object>> read = excel.read();
			final List<TemplateType> templateTypes = new ArrayList<>();
			//Excel 列名： 模板名称	整理方式
			//循环行
			for (int i = 1, length = read.size(); i < length; i++) {
				//名称
				String name = StrUtil.toString(read.get(i).get(0));
				//整理方式
				String finishing = StrUtil.toString(read.get(i).get(1));
				//拼装对象
				TemplateType templateType = TemplateType.builder().name(name).tenantId(tenantId).filingType(FilingTypeEnum.getEnumByName(finishing).getCode()).sortNo(i).build();
				templateTypes.add(templateType);
			}
			boolean batch = Boolean.FALSE;
			if (CollUtil.isNotEmpty(templateTypes)) {
				batch = this.saveBatch(templateTypes);
			}
			return batch ? new R("", "初始化档案模板成功") : new R().fail(null, "初始化档案模板失败！！");
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
		byte[] bytes = tenantTemplate.getTemplateContent();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		return inputStream;
	}


}
