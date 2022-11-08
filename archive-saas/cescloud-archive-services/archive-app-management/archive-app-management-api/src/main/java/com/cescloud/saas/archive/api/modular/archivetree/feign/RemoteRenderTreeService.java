package com.cescloud.saas.archive.api.modular.archivetree.feign;

import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "remoteRenderTreeService", value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteRenderTreeService {
	/**
	 * 渲染档案树定义
	 *
	 * @param id
	 * @param menuId
	 * @param filter
	 * @param path
	 * @param fondsCode
	 * @return
	 */
	@GetMapping(value = "/render-tree/tree")
	R<List<RenderTreeDTO>> tree(@RequestParam(value = "id") String id,
								@RequestParam(value = "menuId", required = false) Long menuId,
								@RequestParam(value = "filter", required = false) String filter,
								@RequestParam(value = "path", required = false) String path,
								@RequestParam(value = "fondsCode") String fondsCode);

	@GetMapping(value = "/render-tree/treeInner")
	R<List<RenderTreeDTO>> treeInner(@RequestParam(value = "id") String id,
									 @RequestParam(value = "menuId", required = false) Long menuId,
									 @RequestParam(value = "filter", required = false) String filter,
									 @RequestParam(value = "path", required = false) String path,
									 @RequestParam(value = "fondsCode") String fondsCode,
									 @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/render-tree/root-node/{fondsCode}")
	R<List<ArchiveTree>> rootNode(@PathVariable("fondsCode") String fondsCode, @RequestHeader(CommonConstants.MENU_ID) Long menuId);

}
