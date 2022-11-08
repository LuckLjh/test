
package com.cescloud.saas.archive.service.modular.help.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.api.modular.help.entity.HelpItems;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.cescloud.saas.archive.service.modular.help.service.HelpItemsService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * 全局数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
@Api(value = "HelpItems", description = "全局数据权限")
@RestController
@AllArgsConstructor
@RequestMapping("/help-items")
public class HelpItemsController {

	private final HelpItemsService helpItemsService;

	/**
	 * 分页查询
	 *
	 * @param page      分页对象
	 * @param helpItems 全局数据权限
	 * @return
	 */
	@ApiOperation(value = "分页查询")
	@GetMapping("/page")
	public R getHelpItemsPage(Page page, HelpItems helpItems) {
		return new R<>(helpItemsService.page(page, Wrappers.query(helpItems)));
	}


	/**
	 * 通过id查询全局数据权限
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询全局数据权限")
	@GetMapping("/{id}")
	public R getById(@PathVariable("id") Long id) {
		return new R<>(helpItemsService.getById(id));
	}

	/**
	 * 新增全局数据权限
	 *
	 * @param helpItems 全局数据权限
	 * @return R
	 */
	@ApiOperation(value = "新增全局数据权限")
	@SysLog("新增全局数据权限")
	@PostMapping
	public R save(@RequestBody HelpItems helpItems) {
		return new R<>(helpItemsService.save(helpItems));
	}

	/**
	 * 修改全局数据权限
	 *
	 * @param helpItems 全局数据权限
	 * @return R
	 */
	@ApiOperation(value = "修改全局数据权限")
	@SysLog("修改全局数据权限")
	@PutMapping
	public R updateById(@RequestBody HelpItems helpItems) {
		return new R<>(helpItemsService.updateById(helpItems));
	}

	/**
	 * 通过id删除全局数据权限
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id删除全局数据权限")
	@SysLog("删除全局数据权限")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable Long id) {
		return new R<>(helpItemsService.removeById(id));
	}

	/**
	 * 列表加载
	 *
	 * @param menuId 菜单Id
	 * @param fondId 全宗Id
	 * @return
	 */
	@GetMapping("/getHelpList/fondId/{menuId}")
	public R<List<HelpItems>> getHelpList(@PathVariable("menuId") Integer menuId, @RequestParam("fondId") Long fondId) {
		return new R<>(helpItemsService.getHelpList(menuId, fondId));
	}

	/**
	 * 排序
	 *
	 * @param fondId 全宗Id
	 * @param menuId 菜单Id
	 * @param fileId 文件Id 顺序格式
	 * @return
	 */
	@PostMapping("/updateDataSort")
	public R<Boolean> updateDataSort(@RequestParam("fondId") Long fondId, @RequestParam("menuId") Integer menuId,
									 @RequestBody List<Integer> fileId) {
		return new R<>(helpItemsService.updateDataSort(fondId, menuId, fileId));
	}

	/**
	 * 删除
	 *
	 * @param fondId 全宗Id
	 * @param menuId 菜单Id
	 * @param fileId 文件Id
	 * @return
	 */
	@PostMapping("/delDataByName")
	public R<Boolean> delDataByName(@RequestParam("fondId") Long fondId, @RequestParam("menuId") Integer menuId,
									@RequestParam("fileId") Long fileId) {
		return new R<>(helpItemsService.delDataByName(fondId, menuId, fileId));
	}

	/**
	 * 恢复系统默认
	 *
	 * @param fondId 全宗Id
	 * @param menuId 菜单Id
	 * @return
	 */
	@PostMapping("/recoverSystemDefault")
	public R recoverSystemDefault(@RequestParam("fondId") Long fondId, @RequestParam("menuId") Integer menuId) {
		return new R<>(helpItemsService.recoverSystemDefault(fondId, menuId));
	}

	/**
	 * 获取当前map
	 *
	 * @param fondId 全宗Id
	 * @param menuId 菜单Id
	 * @param fileId 文件Id
	 * @return
	 */
	@GetMapping("/queryMap")
	public R queryMap(@RequestParam("fondId") Long fondId, @RequestParam("menuId") Integer menuId,
					  @RequestParam("fileId") Integer fileId) {
		return new R<>(helpItemsService.queryMap(fondId, menuId, fileId));
	}

	/**
	 * 文件上传
	 *
	 * @param file   文件
	 * @param fondId 全宗Id
	 * @param menuId 菜单Id
	 * @return
	 */
	@PostMapping("/uploadHelpMap")
	public R uploadHelpMap(@RequestParam("file") MultipartFile file, @RequestParam("fondId") Long fondId, @RequestParam("menuId") Integer menuId) {
		return new R<>(helpItemsService.uploadHelpMap(file, fondId, menuId));
	}
}
