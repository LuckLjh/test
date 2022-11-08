package com.cescloud.saas.archive.api.modular.archivetype.feign;

import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteTemplateTypeService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteTemplateTypeService {

	/**
	 * 获取门类-档案类型模板信息
	 *
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/template-type/data/{tenantId}")
	public R<List<ArrayList<String>>> getArchivesTypeTemplateInfor(@PathVariable("tenantId") Long tenantId);
}
