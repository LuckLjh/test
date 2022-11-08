package com.cescloud.saas.archive.service.modular.archivetree.controller;

import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTypeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.service.modular.archivetree.service.RenderTreeService;
import com.cescloud.saas.archive.service.modular.archivetype.service.ArchiveTypeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

@Api(value = "module-tree", tags = "各个模块档案树的特殊处理")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/module-tree")
public class ModuleTreeController {

	private final RenderTreeService renderTreeService;
	private final ArchiveTypeService archiveTypeService;

	@ApiOperation(value = "全局门类+当前全宗门类，无下层节点", notes = "根节点：ID为-1，全宗号为当前全宗，后面的节点：ID和全宗号都是父节点的值", httpMethod = "GET")
	@GetMapping("/fonds-type-tree")
	public R<List<FondsArchiveTypeSyncTreeNode>> getFondsTypeTree(@ApiParam(name = "id", value = "父节点ID，跟节点为-1", required = true) @NotNull(message = "父节点id不能为空") Long id,
																  @ApiParam(name = "fondsCode", value = "全宗号", required = true) @NotNull(message = "全宗号不能为空") String fondsCode) {
		return new R<>(archiveTypeService.getFondsTypeTree(id, fondsCode));
	}

	@ApiOperation(value = "全局门类+当前全宗门类，有下层节点", notes = "根节点：ID为-1，全宗号为当前全宗，后面的节点：ID和全宗号都是父节点的值", httpMethod = "GET")
	@GetMapping("/fonds-type-table-tree")
	public R<List<FondsArchiveTypeSyncTreeNode>> getFondsTypeTableTree(@ApiParam(name = "id", value = "父节点ID，跟节点为-1", required = true) @NotNull(message = "父节点id不能为空") Long id,
																	   @ApiParam(name = "fondsCode", value = "全宗号", required = true) @NotNull(message = "全宗号不能为空") String fondsCode,
																	   @ApiParam(name = "nodeClass", value = "节点类型", required = true) @NotNull(message = "节点类型不能为空") String nodeClass,
																	   @ApiParam(name = "typeCode", value = "档案门类编码", required = false) String typeCode,
																	   @ApiParam(name = "showDocument", value = "是否显示全文表", required = true) @NotNull(message = "是否显示全文表不能为空") Integer showDocument) {
		return new R<>(archiveTypeService.getFondsTypeTableTree(id, fondsCode, nodeClass, typeCode, showDocument));
	}

	@ApiOperation(value = "全局门类+当前全宗门类+下级数据(同步树)", notes = "用于统计分析-年报统计-条件设置按钮的左边同步树", httpMethod = "GET")
	@GetMapping("/fonds-type-all-table-tree")
	public R<List<FondsArchiveTypeSyncTreeNode>> getFondsAllTypeTableTree(@ApiParam(name = "fondsCode", value = "全宗号", required = true) @NotNull(message = "全宗号不能为空") String fondsCode) {
		return new R<>(archiveTypeService.getFondsAllTypeTableTree(fondsCode));
	}

	/**
	 * 获取档案树的根节点
	 *
	 * @param fondsCode 全宗号
	 * @return R
	 */
	@ApiOperation(value = "获取档案树的根节点", notes = "获取全宗所绑定的档案树信息(档案树定义中可定义全宗绑定指定档案树)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "fondsCode", value = "全宗号", required = false, dataType = "string")
	})
	@GetMapping("/root-node")
	public R rootNode(@RequestParam String fondsCode) {
		return new R<>(renderTreeService.getRootNodeListByFondsCode(fondsCode));
	}

	@ApiOperation(value = "通过父节点ID获取子节点", notes = "通过父节点ID,菜单ID,过滤条件，归档范围节点path获取子节点信息")
	@GetMapping("/tree")
	public R<List<RenderTreeDTO>> tree(@ApiParam(name = "id", value = "父节点ID", required = true) @RequestParam(value = "id") String id,
									   @ApiParam(name = "menuId", value = "菜单ID") @RequestParam(value = "menuId",required = false) Long menuId,
									   @ApiParam(name = "filter", value = "过滤条件") @RequestParam(value = "filter",required = false) String filter,
									   @ApiParam(name = "path", value = "归档范围节点path") @RequestParam(value = "path",required = false) String path,
									   @ApiParam(name = "shouGroupAndDynamic", value = "是否显示组织节点和动态节点") @RequestParam(value = "shouGroupAndDynamic",required = false) Integer shouGroupAndDynamic,
									   @ApiParam(name = "shouLayer", value = "是否显示层级") @RequestParam(value = "shouLayer",required = false) Integer shouLayer,
									   @ApiParam(name = "fondsCode", value = "全宗号", required = true) @RequestParam(value = "fondsCode") String fondsCode) {
		return new R<List<RenderTreeDTO>>(renderTreeService.getModuleTreeData(id, menuId, filter, path, shouGroupAndDynamic, shouLayer, fondsCode));
	}
}
