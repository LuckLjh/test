package com.cescloud.saas.archive.service.modular.archivetree.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.archivetree.constant.ArchiveTreeNodeEnum;
import com.cescloud.saas.archive.api.modular.archivetree.dto.ArchiveTreeGetDTO;
import com.cescloud.saas.archive.api.modular.archivetree.dto.ArchiveTreePutDTO;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.archivetree.entity.ArchiveTree;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archivetree.service.ArchiveTreeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 档案树定义
 *
 * @author qiucs
 * @date 2019-04-12 13:36:59
 */
@Api(value = "archive-tree", tags = "档案树定义")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/archive-tree")
public class ArchiveTreeController {

	private final ArchiveTreeService archiveTreeService;

	@ApiOperation(value = "档案树第一层的全宗节点")
	@GetMapping("/tree/fonds-node")
	public R<List<FondsArchiveTreeSyncTreeNode>> getFondsNode() {
		return new R<>(archiveTreeService.getFondsNode());
	}

	@ApiOperation(value = "档案树第二层及以后的档案树节点")
	@GetMapping("/tree/archive-tree-node")
	public R<List<FondsArchiveTreeSyncTreeNode>> getArchiveTreeNode(@RequestParam("fondsCode") String fondsCode, @RequestParam("archiveTreeId") Long archiveTreeId) {
		return new R<>(archiveTreeService.getArchiveTreeNode(fondsCode, archiveTreeId));
	}

