package com.cescloud.saas.archive.api.modular.archivetree.feign;

import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author qiucs
 * @version 1.0.0 2019年4月30日
 */
@FeignClient(contextId = "remoteArchiveTreeService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveTreeService {

	@GetMapping("/archive-tree/update/fonds-code")
	R<Boolean> updateFondsCode(@RequestParam("oldFondsCode") String oldFondsCode, @RequestParam("newFondsCode") String newFondsCode);

	@RequestMapping(value = "/archive-tree/initialize", method = RequestMethod.POST)
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId);

	@GetMapping("/archive-tree/tree-data/{fondsCode}")
	R<List<ArchiveTree>> getTreeDataListByFondsCode(@PathVariable("fondsCode") String fondsCode);

	@GetMapping("/archive-tree/tree-data")
	R<List<ArchiveTree>> getTreeDataList();

	/**
	 * 获取档案树信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping("/archive-tree/data-tree/{tenantId}")
	R getArchivesTreeInfo(@PathVariable("tenantId") Long tenantId);

	/**
	 * 获取档案树节点信息
	 *
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping("/archive-tree/data-tree-node/{tenantId}")
	R getArchivesTreeNodeInfo(@PathVariable("tenantId") Long tenantId);

	/**
	 * 根据档案树id查询档案树节点信息
	 *
	 * @param id 档案树id
	 * @return
	 */
	@GetMapping("/archive-tree/{id}")
	R<RenderTreeDTO> getById(@PathVariable("id") Long id);

	@GetMapping("/archive-tree/tree/fonds-node")
	R<List<FondsArchiveTreeSyncTreeNode>> getFondsNode();
}
