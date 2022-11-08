
package com.cescloud.saas.archive.service.modular.archivetype.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetype.dto.ArchiveTableSearchDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.ArchiveTypeTreeNode;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.datasource.entity.DataSourceDetail;
import com.cescloud.saas.archive.api.modular.datasource.entity.DataSourceEntity;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteArchiveService;
import com.cescloud.saas.archive.api.modular.datasource.feign.RemoteDataSourceService;
import com.cescloud.saas.archive.api.modular.indexmaintenance.builder.IndexTaskBuilder;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataDTO;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataListDTO;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataTagEnglishDTO;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.tenant.dto.ArchiveTableButtonDTO;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantArchiveButtonService;
import com.cescloud.saas.archive.common.constants.ArchiveConstants;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.FieldConstants;
import com.cescloud.saas.archive.common.constants.NodeTypeEnum;
import com.cescloud.saas.archive.common.util.ArchiveTableUtil;
import com.cescloud.saas.archive.common.util.DataSourceUtil;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigRuleService;
import com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.ArchiveTableMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.archivetype.service.LayerService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.cache.RedisUtil;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.cescloud.saas.archive.service.modular.common.tableoperation.service.DataBaseSqlService;
import com.cescloud.saas.archive.service.modular.indexmaintenance.stream.producer.IndexTaskProducer;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 档案表
 *
 * @author liudong1
 * @date 2019-03-27 12:48:29
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "archive-table")
@RequiredArgsConstructor
public class ArchiveTableServiceImpl extends ServiceImpl<ArchiveTableMapper, ArchiveTable>
        implements ArchiveTableService {

    /**
     * 不包括的层次
     */
    public static final ArchiveLayerEnum[] EXCLUDE_LAYER = new ArchiveLayerEnum[]{
            ArchiveLayerEnum.DOCUMENT, ArchiveLayerEnum.INFO, ArchiveLayerEnum.SINGLE};

    @Autowired
    private MetadataService metadataService;
    @Autowired
    private ArchiveTreeService archiveTreeService;
    @Autowired
    private DataBaseSqlService<Metadata> dataBaseSqlService;
    @Autowired
    private ArchiveConfigRuleService archiveConfigRuleService;
    @Autowired
    private IndexTaskProducer indexTaskProducer;
    @Autowired
    private LayerService layerService;
    @Autowired
    private RedisUtil redisUtil;
    private final RemoteArchiveService remoteArchiveService;
    private final RemoteDataSourceService remoteDataSourceService;
	private final RemoteTenantArchiveButtonService remoteTenantArchiveButtonService;

	/**
	 * search必须的系统字段
	 */
	private final static String[] INDEX_NEED_SYS_FILED={FieldConstants.ID,FieldConstants.IS_DELETE,FieldConstants.FONDS_CODE,
			FieldConstants.STATUS,FieldConstants.IS_BORROWED,FieldConstants.ARCHIVE_TYPE_CODE,FieldConstants.ARCHIVE_LAYER,
			FieldConstants.ITEM_COUNT,FieldConstants.YEAR_CODE,FieldConstants.TEMPLATE_TABLE_ID};


    /**
     * 分库分表数，暂时为1
     **/
    private final Integer SHARDING_NUMBER = 1;

    /**
     * 拼接物理表名
     *
     * @param archiveType
     * @param templateTable
     * @return
     */
    @Override
    public String getStorageLocate(ArchiveType archiveType, TemplateTable templateTable) {
        return ArchiveTableUtil.getArchiveTableName(archiveType.getTenantId(), archiveType.getClassType(),
                archiveType.getTypeCode(), templateTable.getId(), templateTable.getLayerCode());
    }

    /**
     * 根据档案门类获取档案表
     *
     * @param typeCode
     * @return
     */
    @Override
	@Cacheable(
			key = "'archive-app-management:archive-table-service:typeCode:' + #typeCode",
			unless = "#result == null || #result.size() == 0"
	)
    public List<ArchiveTable> getTableListByTypeCode(String typeCode) {
        final List<ArchiveTable> tableList = this.list(Wrappers.<ArchiveTable>query().lambda()
                .eq(ArchiveTable::getArchiveTypeCode, typeCode)
                .orderByAsc(ArchiveTable::getSortNo));
        return tableList;
    }

    /**
     * 根据档案门类获取档案树结构表
     * 返回ArchiveType 纯粹是为了前台保持一致
     *
     * @param type
     * @return
     */
    @Override
    public List<ArchiveTypeTreeNode> getTypeTreeListByType(ArchiveType type) {
        final List<ArchiveTable> tableList = getTableListByTypeCode(type.getTypeCode());
        final List<ArchiveTypeTreeNode> treeList = tableList.stream()
                .map(archiveTable -> {
                    final ArchiveTypeTreeNode treeNode = new ArchiveTypeTreeNode();
                    treeNode.setId(archiveTable.getId());
					treeNode.setPk(treeNode.getId().toString());
                    treeNode.setTypeName(archiveTable.getStorageName());
                    treeNode.setParentId(type.getId());
                    treeNode.setNodeType(NodeTypeEnum.TABLE.getValue());
                    treeNode.setIsLeaf(true);
                    treeNode.setStorageLocate(archiveTable.getStorageLocate());
                    treeNode.setClassType(archiveTable.getClassType());
                    treeNode.setArchiveLayer(archiveTable.getArchiveLayer());
                    treeNode.setTemplateTableId(archiveTable.getTemplateTableId());
                    treeNode.setTypeCode(archiveTable.getArchiveTypeCode());
                    treeNode.setFondsCode(type.getFondsCode());
                    return treeNode;
                }).collect(Collectors.toList());
        return treeList;
    }

    /**
     * 根据档案门类编码删除物理表
     *
     * @param typeCode
     */
    @Override
	@CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void removeStorageLocate(String typeCode) throws ArchiveBusinessException {
        final List<ArchiveTable> tableList = this
                .list(Wrappers.<ArchiveTable>query().lambda().eq(ArchiveTable::getArchiveTypeCode, typeCode));

		for (ArchiveTable archiveTable : tableList) {
			log.info("删除物理表[{}]中元数据", archiveTable.getId());
			//删除元数据
			metadataService.removeByWrapper(Wrappers.<Metadata>query().lambda()
					.eq(Metadata::getTableId, archiveTable.getId()));
			//删除所有绑定的规则
			archiveConfigRuleService.deleteByStorageLocate(archiveTable.getStorageLocate());
			log.info("删除索引库[{}]", archiveTable.getId());
			indexTaskProducer.send(IndexTaskBuilder.createDeleteIndices(archiveTable.getStorageLocate()));
			//删除物理表和dataSourceDetail里面的记录
			log.info("删除物理表[{}]", archiveTable.getId());
			dropTable(archiveTable);
			deletTableCache(archiveTable.getStorageLocate());
		}
        log.info("删除门类编码为[{}]的档案表", typeCode);
        //删除表中数据
        this.remove(Wrappers.<ArchiveTable>query().lambda()
                .eq(ArchiveTable::getArchiveTypeCode, typeCode));
    }

    private void deletTableCache(String storageLocate) {
        String likeKey = SecurityUtils.getUser().getTenantId() + ":*:" + storageLocate;
        redisUtil.deleteKeys(likeKey, 5);
    }

    /**
     * 判断物理表是否能删除
     * 1、档案表已存在数据
     * 2、档案树关联档案类型
     *
     * @param archiveType
     * @return
     */
    @Override
    public R canDeleteArchiveTable(ArchiveType archiveType) {
        final List<ArchiveTable> tableList = getTableListByTypeCode(archiveType.getTypeCode());
        R r = null;
        final Boolean bindingArchiveType = archiveTreeService.bindingArchiveType(archiveType.getTypeCode());
        if (bindingArchiveType) {
            log.warn("档案树已绑定档案类型[{}]节点，不允许删除！", archiveType.getTypeName());
            r = R.builder()
                    .msg("档案树已绑定档案类型[" + archiveType + "]节点，不允许删除！")
                    .code(CommonConstants.FAIL)
                    .info("档案树已绑定档案类型[" + archiveType.getTypeName() + "]节点，不允许删除！")
                    .build();
            return r;
        }
        for (final ArchiveTable archiveTable : tableList) {
            //档案表已存在数据
            final Boolean flag = tableHasData(archiveTable.getStorageLocate());
            if (flag) {
                log.info("表[" + archiveTable.getStorageLocate() + "]中存在数据不允许删除");
                r = R.builder()
                        .msg("表[" + archiveTable.getStorageLocate() + "]中存在数据不允许删除")
                        .code(CommonConstants.FAIL)
                        .info("子节点[" + archiveTable.getStorageName() + "]中存在数据，不允许删除！")
                        .build();
                break;
            }
        }
        return r;
    }

    /**
     * 查询表中是否有数据
     *
     * @param tableName
     * @return
     */
    private Boolean tableHasData(String tableName) {
        //使用sharding接口
        final R<Boolean> result = remoteArchiveService.existData(tableName);
        if (result.getCode() == CommonConstants.SUCCESS) {
            return result.getData();
        }
        return Boolean.TRUE;
    }

    private void createTable(ArchiveTable archiveTable, List<String> hasCreateTableNameList)
            throws ArchiveBusinessException {
        final List<Metadata> metadataList = metadataService.listAllByTableId(archiveTable.getId());

        //建表语句
        final List<String> createSqls = CollectionUtil.newArrayList();
        //创建业务表
        hasCreateTableNameList.add(archiveTable.getStorageLocate());
        String sql = dataBaseSqlService.getCreateTableSql(archiveTable.getStorageLocate(), metadataList, false);
        createSqls.add(sql);
        List<String> indexSqls = getCreateIndexSql(archiveTable, dataBaseSqlService, false);
        createSqls.addAll(indexSqls);
        //创建BAK表(过程信息和签名签章都不需要建对应的bak表)
        if (!ArchiveLayerEnum.SIGNATRUE.getCode().equals(archiveTable.getArchiveLayer())
                && !ArchiveLayerEnum.INFO.getCode().equals(archiveTable.getArchiveLayer())) {
			final List<Metadata> bakMetadataList = metadataService.getBakMetadataList(archiveTable);
            final ArchiveTable archiveBakTable = createArchiveBakTable(archiveTable);
            hasCreateTableNameList.add(archiveBakTable.getStorageLocate());
            sql = dataBaseSqlService.getCreateTableSql(archiveBakTable.getStorageLocate(), bakMetadataList, false);
            createSqls.add(sql);
            List<String> indexBakSqls = getCreateIndexSql(archiveBakTable, dataBaseSqlService, true);
            createSqls.addAll(indexBakSqls);
        }
        //获取租户所在数据源
        final DataSourceEntity dataSourceEntity = getDataSourceByTenantId(archiveTable.getTenantId());

        final boolean result = DataSourceUtil.executeDdls(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(),
                dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), createSqls);
        if (!result) {
            log.error("动态表创建失败");
            DataSourceUtil.dropTable(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(),
                    dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), hasCreateTableNameList);
            throw new ArchiveBusinessException("动态表创建失败");
        }
    }

    private void createTableForCopy(ArchiveTable archiveTable, List<Metadata> metadatas, List<String> hasCreateTableNameList) throws ArchiveBusinessException {
		//建表语句
		final List<String> createSqls = CollectionUtil.newArrayList();
		//创建业务表
		hasCreateTableNameList.add(archiveTable.getStorageLocate());
		String sql = dataBaseSqlService.getCreateTableSql(archiveTable.getStorageLocate(), metadatas, false);
		createSqls.add(sql);
		List<String> indexSqls = getCreateIndexSql(archiveTable, dataBaseSqlService, false);
		createSqls.addAll(indexSqls);
		//创建BAK表(过程信息和签名签章都不需要建对应的bak表)
		if (!ArchiveLayerEnum.SIGNATRUE.getCode().equals(archiveTable.getArchiveLayer())
				&& !ArchiveLayerEnum.INFO.getCode().equals(archiveTable.getArchiveLayer())) {
			final List<Metadata> bakMetadataList = metadataService.getBakMetadataList(archiveTable);
			final ArchiveTable archiveBakTable = createArchiveBakTable(archiveTable);
			hasCreateTableNameList.add(archiveBakTable.getStorageLocate());
			sql = dataBaseSqlService.getCreateTableSql(archiveBakTable.getStorageLocate(), bakMetadataList, false);
			createSqls.add(sql);
			List<String> indexBakSqls = getCreateIndexSql(archiveBakTable, dataBaseSqlService, true);
			createSqls.addAll(indexBakSqls);
		}
		//获取租户所在数据源
		final DataSourceEntity dataSourceEntity = getDataSourceByTenantId(archiveTable.getTenantId());
		final boolean result = DataSourceUtil.executeDdls(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(),
				dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), createSqls);
		if (!result) {
			log.error("动态表创建失败");
			DataSourceUtil.dropTable(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(),
					dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), hasCreateTableNameList);
			throw new ArchiveBusinessException("动态表创建失败");
		}
	}

    private List<String> getCreateIndexSql(ArchiveTable archiveTable, DataBaseSqlService<Metadata> dataBaseSqlService, Boolean isBak) {
        List<String> indexSqls = new ArrayList<>();
        indexSqls.add(getIndexSql(archiveTable, dataBaseSqlService, new String[]{FieldConstants.OWNER_ID}, isBak));
        if (!isBak) {
            indexSqls.add(getIndexSql(archiveTable, dataBaseSqlService, new String[]{FieldConstants.UPDATED_TIME}, isBak));
        }
        return indexSqls;
    }

    private String getIndexSql(ArchiveTable archiveTable, DataBaseSqlService<Metadata> dataBaseSqlService, String[] columns, Boolean isBak) {
        String indexName = ArchiveTableUtil.getArchiveTableIndexName(archiveTable.getId(), columns, isBak);
        return dataBaseSqlService.getCreateIndexSql(archiveTable.getStorageLocate(), indexName, columns);
    }

    @Override
    public ArchiveTable createArchiveBakTable(ArchiveTable archiveTable) {
        return ArchiveTable.builder().id(archiveTable.getId()).tenantId(archiveTable.getTenantId())
                .storageLocate(archiveTable.getStorageLocate() + ArchiveConstants.BAK_TABLE_SUFFIX).build();
    }

    /**
     * 根据租户id获取租户所在数据源
     *
     * @param tenantId 租户id
     * @return
     */
    @Override
    public DataSourceEntity getDataSourceByTenantId(Long tenantId) throws ArchiveBusinessException {
        final R<DataSourceEntity> result = remoteDataSourceService.getDataSourceByTenantId(tenantId, SecurityConstants.FROM_IN);
        if (result.getCode() == CommonConstants.FAIL) {
            log.error("根据租户id获取数据源失败", result.getMsg());
            throw new ArchiveBusinessException("根据租户id获取数据源失败！");
        }
        return result.getData();
    }

    private List<DataSourceDetail> getRemoteDataSourceDetailList(ArchiveTable archiveTable)
            throws ArchiveBusinessException {
        //调用sharding接口，创建物理表
        R<List<DataSourceDetail>> dataSourceDetailListResult = null;
        if (archiveTable.getTenantId() != null) {
            dataSourceDetailListResult = remoteDataSourceService.getDataSourceDetailAndsave(archiveTable.getTenantId(),
                    archiveTable.getStorageLocate(), SHARDING_NUMBER, SecurityConstants.FROM_IN);
        } else {
            dataSourceDetailListResult = remoteDataSourceService.getDataSourceDetailAndsave(
                    SecurityUtils.getUser().getTenantId(), archiveTable.getStorageLocate(), SHARDING_NUMBER,
                    SecurityConstants.FROM_IN);
        }
        if (dataSourceDetailListResult == null || dataSourceDetailListResult.getCode() == CommonConstants.FAIL) {
            log.error("保存并获取分库分表记录失败！");
            throw new ArchiveBusinessException("保存并获取分库分表记录失败！");
        }
        return dataSourceDetailListResult.getData();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTables(List<ArchiveTable> archiveTables, Long tenantId) throws ArchiveBusinessException {
        //创建所有表的sql语句容器
        final List<String> createSqls = CollUtil.<String>newArrayList();
        //获取字段信息
        final List<Metadata> metadataList = metadataService
                .list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getTenantId, tenantId));
        //bak表元字段（所有archiveTable的基本字段一样，tableId和storageLocate不一样）
        final List<Metadata> bakMetadataList = metadataService.getBakMetadataList(archiveTables.get(0));

        archiveTables.stream().forEach(archiveTable -> {
            // 创建业务表
            List<Metadata> list = metadataList.stream().filter(metadata -> metadata.getStorageLocate().equals(archiveTable.getStorageLocate())).collect(Collectors.toList());
            String sql = dataBaseSqlService.getCreateTableSql(archiveTable.getStorageLocate(), list, false);
            createSqls.add(sql);
            List<String> indexSqls = getCreateIndexSql(archiveTable, dataBaseSqlService, false);
            createSqls.addAll(indexSqls);
           // dataBaseSqlService.getCreateTableColumnCommentSql(archiveTable.getStorageLocate(),list,createSqls);
            // 创建bak表(过程信息和签名签章都不需要建对应的bak表)
            if (!ArchiveLayerEnum.SIGNATRUE.getCode().equals(archiveTable.getArchiveLayer())
                    && !ArchiveLayerEnum.INFO.getCode().equals(archiveTable.getArchiveLayer())) {
                final ArchiveTable archiveBakTable = createArchiveBakTable(archiveTable);
//				bakMetadataList.stream().forEach(metadata -> {
//					metadata.setTableId(archiveTable.getId());
//					metadata.setStorageLocate(archiveTable.getStorageLocate());
//				});
//				final List<Metadata> bakMetadata = Stream.concat(list.stream(), bakMetadataList.stream()).collect(Collectors.toList());
                sql = dataBaseSqlService.getCreateTableSql(archiveBakTable.getStorageLocate(), bakMetadataList, false);
                createSqls.add(sql);
                List<String> indexBakSqls = getCreateIndexSql(archiveBakTable, dataBaseSqlService, true);
                createSqls.addAll(indexBakSqls);
                //dataBaseSqlService.getCreateTableColumnCommentSql(archiveBakTable.getStorageLocate(), bakMetadataList,createSqls);
            }
        });

        // 获取租户所在的数据源
        final DataSourceEntity dataSourceEntity = getDataSourceByTenantId(tenantId);
        // 适配 oracle sql 单批次数量过多 进行分段处理
        Integer cursor = 200;
        List<List<String>> split = CollectionUtil.split(createSqls, cursor);
        boolean result = Boolean.FALSE;
        for (List<String> strings : split) {
            result = DataSourceUtil.executeDdls(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(),
                    dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), strings);
        }
        if (!result) {
            log.error("动态表创建失败");
            throw new ArchiveBusinessException("动态表创建失败");
        }
    }

    @Override
    public void dropTables(Long tenantId) throws ArchiveBusinessException {
        //获取所有表信息
        final List<ArchiveTable> archiveTables = this.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId));
        if (CollectionUtil.isEmpty(archiveTables)) {
            return;
        }
        final List<String> dropSqls = archiveTables.stream().map(archiveTable -> dataBaseSqlService.getDropTableSql(archiveTable.getStorageLocate())).collect(Collectors.toList());
        //删除 bak 表
        final List<String> dropBakSqls = archiveTables.stream().filter(archiveTable -> !ArchiveLayerEnum.SIGNATRUE.getCode().equals(archiveTable.getArchiveLayer())
                && !ArchiveLayerEnum.INFO.getCode().equals(archiveTable.getArchiveLayer())).map(archiveTable -> dataBaseSqlService.getDropTableSql(createArchiveBakTable(archiveTable).getStorageLocate())).collect(Collectors.toList());
        final Collection<String> union = CollectionUtil.union(dropSqls, dropBakSqls);
        List<String> createAllSqls = union.stream().collect(Collectors.toList());
        // 获取租户所在的数据源
        final DataSourceEntity dataSourceEntity = getDataSourceByTenantId(tenantId);
        final boolean result = DataSourceUtil.executeDdls(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(),
                dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), createAllSqls);
        if (!result) {
            log.error("删除物理表失败");
            throw new ArchiveBusinessException("删除物理表失败");
        }
    }

    /**
     * 根据表名查询档案数据表
     *
     * @param storageLocate
     * @return
     */
    @Override
    @Cacheable(
            key = "'archive-app-management:archive-table-service:' + #storageLocate",
            unless = "#result == null"
    )
    public ArchiveTable getTableByStorageLocate(String storageLocate) throws ArchiveBusinessException {
        final ArchiveTable archiveTable = this.getOne(Wrappers.<ArchiveTable>query().lambda()
                .eq(ArchiveTable::getStorageLocate, storageLocate));
        if (ObjectUtil.isNull(archiveTable)) {
            log.error("表名[{}]不存在", storageLocate);
            throw new ArchiveBusinessException("表名" + storageLocate + "不存在");
        }
        return archiveTable;
    }
    @Override
    public List<ArchiveTable> getTableByStorageLocates(List<String> storageLocates) throws ArchiveBusinessException {
        List<ArchiveTable> archiveTables = this.list(Wrappers.<ArchiveTable>query().lambda()
                .in(ArchiveTable::getStorageLocate, storageLocates));
        if (ObjectUtil.isNull(archiveTables)) {
            log.error("表名[{}]不存在", storageLocates.toString());
            throw new ArchiveBusinessException("表名" + storageLocates.toString() + "不存在");
        }
        return archiveTables;
    }

    /**
     * 根据表名和字段标签，
     * 获取同档案门类的文件级表名和对应元数据集合
     *
     * @param metadataTagEnglishDTO
     * @return
     * @throws ArchiveBusinessException
     */
    @Override
    public MetadataListDTO getFileTableAndMetadataList(MetadataTagEnglishDTO metadataTagEnglishDTO) {
        final ArchiveTable upTable = getUpTableByStorageLocate(metadataTagEnglishDTO.getStorageLocate());
        if (null == upTable) {
            throw new ArchiveRuntimeException("档案门类表[" + metadataTagEnglishDTO.getStorageLocate() + "]父表不存在！");
        }
        final List<Metadata> metadataList = metadataService.listAllByTableId(upTable.getId());
        final List<String> tagEnglishList = metadataTagEnglishDTO.getTagEnglishList();
        final List<MetadataDTO> metadataDTOList = metadataList.stream()
                .filter(metadata -> tagEnglishList.contains(metadata.getTagEnglish())).map(metadata -> {
                    final MetadataDTO metadataDTO = new MetadataDTO();
                    metadataDTO.setMetadataChinese(metadata.getMetadataChinese());
                    metadataDTO.setMetadataEnglish(metadata.getMetadataEnglish());
                    metadataDTO.setTagEnglish(metadata.getTagEnglish());
                    return metadataDTO;
                }).collect(Collectors.toList());
        return new MetadataListDTO(upTable.getStorageLocate(), metadataDTOList);
    }

    /**
     * 得到全文及上层门类
     *
     * @return
     */
    @Override
    public List<ArchiveTable> getDocumentAndUpTable() {
        List<ArchiveTable> documentUpTable = getBaseMapper().getDocumentUpTable();
		return documentUpTable;
    }

    private List<ArchiveTable> getDocumentUpTable() {
        return getBaseMapper().getDocumentUpTable();
    }

    /**
     * 获取所有上级表名
     *
     * @param storageLocate
     * @return
     */
    @Override
    public List<ArchiveTable> getUpTableListByStorageLocate(String storageLocate) {
        List<ArchiveTable> upTableList = new ArrayList<>();
        ArchiveTable upTable = getUpTableByStorageLocate(storageLocate);
        while (null != upTable) {
            upTableList.add(upTable);
            upTable = getUpTableByStorageLocate(upTable.getStorageLocate());
        }
        return upTableList;
    }

    @Override
    public List<ArchiveTable> getUpTableListByStorageLocateAndTenantId(String storageLocate,Long tenantId) {
        List<ArchiveTable> upTableList = new ArrayList<>();
        ArchiveTable upTable = getUpTableByStorageLocateAndTenantId(storageLocate,tenantId);
        while (null != upTable) {
            upTableList.add(upTable);
            upTable = getUpTableByStorageLocateAndTenantId(upTable.getStorageLocate(),tenantId);
        }
        return upTableList;
    }

	/**
	 * 获取上级的表
	 *
	 * @param storageLocate
	 * @return
	 */
	@Override
	@Cacheable(
			key = "'archive-app-management:archive-table-service:up:storageLocate:' + #storageLocate",
			unless = "#result == null"
	)
	public ArchiveTable getUpTableByStorageLocate(String storageLocate) {
		return getBaseMapper().getUpTableByStorageLocate(storageLocate);
	}

	@Override
	@Cacheable(
			key = "'archive-app-management:archive-table-service:up:storageLocate:' + #storageLocate+ ':' + #tenantId",
			unless = "#result == null"
	)
	public ArchiveTable getUpTableByStorageLocateAndTenantId(String storageLocate,Long tenantId) {
		return getBaseMapper().getUpTableByStorageLocateAndTenantId(storageLocate,tenantId);
	}

    /**
     * 获取下级的表
     *
     * @param storageLocate
     * @return
     */
    @Override
	@Cacheable(key ="'archive-app-management:archive-table-service:down:storageLocate:' + #storageLocate",
			   unless = "#result == null || #result.size() == 0"
	)
    public List<ArchiveTable> getDownTableByStorageLocate(String storageLocate) {
        return getBaseMapper().getDownTableByStorageLocate(storageLocate);
    }

	@Override
	@Cacheable(
			key = "'archive-app-management:archive-table-service:down:storageLocate:' + #storageLocate+ ':' + #tenantId",
			unless = "#result == null"
	)
	public List<ArchiveTable> getDownTableByStorageLocateAndTenantId(String storageLocate,Long tenantId) {
		return getBaseMapper().getDownTableByStorageLocateAndTenantId(storageLocate,tenantId);
	}

    @Override
	@Cacheable(
			key = "'archive-app-management:archive-table-service:down:' + #typeCode + ':' + #templateTableId",
			unless = "#result == null || #result.size() == 0"
	)
    public List<ArchiveTable> getDownTableByTypeCodeAndTemplateTableId(String typeCode, Long templateTableId) {
        return getBaseMapper().getDownTableByTypeCodeAndTemplateTableId(typeCode, templateTableId);
    }

    /**
     * 不包括全文 过程信息 签名签章表的 下级
     *
     * @param storageLocate
     * @return
     */
    @Override
    public List<ArchiveTable> getDownTableByStorageLocateExclude(String storageLocate) {
        final List<ArchiveTable> tableList = getBaseMapper().getDownTableByStorageLocate(storageLocate);
        return tableList.stream()
                .filter(archiveTable -> Arrays.stream(EXCLUDE_LAYER)
                        .noneMatch(archiveLayerEnum -> archiveLayerEnum.getCode().equals(archiveTable.getArchiveLayer())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ArchiveTable> getAllList() {
        return this.list(Wrappers.emptyWrapper());
    }

    @Override
    public ArchiveTable getTableById(Long id) {
        return this.getById(id);
    }

    /**
     * 删除动态库里面的表
     *
     * @param archiveTable 逻辑表名
     */
    private void dropTable(ArchiveTable archiveTable) throws ArchiveBusinessException {
        final List<String> dropSqlList = CollectionUtil.newArrayList();
        final DataSourceEntity dataSourceEntity = getDataSourceByTenantId(archiveTable.getTenantId());
        String sql = dataBaseSqlService.getDropTableSql(archiveTable.getStorageLocate());
        dropSqlList.add(sql);
        //获取BAK表的
        if (!ArchiveLayerEnum.SIGNATRUE.getCode().equals(archiveTable.getArchiveLayer())
                && !ArchiveLayerEnum.INFO.getCode().equals(archiveTable.getArchiveLayer())) {
            final ArchiveTable archiveBakTable = createArchiveBakTable(archiveTable);
            sql = dataBaseSqlService.getDropTableSql(archiveBakTable.getStorageLocate());
            dropSqlList.add(sql);
        }

        final boolean result = DataSourceUtil.executeDdls(dataSourceEntity.getDriverClass(), dataSourceEntity.getUrl(),
                dataSourceEntity.getUserName(), dataSourceEntity.getPassword(), dropSqlList);
        if (!result) {
            log.error("动态表删除失败");
            throw new ArchiveBusinessException("动态表删除失败");
        }
    }

    @Override
    public DataSourceEntity getRemoteDataSourceEntity(String tableName) throws ArchiveBusinessException {
        final R<DataSourceEntity> dataSourceEntityResult = remoteDataSourceService
                .getDataSourceByLogicalTable(tableName, SecurityConstants.FROM_IN);
        if (dataSourceEntityResult == null || dataSourceEntityResult.getCode() == CommonConstants.FAIL) {
            log.error("根据逻辑表名获取动态数据源失败");
            throw new ArchiveBusinessException("根据逻辑表名获取动态数据源失败");
        }
        return dataSourceEntityResult.getData();
    }

    @Override
	@CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void createArchiveTable(ArchiveType archiveType, List<TemplateTable> templateTableList)
            throws ArchiveBusinessException {
        final List<String> hasCreateTableNameList = CollectionUtil.newArrayList();
	    List<ArchiveTableButtonDTO> archiveTableButtonDTOS = CollUtil.newArrayList();
	    for (final TemplateTable templateTable : templateTableList) {
            //插入archive_table
            if (log.isDebugEnabled()) {
                log.debug("插入档案表数据：{}", archiveType.toString());
            }
            final ArchiveTable archiveTable = insertArchiveTableData(archiveType, templateTable);
		    ArchiveTableButtonDTO archiveTableButtonDTO = new ArchiveTableButtonDTO();
		    BeanUtil.copyProperties(archiveTable, archiveTableButtonDTO);
		    archiveTableButtonDTOS.add(archiveTableButtonDTO);
            //拷贝template_metadata元数据到metadata表
            if (log.isDebugEnabled()) {
                log.debug("插入表[{}]中元数据", archiveTable.getStorageLocate());
            }
            metadataService.insertIntoMetadataFromTemplate(archiveTable, templateTable);
            //根据英文绑定 标签和数据字典
            //			archiveTypeService.bingdingTagAndDict(archiveTable);
            //创建《租户标识+档案门类分类标识+档案门类标识+层级标识》的物理表
            log.debug("创建物理表[{}]", archiveTable.getStorageLocate());
            //创建物理表
            //已经创建的表名
            createTable(archiveTable, hasCreateTableNameList);
        }
	    //保存打开门类按钮
	    remoteTenantArchiveButtonService.saveButtonByTypeCode(archiveTableButtonDTOS);
    }

	@Override
	@CacheEvict(allEntries = true)
	@Transactional(rollbackFor = Exception.class)
    public List<ArchiveTable> createArchiveTableForCopy(ArchiveType srcArchiveType, ArchiveType destArchiveType,
														List<TemplateTable> templateTableList, Map<Long, Long> srcDestMetadataMap,
														Map<String,String> destSrcStorageLocateMap) throws ArchiveBusinessException {
    	final List<ArchiveTable> archiveTables = CollectionUtil.newArrayList();
		final List<String> hasCreateTableNameList = CollectionUtil.newArrayList();
		// 查询源档案类型的所有archiveTable信息
		List<ArchiveTable> srcArchiveTables = this.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getArchiveTypeCode, srcArchiveType.getTypeCode()));
		for (final TemplateTable templateTable : templateTableList) {
			final ArchiveTable archiveTable = insertArchiveTableData(destArchiveType, templateTable);
			archiveTables.add(archiveTable);
			// 插入元字段（从复制的表中拷贝出来再插入）
			ArchiveTable srcArchiveTable = srcArchiveTables.stream().filter(table -> table.getTemplateTableId().equals(archiveTable.getTemplateTableId())).findAny().get();
			destSrcStorageLocateMap.put(archiveTable.getStorageLocate(), srcArchiveTable.getStorageLocate());
			List<Metadata> srcMetadatas = metadataService.list(Wrappers.<Metadata>lambdaQuery().eq(Metadata::getStorageLocate, srcArchiveTable.getStorageLocate()));
			List<Metadata> destMetadatas = srcMetadatas.stream().map(metadata -> {
				Metadata m = new Metadata();
				BeanUtil.copyProperties(metadata, m);
				m.setId(null);
				m.setTableId(archiveTable.getId());
				m.setStorageLocate(archiveTable.getStorageLocate());
				//档案类型编码的默认值设置
				if(FieldConstants.ARCHIVE_TYPE_CODE.equalsIgnoreCase(m.getMetadataEnglish())
					&& srcArchiveTable.getArchiveTypeCode().equals(m.getMetadataDefaultValue())) {
					m.setMetadataDefaultValue(archiveTable.getArchiveTypeCode());
				}
				return m;
			}).collect(Collectors.toList());
			metadataService.saveBatch(destMetadatas);
			for (int i = 0, size = srcMetadatas.size(); i < size; i++) {
				srcDestMetadataMap.put(srcMetadatas.get(i).getId(), destMetadatas.get(i).getId());
			}
			// 创建物理表
			createTableForCopy(archiveTable, destMetadatas, hasCreateTableNameList);
		}
		return archiveTables;
	}

    private ArchiveTable insertArchiveTableData(ArchiveType archiveType, TemplateTable templateTable)
            throws ArchiveBusinessException {
        final Layer archiveLayer = layerService.getByCode(templateTable.getLayerCode());
        if (null == archiveLayer) {
            log.error("根据层级编码[{}]获取档案层级为空", templateTable.getLayerCode());
            throw new ArchiveBusinessException("根据层级编码[" + templateTable.getLayerCode() + "]获取档案层级为空");
        }
        final String storageName = getStorageName(archiveType, templateTable);
        final String storageLocate = getStorageLocate(archiveType, templateTable);

        final ArchiveTable archiveTable = new ArchiveTable();
        archiveTable.setArchiveTypeCode(archiveType.getTypeCode());
        archiveTable.setStorageName(storageName);
        archiveTable.setStorageLocate(storageLocate);
        archiveTable.setFilingType(archiveType.getFilingType());
        archiveTable.setClassType(archiveType.getClassType());
        archiveTable.setArchiveLayer(templateTable.getLayerCode());
        archiveTable.setTemplateTableId(templateTable.getId());
        archiveTable.setSortNo(templateTable.getSortNo());
        if (archiveType.getTenantId() != null) {
            archiveTable.setTenantId(archiveType.getTenantId());
        }
        this.save(archiveTable);
        return archiveTable;
    }

    private String getStorageName(ArchiveType archiveType, TemplateTable templateTable) {
        return new StringBuffer().append(archiveType.getTypeName())
                .append(ArchiveConstants.SYMBOL.LEFT_BRACKET).append(templateTable.getName())
                .append(ArchiveConstants.SYMBOL.RIGHT_BRACKET).toString();
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService#getStorageLocateByArchiveTypeCodeAndTemplateTableId(java.lang.String,
     * java.lang.Long)
     */
    @Override
	@Cacheable(key = "'archive-app-management:archive-table-service:storageLocate:' + #archiveTypeCode + ':' + #templateTableId", unless = "#result == null")
    public String getStorageLocateByArchiveTypeCodeAndTemplateTableId(String archiveTypeCode, Long templateTableId) {
        return getTableByArchiveTypeCodeAndTemplateTableId(archiveTypeCode, templateTableId).getStorageLocate();
    }

    @Override
	@Cacheable(key = "'archive-app-management:archive-table-service:archiveTable:' + #archiveTypeCode + ':' + #templateTableId + ':' + #tenantId", unless = "#result == null")
    public ArchiveTable getTableByArchiveTypeCodeAndTemplateTableIdAndTenantId(String archiveTypeCode, Long templateTableId,Long tenantId) {
        final LambdaQueryWrapper<ArchiveTable> lambdaQuery = Wrappers.<ArchiveTable>lambdaQuery();
        lambdaQuery.eq(ArchiveTable::getArchiveTypeCode, archiveTypeCode).eq(ArchiveTable::getTemplateTableId,
                templateTableId).eq(ArchiveTable::getTenantId,tenantId);
        final ArchiveTable entity = getOne(lambdaQuery);
        if (null == entity) {
            throw new ArchiveRuntimeException(
                    String.format("档案门类表不存在（archiveTypeCode=%s,templateTableId=%d），请检查！", archiveTypeCode, templateTableId),
                    "档案门类表不存在，请检查！");
        }
        return entity;
    }

    @Override
	@Cacheable(key = "'archive-app-management:archive-table-service:archiveTable:' + #archiveTypeCode + ':' + #templateTableId", unless = "#result == null")
    public ArchiveTable getTableByArchiveTypeCodeAndTemplateTableId(String archiveTypeCode, Long templateTableId) {
        final LambdaQueryWrapper<ArchiveTable> lambdaQuery = Wrappers.<ArchiveTable>lambdaQuery();
        lambdaQuery.eq(ArchiveTable::getArchiveTypeCode, archiveTypeCode).eq(ArchiveTable::getTemplateTableId,
                templateTableId);
        final ArchiveTable entity = getOne(lambdaQuery);
        if (null == entity) {
            throw new ArchiveRuntimeException(
                    String.format("档案门类表不存在（archiveTypeCode=%s,templateTableId=%d），请检查！", archiveTypeCode, templateTableId),
                    "档案门类表不存在，请检查！");
        }
        return entity;
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService#getDownTableByStorageLocateAndDownLayerCode(java.lang.String,
     * java.lang.String)
     */
    @Override
	@Cacheable(key = "'archive-app-management:archive-table-service:down:' + #storageLocate + ':' + #downLayerCode", unless = "#result == null || #result.size() == 0")
    public List<ArchiveTable> getDownTableByStorageLocateAndDownLayerCode(String storageLocate, String downLayerCode) {
        return getBaseMapper().getDownTableByStorageLocateAndDownLayerCode(storageLocate,downLayerCode);
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService#getDownTableByArchiveTypeCodeAndTemplateTableIdAndDownLayerCode(java.lang.String,
     * java.lang.Long, java.lang.String)
     */
    @Override
	@Cacheable(
			key = "'archive-app-management:archive-table-service:down:' + #archiveTypeCode + ':' + #templateTableId + ':' + #downLayerCode",
			unless = "#result == null || #result.size() == 0"
	)
    public List<ArchiveTable> getDownTableByArchiveTypeCodeAndTemplateTableIdAndDownLayerCode(String archiveTypeCode, Long templateTableId, String downLayerCode) {
        return getBaseMapper().getDownTableByArchiveTypeCodeAndTemplateTableIdAndDownLayerCode(
                archiveTypeCode, templateTableId, downLayerCode);
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService#getUpTableMetadataByStorageLocate(java.lang.String)
     */
    @Override
    public ArchiveTableSearchDTO getUpTableMetadataByStorageLocate(String storageLocate) {
        final ArchiveTable upTable = getUpTableByStorageLocate(storageLocate);
        if (null == upTable) {
            throw new ArchiveRuntimeException("档案门类表[" + storageLocate + "]父表不存在！");
        }
        final List<Metadata> metadataList = metadataService.listAllByTableId(upTable.getId());
        List<String> fields = metadataList.stream().map(m -> {
            if (StrUtil.isNotEmpty(m.getTagEnglish())) {
                return m.getMetadataEnglish() + " as " + m.getTagEnglish();
            }
            return m.getMetadataEnglish();
        }).collect(Collectors.toList());
        return new ArchiveTableSearchDTO(upTable.getStorageLocate(), fields);
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService#getUpTableByArchiveTypeCodeAndTemplateTableId(
     *java.lang.String,
     * java.lang.Long)
     */
    @Override
	@Cacheable(
			key = "'archive-app-management:archive-table-service:up:' + #archiveTypeCode + ':' + #templateTableId",
			unless = "#result == null"
	)
    public ArchiveTable getUpTableByArchiveTypeCodeAndTemplateTableId(String archiveTypeCode, Long templateTableId) {
        return getBaseMapper().getUpTableByArchiveTypeCodeAndTemplateTableId(archiveTypeCode, templateTableId);
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService#getUpTemplateTableIdByArchiveTypeCodeAndTemplateTableId(
     *java.lang.String,
     * java.lang.Long)
     */
    @Override
    public Long getUpTemplateTableIdByArchiveTypeCodeAndTemplateTableId(String archiveTypeCode, Long templateTableId) {
        final ArchiveTable table = getBaseMapper().getUpTableByArchiveTypeCodeAndTemplateTableId(archiveTypeCode, templateTableId);
        if (null == table) {
            return null;
        }
        return table.getTemplateTableId();
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService#getDocumentTableMetadataByStorageLocate(java.lang.String)
     */
    @Override
    public List<ArchiveTableSearchDTO> getDocumentTableMetadataByStorageLocate(String storageLocate) {
        final List<ArchiveTable> downTables = getDownTableByStorageLocateAndDownLayerCode(storageLocate,
                ArchiveLayerEnum.DOCUMENT.getCode());
        if (null == downTables || downTables.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("档案门类表[" + storageLocate + "]对应的全文表不存在！");
            }
            return null;
        }
        return downTables.stream().map(table -> {
            final List<Metadata> metadataList = metadataService.listAllByTableId(table.getId());
            final List<String> fields = metadataList.stream().map(m -> {
                if (StrUtil.isNotEmpty(m.getTagEnglish())) {
                    return m.getMetadataEnglish() + " as " + m.getTagEnglish();
                }
                return m.getMetadataEnglish();
            }).collect(Collectors.toList());
            return new ArchiveTableSearchDTO(table.getStorageLocate(), fields);
        }).collect(Collectors.toList());
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService#getTableMetadataByStorageLocate(java.lang.String)
     */
    @Override
    public ArchiveTableSearchDTO getTableMetadataByStorageLocate(String storageLocate) {
        final List<Metadata> metadataList = metadataService.list(Wrappers.<Metadata>query().lambda()
                .eq(Metadata::getStorageLocate, storageLocate));
        /**
		 * 对ES 建索引的sql field字段进行过滤
		 * 王谷华 2022-03-24 与 马玉荣会议讨论后
		 * 恢复所有系统字段 去除 .filter(m -> checkSearchFiled(m))
		 *
		 * ---------------------------------------
		 * 一下作廢 留档
		 * 去除 除 id, is_delete,fonds_code,status 这些权限外的系统字段
		 *
		 * 王谷华 2022-03-03 与 马玉荣、刘东会议讨论后
		 *
		 *
		 * */
        final List<String> fields = metadataList.stream()
//			.filter(m -> checkSearchFiled(m))
			.map(m -> {
				if (StrUtil.isNotEmpty(m.getTagEnglish())) {
					return m.getMetadataEnglish() + " as " + m.getTagEnglish();
				}
				return m.getMetadataEnglish();
			}).collect(Collectors.toList());
        return new ArchiveTableSearchDTO(storageLocate, fields);
    }

	/**
	 * 王谷华 2022-03-24 与 马玉荣会议讨论后 作废代码暂保留
	 * 检查是否 业务字段 或 id, is_delete,fonds_code,status
	 * @param metadata
	 * @return
	 */
    private boolean checkSearchFiled(Metadata metadata){
		return metadata.getMetadataClass()>0 || ArrayUtil.contains(INDEX_NEED_SYS_FILED,metadata.getMetadataEnglish())
				|| ArrayUtil.contains(INDEX_NEED_SYS_FILED,metadata.getTagEnglish());
	}


    @Override
    public List<ArchiveTable> getIndexArchiveTableList() {
        return this.getBaseMapper().getIndexArchiveTableList();
    }

    @Override
    public List<ArchiveTable> getArchiveTablesByTenantId(Long tenantId) {
        final List<String> layers = Arrays.asList(
                ArchiveLayerEnum.DOCUMENT.getValue(), ArchiveLayerEnum.INFO.getValue(), ArchiveLayerEnum.SIGNATRUE.getValue());
        return this.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId).notIn(ArchiveTable::getArchiveLayer, layers));
    }

	@Override
	public List<ArchiveTable> getArchiveDocumentTablesByTenantId(Long tenantId) {
		return this.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getTenantId, tenantId).eq(ArchiveTable::getArchiveLayer, ArchiveLayerEnum.DOCUMENT.getValue()));
	}
    /**
     *
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService#getIndexArchiveTableListByTenantId(java.lang.Long)
     */
    @Override
    public List<ArchiveTable> getIndexArchiveTableListByTenantId(Long tenantId) {
        final List<String> layers = Arrays.asList(ArchiveLayerEnum.INFO.getValue(),
            ArchiveLayerEnum.SIGNATRUE.getValue());
        return this.list(Wrappers.<ArchiveTable> lambdaQuery().eq(ArchiveTable::getTenantId, tenantId)
            .notIn(ArchiveTable::getArchiveLayer, layers));
    }

	@Override
	public List<ArchiveTable> getTableListByTypeCodeAndTenantId(String typeCode, Long tenantId) {
		final List<ArchiveTable> tableList = this.list(Wrappers.<ArchiveTable>query().lambda()
				.eq(ArchiveTable::getArchiveTypeCode, typeCode)
				.eq(ArchiveTable::getTenantId, tenantId)
				.orderByAsc(ArchiveTable::getSortNo));
		return tableList;
	}


}
