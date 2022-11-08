package com.cescloud.saas.archive.api.modular.metadata.feign;

import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteMetadataTagBaseService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteMetadataTagBaseService {

	@PostMapping("/tag-base/init")
	R initSystemMetadataTag(@RequestParam("tenantId") Long tenantId);

	/**
	 * 获取租户 元数据标签信息
	 *
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/metadata-tag/data/{tenantId}")
	R<List<ArrayList<String>>> getMetadataTagsInfo(@PathVariable("tenantId") Long tenantId);

	/**
	 * 初始化元数据
	 *
	 * @param templateId 模板id
	 * @param tenantId   租户id
	 * @return
	 * @throws ArchiveBusinessException
	 */
	@PostMapping("/metadata-tag/initialize")
	R initializeMetadata(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId);

	/**
	 * 获取标签列表
	 *
	 * @return
	 */
	@GetMapping(value = "/metadata-tag/list")
	R<List<MetadataTag>> getMetadataTagList();

}
