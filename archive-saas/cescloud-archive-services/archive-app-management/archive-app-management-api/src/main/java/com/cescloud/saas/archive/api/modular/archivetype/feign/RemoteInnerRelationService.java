package com.cescloud.saas.archive.api.modular.archivetype.feign;

import com.cescloud.saas.archive.api.modular.archivetype.dto.LayerRelationDTO;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FeignClient(contextId = "remoteInnerRelationService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteInnerRelationService {

	/**
	 * 获取 门类-关联关系数据
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/inner-relation/data/{tenantId}")
	public R<List<ArrayList<String>>> getAssociationDefinitionInfo(@PathVariable("tenantId") Long tenantId);

	@GetMapping("/inner-relation/layer-relation/{storageLocate}/{moduleId}")
	public R<List<LayerRelationDTO>> getLayerRelation(@PathVariable("storageLocate") String storageLocate, @PathVariable("moduleId") Long moduleId);

	@GetMapping("/inner-relation/metadatas/{storageLocate}")
	public R<Map<String, Object>> getMetadataMap(@PathVariable("storageLocate") String storageLocate);
}
