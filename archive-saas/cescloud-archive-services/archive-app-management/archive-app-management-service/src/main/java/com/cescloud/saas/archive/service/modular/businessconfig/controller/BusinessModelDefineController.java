package com.cescloud.saas.archive.service.modular.businessconfig.controller;

import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateType;
import com.cescloud.saas.archive.api.modular.businessconfig.dto.BusinessModelDefineDTO;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessModelDefineService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author liwei
 */
@Api(value = "businessModelDefine", tags = "应用管理-业务模板管理：档案利用、鉴定、销毁、移交、归档、编研、保管（损坏、丢失）业务")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/model-define")
@Validated
public class BusinessModelDefineController {

	private final BusinessModelDefineService businessModelDefineService;


	@ApiOperation(value = "根据模板类型查询")
	@GetMapping("/list")
	public R<List<BusinessModelDefine>> getBusinessModelDefines(@ApiParam(name = "modelType", value = "模板类型（1：利用表单,3、销毁表单，5、鉴定表单，7、移交表单，9、归档表单，11、编研表单，13、保管表单）", example = "1") @NotNull(message = "模板类型不能为空") Integer modelType,
																@ApiParam(name = "keyword", value = "检索关键字") String keyword) {
		return new R<>(businessModelDefineService.getBusinessModelDefines(modelType, keyword));
	}

	@ApiOperation(value = "根据模板类型查询")
	@GetMapping("/all")
	public R<List<BusinessModelDefine>> getBusinessModelDefinesAll(@ApiParam(name = "modelType", value = "模板类型（1：利用表单,3、销毁表单，5、鉴定表单，7、移交表单，9、归档表单，11、编研表单，13、保管表单）", example = "1") @NotNull(message = "模板类型不能为空") Integer modelType) {
		return new R<>(businessModelDefineService.getBusinessModelDefinesAll(modelType));
	}

	@ApiOperation(value = "新增业务模板定义")
	@SysLog("新增业务模板定义")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "businessModelDefineDTO", value = "业务模板定义DTO") @Valid BusinessModelDefineDTO businessModelDefineDTO) throws ArchiveBusinessException {
		try {
			String businsModel = businsModel(businessModelDefineDTO.getModelType());
			SysLogContextHolder.setLogTitle(String.format("新增业务模板定义-模板类型【%s】",businsModel));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(businessModelDefineService.saveBusinessModelDefine(businessModelDefineDTO));
	}

	@ApiOperation(value = "修改业务模板定义")
	@SysLog("修改业务模板定义")
	@PutMapping
	public R update(@RequestBody @ApiParam(name = "businessModelDefineDTO", value = "业务模板定义DTO") @Valid BusinessModelDefineDTO businessModelDefineDTO) throws ArchiveBusinessException {
		try {
			String businsModel = businsModel(businessModelDefineDTO.getModelType());
			SysLogContextHolder.setLogTitle(String.format("修改业务模板定义-模板类型【%s】",businsModel));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(businessModelDefineService.updateBusinessModelDefine(businessModelDefineDTO));
	}

	@ApiOperation(value = "删除业务模板定义")
	@SysLog("删除业务模板定义")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable @ApiParam(value = "传入主键id", name = "id", required = true) @NotNull(message = "id不能为空") Long id) throws ArchiveBusinessException {
		try {
			BusinessModelDefine businessModel = businessModelDefineService.getById(id);
			String businsModel = businsModel(businessModel.getModelType());
			SysLogContextHolder.setLogTitle(String.format("删除业务模板定义-模板类型【%s】",businsModel));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(businessModelDefineService.deleteBusinessModelDefineById(id));
	}

	/**
	 * 启用租户时，新建业务表（利用表、归档表......）
	 * @param tenantId 租户id
	 * @return
	 */
	@ApiOperation(value = "新建业务表")
	@GetMapping("/create-table/{tenantId}")
	public R createTable(@PathVariable("tenantId") @ApiParam(value = "租户id", name = "tenantId", required = true) @NotNull(message = "租户id不能为空") Long tenantId) throws ArchiveBusinessException {
		return new R(businessModelDefineService.createTable(tenantId));
	}


	public String businsModel(Integer modelType){
		String situation=modelType.equals(1)?"利用表单":modelType.equals(3)?"销毁表单":modelType.equals(5)?"鉴定表单":modelType.equals(7)?"移交表单":modelType.equals(9)?"归档表单":modelType.equals(11)?"编研表单":"保管表单";
		return situation;
	}
}
