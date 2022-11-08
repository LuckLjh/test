
package com.cescloud.saas.archive.service.modular.archivetype.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.archivetype.dto.ArchiveTableSearchDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.ArchiveTypeTreeNode;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.api.modular.datasource.entity.DataSourceEntity;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataListDTO;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataTagEnglishDTO;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.List;
import java.util.Map;

/**
 * 档案表
 *
 * @author liudong1
 * @date 2019-03-27 12:48:29
 */
public interface ArchiveTableService extends IService<ArchiveTable> {

    /**
     * 拼接物理表名
     *
     * @param archiveType
     * @param templateTable
     * @return
     */
    String getStorageLocate(ArchiveType archiveType, TemplateTable templateTable);

    /**
     * 根据档案门类获取档案树结构表
     * 返回ArchiveType 纯粹是为了前台保持一致
     *
     * @param archiveType
     * @return
     */
    List<ArchiveTypeTreeNode> getTypeTreeListByType(ArchiveType archiveType);

    /**
     * 根据档案门类编码删除物理表
     *
     * @param typeCode 档案门类编码
     */
    void removeStorageLocate(String typeCode) throws ArchiveBusinessException;

    /**
     * 判断物理表是否能删除。1、档案表已存在数据 2、档案树关联档案类型
     *
     * @param archiveType
     * @return
     */
    R canDeleteArchiveTable(ArchiveType archiveType);

    ArchiveTable createArchiveBakTable(ArchiveTable archiveTable);

    /**
     * 创建物理表(初始化租户用的，批量操作，用于提高效率)
     *
     * @param archiveTables 物理表对象集合
     * @throws ArchiveBusinessException
     */
    void createTables(List<ArchiveTable> archiveTables, Long tenantId) throws ArchiveBusinessException;

    /**
     * 根据租户id 删除物理表
     *
     * @param tenantId
     */
    void dropTables(Long tenantId) throws ArchiveBusinessException;

    /**
     * 根据表名获取档案表对象
     *
     * @param storageLocate
     * @return
     * @throws ArchiveBusinessException
     */
    ArchiveTable getTableByStorageLocate(String storageLocate) throws ArchiveBusinessException;

    /**
     * 根据表名获取档案表对象
     *
     * @param storageLocates
     * @return
     * @throws ArchiveBusinessException
     */
    List<ArchiveTable> getTableByStorageLocates(List<String> storageLocates) throws ArchiveBusinessException;

    /**
     * 根据档案门类编码查询档案表
     *
     * @param typeCode 档案门类编码
     * @return
     */
    List<ArchiveTable> getTableListByTypeCode(String typeCode);

    /**
     * 根据表名和字段标签，获取同档案门类的文件级表名和对应元数据集合
     *
     * @param metadataTagEnglishDTO
     * @return
     * @throws ArchiveBusinessException
     */
    MetadataListDTO getFileTableAndMetadataList(MetadataTagEnglishDTO metadataTagEnglishDTO)
            throws ArchiveBusinessException;

    ArchiveTable getUpTableByStorageLocate(String storageLocate);

    ArchiveTable getUpTableByStorageLocateAndTenantId(String storageLocate,Long tenantId);

    List<ArchiveTable> getDownTableByTypeCodeAndTemplateTableId(String typeCode, Long templateTableId);

    List<ArchiveTable> getDocumentAndUpTable();

    List<ArchiveTable> getUpTableListByStorageLocate(String storageLocate);

    List<ArchiveTable> getUpTableListByStorageLocateAndTenantId(String storageLocate,Long tenantId);

    List<ArchiveTable> getDownTableByStorageLocate(String storageLocate);

	List<ArchiveTable> getDownTableByStorageLocateAndTenantId(String storageLocate,Long tenantId);

    List<ArchiveTable> getDownTableByStorageLocateExclude(String storageLocate);

    /**
     * 获取所有的数据
     *
     * @return ArchiveTable集合
     */
    List<ArchiveTable> getAllList();

    /**
     * 根据id获取对象
     *
     * @param id
     * @return ArchiveTable对象
     */
    ArchiveTable getTableById(Long id);

