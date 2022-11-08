package com.cescloud.saas.archive.service.modular.businessconfig.controller;

import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessModelDefine;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessStyleSetting;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.businessconfig.service.BusinessStyleSettingService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * @author liwei
 */
@Api(value = "businessStyleSetting", tags = "应用管理-业务模板管理：档案利用、鉴定、销毁、移交、归档、编研、保管（损坏、丢失）业务表单设置")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/style-setting")
@Validated
public class BusinessStyleSettingController {

	private final BusinessStyleSettingService businessStyleSettingService;


	@ApiOperation(value = "查询业务模板表单定义内容", httpMethod = "GET")
	@GetMapping
	public R getByModelType(@ApiParam(name = "modelType", value = "模板类型（1：利用表单,3、销毁表单，5、鉴定表单，7、移交表单，9、归档表单，11、编研表单，13、保管表单）", required = true) @NotNull(message = "模板类型不能为空") Integer modelType) {
		BusinessStyleSetting businessStyleSetting = businessStyleSettingService.getBusinessModelDefines(modelType);
		return new R<BusinessStyleSetting>(businessStyleSetting);
	}

	@ApiOperation(value = "保存模板表单定义内容", httpMethod = "POST")
	@SysLog("保存模板表单定义内容")
	@PostMapping
	public R<BusinessStyleSetting> save(@RequestBody @ApiParam(name = "businessStyleSetting", value = "表单定义内容实体", required = true) @Valid BusinessStyleSetting businessStyleSetting) {
		try {
			String businsModel = businsModel(businessStyleSetting.getModelType());
			SysLogContextHolder.setLogTitle(String.format("保存模板表单定义内容-模板类型【%s】-表单定义内容【%s】",businsModel,businessStyleSetting.getFormContent()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(businessStyleSettingService.saveBusinessStyleSetting(businessStyleSetting));
	}

	@ApiOperation(value = "初始化表单页面", httpMethod = "GET")
	@GetMapping("/init-form/{modelType}")
	public R<BusinessStyleSetting> initForm(@ApiParam(name = "modelType", value = "模板类型", required = true)
										   @NotNull(message = "模板类型不能为空") @PathVariable("modelType") Integer modelType) {
		return new R<>(businessStyleSettingService.initForm(modelType));
	}

	@ApiOperation(value = "初始化业务表单模板设置", httpMethod = "POST")
	@PostMapping("/initialize")
	@SysLog("初始化业务表单模板设置")
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId) throws ArchiveBusinessException {
		return businessStyleSettingService.initializeHandle(templateId, tenantId);
	}

	@ApiOperation(value = "获取业务模板管理信息", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取业务模板管理信息")
	public R<List<ArrayList<String>>> getBusinessTemplateFormInfo(@PathVariable("tenantId") Long tenantId) {
		return new R(businessStyleSettingService.getBusinessTemplateFormInfo(tenantId));
	}

	public String businsModel(Integer modelType){
		String situation=modelType.equals(1)?"利用表单":modelType.equals(3)?"销毁表单":modelType.equals(5)?"鉴定表单":modelType.equals(7)?"移交表单":modelType.equals(9)?"归档表单":modelType.equals(11)?"编研表单":"保管表单";
		return situation;
	}
}
