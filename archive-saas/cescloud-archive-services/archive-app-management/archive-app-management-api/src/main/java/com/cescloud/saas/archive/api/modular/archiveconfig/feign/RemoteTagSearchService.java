package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchTag;
import com.cescloud.saas.archive.service.modular.actuator.annotation.Actuator;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteTagSearchService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteTagSearchService {

	/**
	 * 获取租户 元数据检索字段配置信息
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/tag-search/data/{tenantId}")
	R<List<ArrayList<String>>> getMetadataTagsSearchFieldInfo(@PathVariable("tenantId") Long tenantId) ;


	/**
	 * 已定义的列表字段列表
	 * @param from
	 * @return
	 */
	@Actuator(name = "RemoteTagSearchService_getArchiveListDefListByInner")
	@GetMapping(value = "/tag-search/list/defByInner")
	R<List<DefinedSearchTag>> getArchiveListDefListByInner(@RequestHeader(SecurityConstants.FROM) String from) ;

	}