	/**
	 * 查询
	 *
	 * @param archiveTree 档案树定义
	 * @return
	 */
	@ApiOperation(value = "查询")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "archiveTree", value = "树节点对象", required = false, dataType = "ArchiveTree", paramType = "query")
	})
	@GetMapping("/list")
	public R getTreeList(@ApiParam(name = "archiveTree", value = "树节点查询参数") ArchiveTreeGetDTO archiveTree) {
		return new R<>(archiveTreeService.getTreeList(archiveTree));
	}

	/**
	 * 分页查询
	 *
	 * @param page        分页对象
	 * @param archiveTree 档案树定义
	 * @return
	 */
	@ApiOperation(value = "分页查询")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
			@ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int")
	})
	@GetMapping("/page")
	public R getTreePage(Page page, @ApiParam(name = "archiveTree", value = "树节点查询参数") ArchiveTreeGetDTO archiveTree) {
		return new R<>(archiveTreeService.getTreePage(page, archiveTree));
	}

	/**
	 * 档案树的树网格
	 *
	 * @return
	 */
	@ApiOperation(value = "档案树的树网格")
	@GetMapping("/tree-grid")
	public R getTreeGrid() {
		return new R<>(archiveTreeService.getTreeGrid());
	}

	/**
	 * 获取所有树集合
	 *
	 * @return
	 */
	@ApiOperation(value = "获取所有树集合")
	@GetMapping("/tree-roots")
	public R getTreeRoots() {
		return new R<>(archiveTreeService.list(Wrappers.<ArchiveTree>lambdaQuery()
				.eq(ArchiveTree::getNodeType, ArchiveTreeNodeEnum.TREE_ROOT.getCode()).orderByAsc(ArchiveTree::getSortNo)));
	}

	/**
	 * 通过id查询档案树定义
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询档案树定义")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "树节点ID", required = true, dataType = "int", paramType = "query"),
	})
	@GetMapping("/{id}")
	public R getById(@PathVariable("id") Long id) {
		return new R<>(archiveTreeService.getTreeById(id));
	}

	/**
	 * 新增档案树定义
	 *
	 * @param archiveTreeDTO 树节点对象
	 * @return R
	 */
	@ApiOperation(value = "新增档案树定义")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "archiveTreeDTO", value = "树节点对象，格式：{entity: {}}, extendValList: [{id:\"\",name:\"\"},...]}，entity是树节点对象，extendValList是扩展值集合，比如多个全宗，多个档案门类", required = true, dataType = "ArchiveTreeDTO")
	})
	@SysLog("新增档案树定义")
	@PostMapping
	public R save(
			@RequestBody @ApiParam(name = "archiveTreeDTO", value = "树节点对象", required = true) ArchiveTreePutDTO archiveTreeDTO) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增档案树定义-档案树节点名称【%s】,档案树节点编码【%s】",archiveTreeDTO.getEntity().getTreeName(),archiveTreeDTO.getEntity().getTreeCode()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(archiveTreeService.save(archiveTreeDTO));
	}

	/**
	 * 修改档案树定义
	 *
	 * @param archiveTree 档案树定义
	 * @return R
	 */
	@ApiOperation(value = "修改档案树")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "archiveTree", value = "树节点对象", required = true, dataType = "ArchiveTree"),
	})
	@SysLog("修改档案树定义")
	@PutMapping
	public R updateById(
			@RequestBody @ApiParam(name = "archiveTree", value = "树节点对象", required = true) ArchiveTree archiveTree) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改档案树定义-档案树节点名称【%s】,档案树节点编码【%s】",archiveTree.getTreeName(),archiveTree.getTreeCode()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(archiveTreeService.updateById(archiveTree));
	}

	/**
	 * 通过id删除档案树定义
	 *
	 * @param ids ids
	 * @return R
	 */
	@ApiOperation(value = "通过ids删除档案树定义")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "ids", value = "树节点ID", required = true, dataType = "array"),
	})
	@SysLog("删除档案树定义")
	@DeleteMapping("/{ids}")
	public R removeByIds(@PathVariable Long[] ids) {
		return new R<>(archiveTreeService.removeArchiveTree(ids));
	}

	/**
	 * 通过id删除档案树定义
	 *
	 * @param id 父节点ID
	 * @return R
	 */
	@ApiOperation(value = "通过父节点ID获取子节点")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "父节点ID", required = true, dataType = "int"),
	})
	@GetMapping("/tree")
	public R tree(@RequestParam("id") Long id) {
		return new R<>(
				archiveTreeService.getBaseMapper().selectList(Wrappers.<ArchiveTree>lambdaQuery()
						.eq(ArchiveTree::getParentId, id)
						.orderByAsc(ArchiveTree::getSortNo)));
	}

	/**
	 * 更新全宗号
	 *
	 * @param oldFondsCode 旧的全宗号
	 * @param newFondsCode 新的全宗号
	 * @return R
	 */
	@ApiOperation(value = "更新全宗号")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "oldFondsCode", value = "旧的全宗号", required = true, dataType = "string"),
			@ApiImplicitParam(name = "newFondsCode", value = "新的全宗号", required = true, dataType = "string")
	})
	@GetMapping("/update/fonds-code")
	public R<Boolean> updateFondsCode(String oldFondsCode, String newFondsCode) {
		return new R<>(
				archiveTreeService.updateFondsCode(oldFondsCode, newFondsCode));
	}

	/**
	 * 设置默认树
	 *
	 * @param id 档案树ID
	 * @return R
	 */
	@ApiOperation(value = "设置默认树")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "档案树ID", required = true, dataType = "int")
	})
	@GetMapping("/defaulted/{id}")
	public R<Boolean> defaulted(@PathVariable Long id) {
		return new R<>(
				archiveTreeService.setDefaultTree(id));
	}

	@ApiOperation(value = "初始化档案树", httpMethod = "POST")
	@PostMapping("/initialize")
	@SysLog("初始化档案树")
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId,
							  @RequestParam(value = "tenantId") Long tenantId) throws ArchiveBusinessException {
		return archiveTreeService.initializeArchiveTree(templateId, tenantId);
	}


	@ApiOperation(value = "根据全宗号得到树节点全部数据", httpMethod = "GET")
	@GetMapping("/tree-data/{fondsCode}")
	public R<List<ArchiveTree>> getTreeDataList(@PathVariable @ApiParam(name = "fondsCode", value = "全宗号", required = true) String fondsCode)
			throws ArchiveBusinessException {
		return new R<>(archiveTreeService.getTreeDataList(fondsCode));
	}

	@ApiOperation(value = "得到树节点全部数据", httpMethod = "GET")
	@GetMapping("/tree-data")
	public R<List<ArchiveTree>> getTreeDataList() throws ArchiveBusinessException {
		return new R<>(archiveTreeService.getTreeDataList(null));
	}

	@ApiOperation(value = "获取租户档案树管理-树的信息", httpMethod = SwaggerConstants.GET, hidden = true)
	@GetMapping(value = "/data-tree/{tenantId}")
	@SysLog("获取租户档案树管理-树的信息")
	public R getArchivesTreeInfo(@PathVariable("tenantId") Long tenantId) {
		return new R(archiveTreeService.getArchivesTreeInfo(tenantId));
	}

	@ApiOperation(value = "获取租户档案树管理-树节点的信息", httpMethod = SwaggerConstants.GET, hidden = true)
	@GetMapping(value = "/data-tree-node/{tenantId}")
	@SysLog("获取租户档案树管理-树节点的信息")
	public R getArchivesTreeNodeInfo(@PathVariable("tenantId") Long tenantId) {
		return new R(archiveTreeService.getArchivesTreeNodeInfo(tenantId));
	}

	@ApiOperation(value = "是否显示层级设置", httpMethod = SwaggerConstants.POST)
	@PostMapping("/show-layer/switch/{id}/{fondsCode}")
	@SysLog("是否显示层级设置")
	public R<Boolean> switchShowLayer(@PathVariable Long id, @PathVariable String fondsCode) {
		return new R<Boolean>(archiveTreeService.switchShowLayer(id, fondsCode));
	}

	@ApiOperation("树节点排序")
	@PutMapping("/order")
	@SysLog("树节点排序")
	public R<Boolean> archiveTreeOrder(@RequestBody @ApiParam(name = "ids", value = "排序后ids", required = true) List<Long> ids) {
		return new R<Boolean>(archiveTreeService.archiveTreeOrder(ids));
	}

	@ApiOperation("获取动态节点信息")
	@GetMapping("/trends")
	public List<ArchiveTree> getTrends (@RequestParam(value = "nodeId") Long nodeId) {
		return archiveTreeService.getArchiveTreeByParentId(nodeId);
	}

}
