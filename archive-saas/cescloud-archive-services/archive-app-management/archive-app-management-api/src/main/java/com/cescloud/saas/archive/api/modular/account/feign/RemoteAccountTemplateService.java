package com.cescloud.saas.archive.api.modular.account.feign;

import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LS
 * @date 2021/6/4
 */
@FeignClient(contextId = "remoteAccountTemplateService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteAccountTemplateService {

	@GetMapping("/account-template/data/{tenantId}")
	R<List<ArrayList<String>>> getAccountInfo(@PathVariable("tenantId") Long tenantId);

	/**
	 * 根据模板获取角色
	 *
	 * @param templateId
	 * @return
	 */
	@GetMapping("/account-template/accountTemplateRole/{templateId}")
	R getAccountTemplateRole(@PathVariable("templateId") Integer templateId);
}
