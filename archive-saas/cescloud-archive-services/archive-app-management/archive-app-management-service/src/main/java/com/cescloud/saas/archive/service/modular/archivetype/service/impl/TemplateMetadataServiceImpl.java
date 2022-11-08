/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.service.impl</p>
 * <p>文件名:TemplateMetadataService.java</p>
 * <p>创建时间:2020年2月17日 上午9:22:56</p>
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
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateMetadata;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateType;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.MetadataTypeEnum;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.TemplateMetadataMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author qiucs
 * @version 1.0.0 2020年2月17日
 */
@Slf4j
@Component
public class TemplateMetadataServiceImpl extends ServiceImpl<TemplateMetadataMapper, TemplateMetadata>
    implements com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService {

    @Autowired
    private TemplateTableService templateTableService;

    @Autowired
    private TemplateTypeService templateTypeService;

    @Autowired
    private RemoteTenantTemplateService remoteTenantTemplateService;

    /**
     * @see com.baomidou.mybatisplus.extension.service.IService#save(java.lang.Object)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(TemplateMetadata entity) {
        // 英文名称唯一性检查
        checkUniqueCode(entity);
        // 中文名称唯一性检查
        checkUniqueName(entity);
        // 标签绑定检查
        checkUniqueTag(entity);
        // 字段长度处理
        processLength(entity);
        // 设置排序号
        processSortNo(entity);
        return super.save(entity);
    }

    private void checkUniqueName(TemplateMetadata entity) {
        final LambdaQueryWrapper<TemplateMetadata> lambdaQuery = Wrappers.<TemplateMetadata> lambdaQuery();
        lambdaQuery.eq(TemplateMetadata::getTemplateTableId, entity.getTemplateTableId());
        lambdaQuery.eq(TemplateMetadata::getMetadataChinese, entity.getMetadataChinese());
        if (ObjectUtil.isNotNull(entity.getId())) {
            lambdaQuery.ne(TemplateMetadata::getId, entity.getId());
        }
        if (count(lambdaQuery) > 0) {
            throw new ArchiveRuntimeException(String.format("字段中文名称[%s]重复！", entity.getMetadataChinese()));
        }
    }

    private void checkUniqueCode(TemplateMetadata entity) {
        final LambdaQueryWrapper<TemplateMetadata> lambdaQuery = Wrappers.<TemplateMetadata> lambdaQuery();
        lambdaQuery.eq(TemplateMetadata::getTemplateTableId, entity.getTemplateTableId());
        lambdaQuery.eq(TemplateMetadata::getMetadataEnglish, entity.getMetadataEnglish());
        if (ObjectUtil.isNotNull(entity.getId())) {
            lambdaQuery.ne(TemplateMetadata::getId, entity.getId());
        }
        if (count(lambdaQuery) > 0) {
            throw new ArchiveRuntimeException(String.format("字段英文名称[%s]重复！", entity.getMetadataChinese()));
        }
    }

    private void checkUniqueTag(TemplateMetadata entity) {
        if (null == entity.getTagEnglish() || StrUtil.isEmpty(entity.getTagEnglish())) {
            return;
        }
        final LambdaQueryWrapper<TemplateMetadata> lambdaQuery = Wrappers.<TemplateMetadata> lambdaQuery();
        lambdaQuery.eq(TemplateMetadata::getTemplateTableId, entity.getTemplateTableId());
        lambdaQuery.eq(TemplateMetadata::getTagEnglish, entity.getTagEnglish());
        if (ObjectUtil.isNotNull(entity.getId())) {
            lambdaQuery.ne(TemplateMetadata::getId, entity.getId());
        }
        if (count(lambdaQuery) > 0) {
            throw new ArchiveRuntimeException("该标签已经被绑定，不能重复绑定！");
        }
    }

    /*private void processMetadataEnglish(TemplateMetadata entity) {
        if (!StrUtil.isBlank(entity.getMetadataEnglish())) {
            return;
        }
        //拼音首字母编码
        final String metadataEnglish = HanLPUtil.toPinyinFirstCharString(entity.getMetadataChinese());
        if (StrUtil.isBlank(metadataEnglish)) {
            throw new ArchiveRuntimeException("字段英文值生成失败，请检查字段名称！");
        }
        //没有重复的code
        processAutoMetadataEnglish(entity, metadataEnglish, new AtomicInteger(0));
    }

    private void processAutoMetadataEnglish(TemplateMetadata entity, String metadataEnglish, AtomicInteger num) {
        final LambdaQueryWrapper<TemplateMetadata> lambdaQuery = Wrappers.<TemplateMetadata> lambdaQuery();
        lambdaQuery.eq(TemplateMetadata::getTemplateTableId, entity.getTemplateTableId());
        lambdaQuery.eq(TemplateMetadata::getMetadataEnglish, metadataEnglish + num.incrementAndGet());
        if (ObjectUtil.isNotNull(entity.getId())) {
            lambdaQuery.ne(TemplateMetadata::getId, entity.getId());
        }
        if (count(lambdaQuery) > 0) {
            processAutoMetadataEnglish(entity, metadataEnglish, num);
        } else {
            entity.setMetadataEnglish(metadataEnglish + num.get());
        }
    }*/

    private void processLength(TemplateMetadata entity) {
        if (entity.getMetadataType().equals(MetadataTypeEnum.DATE.getValue())
            || entity.getMetadataType().equals(MetadataTypeEnum.DATETIME.getValue())) {
            entity.setMetadataLength(null);
        }
    }

    private void processSortNo(TemplateMetadata entity) {
        if (null != entity.getSortNo()) {
            return;
        }
        Integer maxSortNo = baseMapper.getMaxSortNo(entity.getTemplateTableId());
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
    public boolean updateById(TemplateMetadata entity) {
        // 英文名称唯一性检查
        checkUniqueCode(entity);
        // 中文名称唯一性检查
        checkUniqueName(entity);
        // 标签绑定检查
        checkUniqueTag(entity);
        // 字段长度处理
        processLength(entity);
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
        final TemplateMetadata entity = getById(id);
        if (null == entity) {
            throw new ArchiveRuntimeException("该模板字段不存在或已被删除，请刷新再操作！");
        }
        // 检查模板字段为系统字段
        if (BoolEnum.NO.getCode().equals(entity.getMetadataClass())) {
            throw new ArchiveRuntimeException("该模板字段为系统字段，不能删除！");
        }
        return super.removeById(id);
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService#insertLayerMetadatas(java.lang.Long,
     *      java.util.List)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertLayerMetadatas(Long templateTableId, List<MetadataBase> metadataBases) {
        final List<TemplateMetadata> entityList = new ArrayList<TemplateMetadata>(metadataBases.size());
        TemplateMetadata entity;
        for (int i = 0, len = metadataBases.size(); i < len; i++) {
            entity = new TemplateMetadata();
            BeanUtil.copyProperties(metadataBases.get(i), entity, copyIgnoreProperties());
            entity.setTemplateTableId(templateTableId);
            entity.setSortNo(i + 1);
            entityList.add(entity);
        }
        saveBatch(entityList);
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService#copy(java.lang.Long[],
     *      java.lang.Long)
     */
    @Override
	@Transactional(rollbackFor = Exception.class)
    public void copy(Long[] copyIds, Long toTemplateTableId) {
        final Collection<TemplateMetadata> copyList = listByIds(Arrays.asList(copyIds));
        copyList.forEach(copyEntity -> {
            if (!checkUniqueCode(toTemplateTableId, copyEntity.getMetadataEnglish())) {
                if (log.isDebugEnabled()) {
                    log.debug("字段英文名称[{}]已存在，跳过复制", copyEntity.getMetadataEnglish());
                }
                return;
            }
            final TemplateMetadata entity = new TemplateMetadata();
            BeanUtil.copyProperties(copyEntity, entity, copyIgnoreProperties());
            entity.setTemplateTableId(toTemplateTableId);
            if (!checkUniqueName(toTemplateTableId, entity.getMetadataChinese())) {
                entity.setMetadataChinese(copyEntity.getMetadataChinese() + "（复制）");
            }
            processSortNo(copyEntity);
            // 字段长度处理
            processLength(entity);

            super.save(entity);

        });
    }

    private boolean checkUniqueName(Long toTemplateTableId, String metadataChinese) {
        final LambdaQueryWrapper<TemplateMetadata> lambdaQuery = Wrappers.<TemplateMetadata> lambdaQuery();
        lambdaQuery.eq(TemplateMetadata::getTemplateTableId, toTemplateTableId);
        lambdaQuery.eq(TemplateMetadata::getMetadataChinese, metadataChinese);
        return (0 == count(lambdaQuery));
    }

    private boolean checkUniqueCode(Long toTemplateTableId, String metadataEnglish) {
        final LambdaQueryWrapper<TemplateMetadata> lambdaQuery = Wrappers.<TemplateMetadata> lambdaQuery();
        lambdaQuery.eq(TemplateMetadata::getTemplateTableId, toTemplateTableId);
        lambdaQuery.eq(TemplateMetadata::getMetadataEnglish, metadataEnglish);
        return (0 == count(lambdaQuery));
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService#copyByTemplateTableId(java.lang.Long,
     *      java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyByTemplateTableId(Long copyTemplateTableId, Long toTemplateTableId) {

        final List<TemplateMetadata> copyList = getByTemplateTableId(copyTemplateTableId);

        if (null == copyList || copyList.isEmpty()) {
            return;
        }
        final int size = copyList.size();
        final List<TemplateMetadata> entityList = Lists.newArrayListWithCapacity(size);
        TemplateMetadata entity;
        for (int i = 0; i < size; i++) {
            entity = new TemplateMetadata();
            BeanUtil.copyProperties(copyList.get(i), entity, copyIgnoreProperties());
            entity.setTemplateTableId(toTemplateTableId);
            entity.setSortNo(i + 1);
            entityList.add(entity);
        }
        saveBatch(entityList);

    }

    private String[] copyIgnoreProperties() {
        return new String[] { "id", "sortNo", "tenantId", "revision", "createdBy", "createdTime", "updatedBy",
            "updatedTime" };
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService#getByTemplateTableId(java.lang.Long)
     */
    @Override
    public List<TemplateMetadata> getByTemplateTableId(Long templateTableId) {
        final LambdaQueryWrapper<TemplateMetadata> lambdaQuery = Wrappers.<TemplateMetadata> lambdaQuery();
        lambdaQuery.eq(TemplateMetadata::getTemplateTableId, templateTableId)
            .orderByAsc(TemplateMetadata::getSortNo);
        return list(lambdaQuery);
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService#removeByTemplateTableId(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByTemplateTableId(Long templateTableId) {
        return remove(
            Wrappers.<TemplateMetadata> lambdaQuery().eq(TemplateMetadata::getTemplateTableId, templateTableId));
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService#page(com.baomidou.mybatisplus.core.metadata.IPage,
     *      java.lang.Long, java.lang.String)
     */
    @Override
    public IPage<TemplateMetadata> page(IPage<TemplateMetadata> page, Long templateTableId, String keyword) {
        return page(page, queryLambdaQuery(templateTableId, keyword));
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService#list(java.lang.Long,
     *      java.lang.String)
     */
    @Override
    public List<TemplateMetadata> list(Long templateTableId, String keyword) {
        return list(queryLambdaQuery(templateTableId, keyword));
    }

    private LambdaQueryWrapper<TemplateMetadata> queryLambdaQuery(Long templateTableId, String keyword) {
        final LambdaQueryWrapper<TemplateMetadata> lambdaQuery = Wrappers.<TemplateMetadata> lambdaQuery();
        lambdaQuery.eq(TemplateMetadata::getTemplateTableId, templateTableId);
        if (StrUtil.isNotEmpty(keyword)) {
            lambdaQuery.and(consumer -> {
                consumer.or().like(TemplateMetadata::getMetadataChinese, keyword).or()
                    .like(TemplateMetadata::getMetadataEnglish, keyword.toLowerCase());
            });

        }
        lambdaQuery.orderByAsc(TemplateMetadata::getSortNo);
        return lambdaQuery;
    }

    @Override
    public List<ArrayList<String>> getArchivesTypeTableTemplateMetadataInfor(Long tenantId) {
        //获取档案类型模板
        final List<TemplateType> templateTypes = templateTypeService
            .list(Wrappers.<TemplateType> lambdaQuery().eq(TemplateType::getTenantId, tenantId));
        //处理档案类型模板信息
        final Map<Long, String> templateTypeMap = templateTypes.stream()
            .collect(Collectors.toMap(TemplateType::getId, TemplateType::getName));
        //获取档案类型模板表
        final List<TemplateTable> templateTables = templateTableService
            .list(Wrappers.<TemplateTable> lambdaQuery().eq(TemplateTable::getTenantId, tenantId));
        //处理信息 id ,name
        final Map<Long, String> templateTableMap = templateTables.stream()
            .collect(Collectors.toMap(TemplateTable::getId, TemplateTable::getName));
        //处理信息 id，typeName
        final Map<Long, String> templateTableParentIdMap = templateTables.stream().collect(Collectors
            .toMap(TemplateTable::getId, templateTable -> templateTypeMap.get(templateTable.getTemplateTypeId())));
        //获取档案类型模板表 id
        final List<Long> templateTableids = templateTables.stream().map(templateTable -> templateTable.getId())
            .collect(Collectors.toList());
        //获取字段信息
        final List<TemplateMetadata> templateMetadatas = this
            .list(Wrappers.<TemplateMetadata> lambdaQuery().in(TemplateMetadata::getTemplateTableId, templateTableids));

        // 所属模板 表模板名称	中文名称	英文名称	系统字段[0]/业务字段[1] 标签字段	数据类型	字段长度	数据字典code	是否列表显示	是否参与编辑	是否参与查询	是否重复字段
        final List<ArrayList<String>> collect = templateMetadatas.stream()
            .map(templateMetadata -> CollUtil.newArrayList(
                templateTableParentIdMap.get(templateMetadata.getTemplateTableId()),
                templateTableMap.get(templateMetadata.getTemplateTableId()),
                templateMetadata.getMetadataChinese(),
                InitializeUtil.toString(templateMetadata.getMetadataEnglish()),
                InitializeUtil.toString(templateMetadata.getMetadataSys()),
                InitializeUtil.toString(templateMetadata.getTagEnglish()),
                templateMetadata.getMetadataType(),
                InitializeUtil.toString(templateMetadata.getMetadataLength()),
                InitializeUtil.toString(templateMetadata.getDictCode()),
                templateMetadata.getIsList().toString(), templateMetadata.getIsEdit().toString(),
                templateMetadata.getIsSearch().toString(), templateMetadata.getIsRepeat().toString()
                    ,InitializeUtil.toString(templateMetadata.getMetadataDefaultValue())))
            .collect(Collectors.toList());
        return collect;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R initialArchiveTypeTableField(Long templateId, Long tenantId) {
        ExcelReader excel = null;
        try {
            final InputStream inputStream = getDefaultTemplateStream(templateId);
            if (ObjectUtil.isNull(inputStream)) {
                return new R<>().fail("", "获取初始化文件异常");
            }
            excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.ARCHIVE_TYPE_TABLE_FIELD_TEMPLATE,
                true);
            final List<List<Object>> read = excel.read();
            final List<TemplateMetadata> templateMetadata = convertExcelData(read, tenantId);
            boolean batch = Boolean.FALSE;
            if (CollUtil.isNotEmpty(templateMetadata)) {
                batch = this.saveBatch(templateMetadata);
            }
            return batch ? new R("", "初始化档案类型表字段信息成功") : new R().fail(null, "初始化档案类型表字段信息失败！！");
        } finally {
            IoUtil.close(excel);
        }
    }

    /**
     * 处理Excel 数据
     *
     * @param read
     * @param tenantId
     * @return
     */
    private List<TemplateMetadata> convertExcelData(List<List<Object>> read, Long tenantId) {
        //获取模板信息
        final List<TemplateType> templateTypes = templateTypeService
            .list(Wrappers.<TemplateType> lambdaQuery().eq(TemplateType::getTenantId, tenantId));
        //处理模板信息
        final Map<String, Long> templateTypeMap = templateTypes.stream()
            .collect(Collectors.toMap(TemplateType::getName, TemplateType::getId));
        //获取模板表信息
        final List<TemplateTable> templateTables = templateTableService
            .list(Wrappers.<TemplateTable> lambdaQuery().eq(TemplateTable::getTenantId, tenantId));
        final List<TemplateMetadata> templateMetadataList = new ArrayList<>();
        for (int i = 1, length = read.size(); i < length; i++) {
            //所属模板名称	表模板名称	中文名称	英文名称	系统字段[0]/业务字段[1] 标签字段	数据类型	字段长度	数据字典code	是否列表显示	是否参与编辑	是否参与查询	是否重复字段 默认值
            //模板名称
            final String templateName = StrUtil.toString(read.get(i).get(0));
            //表模板名称
            final String tableName = StrUtil.toString(read.get(i).get(1));
            final TemplateTable templateTable1 = templateTables.stream()
                .filter(templateTable -> templateTable.getTemplateTypeId().equals(templateTypeMap.get(templateName))
                    && templateTable.getName().equals(tableName))
                .findAny().orElseGet(()->new TemplateTable());
            final TemplateMetadata templateMetadata = TemplateMetadata.builder().templateTableId(templateTable1.getId())
                .metadataChinese(InitializeUtil.toString(read.get(i).get(2)))
                .metadataEnglish(InitializeUtil.toString(read.get(i).get(3)))
                .metadataSys(Integer.valueOf(StrUtil.toString(read.get(i).get(4))))
                .tagEnglish(InitializeUtil.toString(read.get(i).get(5)))
                .metadataType(InitializeUtil.toString(read.get(i).get(6)))
                .metadataLength(checkDataTurnInt(read.get(i).get(7)))
                .dictCode(InitializeUtil.toString(read.get(i).get(8)))
                .isList(checkDataTurnInt(read.get(i).get(9))).isEdit(checkDataTurnInt(read.get(i).get(10)))
                .isSearch(checkDataTurnInt(read.get(i).get(11))).isRepeat(checkDataTurnInt(read.get(i).get(12)))
                    .metadataDefaultValue(InitializeUtil.checkListVal(read.get(i),13))
                .build();
            templateMetadataList.add(templateMetadata);
        }
        return templateMetadataList;
    }

    /**
     * 校验 Object 并返回Integer
     *
     * @param obj
     * @return
     */
    private Integer checkDataTurnInt(Object obj) {
        return StrUtil.isNotBlank(StrUtil.toString(obj)) && !StrUtil.NULL.equals(StrUtil.toString(obj))
            ? Integer.valueOf(StrUtil.toString(obj))
            : 0;
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
        final byte[] bytes = (byte[]) tenantTemplate.getTemplateContent();
        final InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }

}
