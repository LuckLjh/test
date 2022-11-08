
package com.cescloud.saas.archive.service.modular.archivetype.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.archivetype.dto.ArchiveTableSearchDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveTable;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataListDTO;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataTagEnglishDTO;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.actuator.annotation.Actuator;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTableService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 档案表
 *
 * @author liudong1
 * @date 2019-03-27 12:48:29
 */
@Api(value = "archiveTable", tags = "档案表管理")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/table")
public class ArchiveTableController {

    private final ArchiveTableService archiveTableService;

    /**
     * 通过id查询档案表
     *
     * @param id
     * @return R
     */
    @ApiOperation(value = "通过id查询档案表", httpMethod = "GET")
    @GetMapping("/{id}")
    public R<ArchiveTable> getById(@PathVariable("id") @ApiParam(name = "id", value = "档案表ID", required = true) @NotNull(message = "档案表ID不能为空") Long id) {
        return new R<>(archiveTableService.getTableById(id));
    }

    @ApiOperation(value = "通过storageLocate查询档案表", httpMethod = "GET")
    @GetMapping("/storageLocate/{storageLocate}")
    public R<ArchiveTable> getByStorageLocate(
        @PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表storageLocate", required = true) @NotNull(message = "档案表storageLocate不能为空") String storageLocate)
        throws ArchiveBusinessException {
        return new R<>(archiveTableService.getTableByStorageLocate(storageLocate));
    }

    /**
     * 根据档案门类编码查询档案表
     *
     * @param typeCode
     * @return
     */
    @ApiOperation(value = "根据档案门类编码查询档案表", httpMethod = "GET")
    @GetMapping("/list/typeCode/{typeCode}")
    public R<List<ArchiveTable>> getByCode(
        @PathVariable("typeCode") @ApiParam(name = "typeCode", value = "档案门类编码", required = true, example = "wsda") String typeCode) {
        return new R<List<ArchiveTable>>(archiveTableService.getTableListByTypeCode(typeCode));
    }

	@GetMapping("/table/{archiveTypeCode}/{templateTableId}")
	public R<ArchiveTable> getTableByArchiveTypeCodeAndTemplateTableId(
			@PathVariable("archiveTypeCode") @NotBlank(message = "档案门类编码不能为空") String archiveTypeCode,
			@PathVariable("templateTableId") Long templateTableId) {
		return new R<>(
				archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(archiveTypeCode, templateTableId));
	}
	/**
	 * 根据档案门类编码查询档案表(外部调用)
	 *
	 * @param typeCode
	 * @return
	 */
	@ApiOperation(value = "根据档案门类编码查询档案表", httpMethod = "GET")
	@GetMapping("/inner/list/typeCode-tenantId/{typeCode}/{tenantId}")
	@Inner
	public R<List<ArchiveTable>> getByTypeCodeAndTenantId(
			@PathVariable("typeCode") @ApiParam(name = "typeCode", value = "档案门类编码", required = true, example = "wsda") String typeCode,
			@PathVariable("tenantId") @ApiParam(name = "tenantId", value = "租户ID", required = true, example = "1") Long tenantId) {
		return new R<List<ArchiveTable>>(archiveTableService.getTableListByTypeCodeAndTenantId(typeCode,tenantId));
	}

    /**
     * 查询所有档案数据表
     *
     * @return
     */
    @ApiOperation(value = "查询所有档案表", httpMethod = "GET")
    @GetMapping("/all-list")
    public R<List<ArchiveTable>> getAllArchiveTableList() {
        return new R<List<ArchiveTable>>(archiveTableService.getAllList());
    }

    /**
     * 查询所有档案数据表
     *
     * @return
     */
    @ApiOperation(value = "查询所有要创建索引档案表", httpMethod = SwaggerConstants.GET)
    @GetMapping("/list/index-tables")
    public R<List<ArchiveTable>> getIndexArchiveTableList() {
        return new R<List<ArchiveTable>>(archiveTableService.getIndexArchiveTableList());
    }

    /**
     * 根据表名和字段标签，
     * 获取同档案门类的文件级表名和对应元数据集合
     *
     * @param metadataTagEnglishDTO
     * @return
     * @throws ArchiveBusinessException
     */
    @ApiOperation(value = "根据表名和字段标签，获取同档案门类的文件级表名和对应元数据集合", httpMethod = "POST")
    @PostMapping("/tag-metadata")
    public R<MetadataListDTO> getFileTableAndMetadataList(
        @RequestBody @ApiParam(name = "metadataTagEnglishDTO", value = "元数据标签英文对象", required = true) @Valid MetadataTagEnglishDTO metadataTagEnglishDTO)
        throws ArchiveBusinessException {
        return new R<>(archiveTableService.getFileTableAndMetadataList(metadataTagEnglishDTO));
    }

    @ApiOperation(value = "查询上级表名", httpMethod = "GET")
    @GetMapping("/up/{storageLocate}")
    public R<ArchiveTable> getUpTableByStorageLocate(
        @PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate) {
        return new R<>(archiveTableService.getUpTableByStorageLocate(storageLocate));
    }

    @ApiOperation(value = "查询所有上级表，从下往上排序", httpMethod = "GET")
    @GetMapping("/up/list/{storageLocate}")
    public R<List<ArchiveTable>> getUpTableListByStorageLocate(
        @PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate) {
        return new R<>(archiveTableService.getUpTableListByStorageLocate(storageLocate));
    }

    @ApiOperation(value = "查询所有上级表，从下往上排序(内部调用)", httpMethod = "GET")
    @GetMapping("/up/list/{storageLocate}/{tenantId}")
    @Inner
    public R<List<ArchiveTable>> getUpTableListByStorageLocate(
        @PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate,
        @PathVariable("tenantId") Long tenantId) {
        return new R<>(archiveTableService.getUpTableListByStorageLocateAndTenantId(storageLocate,tenantId));
    }

    @ApiOperation(value = "查询下级表名", httpMethod = "GET")
    @GetMapping("/down/{storageLocate}")
    public R<List<ArchiveTable>> getDownTableByStorageLocate(
        @PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate) {
        return new R<>(archiveTableService.getDownTableByStorageLocate(storageLocate));
    }

	@ApiOperation(value = "查询下级表名", httpMethod = "GET")
	@GetMapping("/down/inner/{storageLocate}/{tenantId}")
	@Inner
	public R<List<ArchiveTable>> getDownTableByStorageLocateInner(
			@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate,
			@PathVariable("tenantId") Long tenantId) {
		return new R<>(archiveTableService.getDownTableByStorageLocateAndTenantId(storageLocate,tenantId));
	}

    @ApiOperation(value = "查询下级表名", httpMethod = "GET")
    @GetMapping("/down/{storageLocate}/{downLayerCode}")
    public R<List<ArchiveTable>> getDownTableByStorageLocateAndDownLayerCode(
        @PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate,
        @PathVariable("downLayerCode") @ApiParam(name = "downLayerCode", value = "下级层级", required = true) @NotBlank(message = "下级层级不能为空") String downLayerCode) {
        return new R<>(archiveTableService.getDownTableByStorageLocateAndDownLayerCode(storageLocate, downLayerCode));
    }

	@ApiOperation(value = "查询下级表名", httpMethod = "GET")
    @GetMapping("/down/{archiveCode}/{templateTableId}/{downLayerCode}")
    public R<List<ArchiveTable>> getDownTableByArchiveTypeCodeAndTemplateTableIdAndDownLayerCode(
        @PathVariable("archiveCode") @ApiParam(name = "archiveCode", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String archiveCode,
        @PathVariable("templateTableId") @ApiParam(name = "templateTableId", value = "表模板ID", required = true) @NotNull(message = "表模板ID不能为空") Long templateTableId,
        @PathVariable("downLayerCode") @ApiParam(name = "downLayerCode", value = "下级层级", required = true) @NotBlank(message = "下级层级不能为空") String downLayerCode) {
        return new R<>(archiveTableService.getDownTableByArchiveTypeCodeAndTemplateTableIdAndDownLayerCode(
            archiveCode, templateTableId, downLayerCode));
    }

    @ApiOperation(value = "查询下级表名", httpMethod = "GET")
    @GetMapping("/down")
    public R<List<ArchiveTable>> getDownTableByTypeCodeAndTemplateTableId(
        @RequestParam("typeCode") @ApiParam(name = "typeCode", value = "档案类型编码", required = true) @NotBlank(message = "档案类型编码不能为空") String typeCode,
        @RequestParam("templateTableId") @ApiParam(name = "templateTableId", value = "表模板id", required = true) @NotNull(message = "表模板id不能为空") Long templateTableId) {
        return new R<>(archiveTableService.getDownTableByTypeCodeAndTemplateTableId(typeCode, templateTableId));
    }

	@ApiOperation(value = "查询下级表名", httpMethod = "GET")
	@GetMapping("/inner/down")
	@Inner
	public R<List<ArchiveTable>> getDownTableByTypeCodeAndTemplateTableIdInner(
			@RequestParam("typeCode") @ApiParam(name = "typeCode", value = "档案类型编码", required = true) @NotBlank(message = "档案类型编码不能为空") String typeCode,
			@RequestParam("templateTableId") @ApiParam(name = "templateTableId", value = "表模板id", required = true) @NotNull(message = "表模板id不能为空") Long templateTableId) {
		return new R<>(archiveTableService.getDownTableByTypeCodeAndTemplateTableId(typeCode, templateTableId));
	}

	@ApiOperation(value = "查询下级表名", httpMethod = "GET")
	@GetMapping("/inner/downByTypeCode")
	@Inner
	public R<List<ArchiveTable>> getDownStorageLocateByTypeCodeInner(
			@RequestParam("typeCode") @ApiParam(name = "typeCode", value = "档案类型编码", required = true) @NotBlank(message = "档案类型编码不能为空") String typeCode) {
		return new R<>(archiveTableService.list(Wrappers.<ArchiveTable>lambdaQuery().eq(ArchiveTable::getArchiveTypeCode, typeCode)));
	}

	/***
	 * 获取物理表名（外部访问）
	 * @param archiveTypeCode
	 * @param templateTableId
	 * @return
	 */
    @Inner
    @Actuator(name="getStorageLocateByArchiveTypeCodeAndTemplateTableId[/storage-locate]")
    @ApiOperation(value = "获取物理表名", httpMethod = SwaggerConstants.GET)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "archiveTypeCode", value = "档案门类编码", required = true, paramType = "string"),
        @ApiImplicitParam(name = "templateTableId", value = "表模板ID", required = true, paramType = "int")
    })
    @GetMapping("/inner/storage-locate/{archiveTypeCode}/{templateTableId}")
	public R<String> getStorageLocateByArchiveTypeCodeAndTemplateTableIdInner(
        @PathVariable("archiveTypeCode") @NotBlank(message = "档案门类编码不能为空") String archiveTypeCode,
        @PathVariable("templateTableId") Long templateTableId) {
        return new R<>(
            archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(archiveTypeCode, templateTableId));
    }

	@Actuator(name="getStorageLocateByArchiveTypeCodeAndTemplateTableId[/storage-locate]")
	@ApiOperation(value = "获取物理表名", httpMethod = SwaggerConstants.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "archiveTypeCode", value = "档案门类编码", required = true, paramType = "string"),
			@ApiImplicitParam(name = "templateTableId", value = "表模板ID", required = true, paramType = "int")
	})
	@GetMapping("/storage-locate/{archiveTypeCode}/{templateTableId}")
	public R<String> getStorageLocateByArchiveTypeCodeAndTemplateTableId(
			@PathVariable("archiveTypeCode") @NotBlank(message = "档案门类编码不能为空") String archiveTypeCode,
			@PathVariable("templateTableId") Long templateTableId) {
		return new R<>(
				archiveTableService.getStorageLocateByArchiveTypeCodeAndTemplateTableId(archiveTypeCode, templateTableId));
	}


	@Inner
    @GetMapping("/inner/table/{archiveTypeCode}/{templateTableId}")
    public R<ArchiveTable> getTableByArchiveTypeCodeAndTemplateTableIdInner(
        @PathVariable("archiveTypeCode") @NotBlank(message = "档案门类编码不能为空") String archiveTypeCode,
        @PathVariable("templateTableId") Long templateTableId) {
        return new R<>(
            archiveTableService.getTableByArchiveTypeCodeAndTemplateTableId(archiveTypeCode, templateTableId));
    }

    @Inner
    @GetMapping("/inner/table/{archiveTypeCode}/{templateTableId}/{tenantId}")
    public R<ArchiveTable> getTableByArchiveTypeCodeAndTemplateTableIdAndTenantId(
        @PathVariable("archiveTypeCode") @NotBlank(message = "档案门类编码不能为空") String archiveTypeCode,
        @PathVariable("templateTableId") Long templateTableId,@PathVariable("tenantId") Long tenantId) {
        return new R<>(
            archiveTableService.getTableByArchiveTypeCodeAndTemplateTableIdAndTenantId(archiveTypeCode, templateTableId,tenantId));
    }

    @ApiOperation(value = "查询上级表名字段", httpMethod = "GET")
    @GetMapping("/up/metadatas/{storageLocate}")
    @Inner
    public R<ArchiveTableSearchDTO> getUpTableMetadatasByStorageLocate(
        @PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate) {
        return new R<>(archiveTableService.getUpTableMetadataByStorageLocate(storageLocate));
    }

    @ApiOperation(value = "查询上级表模板ID", httpMethod = "GET")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "archiveTypeCode", value = "档案门类编码", required = true),
        @ApiImplicitParam(name = "templateTableId", value = "档案表名", required = true)
    })
    @GetMapping("/up/template-table-id")
    public R<?> getUpTemplateTableId(
        @NotBlank(message = "档案表名不能为空") String archiveTypeCode,
        @NotNull(message = "表模板ID不能为空") Long templateTableId) {
        return new R<>(archiveTableService.getUpTemplateTableIdByArchiveTypeCodeAndTemplateTableId(
            archiveTypeCode, templateTableId));
    }

    @ApiOperation(value = "查询档案表对应全文表名及字段", httpMethod = "GET")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "storageLocate", value = "档案表名", required = true)
    })
    @GetMapping("/document/metadatas/{storageLocate}")
    @Inner
    public R<List<ArchiveTableSearchDTO>> getDocumentTableMetadatasByStorageLocate(
        @PathVariable("storageLocate") @NotBlank(message = "档案表名不能为空") String storageLocate) {
        return new R<>(archiveTableService.getDocumentTableMetadataByStorageLocate(storageLocate));
    }

    @ApiOperation(value = "查询档案表表名及字段", httpMethod = "GET")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "storageLocate", value = "档案表名", required = true)
    })
    @GetMapping("/metadatas/{storageLocate}")
    @Inner
    public R<ArchiveTableSearchDTO> getTableMetadatasByStorageLocate(
        @PathVariable("storageLocate") @NotBlank(message = "档案表名不能为空") String storageLocate) {
        return new R<>(archiveTableService.getTableMetadataByStorageLocate(storageLocate));
    }

    @ApiOperation(value = "根据租户id获取所有的档案表，过滤掉 全文，过程信息，签名签章的记录", httpMethod = "GET")
    @GetMapping("/list/{tenantId}")
    public R<List<ArchiveTable>> getArchiveTablesByTenantId(
        @PathVariable("tenantId") @ApiParam(name = "tenantId", value = "租户id", required = true) Long tenantId) {
        return new R<List<ArchiveTable>>(archiveTableService.getArchiveTablesByTenantId(tenantId));
    }

	@ApiOperation(value = "根据租户id获取所有的档案的电子文件表", httpMethod = "GET")
	@GetMapping("/document/list/{tenantId}")
	@Inner
	public R<List<ArchiveTable>> getArchiveDocumentTablesByTenantId(
			@PathVariable("tenantId") @ApiParam(name = "tenantId", value = "租户id", required = true) Long tenantId) {
		return new R<List<ArchiveTable>>(archiveTableService.getArchiveDocumentTablesByTenantId(tenantId));
	}

    @ApiOperation(value = "查询上级表名（内部调用）", httpMethod = "GET", hidden = true)
    @GetMapping("/inner/up/{storageLocate}")
    @Inner
    public R<ArchiveTable> getInnerUpTableByStorageLocate(
        @PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate) {
        return new R<>(archiveTableService.getUpTableByStorageLocate(storageLocate));
    }

    @ApiOperation(value = "查询租户档案门类表（内部调用）", httpMethod = "GET", hidden = true)
    @GetMapping("/inner/index/list")
    @Inner
    public R<List<ArchiveTable>> getInnerIndexTableList() {
        return new R<>(archiveTableService.getIndexArchiveTableList());
    }
	@Inner
	@ApiOperation(value = "查询下级表名(内部)", httpMethod = "GET")
	@GetMapping("/downInner/{storageLocate}/{downLayerCode}")
	public R<List<ArchiveTable>> getDownTableByStorageLocateAndDownLayerCodeInner(
			@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "档案表名", required = true) @NotBlank(message = "档案表名不能为空") String storageLocate,
			@PathVariable("downLayerCode") @ApiParam(name = "downLayerCode", value = "下级层级", required = true) @NotBlank(message = "下级层级不能为空") String downLayerCode) {
		return new R<>(archiveTableService.getDownTableByStorageLocateAndDownLayerCode(storageLocate, downLayerCode));
    }
}
