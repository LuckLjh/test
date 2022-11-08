package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSortMetadata;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteArchiveSortService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveSortService {


	@GetMapping("/sort/list")
	R<List<DefinedSortMetadata>> getArchiveSortdefList(@RequestParam("storageLocate") String storageLocate,@RequestParam("moduleId")Long moduleId);

	/**
	 * 获取门类-排序列表信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/sort/data/{tenantId}")
	public R<List<ArrayList<String>>> getSortDefinitionInfo(@PathVariable("tenantId") Long tenantId);


	/**
	 * 获取排序字段
	 * @param storageLocate
	 * @param specialId
	 * @param moduleId
	 * @param tagging
	 * @return
	 */
	@GetMapping("/sort/business/listSpecialOfDefined")
	R<List<DefinedSortMetadata>> listSpecialOfDefined(@RequestParam("storageLocate")String storageLocate,
													  @RequestParam("specialId") Long specialId,
													  @RequestParam("moduleId") Long moduleId,
													  @RequestParam("tagging") Boolean tagging);


	/**
	 * 获取元数据默认排序
	 * @return
	 */
	@GetMapping("/sort/metadata/list/def")
	R<List<DefinedSortMetadata>> listMetadataOfDefined();

}
