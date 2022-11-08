package com.cescloud.saas.archive.api.modular.businessconfig.feign;

import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessStyleSetting;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangxuehu
 */
@FeignClient(contextId = "remoteBusinessStyleSettingService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteBusinessStyleSettingService {

	/**
	 * 初始化表单页面
	 *
	 * @param modelType 模板类型
	 * @return R
	 */
	@RequestMapping(value = "/style-setting/init-form/{modelType}", method = RequestMethod.GET)
	R<BusinessStyleSetting> initForm(@PathVariable("modelType") Integer modelType);

	/**
	 * 程序初始化表单页面
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 */
	@RequestMapping(value = "/style-setting/initialize", method = RequestMethod.POST)
	R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId);

	/**
	 * 获取租户业务模板管理信息
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/style-setting/data/{tenantId}")
	public R<List<ArrayList<String>>> getBusinessTemplateFormInfo(@PathVariable("tenantId") Long tenantId);
}
