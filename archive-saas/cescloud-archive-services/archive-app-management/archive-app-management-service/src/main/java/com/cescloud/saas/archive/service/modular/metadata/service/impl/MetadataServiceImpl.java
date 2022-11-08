
package com.cescloud.saas.archive.service.modular.metadata.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.api.modular.archivetype.constant.SystemMetadataDefaultValueEnum;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateMetadata;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.datasource.dto.DynamicArchiveDTO;
import com.cescloud.saas.archive.api.modular.datasource.entity.DataSourceEntity;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveService;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataDTO;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.*;
import com.cescloud.saas.archive.common.util.CesBeanUtil;
import com.cescloud.saas.archive.common.util.DataSourceUtil;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigRuleService;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.hanlp.util.HanLPUtil;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.tableoperation.service.DataBaseSqlService;
import com.cescloud.saas.archive.service.modular.metadata.cachesupport.annotation.MetadataReload;
import com.cescloud.saas.archive.service.modular.metadata.mapper.MetadataMapper;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataBaseService;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 档案元数据
 *
 * @author liudong1
 * @date 2019-03-28 09:42:53
 */
@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "metadata")
public class MetadataServiceImpl extends ServiceImpl<MetadataMapper, Metadata> implements MetadataService {

    @Autowired
    private ArchiveTableService tableService;
    @Autowired
    private MetadataBaseService metadataBaseService;
    @Autowired
    private MetadataService metadataService;
    @Autowired
    private ArchiveConfigRuleService archiveConfigRuleService;
    @Autowired
    private DictItemService dictItemService;
    @Autowired
    private ArchiveTableService archiveTableService;
    @Autowired
    private TemplateMetadataService templateMetadataService;
    @Autowired
    private DataBaseSqlService<Metadata> dataBaseSqlService;
    @Autowired
    private RemoteTenantTemplateService remoteTenantTemplateService;
    @Autowired
    private RemoteArchiveService remoteArchiveService;

