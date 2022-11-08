package com.cescloud.saas.archive.service.modular.businessconfig.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.businessconfig.entity.BusinessStyleSetting;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liwei
 */
public interface BusinessStyleSettingService extends IService<BusinessStyleSetting> {

	/**
	 * 根据模板类型获取业务表单
	 *
	 * @param modelType 模板类型
	 * @return BusinessStyleSetting
	 */
	BusinessStyleSetting getBusinessModelDefines(Integer modelType);

	/**
	 * 保存模板表单定义内容
	 *
	 * @param businessStyleSetting 模板表单
	 * @return BusinessStyleSetting
	 */
	BusinessStyleSetting saveBusinessStyleSetting(BusinessStyleSetting businessStyleSetting);

	/**
	 * @param modelType 模板ID
	 * @return BusinessStyleSetting
	 */
	BusinessStyleSetting initForm(Integer modelType);

	/**
	 * 租户初始化--表单初始化
	 * @param templateId 模板id
	 * @param tenantId 租户ID
	 * @return
	 * @throws ArchiveBusinessException
	 */
	R initializeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException;

	/**
	 * 获取获取业务模板管理信息
	 * @param tenantId 租户id
	 * @return
	 */
	List<ArrayList<String>> getBusinessTemplateFormInfo(Long tenantId);
}
