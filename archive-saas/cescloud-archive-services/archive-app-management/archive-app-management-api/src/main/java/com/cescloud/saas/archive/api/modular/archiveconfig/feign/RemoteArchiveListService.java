package com.cescloud.saas.archive.api.modular.archiveconfig.feign;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListMetadata;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@FeignClient(contextId = "remoteArchiveListService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveListService {

	@GetMapping("/list/list/def")
	R<List<DefinedListMetadata>> listOfDef(@RequestParam("storageLocate") String storageLocate,@RequestParam("moduleId") Long moduleId,@RequestParam("tagging") Boolean tagging);

	/**
	 * 获取 门类信息-列表定义信息
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/list/data/{tenantId}")
	public R<List<ArrayList<String>>> getListDefinitionInfo(@PathVariable("tenantId") Long tenantId);


	/**
	 * 已定义的专题列表字段列表
	 * @param storageLocate
	 * @param specialId
	 * @param moduleId
	 * @param tagging
	 * @return
	 */
	@GetMapping("/list/list/specialDef")
	R<List<DefinedListMetadata>> listSpecialOfDefined(@RequestParam("storageLocate")  String storageLocate,
		@RequestParam("specialId")  Long specialId,@RequestParam("moduleId")  Long moduleId,@RequestParam("tagging") Boolean tagging);


	/**
	 * 保存默认配置
	 * @param storageLocate
	 * @param specialId
	 * @param moduleCode
	 * @return
	 */
	@GetMapping("/list/list/saveDefaultSpecialDefined")
	R<List<DefinedListMetadata>> saveDefaultSpecialDefined(@RequestParam("storageLocate")  String storageLocate,
													  @RequestParam("specialId")  Long specialId,@RequestParam("moduleCode")  String moduleCode);


	/**
	 * 清空专题
	 * @param storageLocate
	 * @return
	 */
	@GetMapping("/list/list/removeSpecialDefinedBySpecialId")
	R<Boolean> removeSpecialDefinedBySpecialId(@RequestParam("storageLocate")  String storageLocate);
}
