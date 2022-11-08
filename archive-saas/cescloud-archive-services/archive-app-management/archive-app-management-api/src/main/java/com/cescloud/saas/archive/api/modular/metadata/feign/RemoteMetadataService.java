package com.cescloud.saas.archive.api.modular.metadata.feign;

import com.cescloud.saas.archive.api.modular.metadata.dto.MetadataDTO;
import com.cescloud.saas.archive.api.modular.metadata.entity.Metadata;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteMetadataService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteMetadataService {

	@RequestMapping(value = "/metadata/{storageLocate}/{metadataEnglish}", method = RequestMethod.GET)
	R<Metadata> getByStorageLocateAndMetadataEnglish(@PathVariable("storageLocate") String storageLocate, @PathVariable("metadataEnglish") String metadataEnglish);

	@RequestMapping(value = "/metadata/{id}", method = RequestMethod.GET)
	R<Metadata> getById(@PathVariable("id") Long id);

	/**
	 * 根据表名获取元数据信息（包括标签信息）
	 *
	 * @param storageLocate
	 * @return
	 */
	@GetMapping("/metadata/metadata-dto/{storageLocate}")
	R<List<MetadataDTO>> getMetadataDTOList(@PathVariable("storageLocate") String storageLocate);

	/**
	 * 获取元数据列表
	 *
	 * @param storageLocate
	 * @return
	 */
	@GetMapping("/metadata/list")
	R<List<Metadata>> getMetadataList(@RequestParam("storageLocate") String storageLocate);

	/**
	 * 获取元数据列表(外部调用)
	 *
	 * @param storageLocate
	 * @return
	 */
	@GetMapping("/metadata/inner/list")
	R<List<Metadata>> getMetadataListInner(@RequestParam("storageLocate") String storageLocate, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取所有元数据列表
	 *
	 * @param storageLocate
	 * @return
	 */
	@GetMapping("/metadata/list-all-metadata")
	R<List<Metadata>> getAllMetadataList(@RequestParam("storageLocate") String storageLocate);

	/**
	 * 获取所有元数据列表(外部调用)
	 *
	 * @param storageLocate
	 * @return
	 */
	@GetMapping("/metadata/inner/list-all-metadata")
	R<List<Metadata>> getAllMetadataListInner(@RequestParam("storageLocate") String storageLocate, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取门类字段信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/metadata/data/{tenantId}")
	public R<List<ArrayList<String>>> getFieldManagementInfo(@PathVariable("tenantId") Long tenantId);

	/**
	 * 内部调用 根据storageLocate 获取所有元数据列表
	 *
	 * @param storageLocate
	 * @param from
	 * @return
	 */
	@GetMapping("/metadata/inner-list-all-metadata")
	R<List<Metadata>> getInnerAllMetadataList(@RequestParam("storageLocate") String storageLocate, @RequestHeader(SecurityConstants.FROM) String from);

}
