
package com.cescloud.saas.archive.service.modular.archivetree.controller;

import com.cescloud.saas.archive.api.modular.archivetree.dto.RenderTreeDTO;
import com.cescloud.saas.archive.service.modular.archivetree.service.RenderTreeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 档案树渲染（前台页面树）
 *
 * @author qiucs
 * @date 2019-04-22 13:36:59
 */
@Api(value = "render-tree", tags = "档案树前台渲染")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/render-tree")
public class RenderTreeController {

	private final RenderTreeService renderTreeService;

	/**
	 * 渲染档案树定义
	 *
	 * @param id     父节点ID
	 * @param menuId 菜单ID
	 * @return R
	 */
	@CacheEvict(cacheNames = "archive-tree-auth", allEntries = true)
	@ApiOperation(value = "通过父节点ID获取子节点", notes = "通过父节点ID,菜单ID,过滤条件，归档范围节点path获取子节点信息")
	@GetMapping("/tree")
	public R<List<RenderTreeDTO>> tree(@ApiParam(name = "id", value = "父节点ID", required = true) @RequestParam(value = "id") String id,
									   @ApiParam(name = "menuId", value = "菜单ID") @RequestParam(value = "menuId", required = false) Long menuId,
									   @ApiParam(name = "filter", value = "过滤条件") @RequestParam(value = "filter", required = false) String filter,
									   @ApiParam(name = "path", value = "归档范围节点path") @RequestParam(value = "path", required = false) String path,
									   @ApiParam(name = "fondsCode", value = "全宗号", required = true) @RequestParam(value = "fondsCode") String fondsCode) {
		return new R<List<RenderTreeDTO>>(renderTreeService.getTreeData(id, menuId, filter, path, fondsCode));
	}

	/**
	 * 渲染档案树定义 内部调用
	 *
	 * @param id     父节点ID
	 * @param menuId 菜单ID
	 * @return R
	 */
	@CacheEvict(cacheNames = "archive-tree-auth", allEntries = true)
	@ApiOperation(value = "通过父节点ID获取子节点", notes = "通过父节点ID,菜单ID,过滤条件，归档范围节点path获取子节点信息")
	@GetMapping("/treeInner")
	@Inner
	public R<List<RenderTreeDTO>> treeInner(@ApiParam(name = "id", value = "父节点ID", required = true) @RequestParam(value = "id") String id,
											@ApiParam(name = "menuId", value = "菜单ID") @RequestParam(value = "menuId", required = false) Long menuId,
											@ApiParam(name = "filter", value = "过滤条件") @RequestParam(value = "filter", required = false) String filter,
											@ApiParam(name = "path", value = "归档范围节点path") @RequestParam(value = "path", required = false) String path,
											@ApiParam(name = "fondsCode", value = "全宗号", required = true) @RequestParam(value = "fondsCode") String fondsCode) {
		return new R<List<RenderTreeDTO>>(renderTreeService.getTreeData(id, menuId, filter, path, fondsCode));
	}

	/**
	 * 获取档案树的根节点
	 *
	 * @param fondsCode 全宗号
	 * @return R
	 */
	@ApiOperation(value = "获取档案树的根节点", notes = "获取全宗所绑定的档案树信息(档案树定义中可定义全宗绑定指定档案树)")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "fondsCode", value = "全宗号", required = true, dataType = "string")
	})
	@GetMapping("/root-node/{fondsCode}")
	public R rootNode(@PathVariable String fondsCode) {
		return new R<>(
				renderTreeService.getDefaultTreeListByFondsCode(fondsCode));
	}


}
