package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "remoteArchiveColumnRuleService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveColumnRuleService {

	@RequestMapping(value = "/column-rule/list/def" , method = RequestMethod.GET)
	R<List<DefinedColumnRuleMetadata>> getColumnRuleDefList(@RequestParam("storageLocate") String storageLocate, @RequestParam("metadataSourceEnglish") String metadataSourceEnglish);

}
