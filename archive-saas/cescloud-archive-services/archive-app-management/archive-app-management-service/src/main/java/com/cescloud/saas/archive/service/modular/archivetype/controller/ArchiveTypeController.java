
package com.cescloud.saas.archive.service.modular.archivetype.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTypeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetype.dto.*;
import com.cescloud.saas.archive.api.modular.archivetype.entity.ArchiveType;
import com.cescloud.saas.archive.common.constants.NodeTypeEnum;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * 档案门类
 *
 * @author liudong1
 */
@Api(value = "archiveType", tags = "档案门类管理")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/type")
public class ArchiveTypeController {

	private final ArchiveTypeService archiveTypeService;

	/**
	 * 通过id查询档案门类
	 *
	 * @param id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询档案门类", httpMethod = "GET")
	@GetMapping("/{id}")
	public R<ArchiveType> getById(@PathVariable("id") @ApiParam(name = "id", value = "档案门类ID", required = true) @NotNull(message = "档案门类ID不能为空") Long id) {
		return new R<ArchiveType>(archiveTypeService.getArchiveTypeById(id));
	}

	/**
	 * 新增档案门类
	 *
	 * @param archiveType 档案门类
	 * @return R
	 */
	@ApiOperation(value = "新增档案门类", httpMethod = "POST")
	@SysLog("新增档案门类")
	@PostMapping
	public R<ArchiveType> save(@RequestBody @Valid @ApiParam(name = "archiveType", value = "档案门类实体", required = true) ArchiveType archiveType)
			throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增档案门类-档案门类名字【%s】,全宗名称【%s】",archiveType.getTypeName(),archiveType.getFondsName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(archiveTypeService.saveArchiveType(archiveType));
	}

	/**
	 * 修改档案门类
	 *
	 * @param archiveType 档案门类
	 * @return R
	 */
	@ApiOperation(value = "修改档案门类", httpMethod = "PUT")
	@SysLog("修改档案门类")
	@PutMapping
	public R<ArchiveType> updateById(@RequestBody @ApiParam(name = "archiveType", value = "档案门类实体", required = true) ArchiveType archiveType) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改档案门类-档案门类名字【%s】,全宗名称【%s】",archiveType.getTypeName(),archiveType.getFondsName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(archiveTypeService.updateArchiveType(archiveType));
	}

	/**
	 * 通过id删除档案门类
	 *
	 * @param id
	 * @return R
	 */
	@ApiOperation(value = "通过id删除档案门类", httpMethod = "DELETE")
	@SysLog("删除档案门类")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable @ApiParam(name = "id", value = "档案门类ID", required = true) Long id) throws ArchiveBusinessException {
		try {
			ArchiveType archiveTypeById = archiveTypeService.getArchiveTypeById(id);
			SysLogContextHolder.setLogTitle(String.format("删除档案门类-档案门类名字【%s】,全宗名称【%s】",archiveTypeById.getTypeName(),archiveTypeById.getFondsName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return archiveTypeService.deleteArchiveType(id);
	}


	/**
	 * 档案门类树考虑全宗
	 * 如果是分类节点，则下面是档案门类节点
	 * 如果是档案门类节点，则下面是档案表数据
	 *
	 * @param id
	 * @param nodeType
	 * @return
	 */
	@ApiOperation(value = "获取范围全宗的异步档案门类树", notes = "根节点：id为0，nodeType可为任意值", httpMethod = "GET")
	@GetMapping("/tree")
	public R<List<ArchiveTypeTreeNode>> getArchiveTypeTree(@ApiParam(name = "id", value = "父节点ID，跟节点为0", required = true) @NotNull(message = "父节点id不能为空") Long id,
														   @ApiParam(name = "nodeType", value = "节点类型", required = true) @NotBlank(message = "节点类型不能为空") String nodeType,
														   @ApiParam(name = "fondsCodes", value = "全宗编码列表", required = false) @RequestParam(value = "fondsCodes",required = false)  List<String> fondsCodes)
			throws ArchiveBusinessException {
		return new R<>(archiveTypeService.getTypeTreeNodes(id, NodeTypeEnum.getEnum(nodeType),fondsCodes));
	}

	@ApiOperation(value = "获取范围全宗的异步档案门类树", notes = "根节点：id为-1，nodeType可为任意值", httpMethod = "GET")
	@GetMapping("/fonds-type-tree")
	public R<List<FondsArchiveTypeSyncTreeNode>> getFondsTypeTree(@ApiParam(name = "id", value = "父节点ID，跟节点为-1", required = true) @NotNull(message = "父节点id不能为空") Long id,
																  @ApiParam(name = "fondsCode", value = "全宗号", required = false) @RequestParam(value = "fondsCode", required = false) String fondsCode) {
		return new R<>(archiveTypeService.getFondsTypeTree(id, fondsCode));
	}


	/**
	 * 档案门类树考虑全宗
	 * 如果是分类节点，则下面是档案门类节点
	 * 如果是档案门类节点，则下面是档案表数据
	 *
	 * @param id
	 * @param nodeType
	 * @return
	 */
	@ApiOperation(value = "获取四性检测配置的异步档案门类树", notes = "根节点：id为0，nodeType可为任意值", httpMethod = "GET")
	@GetMapping("/fourCheck-tree")
	public R<List<ArchiveTypeTreeNode>> getArchiveTypeTreeForFourCheck(@ApiParam(name = "id", value = "父节点ID，跟节点为0", required = true) @NotNull(message = "父节点id不能为空") Long id,
														   @ApiParam(name = "nodeType", value = "节点类型", required = true) @NotBlank(message = "节点类型不能为空") String nodeType,
														   @ApiParam(name = "fondsCodes", value = "全宗编码列表", required = false) @RequestParam(value = "fondsCodes",required = false)  List<String> fondsCodes)
			throws ArchiveBusinessException {
		return new R<>(archiveTypeService.getTypeTreeNodesForFourCheck(id, NodeTypeEnum.getEnum(nodeType),fondsCodes));
	}

	@ApiOperation("档案门类排序")
	@PutMapping("/order")
	@SysLog("档案门类排序")
	public R<Boolean> archiveTypeOrder(@RequestBody @ApiParam(name = "menuOrderDTO", value = "档案门类排序", required = true) ArchiveTypeOrderDTO archiveTypeOrderDTO) {
		return new R<Boolean>(archiveTypeService.archiveTypeOrder(archiveTypeOrderDTO));
	}

	@ApiOperation(value = "获取档案门类同步树", httpMethod = "GET")
	@GetMapping("/sync-tree")
	public R<List<ArchiveTypeTableTree>> getArchiveTypeTableSyncTree() {
		return new R<>(archiveTypeService.getArchiveTypeTableSyncTree());
	}

	/**
	 * 获取节点类型为档案类型节点的档案门类集合 考虑全宗列表范围
	 * @param fondsCodes
	 * @return
	 */
	@ApiOperation(value = "获取节点类型为档案类型节点的档案门类集合 考虑全宗列表范围", httpMethod = "GET")
	@GetMapping("/relation-tree")
	public R<List<ArchiveTypeDTO>> getArchiveTypeRelationTree(@ApiParam(name = "fondsCodes", value = "全宗编码列表", required = false) @RequestParam(value = "fondsCodes",required = false)  List<String> fondsCodes) {
		return new R<List<ArchiveTypeDTO>>(archiveTypeService.getArchiveTypeRelationTree(fondsCodes));
	}

	/**
	 * 获取节点类型为档案类型节点的档案门类树集合 考虑全宗列表范围 全查询 携带子节点
	 * @param fondsCodes 全宗列表范围
	 * @return
	 */
	@ApiOperation(value = "获取节点类型为档案类型节点的档案门类树集合 考虑全宗列表范围", httpMethod = "GET")
	@GetMapping("/relation-fonds-tree")
	public R<List<ArchiveTypeChildTreeNode>> getArchiveTypeRelationFondsTree(@ApiParam(name = "fondsCodes", value = "全宗编码列表", required = false) @RequestParam(value = "fondsCodes",required = false)  List<String> fondsCodes) {
		return new R<List<ArchiveTypeChildTreeNode>>(archiveTypeService.getArchiveTypeRelationFondsTree(fondsCodes));
	}

	@ApiOperation(value = "初始化档案门类", httpMethod = "POST")
	@PostMapping("/initialize")
	@SysLog("初始化档案门类")
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId) throws ArchiveBusinessException {
		return archiveTypeService.initializeHandle(templateId, tenantId);
	}

	/*@ApiOperation(value = "获取档案门类目录同步树", httpMethod = "GET")
	@GetMapping("/catalog/list/{fondsCode}")
	public R<List<ArchiveTypeTableSyncTreeNode>> getArchiveCatalogSyncTree(@PathVariable("fondsCode") String fondsCode) {
		return new R<>(archiveTypeService.getArchiveTypeTableSyncTree(Boolean.TRUE, fondsCode));
	}

	@ApiOperation(value = "获取档案门类全文同步树", httpMethod = "GET")
	@GetMapping("/document/list/{fondsCode}")
	public R<List<ArchiveTypeTableSyncTreeNode>> getArchiveDocumentSyncTree(@PathVariable("fondsCode") String fondsCode) {
		return new R<>(archiveTypeService.getArchiveTypeTableSyncTree(Boolean.FALSE, fondsCode));
	}*/

	@ApiOperation(value = "获取档案门类目录同步树", httpMethod = "POST")
	@PostMapping("/catalog/list")
	public R<List<ArchiveTypeTableSyncTreeNode>> getArchiveCatalogSyncTree(@RequestBody List<String> fondsCodeList) {
		return new R<>(archiveTypeService.getArchiveTypeTableSyncTree(Boolean.TRUE, fondsCodeList));
	}

	@ApiOperation(value = "获取档案门类全文同步树", httpMethod = "POST")
	@PostMapping("/document/list")
	public R<List<ArchiveTypeTableSyncTreeNode>> getArchiveDocumentSyncTree(@RequestBody List<String> fondsCodeList) {
		return new R<>(archiveTypeService.getArchiveTypeTableSyncTree(Boolean.TRUE, fondsCodeList));
	}

	/**
	 * 权限管理-数据权限-全局设置中标签为档案门类对应的下拉接口
	 *
	 * @return
	 */
	@ApiOperation(value = "获取节点类型为档案门类的列表", httpMethod = "GET")
	@GetMapping("/node-type/{nodeType}/list")
	public R<List<ArchiveType>> getNodeTypeAList(@NotBlank(message = "节点类型不能为空!") @PathVariable String nodeType) {
		return new R<>(archiveTypeService.list(Wrappers.<ArchiveType>lambdaQuery()
				.eq(ArchiveType::getNodeType, NodeTypeEnum.getEnum(nodeType).getValue())));
	}

	@ApiOperation(value = "获取门类-档案门类信息", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	public R<List<ArrayList<String>>> getArchivesClassInfor(@PathVariable("tenantId") Long tenantId) {
		return new R(archiveTypeService.getArchivesClassInfor(tenantId));
	}

	@ApiOperation(value = "根据档案门类编码获取档案门类信息",httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/code/{typeCode}")
	public R<ArchiveType> getArchiveType(@PathVariable("typeCode") String typeCode) throws ArchiveBusinessException {
		return new R(archiveTypeService.getByTypeCode(typeCode));
	}

	@ApiOperation(value = "获取所有的档案门类信息",httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/list")
	public R<List<ArchiveType>> getArchiveTypes() {
		return new R(archiveTypeService.getArchiveTypes());
	}

	@ApiOperation(value = "复制档案类型", httpMethod = SwaggerConstants.POST)
	@SysLog("复制档案类型")
	@PostMapping("/copy")
	public R<Boolean> copyArchiveType(@RequestBody @Valid @ApiParam(name = "archiveType", value = "档案门类带全宗的实体", required = true) ArchiveTypeCopyPostDTO archiveType) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("复制档案类型-目标全宗名称【%s】",archiveType.getTargetFondsName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R(archiveTypeService.copyArchiveType(archiveType));
	}

	@ApiOperation(value = "更新全宗名称的时候更新档案门类里根据全宗绑定的节点", httpMethod = SwaggerConstants.GET)
	@SysLog("更新档案门类里根据全宗绑定的节点")
	@GetMapping("/updateArchiveTypeTree")
	public void updateArchiveTypeTree(@RequestParam("fondsName") String fondsName , @RequestParam("fondsCode") String fondsCode) {
		try {
			SysLogContextHolder.setLogTitle(String.format("更新档案门类里根据全宗绑定的节点-全宗名称【%s】",fondsName));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		archiveTypeService.updateArchiveTypeTree(fondsName , fondsCode);
	}

}
