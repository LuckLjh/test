package com.cescloud.saas.archive.api.modular.archivetype.feign;

import com.cescloud.saas.archive.api.modular.archivetype.dto.*;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.service.modular.common.core.constant.ServiceNameConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Package: com.cescloud.saas.archive.api.modular.feign.archiveType
 * @Classname RemoteArchiveTypeService
 * @Description TODO
 * @Date 2019-03-07 10:44
 * @author  by zhangpeng
 */

@FeignClient(contextId = "remoteArchiveTypeService",value = ServiceNameConstants.ARCHIVE_APP_MANAGEMENT)
public interface RemoteArchiveTypeService {

	@GetMapping("/type/{id}")
	R<ArchiveType> getById(@PathVariable("id") Long id);

	@GetMapping("/type/sync-tree")
	R<List<ArchiveTypeTableTree>> getArchiveTypeTableSyncTree();

//	@GetMapping("/type/catalog/list/{fondsCode}")
//	R<List<ArchiveTypeTableSyncTreeNode>> getArchiveCatalogSyncTreeList(@PathVariable("fondsCode") String fondsCode);

//	@GetMapping("/type/document/list/{fondsCode}")
//	R<List<ArchiveTypeTableSyncTreeNode>> getArchiveDocumentSyncTreeList(@PathVariable("fondsCode") String fondsCode);

	@PostMapping("/type/catalog/list")
	R<List<ArchiveTypeTableSyncTreeNode>> getArchiveCatalogSyncTreeList(@RequestBody List<String> fondsCodeList);

	@PostMapping("/type/document/list")
	R<List<ArchiveTypeTableSyncTreeNode>> getArchiveDocumentSyncTreeList(@RequestBody List<String> fondsCodeList);

	@RequestMapping(value = "/type/initialize",method = RequestMethod.POST)
	public R initializeHandle(@RequestParam(value = "templateId",required=false) Long templateId, @RequestParam(value = "tenantId") Long tenantId);

	/**
	 * 获取档案门类信息
	 * @param tenantId 租户id
	 * @return
	 */
	@GetMapping(value = "/type/data/{tenantId}")
	public R<List<ArrayList<String>>> getArchivesClassInfor(@PathVariable("tenantId") Long tenantId);

	/**
	 * 门类树 考虑所属的范围全宗
	 * @param id 父节点id
	 * @param nodeType 节点类型
	 * @param fondsCodes 全宗编码
	 * @return
	 */
	@GetMapping("/type/tree")
	public R<List<ArchiveTypeTreeNode>> getArchiveTypeTree(@RequestParam("id") Long id, @RequestParam("nodeType") String nodeType, @RequestParam(value = "fondsCodes",required = false) List<String> fondsCodes);


	@GetMapping(value = "/type/code/{typeCode}")
	public R<ArchiveType> getArchiveType(@PathVariable("typeCode") String typeCode);

	@GetMapping(value = "/type/list")
	R<List<ArchiveType>> getArchiveTypes();

	@GetMapping("/type/relation-tree")
	R<List<ArchiveTypeDTO>> getArchiveTypeRelationTree(@RequestParam(value = "fondsCodes",required = false) List<String> fondsCodes);

	/**
	 * 获取节点类型为档案类型节点的档案门类树集合 考虑全宗列表范围 全查询 携带子节点
	 * @param fondsCodes 全宗列表范围
	 * @return
	 */
	@GetMapping("/type/relation-fonds-tree")
	R<List<ArchiveTypeChildTreeNode>> getArchiveTypeRelationFondsTree(@RequestParam(value = "fondsCodes",required = false) List<String> fondsCodes);

	/**
	 * 更新全宗的时候 也去更新档案门类里根据全宗绑定的节点
	 * @param fondsName 全宗名称
	 * @param fondsCode	全宗代码
	 */
	@GetMapping("/type/updateArchiveTypeTree")
	void updateArchiveTypeTree(@RequestParam("fondsName") String fondsName , @RequestParam("fondsCode") String fondsCode);

}
