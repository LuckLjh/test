
package com.cescloud.saas.archive.service.modular.businessconfig.controller;

import com.cescloud.saas.archive.api.modular.businessconfig.dto.DynamicBusinessModelDefineDTO;
import com.cescloud.saas.archive.service.modular.businessconfig.service.DynamicModelDefineService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 动态表字段对应
 *
 * @author 王谷华
 * @date 2021-04-01 16:39:49
 */
@Api(value = "DynamicModelDefine", description = "动态表字段对应")
@RestController
@AllArgsConstructor
@RequestMapping("/dynamic-model-define")
public class DynamicModelDefineController {

	private DynamicModelDefineService dynamicModelDefineService;

	@ApiOperation(value = "根据模板类型查询")
	@GetMapping("/list")
	public R<List<DynamicBusinessModelDefineDTO>> getBusinessModelDefines(
			@ApiParam(name = "modelType",value = "模板类型（1：利用表单,3、销毁表单，5、鉴定表单，7、移交表单，9、归档表单，11、编研表单，13、保管表单）", example = "1") @NotNull(message = "模板类型不能为空") Integer modelType,
			@ApiParam(name = "modelCode",value = "模板编码", example = "abc") @NotNull(message = "模板类型不能为空") String modelCode,
			@ApiParam(name = "keyword",value = "检索关键字") String keyword) {
		return new R(dynamicModelDefineService.getBusinessModelDefinesByDynamic( modelType, null,  modelCode,  keyword));
	}

	@ApiOperation(value = "根据模板类型查询")
	@GetMapping("/all/list")
	public R<List<DynamicBusinessModelDefineDTO>> getAllBusinessModelDefines(
			@ApiParam(name = "modelType", value = "模板类型（1：利用表单,3、销毁表单，5、鉴定表单，7、移交表单，9、归档表单，11、编研表单，13、保管表单）", example = "1") @NotNull(message = "模板类型不能为空") Integer modelType,
			@ApiParam(name = "modelCode", value = "模板编码", example = "abc") @NotNull(message = "模板类型不能为空") String modelCode) {
		return new R(dynamicModelDefineService.getAllBusinessModelDefines(modelType, modelCode));
	}

	@ApiOperation(value = "根据模板类型查询字段")
	@GetMapping("/getDynamicFields")
	public R<List<String>> getDynamicFields(
			@ApiParam(name = "modelType",value = "模板类型（1：利用表单,3、销毁表单，5、鉴定表单，7、移交表单，9、归档表单，11、编研表单，13、保管表单）", example = "1") @NotNull(message = "模板类型不能为空") Integer modelType,
			@ApiParam(name = "modelCode",value = "模板编码", example = "abc") @NotNull(message = "模板类型不能为空") String modelCode,
			@ApiParam(name = "keyword",value = "检索关键字") String keyword) {
		return new R(dynamicModelDefineService.getDynamicFields( modelType, null,  modelCode,  keyword));
	}


	/**
	 * 创建分类 同时创建表
	 *
	 * @param code 分类code
	 * @param modelType 模板类型
	 * @param fondsCode fondsCode
	 * @return
	 */
	@ApiOperation(value = "新建动态表")
	@GetMapping("/create-table/{code}/{modelType}/{fondsCode}")
	public R createTable(@PathVariable("code") @ApiParam(value = "分类code", name = "code", required = true) @NotNull(message = "分类code不能为空") String code,
						 @PathVariable("modelType") @ApiParam(value = "模板类型", name = "modelType", required = true) @NotNull(message = "模板类型不能为空") Integer modelType,
						 @PathVariable("fondsCode") @ApiParam(value = "全宗编号", name = "fondsCode", required = true) @NotNull(message = "全宗编号不能为空") String fondsCode
						 ) throws ArchiveBusinessException {
		return new R(dynamicModelDefineService.createTable(code,modelType,fondsCode));
	}


	/**
	 * 删除分类 同时创建表
	 *
	 * @param code 分类code
	 * @param modelType 模板类型
	 * @return
	 */
	@ApiOperation(value = "删除动态表")
	@GetMapping("/drop-table/{code}/{modelType}")
	public R dropTable(@PathVariable("code") @ApiParam(value = "分类code", name = "code", required = true) @NotNull(message = "分类code不能为空") String code,
						 @PathVariable("modelType") @ApiParam(value = "模板类型", name = "modelType", required = true) @NotNull(message = "模板类型不能为空") Integer modelType
	) throws ArchiveBusinessException {
		return new R(dynamicModelDefineService.dropTable(code,modelType));
	}


	@ApiOperation(value = "新增业务模板定义")
	@SysLog("新增业务模板定义")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "businessModelDefineDTO",value = "业务模板定义DTO") @Valid DynamicBusinessModelDefineDTO dynamicBusinessModelDefineDTO) throws ArchiveBusinessException {
		return new R<>(dynamicModelDefineService.saveDynamicModelDefine(dynamicBusinessModelDefineDTO));
	}

	@ApiOperation(value = "修改业务模板定义")
	@SysLog("修改业务模板定义")
	@PutMapping
	public R update(@RequestBody @ApiParam(name = "businessModelDefineDTO", value = "业务模板定义DTO") @Valid DynamicBusinessModelDefineDTO dynamicBusinessModelDefineDTO) throws ArchiveBusinessException {
		return new R<>(dynamicModelDefineService.updateDynamicModelDefine(dynamicBusinessModelDefineDTO));
	}

	@ApiOperation(value = "删除业务模板定义")
	@SysLog("删除业务模板定义")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable @ApiParam(value = "传入主键id", name = "id", required = true) @NotNull(message = "id不能为空") Long id) throws ArchiveBusinessException {
		return new R<>(dynamicModelDefineService.deleteDynamicModelDefineById(id));
	}

}
