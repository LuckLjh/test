package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListTag;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteTagListService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteTagListService {

	/**
	 * 获取租户 元数据检索列表信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/tag-list/data/{tenantId}")
	R<List<ArrayList<String>>> getMetadataTagsSearchListInfo(@PathVariable("tenantId") Long tenantId);

	@GetMapping("/tag-list/list/def")
	R<List<DefinedListTag>> getArchiveListdefList();

}
