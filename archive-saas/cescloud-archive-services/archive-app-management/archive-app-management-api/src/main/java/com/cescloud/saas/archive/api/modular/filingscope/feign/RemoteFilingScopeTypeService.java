package com.cescloud.saas.archive.api.modular.filingscope.feign;

import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScopeType;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(contextId = "remoteFilingScopeTypeService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteFilingScopeTypeService {
	/**
	 * 根据主键id 获取归档范围定义信息
	 * @param id
	 * @return
	 */
	@GetMapping("/filing-scope-type/{id}")
	R<FilingScopeType> getById(@PathVariable("id") Long id) ;

	@GetMapping(value = "/filing-scope-type/data/{tenantId}")
	R getFilingScopeTypeInfo(@PathVariable("tenantId") Long tenantId);
}
