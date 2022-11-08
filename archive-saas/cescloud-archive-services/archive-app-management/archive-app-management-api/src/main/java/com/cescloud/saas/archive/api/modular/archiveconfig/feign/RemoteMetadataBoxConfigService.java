package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxConfigDTO;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.MetadataBoxRuleDTO;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteMetadataBoxConfigService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteMetadataBoxConfigService {

    @GetMapping("/box-config")
    R<MetadataBoxConfigDTO> getBoxConfig(@RequestParam("storageLocate") String storageLocate,@RequestParam("moduleId") Long moduleId);

    @GetMapping("/box-config/field-info")
    R<MetadataBoxRuleDTO> getBoxFieldInfo(@RequestParam("storageLocate") String storageLocate,@RequestParam("moduleId") Long moduleId);

	@GetMapping("/box-config/data/{tenantId}")
	R<List<ArrayList<String>>> getMetadataBoxConfigInfo(@PathVariable("tenantId") Long tenantId);
}