    DataSourceEntity getRemoteDataSourceEntity(String tableName) throws ArchiveBusinessException;

    DataSourceEntity getDataSourceByTenantId(Long tenantId) throws ArchiveBusinessException;

    void createArchiveTable(ArchiveType archiveType, List<TemplateTable> TemplateTableList) throws ArchiveBusinessException;

    List<ArchiveTable> createArchiveTableForCopy(ArchiveType srcArchiveType, ArchiveType destArchiveType,
                                                 List<TemplateTable> TemplateTableList, Map<Long, Long> srcDestMetadataMap,
                                                 Map<String, String> destSrcStorageLocateMap) throws ArchiveBusinessException;

    /**
     * 获取物理表名
     *
     * @param archiveTypeCode
     * @param templateTableId
     * @return
     */
    String getStorageLocateByArchiveTypeCodeAndTemplateTableId(String archiveTypeCode, Long templateTableId);

    ArchiveTable getTableByArchiveTypeCodeAndTemplateTableId(String archiveTypeCode, Long templateTableId);

    ArchiveTable getTableByArchiveTypeCodeAndTemplateTableIdAndTenantId(String archiveTypeCode, Long templateTableId,Long tenantId);

    /**
     * 获取指定表名下指定层级的的子表
     *
     * @param storageLocate
     * @param downLayerCode
     * @return
     */
    List<ArchiveTable> getDownTableByStorageLocateAndDownLayerCode(String storageLocate, String downLayerCode);

    /**
     * 获取指定档案门类表模板ID下指定层级的的子表
     *
     * @param archiveTypeCode
     * @param templateTableId
     * @param downLayerCode
     * @return
     */
    List<ArchiveTable> getDownTableByArchiveTypeCodeAndTemplateTableIdAndDownLayerCode(String archiveTypeCode, Long templateTableId, String downLayerCode);

    /**
     * 获取storageLocate上级的检索SQL
     *
     * @param storageLocate
     * @return
     */
    ArchiveTableSearchDTO getUpTableMetadataByStorageLocate(String storageLocate);

    /**
     * 获取指定档案门类表模板ID下的上层级档案门类表
     *
     * @param archiveTypeCode
     * @param templateTableId
     * @return
     */
    ArchiveTable getUpTableByArchiveTypeCodeAndTemplateTableId(String archiveTypeCode, Long templateTableId);

    /**
     * 获取指定档案门类表模板ID下的上层级档案门类表模板ID
     *
     * @param archiveTypeCode
     * @param templateTableId
     * @return
     */
    Long getUpTemplateTableIdByArchiveTypeCodeAndTemplateTableId(String archiveTypeCode, Long templateTableId);

    /**
     * 获取storageLocate全文层级的检索SQL
     *
     * @param storageLocate
     * @return
     */
    List<ArchiveTableSearchDTO> getDocumentTableMetadataByStorageLocate(String storageLocate);

    /**
     * 获取storageLocate的检索SQL
     *
     * @param storageLocate
     * @return
     */
    ArchiveTableSearchDTO getTableMetadataByStorageLocate(String storageLocate);

    /**
     * 查询所有要创建索引档案表
     *
     * @return
     */
    List<ArchiveTable> getIndexArchiveTableList();

    /**
     * 根据租户id获取所有的档案表，过滤掉 全文，过程信息，签名签章的记录
     *
     * @param tenantId
     * @return
     */
    List<ArchiveTable> getArchiveTablesByTenantId(Long tenantId);

    /**
     * 根据租户id获取所有的档案表的电子文件表
     *
     * @param tenantId
     * @return
     */
    List<ArchiveTable> getArchiveDocumentTablesByTenantId(Long tenantId);

    /**
     * 根据租户ID查询所有要创建索引档案表
     *
     * @return
     */
    List<ArchiveTable> getIndexArchiveTableListByTenantId(Long tenantId);

	/**
	 * 根据租户ID和档案类型编码
	 *
	 * @return
	 */
	List<ArchiveTable> getTableListByTypeCodeAndTenantId(String typeCode, Long tenantId);
}
