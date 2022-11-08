package com.cescloud.saas.archive.api.modular.archivetype.feign;

import com.cescloud.saas.archive.api.modular.archivetype.dto.AutovalueDTO;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteMetadataAutovalueService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteMetadataAutovalueService {

	/**
	 * 根据档案类型编码和档案层级获取字段规则
	 * @param typeCode 档案类型编码
	 * @param templateTableId    档案模板id
	 * @param moduleId 模块id
	 * @return 根据档案类型编码和档案层级获取字段规则
	 */
	@RequestMapping(value = "/metadata-autovalue/list/{moduleId}/{typeCode}/{templateTableId}",method = RequestMethod.GET)
	R<List<AutovalueDTO>> getAutovaluesByCodeAndLayer(@PathVariable("typeCode") String typeCode,
	                                                  @PathVariable("templateTableId") Long templateTableId,
	                                                  @PathVariable("moduleId") Long moduleId);

	/**
	 * 获取档案门类数据规则信息
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/metadata-autovalue/data/{tenantId}")
	public R<List<ArrayList<String>>> getDataRuleDefinitionInfo(@PathVariable("tenantId") Long tenantId);

}
