
package com.cescloud.saas.archive.service.modular.metadata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataDTO;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


/**
 * 档案元数据
 *
 * @author liudong1
 * @date 2019-03-28 09:42:53
 */
@Api(value = "metadata", tags = "档案元数据管理")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    /**
     * 分页查询元数据
     *
     * @return
     */
    @ApiOperation(value = "分页查询元数据", httpMethod = "GET")
    @GetMapping("/page")
    public R getMetadataPage(@ApiParam(name = "page", value = "分页对象", required = true) Page page,
                             @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
                             @ApiParam(name = "keyword", value = "检索关键字", required = false) String keyword) {
        return new R<>(metadataService.getPage(page, storageLocate, keyword));
    }

    /**
     * 根据表名获取所有业务元数据
     *
     * @param storageLocate
     * @return
     */
    @ApiOperation(value = "根据表名获取所有业务元数据", httpMethod = "GET")
    @GetMapping("/list")
	public R<List<Metadata>> getMetadataList(@ApiParam(name = "storageLocate", value = "档案存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate) {
        return new R<List<Metadata>>(metadataService.listByStorageLocate(storageLocate));
    }

	@ApiOperation(value = "根据表名获取所有业务元数据(内部调用)", httpMethod = "GET")
	@GetMapping("/inner/list")
	@Inner
	public R<List<Metadata>> getMetadataListInner(@ApiParam(name = "storageLocate", value = "档案存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate) {
		return new R<List<Metadata>>(metadataService.allByStorageLocate(storageLocate));
	}

    /**
     * 根据表名获取所有元数据（业务+系统）
     *
     * @param storageLocate
     * @return
     */
    @ApiOperation(value = "根据表名获取所有元数据（业务+系统）", httpMethod = "GET")
    @GetMapping("/list-all-metadata")
    public R<List<Metadata>> getAllMetadataList(@ApiParam(name = "storageLocate", value = "档案存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate) {
        return new R<List<Metadata>>(metadataService.listAllByStorageLocate(storageLocate));
    }

	/**
	 * 根据表名获取所有元数据（业务+系统）
	 *
	 * @param storageLocate
	 * @return
	 */
	@ApiOperation(value = "根据表名获取所有元数据（业务+系统）(外部调用)", httpMethod = "GET")
	@GetMapping("/inner/list-all-metadata")
	@Inner
	public R<List<Metadata>> getAllMetadataListInner(@ApiParam(name = "storageLocate", value = "档案存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate) {
		return new R<List<Metadata>>(metadataService.listAllByStorageLocate(storageLocate));
	}

    /**
     * 通过id查询档案元数据
     *
     * @param id
     * @return R
     */
    @ApiOperation(value = "根据ID获取元数据", httpMethod = "GET")
    @GetMapping("/{id}")
    public R<Metadata> getById(@PathVariable("id") @ApiParam(name = "id", value = "元数据ID", required = true) @NotNull(message = "元数据id不能为空") Long id) {
        return new R<>(metadataService.getMetadataById(id));
    }

    /**
     * 新增档案元数据
     *
     * @param metadata 档案元数据
     * @return R
     */
    @ApiOperation(value = "新增档案元数据", httpMethod = "POST")
    @SysLog("新增档案元数据")
    @PostMapping
    public R<Metadata> save(@Valid @RequestBody @ApiParam(name = "metadata", value = "元数据实体", required = true) Metadata metadata)
            throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增档案元数据-元数据中文名称【%s】", metadata.getMetadataChinese()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(metadataService.saveMetadata(metadata));
    }

    /**
     * 修改档案元数据
     *
     * @param metadata 档案元数据
     * @return R
     */
    @ApiOperation(value = "修改档案元数据", httpMethod = "PUT")
    @SysLog("修改档案元数据")
    @PutMapping
    public R updateById(@Valid @RequestBody @ApiParam(name = "metadata", value = "元数据实体", required = true) Metadata metadata)
            throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改档案元数据-元数据中文名称【%s】", metadata.getMetadataChinese()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(metadataService.updateMetadata(metadata));
    }

    /**
     * 通过id删除档案元数据
     *
     * @param id
     * @return R
     */
    @ApiOperation(value = "删除档案元数据", httpMethod = "DELETE")
    @SysLog("删除档案元数据")
    @DeleteMapping("/{id}")
    public R removeById(@PathVariable("id") @ApiParam(name = "id", value = "元数据ID", required = true) @NotNull(message = "元数据ID不能为空") Long id)
            throws ArchiveBusinessException {
		try {
			Metadata metadataById = metadataService.getMetadataById(id);
			SysLogContextHolder.setLogTitle(String.format("删除档案元数据-元数据中文名称【%s】", metadataById.getMetadataChinese()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(metadataService.removeMetadata(id));
    }

    @ApiOperation(value = "根据表名和英文字段名获取元字段", httpMethod = "GET")
    @GetMapping("/{storageLocate}/{metadataEnglish}")
    public R<Metadata> getByStorageLocateAndMetadataEnglish(@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
                                                            @PathVariable("metadataEnglish") @ApiParam(name = "metadataEnglish", value = "字段英文名", required = true) @NotBlank(message = "字段英文名不能为空") String metadataEnglish) throws ArchiveBusinessException {
        return new R<Metadata>(metadataService.getByStorageLocateAndMetadataEnglish(storageLocate, metadataEnglish));
    }

    /**
     * 根据表名获取元数据信息（包括标签信息）
     *
     * @param storageLocate
     * @return
     */
    @ApiOperation(value = "根据表名获取元数据信息（包括标签信息）", httpMethod = "GET")
    @GetMapping("/metadata-dto/{storageLocate}")
    public R<List<MetadataDTO>> getMetadataDTOList(@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate) {
        return new R<>(metadataService.getMetadataDTOList(storageLocate));
    }

    /**
     * @param typeCode:        档案类型code
     * @param templateTableId: 模板ID
     * @Description: 根据档案类型code和档案层次获取元数据列表
     * @return: com.cescloud.saas.archive.service.modular.common.core.util.R
     **/
    @ApiOperation(value = "根据档案类型编码和模板ID获取元数据列表", httpMethod = "GET")
    @GetMapping("/list/{typeCode}/{templateTableId}")
    public R<List<Metadata>> list(@PathVariable("typeCode") @ApiParam(name = "typeCode", value = "档案门类编码", required = true) @NotBlank(message = "档案门类编码不能为空") String typeCode,
                                  @PathVariable("templateTableId") @ApiParam(name = "templateTableId", value = "模板iD", required = true) @NotNull(message = "模板ID不能为空") Long templateTableId) throws ArchiveBusinessException {
        return new R<>(metadataService.listByTypeCodeAndTemplateTableId(typeCode, templateTableId));
    }

    @ApiOperation(value = "获取门类-档案门类字段信息", httpMethod = SwaggerConstants.GET)
    @GetMapping(value = "/data/{tenantId}")
    @SysLog("获取门类-档案门类信息")
    public R<List<ArrayList<String>>> getFieldManagementInfo(@PathVariable("tenantId") Long tenantId) {
        return new R(metadataService.getFieldManagementInfo(tenantId));
    }

    @ApiOperation(value = "校验表单输入字符是否重复", httpMethod = SwaggerConstants.GET)
    @GetMapping("/unique-check/form")
    public R verifyFormField(@ApiParam(name = "typeCode", value = "档案类型编码", required = true) String typeCode,
                             @ApiParam(name = "templateTableId", value = "表模板id", required = true) Long templateTableId,
                             @ApiParam(name = "fieldName", value = "字段名称", required = true) String fieldName,
                             @ApiParam(name = "value", value = "字段值", required = true) String value,
                             @ApiParam(name = "id", value = "条目id,没有不传", required = true) Long id) {
        return metadataService.verifyFormField(typeCode, templateTableId, fieldName, value, id);
    }

    @ApiOperation(value = "内部调用根据表名获取所有元数据（业务+系统）", httpMethod = "GET")
    @GetMapping("/inner-list-all-metadata")
    @Inner
    public R<List<Metadata>> getInnerAllMetadataList(@ApiParam(name = "storageLocate", value = "档案存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate) {
        return new R<List<Metadata>>(metadataService.listAllByStorageLocate(storageLocate));
    }
}
