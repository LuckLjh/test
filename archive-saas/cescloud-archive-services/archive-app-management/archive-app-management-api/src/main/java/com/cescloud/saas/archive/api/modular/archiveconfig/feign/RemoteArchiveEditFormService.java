package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.AutovalueRuleDTO;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteArchiveEditFormService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveEditFormService {

	/**
	 * 获取档案门类表单定义信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/edit-form/data/{tenantId}")
	public R<List<ArrayList<String>>> getFormDefinitionInfo(@PathVariable("tenantId") Long tenantId);

	@GetMapping("/edit-form/column/autovalue-rule")
	public R<AutovalueRuleDTO> getRuleColumn(@RequestParam("typeCode") String typeCode,
											 @RequestParam("templateTableId") Long templateTableId,
											 @RequestParam("moduleId") Long moduleId);

}
