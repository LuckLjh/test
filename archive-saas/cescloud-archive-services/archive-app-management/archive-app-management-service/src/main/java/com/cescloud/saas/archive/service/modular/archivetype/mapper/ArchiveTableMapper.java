
package com.cescloud.saas.archive.service.modular.archivetype.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 档案表
 *
 * @author liudong1
 * @date 2019-03-27 12:48:29
 */
public interface ArchiveTableMapper extends BaseMapper<ArchiveTable> {

    ArchiveTable getUpTableByStorageLocate(@Param("storageLocate") String storageLocate);

    ArchiveTable getUpTableByStorageLocateAndTenantId(@Param("storageLocate") String storageLocate,@Param("tenantId") Long tenantId);

    List<ArchiveTable> getDocumentUpTable();

    List<ArchiveTable> getDownTableByStorageLocate(@Param("storageLocate") String storageLocate);

	List<ArchiveTable> getDownTableByStorageLocateAndTenantId(@Param("storageLocate") String storageLocate,@Param("tenantId") Long tenantId);

	List<ArchiveTable> getDownTableByTypeCodeAndTemplateTableId(@Param("typeCode") String typeCode, @Param("templateTableId") Long templateTableId);

    /**
     * 获取指定表名下指定层级的的子表
     *
     * @param storageLocate
     * @param downLayerCode
     * @return
     */
    List<ArchiveTable> getDownTableByStorageLocateAndDownLayerCode(@Param("storageLocate") String storageLocate,
        @Param("downLayerCode") String downLayerCode);

    /**
     * 获取指定档案门类表模板ID下指定层级的的子表
     *
     * @param archiveTypeCode
     * @param templateTableId
     * @param downLayerCode
     * @return
     */
    List<ArchiveTable> getDownTableByArchiveTypeCodeAndTemplateTableIdAndDownLayerCode(
        @Param("archiveTypeCode") String archiveTypeCode,
        @Param("templateTableId") Long templateTableId, @Param("downLayerCode") String downLayerCode);

    /**
     * 获取指定档案门类表模板ID下的上层级档案门类表
     *
     * @param archiveTypeCode
     * @param templateTableId
     * @return
     */
    ArchiveTable getUpTableByArchiveTypeCodeAndTemplateTableId(
        @Param("archiveTypeCode") String archiveTypeCode, @Param("templateTableId") Long templateTableId);
    
    /**
     * 查询所有要创建索引档案表
     * @return
     */
    List<ArchiveTable> getIndexArchiveTableList();

}
