package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author liwei
 */
@FeignClient(contextId = "remoteLinkLayerService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteLinkLayerService {

	@GetMapping("/link-layer/list/root/{tenantId}")
	public R<List<LinkLayer>> getRootsByTenantId(@PathVariable("tenantId") Long tenantId, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 客户端挂接挂接文件规则
	 * @param typeCode
	 * @param moduleId
	 * @return com.cescloud.saas.archive.api.modular.archiveconfig.entity.LinkLayer
	 */
	@GetMapping("/link-layer/batch-attach/typeCode")
	R<LinkLayer> getBatchAttachByTypeCode(@RequestParam("typeCode") String typeCode, @RequestParam("moduleId") Long moduleId);

}
