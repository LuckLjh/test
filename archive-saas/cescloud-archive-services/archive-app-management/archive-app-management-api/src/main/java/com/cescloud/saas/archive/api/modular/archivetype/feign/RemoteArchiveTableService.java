package com.cescloud.saas.archive.api.modular.archivetype.feign;

import com.cescloud.saas.archive.api.modular.archivetype.dto.ArchiveTableSearchDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataListDTO;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataTagEnglishDTO;
import com.cescloud.saas.archive.service.modular.actuator.annotation.Actuator;
import com.cescloud.saas.archive.service.modular.actuator.annotation.ActuatorType;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(contextId = "remoteArchiveTableService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveTableService {

    @GetMapping("/table/all-list")
    public R<List<ArchiveTable>> getTableList();

    @GetMapping("/table/list/typeCode/{typeCode}")
    public R<List<ArchiveTable>> getTableListByTypeCode(@PathVariable("typeCode") String typeCode);

	@GetMapping("/table/inner/list/typeCode-tenantId/{typeCode}/{tenantId}")
	public R<List<ArchiveTable>> getTableListByTypeCodeAndTenantIdInner(@PathVariable("typeCode") String typeCode,
																   @PathVariable("tenantId") Long tenantId,
																   @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/table/{tableId}")
    public R<ArchiveTable> getTableById(@PathVariable("tableId") Long tableId);

	@GetMapping("/table/storageLocate/{storageLocate}")
    public R<ArchiveTable> getByStorageLocate(@PathVariable("storageLocate") String storageLocate);

    @PostMapping("/table/tag-metadata")
    public R<MetadataListDTO> getFileTableAndMetadataList(@RequestBody MetadataTagEnglishDTO metadataTagEnglishDTO,
        @RequestHeader(SecurityConstants.FROM) String from);

    @GetMapping("/table/up/{storageLocate}")
    public R<ArchiveTable> getUpTableByStorageLocate(@PathVariable("storageLocate") String storageLocate);

    @GetMapping("/table/up/list/{storageLocate}")
    public R<List<ArchiveTable>> getUpTableListByStorageLocate(@PathVariable("storageLocate") String storageLocate);

    @GetMapping("/table/up/list/{storageLocate}/{tenantId}")
    public R<List<ArchiveTable>> getUpTableListByStorageLocateInner(@PathVariable("storageLocate") String storageLocate,@PathVariable("tenantId") Long tenantId, @RequestHeader(SecurityConstants.FROM) String from);

    @GetMapping("/table/down/{storageLocate}")
    public R<List<ArchiveTable>> getDownTableByStorageLocate(@PathVariable("storageLocate") String storageLocate);

	@Actuator(name=ServiceNameConstants.ARCHIVE_APP_MANAGEMENT+"[/table/down]",type= ActuatorType.Feign)
	@GetMapping("/table/down/inner/{storageLocate}/{tenantId}")
	public R<List<ArchiveTable>> getDownTableByStorageLocateInner(@PathVariable("storageLocate") String storageLocate,@PathVariable("tenantId") Long tenantId, @RequestHeader(SecurityConstants.FROM) String from);

	@Actuator(name=ServiceNameConstants.ARCHIVE_APP_MANAGEMENT+"[/table/down]",type= ActuatorType.Feign)
    @GetMapping("/table/down")
    public R<List<ArchiveTable>> getDownTableByTypeCodeAndTemplateTableId(@RequestParam("typeCode") String typeCode,
        @RequestParam("templateTableId") Long templateTableId);

	@GetMapping("/table/inner/down")
	public R<List<ArchiveTable>> getDownTableByTypeCodeAndTemplateTableIdInner(@RequestParam("typeCode") String typeCode,
																		  @RequestParam("templateTableId") Long templateTableId,
																		  @RequestHeader(SecurityConstants.FROM) String from);
    @GetMapping("/table/inner/downByTypeCode")
	public R<List<ArchiveTable>> getDownStorageLocateByTypeCodeInner(@RequestParam("typeCode") String typeCode, @RequestHeader(SecurityConstants.FROM) String from);

    @GetMapping("/table/down/{storageLocate}/{downLayerCode}")
    public R<List<ArchiveTable>> getDownTableByStorageLocateAndDownLayerCode(
        @PathVariable("storageLocate") String storageLocate, @PathVariable("downLayerCode") String downLayerCode);

    @GetMapping("/table/down/{archiveCode}/{templateTableId}/{downLayerCode}")
    public R<List<ArchiveTable>> getDownTableByArchiveTypeCodeAndTemplateTableIdAndDownLayerCode(
        @PathVariable("archiveCode") String archiveCode, @PathVariable("templateTableId") Long templateTableId,
        @PathVariable("downLayerCode") String downLayerCode);

    @Actuator(name=ServiceNameConstants.ARCHIVE_APP_MANAGEMENT+"[/table/storage-locate]",type= ActuatorType.Feign)
    @GetMapping("/table/storage-locate/{archiveTypeCode}/{templateTableId}")
    R<String> getStorageLocateByArchiveTypeCodeAndTemplateTableId(
        @PathVariable("archiveTypeCode") String archiveTypeCode,
        @PathVariable("templateTableId") Long templateTableId);

	@Actuator(name="remote_getStorageLocateByArchiveTypeCodeAndTemplateTableId[/table/storage-locate]",type= ActuatorType.Feign)
	@GetMapping("/table/inner/storage-locate/{archiveTypeCode}/{templateTableId}")
	R<String> getStorageLocateByArchiveTypeCodeAndTemplateTableIdInner(
			@PathVariable("archiveTypeCode") String archiveTypeCode,
			@PathVariable("templateTableId") Long templateTableId,
			@RequestHeader(SecurityConstants.FROM) String from);

    @GetMapping("/table/table/{archiveTypeCode}/{templateTableId}")
    R<ArchiveTable> getTableByArchiveTypeCodeAndTemplateTableId(
        @PathVariable("archiveTypeCode") String archiveTypeCode,
        @PathVariable("templateTableId") Long templateTableId);

	@GetMapping("/table/inner/table/{archiveTypeCode}/{templateTableId}")
	R<ArchiveTable> getTableByArchiveTypeCodeAndTemplateTableIdInner(
			@PathVariable("archiveTypeCode") String archiveTypeCode,
			@PathVariable("templateTableId") Long templateTableId,
			@RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/table/inner/table/{archiveTypeCode}/{templateTableId}/{tenantId}")
    R<ArchiveTable> getTableByArchiveTypeCodeAndTemplateTableIdAndTenantIdInner(
        @PathVariable("archiveTypeCode") String archiveTypeCode,
        @PathVariable("templateTableId") Long templateTableId,
        @PathVariable("tenantId") Long tenantId, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取storageLocate上级表及字段
     *
     * @param storageLocate
     * @param from
     * @return
     */
    @GetMapping("/table/up/metadatas/{storageLocate}")
    R<ArchiveTableSearchDTO> getUpTableMetadatasByStorageLocate(@PathVariable("storageLocate") String storageLocate,
        @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取storageLocate对应全文表及字段
     *
     * @param storageLocate
     * @return
     */
    @GetMapping("/table/document/metadatas/{storageLocate}")
    R<List<ArchiveTableSearchDTO>> getDocumentTableMetadatasByStorageLocate(
        @PathVariable("storageLocate") String storageLocate, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 获取storageLocate表名及字段
     * 只包含 业务字段与必须的系统字段
     * @param storageLocate
     * @param from
     * @return
     */
    @GetMapping("/table/metadatas/{storageLocate}")
    R<ArchiveTableSearchDTO> getTableMetadatasByStorageLocate(@PathVariable("storageLocate") String storageLocate,
        @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 根据租户id获取所有的档案表，过滤掉 全文，过程信息，签名签章的记录
     *
     * @param tenantId
     *            租户id
     * @return
     */
    @GetMapping("/table/list/{tenantId}")
    R<List<ArchiveTable>> getArchiveTablesByTenantId(@PathVariable("tenantId") Long tenantId);


	/**
	 * 根据租户id获取所有的档案的电子文件表(内部调用)
	 *
	 * @param tenantId
	 *            租户id
	 * @return
	 */
	@GetMapping("/table/document/list/{tenantId}")
	R<List<ArchiveTable>> getArchiveDocumentTablesByTenantId(@PathVariable("tenantId") Long tenantId,@RequestHeader(SecurityConstants.FROM) String from);

	/**
     * 根据档案门类表获取上层表（内部调用）
     *
     * @param storageLocate
     * @param from
     * @return
     */
    @GetMapping("/table/inner/up/{storageLocate}")
    public R<ArchiveTable> getInnerUpTableByStorageLocate(
        @PathVariable("storageLocate") String storageLocate, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 查询所有租户档案门类表（内部调用）
     *
     * @param from
     * @return
     */
    @GetMapping("/table/inner/index/list")
    public R<List<ArchiveTable>> getInnerIndexTableList(@RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/table/downInner/{storageLocate}/{downLayerCode}")
	public R<List<ArchiveTable>> getDownTableByStorageLocateAndDownLayerCodeInner(
			@PathVariable("storageLocate") String storageLocate, @PathVariable("downLayerCode") String downLayerCode, @RequestHeader(SecurityConstants.FROM) String from);
}
