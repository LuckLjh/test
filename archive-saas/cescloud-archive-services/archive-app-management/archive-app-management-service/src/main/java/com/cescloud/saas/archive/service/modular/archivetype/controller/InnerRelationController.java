
package com.cescloud.saas.archive.service.modular.archivetype.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.ColumnComputeRuleDTO;
import com.cescloud.saas.archive.api.modular.archivedict.dto.CopyPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationOutDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationPostDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.InnerRelationPutDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.LayerRelationDTO;
import com.cescloud.saas.archive.api.modular.archivetype.entity.InnerRelation;
import com.cescloud.saas.archive.common.constants.FormStatusEnum;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archivetype.service.InnerRelationService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


/**
 * 档案类型关联
 *
 * @author xieanzhu
 * @date 2019-04-16 14:13:01
 */
@Api(value = "innerRelation", tags = "应用管理-档案门类管理：关联关系定义")
@Slf4j
@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/inner-relation")
public class InnerRelationController {

	private final InnerRelationService innerRelationService;

	/**
	 * 根据tableId获取list
	 *
	 * @param storageLocate 表名 比如T_1_KJ_KJDA_V
	 * @return
	 */
	@ApiOperation(value = "获取档案类型关联列表", httpMethod = "GET")
	@GetMapping("/list/{storageLocate}/{moduleId}")
	public R<List<InnerRelationOutDTO>> getInnerRelationList(@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
															 @PathVariable("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) throws ArchiveBusinessException {
		return new R<>(innerRelationService.listByArchiveTableName(storageLocate,moduleId));
	}

	/**
	 * 通过id查询档案类型关联
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "根据ID查询档案类型关联", httpMethod = "GET")
	@GetMapping("/{id}")
	public R<InnerRelation> getById(@PathVariable("id") @ApiParam(name = "id", value = "档案类型关联ID", required = true) @NotNull(message = "档案类型关联ID不能为空") Long id) {
		return new R<InnerRelation>(innerRelationService.getInnerRelationById(id));
	}

	/**
	 * 新增档案类型关联
	 *
	 * @param innerRelationPostDTO 档案类型关联
	 * @return R
	 */
	@ApiOperation(value = "新增关联关系规则", httpMethod = "POST")
	@SysLog("新增档案类型关联")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "关联关系对象", value = "传入json格式", required = true) @Valid InnerRelationPostDTO innerRelationPostDTO) throws ArchiveBusinessException {
		return innerRelationService.save(innerRelationPostDTO);
	}

	/**
	 * 修改档案类型关联
	 *
	 * @param innerRelationPutDTO 档案类型关联
	 * @return R
	 */
	@ApiOperation(value = "修改关联关系规则", httpMethod = "PUT")
	@SysLog("修改档案类型关联")
	@PutMapping
	public R updateById(@RequestBody @ApiParam(name = "关联关系对象", value = "传入json格式", required = true) @Valid InnerRelationPutDTO innerRelationPutDTO) throws ArchiveBusinessException {
		return innerRelationService.update(innerRelationPutDTO);
	}

	/**
	 * 通过id删除档案类型关联
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "根据ID删除关联关系规则", httpMethod = "DELETE")
	@SysLog("删除档案类型关联")
	@DeleteMapping("/remove")
	public R removeById(@RequestParam("id") @ApiParam(name = "id", value = "档案类型关联ID", required = true) @NotNull(message = "档案类型关联ID不能为空") Long id,
						@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
						@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId) {
		return new R<>(innerRelationService.removeInnerRelationById(id,storageLocate,moduleId));
	}


	/**
	 * 获取关联关系元数据列表（包括关联和被关联表元数据）
	 *
	 * @param storageLocate 表名 比如T_1_KJ_KJDA_V
	 * @return
	 */
	@ApiOperation(value = "获取关联关系元数据列表（包括关联和被关联表元数据）", httpMethod = "GET")
	@GetMapping("/metadatas/{storageLocate}")
	public R<Map<String, Object>> getMetadataMap(@PathVariable("storageLocate") @ApiParam(name = "storageLocate", value = "关联表名", required = true) @NotBlank(message = "关联表名不能为空") String storageLocate) throws ArchiveBusinessException {
		return new R<>(innerRelationService.getMetadataMap(storageLocate));
	}

	@ApiIgnore
	@GetMapping("/compute-rule/{storageLocate}")
	public R<List<ColumnComputeRuleDTO>> getComputeRuleByStorageLocate(@PathVariable @ApiParam(name = "storageLocate", value = "关联表名", required = true) @NotBlank(message = "关联表名不能为空") String storageLocate) throws ArchiveBusinessException {
		return new R<>(innerRelationService.getComputeRuleByStorageLocate(storageLocate, FormStatusEnum.ADD));
	}

	@ApiOperation(value = "获取档案门类关联关系信息", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取档案门类关联关系信息")
	public R getAssociationDefinitionInfo(@PathVariable("tenantId") Long tenantId) throws ArchiveBusinessException {
		return new R(innerRelationService.getAssociationDefinitionInfo(tenantId));
	}

	@SysLog("清除关联关系定义配置")
	@ApiOperation(value = "清除关联关系定义配置", httpMethod = SwaggerConstants.DELETE)
	@DeleteMapping("/remove-config")
	public R remove(@RequestParam("storageLocate")@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
					@RequestParam("moduleId") @ApiParam(name = "moduleId", value = "模块id", required = true) @NotNull(message = "模块id不能为空") Long moduleId ){
		return new R(innerRelationService.removeByModuleId(storageLocate,moduleId));
	}

	@SysLog("复制到其他模块")
	@ApiOperation(value = "复制到其他模块接口", httpMethod = SwaggerConstants.POST)
	@PostMapping("/copy")
	public R copyToModule(@Valid @RequestBody @ApiParam(name = "copyPostDTO",value = "复制到其他模块参数DTO") CopyPostDTO copyPostDTO) {
		return innerRelationService.copy(copyPostDTO);
	}

	@ApiOperation(value = "获取相等的关联关系", httpMethod = SwaggerConstants.GET)
	@GetMapping("/layer-relation/{storageLocate}/{moduleId}")
	public R<List<LayerRelationDTO>> getLayerRelation(@PathVariable("storageLocate") String storageLocate, @PathVariable("moduleId") Long moduleId) {
		return new R<>(innerRelationService.getLayerRelation(storageLocate, moduleId));
	}

}
