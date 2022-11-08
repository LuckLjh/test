package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangxuehu
 */
@FeignClient(contextId = "remoteArchiveEditService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveEditService {

	/**
	 * 获取租户表单字段
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping("/edit/data/{tenantId}")
	public R<List<ArrayList<String>>> getFormFieldInfo(@PathVariable("tenantId") Long tenantId);
}