    @Override
    public IPage<Metadata> getPage(Page page, String storageLocate, String keyword) {
        LambdaQueryWrapper<Metadata> queryWrapper = Wrappers.<Metadata>query().lambda();
        queryWrapper.eq(Metadata::getStorageLocate, storageLocate);
        queryWrapper.eq(Metadata::getMetadataClass, BoolEnum.YES.getCode());
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper
                    .and(wrapper -> wrapper.like(Metadata::getMetadataChinese, StrUtil.trim(keyword)).or()
		                    .like(Metadata::getRemark,StrUtil.trim(keyword)));
        }
        return metadataService.page(page, queryWrapper);
    }

    @Override
	@Cacheable(key = "'archive-app-management:metadata-service:' + #id", unless = "#result == null")
    public Metadata getMetadataById(Long id) {
        return this.getById(id);
    }

    @Override
	@MetadataReload
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"archive-list", "archive-edit", "archive-sort", "metadata"}, allEntries = true)
    public boolean removeByWrapper(Wrapper<Metadata> queryWrapper) {
        return this.remove(queryWrapper);
    }

	@Override
	public List<Metadata> listAllByStorageLocate(String storageLocate) {
		List<Metadata> metadataList = this.list(Wrappers.<Metadata>query().lambda()
				.eq(Metadata::getStorageLocate, storageLocate));
		return metadataList;
	}


    @Override
    public List<Metadata> listAllByTableId(Long tableId) {
        List<Metadata> metadataList = this.list(Wrappers.<Metadata>query().lambda()
                .eq(Metadata::getTableId, tableId));
        return metadataList;
    }

    @Override
    public List<Map<String, String>> getEditFormSelectOption(String storageLocate,String typeCode, String metadataEnglish) throws ArchiveBusinessException {
        Metadata metadata = this.getByStorageLocateAndMetadataEnglish(storageLocate, metadataEnglish);
        String dictCode = metadata.getDictCode();
        if(StrUtil.isNotEmpty(dictCode) && DictEnum.BGQX.getValue().equals(dictCode)){
            List<DictItem> dictItemList = dictItemService.getItemListByDictCodeRel(dictCode,typeCode);
            List<Map<String, String>> options = dictItemList.stream().map(dictItem -> {
                Map<String, String> map = new HashMap<>();
                map.put("label", dictItem.getItemLabel());
                map.put("value", dictItem.getItemCode());
                return map;
            }).collect(Collectors.toList());
            return options;
        }else if (StrUtil.isNotEmpty(dictCode)) {
            List<DictItem> dictItemList = dictItemService.getItemListByDictCode(dictCode);
            List<Map<String, String>> options = dictItemList.stream().map(dictItem -> {
                Map<String, String> map = new HashMap<>();
                map.put("label", dictItem.getItemLabel());
                map.put("value", dictItem.getItemCode());
                return map;
            }).collect(Collectors.toList());
            return options;
        }
        return CollectionUtil.<Map<String, String>>newArrayList();
    }

    @Override
	@MetadataReload
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"archive-list", "archive-edit", "archive-sort", "metadata"}, allEntries = true)
    public Metadata saveMetadata(Metadata metadata) throws ArchiveBusinessException {
        ArchiveTable table = tableService.getTableByStorageLocate(metadata.getStorageLocate());
        metadata.setTableId(table.getId());
        checkUnique(metadata);
        checkTag(metadata);
        setLength(metadata);
        this.save(metadata);
        log.debug("在物理表[{}]中添加字段[{}]", metadata.getStorageLocate(), metadata.getMetadataEnglish());
        addColumn(table, metadata);
        return metadata;
    }

    /**
     * 修改元数据时候不修改英文字段
     *
     * @param metadata
     * @return
     * @throws ArchiveBusinessException
     */
    @Override
	@MetadataReload
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"archive-list", "archive-edit", "archive-sort", "metadata"}, allEntries = true)
    public Metadata updateMetadata(Metadata metadata) throws ArchiveBusinessException {
        Metadata oldMetadata = this.getById(metadata.getId());
        checkUnique(metadata);
        checkTag(metadata);
        checkType(oldMetadata, metadata);
        checkName(oldMetadata, metadata);
        //长度只能边长，不能缩短
        checkLength(oldMetadata, metadata);
        setLength(metadata);
        //如果是系统字段 只能修改中文和长度
        if (oldMetadata.getMetadataClass().equals(BoolEnum.NO.getCode())) {
            oldMetadata.setMetadataChinese(metadata.getMetadataChinese());
            oldMetadata.setMetadataLength(metadata.getMetadataLength());
            oldMetadata.setTagEnglish(metadata.getTagEnglish());
            oldMetadata.setDictCode(metadata.getDictCode());
        } else {
            //BeanUtil.copyProperties(metadata, oldMetadata);
            CesBeanUtil.copyProperties(metadata, oldMetadata);
        }
        this.updateById(oldMetadata);

        log.debug("在物理表[{}]中修改字段[{}]", metadata.getStorageLocate(), metadata.getMetadataEnglish());
        ArchiveTable archiveTable = tableService.getTableByStorageLocate(metadata.getStorageLocate());
        modifyColumn(archiveTable, oldMetadata);
        return oldMetadata;
    }

    /**
     * 设置长度
     * 设置日期长度和时间 为null
     *
     * @param metadata
     */
    private void setLength(Metadata metadata) {
        if (metadata.getMetadataType().equals(MetadataTypeEnum.DATE.getValue())
                || metadata.getMetadataType().equals(MetadataTypeEnum.DATETIME.getValue())) {
            metadata.setMetadataLength(null);
        }
    }

    private void checkType(Metadata oldMetadata, Metadata metadata) throws ArchiveBusinessException {
        if (!oldMetadata.getMetadataType().equals(metadata.getMetadataType())) {
            throw new ArchiveBusinessException("字段类型不能修改！");
        }
    }

    private void checkName(Metadata oldMetadata, Metadata metadata) throws ArchiveBusinessException {
        if (!oldMetadata.getMetadataEnglish().equals(metadata.getMetadataEnglish())) {
            throw new ArchiveBusinessException("字段英文编码不能修改！");
        }
    }

    private void checkLength(Metadata oldMetadata, Metadata metadata) throws ArchiveBusinessException {
        if (!metadata.getMetadataType().equals(MetadataTypeEnum.DATE.getValue())
                && !metadata.getMetadataType().equals(MetadataTypeEnum.DATETIME.getValue())) {
            if (metadata.getMetadataLength() < oldMetadata.getMetadataLength()) {
                throw new ArchiveBusinessException("字段长度不能缩小");
            }
        }
    }


    /**
     * 删除元数据字段
     * 判断该字段是否被规则配置
     *
     * @param id
     * @return
     * @throws ArchiveBusinessException
     */
    @Override
	@MetadataReload
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"archive-list", "archive-edit", "archive-sort", "metadata"}, allEntries = true)
    public Metadata removeMetadata(Long id) throws ArchiveBusinessException {
        Metadata metadata = getById(id);
        if (metadata.getMetadataClass().equals(BoolEnum.NO.getCode())) {
            throw new ArchiveBusinessException("该元数据为系统字段，不能删除！");
        }

        log.debug("删除元数据前，判断是否被绑定规则");
        Boolean checkUsed = archiveConfigRuleService.checkUsed(id, metadata.getStorageLocate());
        if (checkUsed) {
            throw new ArchiveBusinessException("该元数据已经被使用，不能删除！");
        }
        this.removeById(id);

        log.debug("在物理表[{}]中删除字段[{}]", metadata.getStorageLocate(), metadata.getMetadataEnglish());
        ArchiveTable archiveTable = tableService.getTableByStorageLocate(metadata.getStorageLocate());
        dropColumn(archiveTable, metadata);
        return metadata;
    }

    private Metadata setMetadataEnglish(Metadata metadata) throws ArchiveBusinessException {
        log.debug("根据拼音首字母设置english");
        //拼音首字母编码
        String metadataEnglish = HanLPUtil.toPinyinFirstCharString(metadata.getMetadataChinese());
        if (StrUtil.isBlank(metadataEnglish)) {
            throw new ArchiveBusinessException("字段英文值生成失败，请检查字段名称！");
        }
        Integer maxEnglishNo = baseMapper.getMaxEnglishNoByHidden(metadataEnglish, metadata.getTableId());
        //没有重复的code
        if (maxEnglishNo == null) {
            metadata.setMetadataEnglish(metadataEnglish);
        } else {
            metadata.setMetadataEnglish(metadataEnglish + ArchiveConstants.SYMBOL.UNDER_LINE + String.valueOf(maxEnglishNo + 1));
        }

        return metadata;
    }

    /**
     * 校验唯一
     *
     * @param metadata
     * @throws ArchiveBusinessException
     */
    private void checkUnique(Metadata metadata) throws ArchiveBusinessException {
        checkUniqueChinese(metadata);
        checkUniqueEnglish(metadata);
    }

    private void checkUniqueChinese(Metadata metadata) throws ArchiveBusinessException {
        Map<String, Object> params = new HashMap<>();
        params.put("storage_locate", metadata.getStorageLocate());
        params.put("metadata_chinese", metadata.getMetadataChinese());
        R r = metadataBaseService.checkUnique("apma_metadata", params, "字段名称");
        if (r.getCode() == CommonConstants.FAIL) {
            if (ObjectUtil.isNull(metadata.getId())) {
                throw new ArchiveBusinessException(r.getMsg());
            } else if (metadata.getId() != Long.parseLong(r.getData().toString())) {
                throw new ArchiveBusinessException(r.getMsg());
            }
        }
    }

    private void checkUniqueEnglish(Metadata metadata) throws ArchiveBusinessException {
        Map<String, Object> params = new HashMap<>();
        params.put("storage_locate", metadata.getStorageLocate());
        params.put("metadata_english", metadata.getMetadataEnglish());
        R r = metadataBaseService.checkUnique("apma_metadata", params, "字段编码");
        if (r.getCode() == CommonConstants.FAIL) {
            if (ObjectUtil.isNull(metadata.getId())) {
                throw new ArchiveBusinessException(r.getMsg());
            } else if (metadata.getId() != Long.parseLong(r.getData().toString())) {
                throw new ArchiveBusinessException(r.getMsg());
            }
        }
    }

    private void checkTag(Metadata metadata) throws ArchiveBusinessException {
        if (StrUtil.isNotBlank(metadata.getTagEnglish())) {
            LambdaQueryWrapper<Metadata> queryWrapper = Wrappers.<Metadata>query().lambda()
                    .eq(Metadata::getStorageLocate, metadata.getStorageLocate())
                    .eq(Metadata::getTagEnglish, metadata.getTagEnglish());

            List<Metadata> list = this.list(queryWrapper);
            if (CollectionUtil.isNotEmpty(list)) {
                Metadata tagMetadata = list.get(0);
                //新增
                if (null == metadata.getId()) {
                    if (null != tagMetadata) {
                        throw new ArchiveBusinessException("该标签已经被绑定，不能重复绑定");
                    }
                } else { //修改
                    if (!metadata.getId().equals(tagMetadata.getId())) {
                        throw new ArchiveBusinessException("该标签已经被绑定，不能重复绑定");
                    }
                }
            }
        }
    }

    /**
     * @return com.cescloud.saas.archive.api.modular.metadata.entity.Metadata
     * @Author xieanzhu
     * @Description //根据表名 英文字段查询元数据
     * @Date 17:15 2019/5/22
     * @Param [storageLoacte, metadataEnglish]
     **/
    @Override
    public Metadata getByStorageLocateAndMetadataEnglish(String storageLoacte, String metadataEnglish) throws ArchiveBusinessException {
    	if (log.isDebugEnabled()){
		    log.debug("根据表名{}，和字段{}查询元数据",storageLoacte, metadataEnglish);
	    }
        List<Metadata> list = this.list(Wrappers.<Metadata>query().lambda()
                .eq(Metadata::getStorageLocate, storageLoacte)
                .eq(Metadata::getMetadataEnglish, metadataEnglish));
        if (CollectionUtil.isEmpty(list)) {
            throw new ArchiveBusinessException("找不到表名为{" + storageLoacte + "}，字段为{" + metadataEnglish + "}的元数据信息！");
        }
        return list.get(0);
    }

    @Override
    public Boolean checkBindedTag(String tagEnglish) {
        int count = this.count(Wrappers.<Metadata>query().lambda()
                .eq(Metadata::getTagEnglish, tagEnglish));
        return count > 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    @Cacheable(
            key = "'archive-app-management:metadata-dto:'+#storageLocate",
            unless = "#result == null || #result.size() == 0"
    )
    public List<MetadataDTO> getMetadataDTOList(String storageLocate) {
        return baseMapper.getMetadataDTOList(storageLocate);
    }

    @Override
    @CacheEvict(cacheNames = {"metadata"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void autoBindingTag(MetadataTag metadataTag) {
        //查询所有表名
        List<ArchiveTable> archiveTableList = tableService.list();
        List<Long> tableIdList = archiveTableList.stream().map(archiveTable -> archiveTable.getId()).collect(Collectors.toList());

        //更改tagId
        Metadata cleanMetadata = new Metadata();
        cleanMetadata.setTagEnglish("");
        //先删除之前绑定的标签
        log.debug("删除tagId为[{}]的绑定信息", metadataTag.getId());
        this.update(cleanMetadata, Wrappers.<Metadata>query().lambda()
                .eq(Metadata::getTagEnglish, metadataTag.getTagEnglish()));

        //更改tagId
        Metadata metadata = new Metadata();
        metadata.setTagEnglish(metadataTag.getTagEnglish());
        //重新绑定
        log.debug("重新绑定英文名为[{}]的tagId为[{}]", metadataTag.getTagEnglish(), metadataTag.getId());
        this.update(metadata, Wrappers.<Metadata>query().lambda()
                .in(Metadata::getTableId, tableIdList));

    }

    /**
     * 动态表里新增字段
     *
     * @param archiveTable 动态表名
     * @param metadata     元字段实体
     */
    private void addColumn(ArchiveTable archiveTable, Metadata metadata) throws ArchiveBusinessException {
        final List<String> addColumnSqlList = CollectionUtil.newArrayList();

        DataSourceEntity dataSourceEntity = tableService.getDataSourceByTenantId(archiveTable.getTenantId());
        String sql = dataBaseSqlService.getAddColumnSql(archiveTable.getStorageLocate(), metadata);
        addColumnSqlList.add(sql);

        boolean result = DataSourceUtil.executeDdls(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(), dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), addColumnSqlList);
        if (!result) {
            log.error("新增字段失败");
            throw new ArchiveBusinessException("新增字段失败");
        }
    }

    /**
     * 动态表里删除字段
     *
     * @param archiveTable 档案表
     * @param metadata     元字段实体
     */
    private void dropColumn(ArchiveTable archiveTable, Metadata metadata) throws ArchiveBusinessException {
        final List<String> dropColumnSqlList = CollectionUtil.newArrayList();

        DataSourceEntity dataSourceEntity = tableService.getDataSourceByTenantId(archiveTable.getTenantId());
        String sql = dataBaseSqlService.getDropColumnSql(archiveTable.getStorageLocate(), metadata);
        dropColumnSqlList.add(sql);
        boolean result = DataSourceUtil.executeDdls(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(), dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), dropColumnSqlList);
        if (!result) {
            log.error("删除字段失败");
            throw new ArchiveBusinessException("删除字段失败");
        }
    }

    /**
     * 动态表里修改字段
     *
     * @param archiveTable 动态表名
     * @param metadata     元字段实体
     */
    private void modifyColumn(ArchiveTable archiveTable, Metadata metadata) throws ArchiveBusinessException {
        final List<String> modifyColumnSqlList = CollectionUtil.newArrayList();

        DataSourceEntity dataSourceEntity = tableService.getDataSourceByTenantId(archiveTable.getTenantId());
	    //oracle，dm 修改 字段时，不需要拼接 是否允许为空 ，会保持原 是否允许为空 状态
	    //mysql，sqlServer 修改 时需要拼接原 是否允许为空 状态，否则会默认修改为 允许为空
        String sql = dataBaseSqlService.getModifyColumnSql(archiveTable.getStorageLocate(), metadata);
	    log.info("动态表修改字段：sql->[{}]",sql);
        modifyColumnSqlList.add(sql);
        boolean result = DataSourceUtil.executeDdls(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(), dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), modifyColumnSqlList);
        if (!result) {
            log.error("修改字段失败");
            throw new ArchiveBusinessException("修改字段失败");
        }
    }

	/**
	 * 获取业务的字段
	 *
	 * @param storageLocate 档案存储表名
	 * @return
	 */
	@Override
	@Cacheable(key = "'archive-app-management:metadata-service:' + #storageLocate",
			unless = "#result == null || #result.size() == 0"
	)
	public List<Metadata> listByStorageLocate(String storageLocate) {
		List<Metadata> metadataList = this.list(Wrappers.<Metadata>query().lambda()
				.eq(Metadata::getStorageLocate, storageLocate)
				.eq(Metadata::getMetadataClass, BoolEnum.YES.getCode()));
		return metadataList;
	}

	/**
	 * 获取业务的字段
	 *
	 * @param storageLocate 档案存储表名
	 * @return
	 */
	@Override
	public List<Metadata> allByStorageLocate(String storageLocate) {
		List<Metadata> metadataList = this.list(Wrappers.<Metadata>query().lambda()
				.eq(Metadata::getStorageLocate, storageLocate));
		return metadataList;
	}

    @Override
	@Cacheable(key = "'archive-app-management:metadata-service:' + #typeCode + ':' + #templateTableId", unless = "#result == null || #result.size() == 0")
    public List<Metadata> listByTypeCodeAndTemplateTableId(String typeCode, Long templateTableId) {
        ArchiveTable table = tableService.getTableByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
        return listByStorageLocate(table.getStorageLocate());
    }

    @Override
	@MetadataReload
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"archive-list", "archive-edit", "archive-sort", "metadata"}, allEntries = true)
    public boolean saveBatchForMysql(List<Metadata> metadatas) {
        Integer result = getBaseMapper().saveBatch(metadatas);
        return SqlHelper.retBool(result);
    }

    /**
     * @param archiveTable
     * @param templateTable
     * @throws ArchiveBusinessException
     */
    @Override
	@MetadataReload
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"archive-list", "archive-edit", "archive-sort", "metadata"}, allEntries = true)
    public void insertIntoMetadataFromTemplate(ArchiveTable archiveTable, TemplateTable templateTable) throws ArchiveBusinessException {
        List<Metadata> templateMetadataList = getTemplateMetadataList(archiveTable, templateTable);
        List<Metadata> sysMetadataList = getSysMetadataList(archiveTable);
        sysMetadataList = sysMetadataList.stream().filter(sysMetadata -> {
            return !templateMetadataList.stream().anyMatch(templateMetadata ->
                    templateMetadata.getMetadataEnglish().equals(sysMetadata.getMetadataEnglish()));
        }).collect(Collectors.toList());
        sysMetadataList = Stream.concat(templateMetadataList.stream(), sysMetadataList.stream()).collect(Collectors.toList());

        sysMetadataList = sysMetadataList.stream().peek(metadata -> {
        	metadata.setId(null);
            if (SystemMetadataDefaultValueEnum.ARCHIVE_LAYER.toString()
                    .equalsIgnoreCase(metadata.getMetadataEnglish())) {
                metadata.setMetadataDefaultValue(archiveTable.getArchiveLayer());
            } else if (SystemMetadataDefaultValueEnum.TYPE_CODE.toString()
                    .equalsIgnoreCase(metadata.getMetadataEnglish())) {
                metadata.setMetadataDefaultValue(archiveTable.getArchiveTypeCode());
            } else if (SystemMetadataDefaultValueEnum.IS_DELETE.toString()
                    .equalsIgnoreCase(metadata.getMetadataEnglish())) {
                metadata.setMetadataDefaultValue(BoolEnum.NO.getCode().toString());
            }
        }).collect(Collectors.toList());
        this.saveBatch(sysMetadataList);
    }

    private List<Metadata> getTemplateMetadataList(ArchiveTable archiveTable, TemplateTable templateTable) throws ArchiveBusinessException {
        List<TemplateMetadata> templateMetadataList = templateMetadataService.getByTemplateTableId(templateTable.getId());
        if (CollectionUtil.isEmpty(templateMetadataList)) {
            log.error("表模板[{}]的字段为空", templateTable.getName());
            throw new ArchiveBusinessException("表模板[" + templateTable.getName() + "]的字段为空");
        }
        //模板中字段
        List<Metadata> metadataList = templateMetadataList.stream().map(templateMetadata -> {
            Metadata metadata = new Metadata();
            BeanUtils.copyProperties(templateMetadata, metadata);
            metadata.setTableId(archiveTable.getId());
            metadata.setStorageLocate(archiveTable.getStorageLocate());

            return metadata;
        }).collect(Collectors.toList());

        return metadataList;
    }

    private List<Metadata> getSysMetadataList(ArchiveTable archiveTable) {
        List<MetadataBase> sysMetadata = metadataBaseService.getSysMetadata();
        //系统字段
        List<Metadata> metadataList = sysMetadata.stream().map(metadataBase -> {
            Metadata metadata = new Metadata();
            BeanUtils.copyProperties(metadataBase, metadata);
            metadata.setTableId(archiveTable.getId());
            metadata.setStorageLocate(archiveTable.getStorageLocate());

            return metadata;
        }).collect(Collectors.toList());
        return metadataList;
    }

    @Override
    public List<Metadata> getBakMetadataList(ArchiveTable archiveTable) {
        List<MetadataBase> bakMetadata = metadataBaseService.getBakMetadata();
        //系统字段
        List<Metadata> metadataList = bakMetadata.stream().map(metadataBase -> {
            Metadata metadata = new Metadata();
            BeanUtils.copyProperties(metadataBase, metadata);
            metadata.setTableId(archiveTable.getId());
            metadata.setStorageLocate(archiveTable.getStorageLocate());

            return metadata;
        }).collect(Collectors.toList());
        return metadataList;
    }

    @Override
    public List<ArrayList<String>> getFieldManagementInfo(Long tenantId) {
        //获取门类信息
        final List<ArchiveTable> archiveTables = tableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
        //处理门类信息
        final Map<String, ArchiveTable> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageLocate, archiveTable -> archiveTable));
        //获取字段信息
        final List<Metadata> metadataList = this.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
        //门类名称	中文名称	英文名称	系统字段[0]/业务字段[1] 标签字段	数据类型	字段长度	数据字典code 是否为空 默认值	是否列表显示	是否参与编辑	是否参与查询
        List<ArrayList<String>> collect = metadataList.stream().map(metadata -> CollectionUtil.newArrayList(archiveTableMap.get(metadata.getStorageLocate()).getStorageName(),
                metadata.getMetadataChinese(), metadata.getMetadataEnglish(),
                metadata.getMetadataClass().toString(),
                InitializeUtil.toString(metadata.getTagEnglish()),
                metadata.getMetadataType(),
                InitializeUtil.toString(metadata.getMetadataLength()),
                InitializeUtil.toString((metadata.getDictCode())),
                metadata.getMetadataNull().toString(),
                InitializeUtil.toString(metadata.getMetadataDefaultValue()),
                metadata.getIsList().toString(),
                metadata.getIsEdit().toString(),
                metadata.getIsSearch().toString()
        )).collect(Collectors.toList());
        return collect;
    }

    @Override
	@MetadataReload(value = "#tenantId")
    @Transactional(rollbackFor = Exception.class)
    public R initializeMetadata(Long templateId, Long tenantId) {
        ExcelReader excel = null;
        try {
            InputStream inputStream = getDefaultTemplateStream(templateId);
            if (ObjectUtil.isNull(inputStream)) {
                return new R<>().fail("", "获取初始化文件异常");
            }
            excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.ARCHIVE_TYPE_FIELD_NAME, true);
            List<List<Object>> read = excel.read();
            //获取门类信息
            final List<ArchiveTable> archiveTables = tableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
            //处理门类信息
            final Map<String, ArchiveTable> archiveTableMap = archiveTables.stream().collect(Collectors.toMap(ArchiveTable::getStorageName, archiveTable -> archiveTable));
            List<Metadata> metadataList = new ArrayList<>();
            // 行循环
            for (int i = 1, length = read.size(); i < length; i++) {
                List<Object> objectList = read.get(i);
                //门类名称 中文名称	英文名称	系统字段[0]/业务字段[1] 标签字段	数据类型	字段长度	数据字典code 是否为空 默认值	是否列表显示	是否参与编辑	是否参与查询
                //获取门类名称
                String archiveTypeName = StrUtil.toString(objectList.get(0));
                ArchiveTable archiveTable = archiveTableMap.get(archiveTypeName);
				Long tableid = ObjectUtil.isNotEmpty(archiveTable) ? archiveTable.getId() : 1;
                String StorageLocate = ObjectUtil.isNotEmpty(archiveTable) ? archiveTable.getStorageLocate() : "有问题";
                if(StorageLocate.equals("有问题")){
                	log.info("+++++++++++++++++++++++有问题name:"+archiveTypeName);
				}
                Metadata metadata = Metadata.builder().tableId(tableid).storageLocate(StorageLocate)
                        .metadataChinese(InitializeUtil.toString(objectList.get(1)))
                        .metadataEnglish(InitializeUtil.toString(objectList.get(2)))
                        .metadataClass(disposeInt(objectList.get(3)))
                        .tagEnglish(InitializeUtil.toString(objectList.get(4)))
                        .metadataType(InitializeUtil.toString(objectList.get(5)))
                        .metadataLength(disposeInt(objectList.get(6))).dictCode(InitializeUtil.toString(objectList.get(7)))
                        .metadataNull(disposeInt(objectList.get(8))).metadataDefaultValue(InitializeUtil.toString(objectList.get(9)))
                        .isList(disposeInt(objectList.get(10))).isEdit(disposeInt(objectList.get(11)))
                        .isSearch(disposeInt(objectList.get(12))).tenantId(tenantId).build();
                metadataList.add(metadata);
            }
            Boolean batch = Boolean.FALSE;
            if (CollectionUtil.isNotEmpty(metadataList)) {
                batch = this.saveBatch(metadataList);
            }
            //进行物理表创建
            try {
                tableService.createTables(archiveTables, tenantId);
            } catch (ArchiveBusinessException e) {
                log.error("生成物理表失败", e);
            }
            //5、动态数据源重新初始化
            //messageSendService.reloadDataSource();
            return batch ? new R("", " 初始化门类字段信息成功") : new R().fail(null, " 初始化门类字段信息失败！！");
        } finally {
            IoUtil.close(excel);
        }
    }

    private Integer disposeInt(Object object) {
        String string = StrUtil.toString(object);
        return StrUtil.isNotBlank(string) && !"null".equals(string) ? Integer.valueOf(string) : null;
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
    public R verifyFormField(String typeCode, Long templateTableId, String fieldName, String value,Long id) {
        String storageLocate = archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(typeCode, templateTableId);
        Metadata metadata = this.getOne(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getStorageLocate, storageLocate).eq(Metadata::getMetadataEnglish, fieldName));
        StringBuilder where = new StringBuilder();
        where.append(metadata.getMetadataEnglish()).append(" = ");
        String metadataType = metadata.getMetadataType();
        if (MetadataTypeEnum.VARCHAR.getValue().equals(metadataType)) {
            where.append(" '").append(value).append("' ");
        } else {
            where.append(value);
        }
        if(ObjectUtil.isNotNull(id)){
            where.append(" and ").append(FieldConstants.ID).append(" != ").append(id);
        }
        where.append(" and ").append(FieldConstants.IS_DELETE).append(" = ").append(DeleteEnum.BEDEFAULT.getCode());
        DynamicArchiveDTO dynamicArchiveDTO = new DynamicArchiveDTO();
        dynamicArchiveDTO.setTableName(storageLocate);
        dynamicArchiveDTO.setWhere(where.toString());
        R<Integer> countByCondition = remoteArchiveService.getCountByCondition(dynamicArchiveDTO, Boolean.FALSE);
        Integer count = countByCondition.getData();
        if (count > 0) {
            return new R().fail(false, metadata.getMetadataChinese() + "字段值不允许重复");
        }
        return new R(true, "该字段值可添加");
    }

	@Override
	public IPage<Metadata> getAllTenantMetadatas(long current,long size) {
		return this.page(new Page<Metadata>(current, size),
				Wrappers.<Metadata>lambdaQuery()
						.select(Metadata::getStorageLocate,Metadata::getMetadataEnglish,Metadata::getMetadataType,Metadata::getMetadataLength)
						.orderByAsc(Metadata::getTenantId, Metadata::getId));
	}

	@Override
	public IPage<Metadata> getTenantMetadatas(long current, long size, Long tenantId) {
		return this.page(new Page<Metadata>(current,size),
				Wrappers.<Metadata>lambdaQuery().select(Metadata::getStorageLocate,Metadata::getMetadataEnglish,Metadata::getMetadataType,Metadata::getMetadataLength)
						.eq(Metadata::getTenantId,tenantId)
						.orderByAsc(Metadata::getId));
	}

	@Override
	@Cacheable(
			key = "'archive-app-management:metadata-service:isrepeat' + #storageLocate",
			unless = "#result == null || #result.size() == 0"
	)
	public List<Metadata> getRepeatMetadatasByStorageLocate(String storageLocate) {
		return metadataService.list(Wrappers.<Metadata>lambdaQuery()
				.eq(Metadata::getStorageLocate, storageLocate)
				.eq(Metadata::getIsRepeat, BoolEnum.YES.getCode()));
	}

}
