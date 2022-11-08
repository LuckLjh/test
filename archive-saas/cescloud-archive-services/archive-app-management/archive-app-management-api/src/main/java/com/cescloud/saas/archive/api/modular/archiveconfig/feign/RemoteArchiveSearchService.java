package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchMetadata;
import com.cescloud.saas.archive.service.modular.actuator.annotation.Actuator;
import com.cescloud.saas.archive.service.modular.actuator.annotation.ActuatorType;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteArchiveSearchService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveSearchService {

	@Actuator(name="getArchiveListdefList[/search/list]",type= ActuatorType.Feign)
	@GetMapping("/search/list")
	R<List<DefinedSearchMetadata>> getArchiveListdefList(@RequestParam("storageLocate") String storageLocate,@RequestParam("searchType")Integer searchType,@RequestParam("tagging")Boolean tagging,@RequestParam("moduleId")Long moduleId);

	/**
	 * 获取门类信息-检索列表定义信息
	 * @param tenantId
	 * @return
	 */
	@Actuator(name="getRetrieveDefinitionInfo[/search/data/{tenantId}]",type= ActuatorType.Feign)
	@GetMapping(value = "/search/data/{tenantId}")
	public R<List<ArrayList<String>>> getRetrieveDefinitionInfo(@PathVariable("tenantId") Long tenantId);


	/**
	 * 获取检索字段
	 * @param searchListDTO
	 * @return
	 */
	@Actuator(name="listOfSpecialDefined[/search/special/def]",type= ActuatorType.Feign)
	@GetMapping("/search/special/list/def" )
	R<List<DefinedSearchMetadata>> listOfSpecialDefined(@RequestParam("typeCode") String typeCode,
														@RequestParam("searchType") Integer searchType,@RequestParam("moduleId") Long moduleId,@RequestParam("specialId") Long specialId,@RequestParam("tagging") Boolean tagging) ;
}
