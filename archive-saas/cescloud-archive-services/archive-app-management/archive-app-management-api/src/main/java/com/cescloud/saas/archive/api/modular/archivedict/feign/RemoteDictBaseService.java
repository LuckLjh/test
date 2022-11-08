package com.cescloud.saas.archive.api.modular.archivedict.feign;

import com.cescloud.saas.archive.api.modular.archivedict.entity.DictBase;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.localcache.LocalCacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(contextId = "remoteDictBaseService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteDictBaseService {

	@GetMapping("/dict-base/sys")
	public R<List<DictBase>> getSystemDict();

	@PostMapping("/dict-base/init")
	public R initSystemDict(@RequestParam("tenantId") Long tenantId);

	@RequestMapping(value = "/dict/initialize", method = RequestMethod.POST)
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam("tenantId") Long tenantId);
}
