package com.cescloud.saas.archive.api.modular.archivetype.feign;

import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteTemplateMetadataService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteTemplateMetadataService {
	/**
	 * 获取门类-档案类型表模板表字段信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/template-metadata/data/{tenantId}")
	public R<List<ArrayList<String>>> getArchivesTypeTableTemplateMetadataInfor(@PathVariable("tenantId") Long tenantId);
}
