
package com.cescloud.saas.archive.service.modular.help.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.api.modular.help.entity.HelpUserShow;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.cescloud.saas.archive.service.modular.help.service.HelpUserShowService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 档案类型数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
@Api(value = "HelpUserShow", description = "档案类型数据权限")
@RestController
@AllArgsConstructor
@RequestMapping("/help-user-show")
public class HelpUserShowController {

	private final HelpUserShowService helpUserShowService;

	/**
	 * 分页查询
	 *
	 * @param page         分页对象
	 * @param helpUserShow 档案类型数据权限
	 * @return
	 */
	@ApiOperation(value = "分页查询")
	@GetMapping("/page")
	public R getHelpUserShowPage(Page page, HelpUserShow helpUserShow) {
		return new R<>(helpUserShowService.page(page, Wrappers.query(helpUserShow)));
	}


	/**
	 * 通过id查询档案类型数据权限
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询档案类型数据权限")
	@GetMapping("/{id}")
	public R getById(@PathVariable("id") Long id) {
		return new R<>(helpUserShowService.getById(id));
	}

	/**
	 * 新增档案类型数据权限
	 *
	 * @param helpUserShow 档案类型数据权限
	 * @return R
	 */
	@ApiOperation(value = "新增档案类型数据权限")
	@SysLog("新增档案类型数据权限")
	@PostMapping
	public R save(@RequestBody HelpUserShow helpUserShow) {
		return new R<>(helpUserShowService.save(helpUserShow));
	}

	/**
	 * 修改档案类型数据权限
	 *
	 * @param helpUserShow 档案类型数据权限
	 * @return R
	 */
	@ApiOperation(value = "修改档案类型数据权限")
	@SysLog("修改档案类型数据权限")
	@PutMapping
	public R updateById(@RequestBody HelpUserShow helpUserShow) {
		return new R<>(helpUserShowService.updateById(helpUserShow));
	}

	/**
	 * 通过id删除档案类型数据权限
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id删除档案类型数据权限")
	@SysLog("删除档案类型数据权限")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable Long id) {
		return new R<>(helpUserShowService.removeById(id));
	}

	/**
	 * @param fondId     全宗Id
	 * @param menuId     菜单Id
	 * @param isShowType 0:显示 / 1:不显示
	 * @return
	 */
	@PostMapping("/isShow")
	public R isShow(@RequestParam("fondId") Long fondId, @RequestParam("menuId") Integer menuId,
					@RequestParam("menuId") Integer isShowType) {
		return new R<>(helpUserShowService.isShow(fondId, menuId, isShowType));
	}
}
