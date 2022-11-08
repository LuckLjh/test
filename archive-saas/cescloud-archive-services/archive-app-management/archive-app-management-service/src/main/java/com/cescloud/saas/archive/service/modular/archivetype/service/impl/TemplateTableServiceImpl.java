/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.service.impl</p>
 * <p>文件名:TemplateTableServiceImpl.java</p>
 * <p>创建时间:2020年2月17日 上午9:20:27</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.FilingTypeEnum;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.TemplateTableMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.localcache.LocalCacheExpireTimeEnum;
import com.cescloud.saas.archive.service.modular.common.data.localcache.LocalCacheable;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataBaseService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author qiucs
 * @version 1.0.0 2020年2月17日
 */
@Component
public class TemplateTableServiceImpl extends ServiceImpl<TemplateTableMapper, TemplateTable>
    implements TemplateTableService {

    @Autowired
    private TemplateMetadataService templateMetadataService;

    @Autowired
    private MetadataBaseService metadataBaseService;

    @Autowired
    private ArchiveTypeService archiveTypeService;

    @Autowired
    private TemplateTypeService templateTypeService;

    @Autowired
    private RemoteTenantTemplateService remoteTenantTemplateService;


    /**
     * @see com.baomidou.mybatisplus.extension.service.IService#save(java.lang.Object)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(TemplateTable entity) {
        //
        checkInsertOrDelete(entity.getTemplateTypeId());
        // 检查预置常规父子层级关系，如案卷不能作项目的父级
        checkGeneralRelation(entity);
        // 名称唯一性检查
        checkUniqueName(entity);
        // 设置排序号
        processSortNo(entity);
        // 保存表模板
        //final boolean success = super.save(entity);
		entity.setRevision(1L);
		entity.setCreatedTime(LocalDateTime.now());
		entity.setCreatedBy(SecurityUtils.getUser().getId());
		getBaseMapper().saveTemplateTable(entity);
        // 从基础字段表中继承对应层级的字段
        insertLayerMetadatas(entity);
        return true;
    }

    private void checkInsertOrDelete(Long templateTypeId) {
        final TemplateType templateType = templateTypeService.getById(templateTypeId);
        if (null == templateType) {
            throw new ArchiveRuntimeException("该档案门类模板不存在或已被删除，请刷新再操作！");
        }
        if (!FilingTypeEnum.CUSTOM.getCode().equals(templateType.getFilingType())) {
            throw new ArchiveRuntimeException(
                String.format("该档案门类模板不是“%s”整理方式，不能新增或删除表模板", FilingTypeEnum.CUSTOM.getName()));
        }
    }

    private void checkUniqueName(TemplateTable entity) {
        final LambdaQueryWrapper<TemplateTable> lambdaQuery = Wrappers.lambdaQuery();
        lambdaQuery.eq(TemplateTable::getTemplateTypeId, entity.getTemplateTypeId());
        lambdaQuery.eq(TemplateTable::getName, entity.getName());
        if (ObjectUtil.isNotNull(entity.getId())) {
            lambdaQuery.ne(TemplateTable::getId, entity.getId());
        }
        if (count(lambdaQuery) > 0) {
            throw new ArchiveRuntimeException(String.format("名称[%s]重复！", entity.getName()));
        }
    }

    private void processSortNo(TemplateTable entity) {
        if (null != entity.getSortNo()) {
            return;
        }
        Integer maxSortNo = baseMapper.getMaxSortNo();
        if (null == maxSortNo) {
            maxSortNo = 0;
        }
        entity.setSortNo(++maxSortNo);
    }

    // 表模板创建时，自动从基础字段表中继承对应层级的字段
    private void insertLayerMetadatas(TemplateTable entity) {
        final LambdaQueryWrapper<MetadataBase> lambdaQuery = Wrappers.lambdaQuery();
        lambdaQuery.eq(MetadataBase::getArchiveLayer, entity.getLayerCode()).orderByAsc(MetadataBase::getSortNo);
        final List<MetadataBase> list = metadataBaseService.list(lambdaQuery);
        templateMetadataService.insertLayerMetadatas(entity.getId(), list);
    }

    /**
     * @see com.baomidou.mybatisplus.extension.service.impl.ServiceImpl#updateById(java.lang.Object)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(TemplateTable entity) {
        // 如果表模板被使用了，则不能修改层级编码
        checkUpdateProperties(entity);
        // 检查预置常规父子层级关系，如案卷不能作项目的父级
        checkGeneralRelation(entity);
        // 名称唯一性检查
        checkUniqueName(entity);
        // 设置排序号
        processSortNo(entity);
        return super.updateById(entity);
    }

    private void checkUpdateProperties(TemplateTable entity) {
        // 不能修改层级编码
        final TemplateTable oldEntity = getById(entity.getId());
        if (!oldEntity.getLayerCode().equals(entity.getLayerCode())) {
            throw new ArchiveRuntimeException("表模板的层级不能修改！");
        }
        // 如果表模板被使用了，则不能修改层父子关系
        if (null != oldEntity.getParentId() && null != entity.getParentId()
            && !oldEntity.getParentId().equals(entity.getParentId())) {
            if (!archiveTypeService.checkTemplateType(entity.getTemplateTypeId())) {
                throw new ArchiveRuntimeException("该表模板已被使用，不能修改父表模板关系！");
            }
        }
    }

    /**
     * 检查预置常规父子层级关系，如案卷不能作项目的父级
     *
     * @param entity
     */
    private void checkGeneralRelation(TemplateTable entity) {
        if (null == entity.getParentId()) {
            return;
        }
        final TemplateTable parentEntity = getById(entity.getParentId());
        if (null == parentEntity) {
            throw new ArchiveRuntimeException("该表模板的父表模板不存在或已删除，请检查！");
        }
        final ArchiveLayerEnum currentLayer = ArchiveLayerEnum.getEnum(entity.getLayerCode());
        if (null == currentLayer) {
            return;
        }
        final ArchiveLayerEnum parentLayer = ArchiveLayerEnum.getEnum(parentEntity.getLayerCode());
        if (null == parentLayer) {
            return;
        }
        final ArchiveLayerEnum[] parents = currentLayer.getParents();
        for (final ArchiveLayerEnum p : parents) {
            if (p == parentLayer) {
                return;
            }
        }
        throw new ArchiveRuntimeException(
            String.format("父表模板层级为[%s]不能作为当前模板的[%s]父级，请检查！", parentLayer.getName(), currentLayer.getName()));
    }

    /**
     * @see com.baomidou.mybatisplus.extension.service.IService#removeById(java.io.Serializable)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        final TemplateTable entity = getById(id);
        if (null == entity) {
            throw new ArchiveRuntimeException("该表模板不存在或已被删除，请刷新再操作！");
        }
        //
        checkInsertOrDelete(entity.getTemplateTypeId());
        // 检查表模板是否被应用了
        if (!archiveTypeService.checkTemplateType(entity.getTemplateTypeId())) {
            throw new ArchiveRuntimeException("该表模板已被使用，不能删除！");
        }
        // 检查是否有父子关系
        checkParentReference(entity);
        // 删除表模板下所有字段
        templateMetadataService.removeByTemplateTableId(entity.getId());
        return super.removeById(id);
    }

    private void checkParentReference(TemplateTable entity) {
        final int count = count(
            Wrappers.<TemplateTable> lambdaQuery().eq(TemplateTable::getTemplateTypeId, entity.getTemplateTypeId())
                .eq(TemplateTable::getParentId, entity.getId()));
        if (count > 0) {
            throw new ArchiveRuntimeException("请先解除与子模板的父系再删除！");
        }
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#removeByTemplateTypeId(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByTemplateTypeId(Long templateTypeId) {
        final List<TemplateTable> list = list(
            Wrappers.<TemplateTable> lambdaQuery().eq(TemplateTable::getTemplateTypeId, templateTypeId));
        if (null == list || list.isEmpty()) {
            return;
        }

        list.forEach(entity -> {
            // 删除表模板下所有字段
            templateMetadataService.removeByTemplateTableId(entity.getId());
            super.removeById(entity.getId());
        });
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#copy(java.lang.Long,
     *      java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copy(Long copyId, Long toTemplateTypeId,Map<String, Long> parentMap) {
        final TemplateTable copyEntity = getById(copyId);
        checkInsertOrDelete(toTemplateTypeId);
        copy(copyEntity, toTemplateTypeId,parentMap);
    }

    private void copy(TemplateTable copyEntity, Long toTemplateTypeId ,Map<String, Long> parentMap) {
        if (null == copyEntity) {
            throw new ArchiveRuntimeException("复制对象不存在或已被删除，请刷新再操作！");
        }

        final TemplateTable entity = new TemplateTable();
        BeanUtil.copyProperties(copyEntity, entity, copyIgnoreProperties());
        entity.setTemplateTypeId(toTemplateTypeId);
        entity.setName(getTemplateTableName(copyEntity.getName()));
        entity.setCreatedBy(SecurityUtils.getUser().getId());
        entity.setCreatedTime(LocalDateTime.now());
        processSortNo(entity);
		ArchiveLayerEnum archiveLayerEnum  = ArchiveLayerEnum.getEnum(entity.getLayerCode());
		final ArchiveLayerEnum[] parents = archiveLayerEnum.getParents();
		// 设置父模板
		for (final ArchiveLayerEnum layer : parents) {
			if (parentMap.containsKey(layer.getCode())) {
				entity.setParentId(parentMap.get(layer.getCode()));
				break;
			}
		}
        //super.save(entity);
		getBaseMapper().saveTemplateTable(entity);
		parentMap.put(entity.getLayerCode(),entity.getId());
        // 复制表模板下的字段
        templateMetadataService.copyByTemplateTableId(copyEntity.getId(), entity.getId());
    }

    private String getTemplateTableName(String name) {
        final LambdaQueryWrapper<TemplateTable> lambdaQuery = Wrappers.lambdaQuery();
        lambdaQuery.eq(TemplateTable::getName, name);
        if (count(lambdaQuery) > 0) {
            return getTemplateTableName(name + "（复制）");
        }
        return name;
    }

    private String[] copyIgnoreProperties() {
        return new String[] { "id", "sortNo", "tenantId", "revision", "createdBy", "createdTime", "updatedBy",
            "updatedTime" };
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#copyByTemplateTypeId(java.lang.Long,
     *      java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyByTemplateTypeId(Long copyTemplateTypeId, Long toTemplateTypeId) {
        final List<TemplateTable> copyList = getByTemplateTypeId(copyTemplateTypeId);
        if (null == copyList || copyList.isEmpty()) {
            return;
        }
		final Map<String, Long> parentMap = Maps.newHashMap();
        copyList.forEach(copyEntity -> copy(copyEntity, toTemplateTypeId,parentMap));
    }

    /**
     * 根据类型模板ID获取模板表
     *
     * @param templateTypeId
     * @return
     */
    @Override
    public List<TemplateTable> getByTemplateTypeId(Long templateTypeId) {
        return this.list(Wrappers.<TemplateTable> lambdaQuery().eq(TemplateTable::getTemplateTypeId, templateTypeId)
            .orderByAsc(TemplateTable::getSortNo));
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#autoGenerateByLayers(java.lang.Long,
     *      com.cescloud.saas.archive.common.constants.ArchiveLayerEnum[])
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoGenerateByLayers(Long templateTypeId, ArchiveLayerEnum[] layerEnums) {
        final Map<String, Long> parentMap = Maps.newHashMap();
        Integer sortNo = 0;
        TemplateTable entity;

        for (final ArchiveLayerEnum layerEnum : layerEnums) {
            entity = castByLayer(templateTypeId, layerEnum, parentMap);
            entity.setSortNo(++sortNo);
            //super.save(entity);
			entity.setCreatedBy(SecurityUtils.getUser().getId());
			entity.setCreatedTime(LocalDateTime.now());
			getBaseMapper().saveTemplateTable(entity);
            insertLayerMetadatas(entity);
            parentMap.put(layerEnum.getCode(), entity.getId());
        }
    }

    @Override
    public List<ArrayList<String>> getArchivesTypeTableTemplateInfor(Long tenantId) {
        //获取档案类型模板信息
        final List<TemplateType> templateTypes = templateTypeService
            .list(Wrappers.<TemplateType> lambdaQuery().eq(TemplateType::getTenantId, tenantId));
        //处理数据
        final Map<Long, String> templateTypeMap = templateTypes.stream()
            .collect(Collectors.toMap(TemplateType::getId, TemplateType::getName));
        //获取档案类型表模板信息
        final List<TemplateTable> templateTables = this
            .list(Wrappers.<TemplateTable> lambdaQuery().eq(TemplateTable::getTenantId, tenantId));
        //处理信息
        final Map<Long, String> templateTableMap = templateTables.stream()
            .collect(Collectors.toMap(TemplateTable::getId, TemplateTable::getName));
        //所属模板	表模板名称 父级层级名称	层级编码
        final List<ArrayList<String>> collect = templateTables.stream()
            .map(templateTable -> CollUtil.newArrayList(templateTypeMap.get(templateTable.getTemplateTypeId()),
                templateTable.getName(), templateTableMap.get(templateTable.getParentId()),
                templateTable.getLayerCode()))
            .collect(Collectors.toList());
        return collect;
    }

    private TemplateTable castByLayer(Long templateTypeId, ArchiveLayerEnum layerEnum,Map<String, Long> parentMap) {
        final TemplateTable entity = new TemplateTable();

        entity.setTemplateTypeId(templateTypeId);
        entity.setLayerCode(layerEnum.getCode());
        entity.setName(layerEnum.getName());

        final ArchiveLayerEnum[] parents = layerEnum.getParents();

        // 设置父模板
        for (final ArchiveLayerEnum layer : parents) {
            if (parentMap.containsKey(layer.getCode())) {
                entity.setParentId(parentMap.get(layer.getCode()));
                break;
            }
        }
        return entity;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#getParentMapById(java.lang.Long)
     */
    @Override
    public Map<Long, TemplateTable> getParentMapById(Long id) {
        final Map<Long, TemplateTable> parentMap = Maps.newHashMap();
        final Map<Long, TemplateTable> entityMap = Maps.newHashMap();
        final TemplateTable entity = getById(id);
        final List<TemplateTable> entityList = getByTemplateTypeId(entity.getTemplateTypeId());

        entityList.forEach(e -> entityMap.put(e.getId(), e));
        entityList.forEach(e -> {
            if (null != e.getParentId()) {
                parentMap.put(e.getId(), entityMap.get(e.getParentId()));
            }
        });
        return parentMap;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#getParentListById(java.lang.Long)
     */
    @Override
    public List<TemplateTable> getParentListById(Long id) {
        final List<TemplateTable> parentList = Lists.newArrayList();
        final Map<Long, TemplateTable> parentMap = getParentMapById(id);
        TemplateTable entity = parentMap.get(id);
        while (null != entity) {
            parentList.add(entity);
            entity = parentMap.get(entity.getParentId());
        }
        return parentList;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#page(com.baomidou.mybatisplus.core.metadata.IPage,
     *      java.lang.Long, java.lang.String)
     */
    @Override
    public IPage<TemplateTable> page(IPage<TemplateTable> page, Long templateTypeId, String keyword) {
        return page(page, queryLambdaQuery(templateTypeId, keyword));
    }

    private LambdaQueryWrapper<TemplateTable> queryLambdaQuery(Long templateTypeId, String keyword) {
        final LambdaQueryWrapper<TemplateTable> lambdaQuery = Wrappers.lambdaQuery();
        lambdaQuery.eq(TemplateTable::getTemplateTypeId, templateTypeId);
        if (StrUtil.isNotEmpty(keyword)) {
            lambdaQuery.like(TemplateTable::getName, keyword);
        }
        lambdaQuery.orderByAsc(TemplateTable::getSortNo);
        return lambdaQuery;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#list(java.lang.Long,
     *      java.lang.String)
     */
    @Override
    public List<TemplateTable> list(Long templateTypeId, String keyword) {
        return list(queryLambdaQuery(templateTypeId, keyword));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R initialArchiveTypeTable(Long templateId, Long tenantId) {
        ExcelReader excel = null;
        try {
            final InputStream inputStream = getDefaultTemplateStream(templateId);
            if (ObjectUtil.isNull(inputStream)) {
                return new R<>().fail("", "获取初始化文件异常");
            }
            excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.ARCHIVE_TYPE_TABLE_TEMPLATE, true);
            final List<List<Object>> read = excel.read();
            //获取模板信息
            final List<TemplateType> templateTypes = templateTypeService
                .list(Wrappers.<TemplateType> lambdaQuery().eq(TemplateType::getTenantId, tenantId));
            //处理数据
            final Map<String, Long> templateTypeMap = templateTypes.stream()
                .collect(Collectors.toMap(TemplateType::getName, TemplateType::getId));
            //列名 所属模板	表模板名称	层级编码
            final List<TemplateTable> templateTables = new ArrayList<>();
            //循环行
            for (int i = 1, length = read.size(); i < length; i++) {
                //获取所属模板id
                final Long templateTypeId = templateTypeMap.get(StrUtil.toString(read.get(i).get(0)));
                //获取层级
                final String layerCode = StrUtil.toString(read.get(i).get(3));
                final TemplateTable templateTable = TemplateTable.builder().templateTypeId(templateTypeId)
                    .name(StrUtil.toString(read.get(i).get(1))).layerCode(layerCode).sortNo(i).tenantId(tenantId)
                    .build();
                templateTables.add(templateTable);
            }
            //先进性批次报存 ，在进行数据关联
            boolean batch = Boolean.FALSE;
            if (CollUtil.isNotEmpty(templateTables)) {
                //batch = this.saveBatch(templateTables);
				getBaseMapper().saveTemplateTableBatch(templateTables);
            }
            batch = updateDataMessage(templateTables, read, templateTypeMap, tenantId);
            return batch ? new R("", "初始化档案类型表信息成功") : new R().fail(null, "初始化档案类型表信息失败！！");

        } finally {
            IoUtil.close(excel);
        }
    }

    /**
     * 对数据修改（初始化 对数据修改）
     *
     * @param templateTables
     * @param read
     * @param templateTypeMap
     * @param tenantId
     * @return
     */
    private Boolean updateDataMessage(List<TemplateTable> templateTables, List<List<Object>> read, Map<String, Long> templateTypeMap, Long tenantId) {
        //获取数据进行修改
        final List<TemplateTable> templateTable = this
            .list(Wrappers.<TemplateTable> lambdaQuery().eq(TemplateTable::getTenantId, tenantId));
        templateTables = new ArrayList<>();
        for (int i = 1, length = read.size(); i < length; i++) {
            //获取所属模板id
            final Long templateTypeId = templateTypeMap.get(StrUtil.toString(read.get(i).get(0)));
            //获取父级名称
            final String parentName = StrUtil.toString(read.get(i).get(2));
            //获取当前表模板名称
            final String tableName = StrUtil.toString(read.get(i).get(1));
            for (final TemplateTable table : templateTable) {
                if (templateTypeId.equals(table.getTemplateTypeId()) && tableName.equals(table.getName())
                    && StrUtil.isNotBlank(parentName)) {
                    //过滤父级模板表名称
                    final TemplateTable templateTable2 = templateTable.stream()
                        .filter(templateTable1 -> templateTable1.getTemplateTypeId().equals(templateTypeId)
                            && templateTable1.getName().equals(parentName))
                        .findAny().orElseGet(()->new TemplateTable());
                    table.setParentId(templateTable2.getId());
                    templateTables.add(table);
                    break;
                }
            }
        }
        boolean batch = Boolean.FALSE;
        if (CollUtil.isNotEmpty(templateTables)) {
            batch = this.updateBatchById(templateTables);
        }
        return batch;
    }

    /**
     * 获取 初始化模板文件流
     *
     * @param templateId
     *            模板id
     * @return
     */
    private InputStream getDefaultTemplateStream(Long templateId) {
        final TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
        final byte[] bytes = tenantTemplate.getTemplateContent();
        final InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#getEntryChildListById(java.lang.Long)
     */
    @Override
    public List<TemplateTable> getEntryChildListById(Long id) {
        final LambdaQueryWrapper<TemplateTable> lambdaQuery = Wrappers.lambdaQuery();
        lambdaQuery.eq(TemplateTable::getParentId, id).orderByAsc(TemplateTable::getSortNo);
        final List<TemplateTable> list = list(lambdaQuery);
        return list.stream().filter(entity -> {
            return !ArchiveLayerEnum.INFO.getCode().equals(entity.getLayerCode())
                && !ArchiveLayerEnum.SIGNATRUE.getCode().equals(entity.getLayerCode());
        }).collect(Collectors.toList());
    }

	@Override
	public List<TemplateTable> getAllChildListById(Long id) {
		final List<TemplateTable> childList = Lists.newArrayList();
		final TemplateTable entity = getById(id);
		final List<TemplateTable> entityList = getByTemplateTypeId(entity.getTemplateTypeId());
		childList.add(entity);
		List<TemplateTable> collect = entityList.stream().filter(e -> {
			return !ArchiveLayerEnum.INFO.getCode().equals(e.getLayerCode())
					&& !ArchiveLayerEnum.SIGNATRUE.getCode().equals(e.getLayerCode())
					&& !ArchiveLayerEnum.DOCUMENT.getCode().equals(e.getLayerCode());
		}).collect(Collectors.toList());
		return getChildsById(id, collect, childList);
	}

	@Override
	public List<TemplateTable> getChildsById(Long id,List<TemplateTable> templateTables,List<TemplateTable> childList) {
		for (TemplateTable templateTable : templateTables) {
			if (templateTable.getParentId() != null && templateTable.getParentId().equals(id)){
				childList.add(templateTable);
				getChildsById(templateTable.getId(), templateTables, childList);
			}
		}
		return childList;
	}

	/**
     *
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService#getParentById(java.lang.Long)
     */
    @Override
    public TemplateTable getParentById(Long id) {
        return getParentMapById(id).get(id);
    }

	@Override
	public List<TemplateTable> getListByArchiveCode(String archiveCode) {
		return this.baseMapper.getListByArchiveCode(archiveCode);
	}

}
