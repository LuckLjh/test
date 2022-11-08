
package com.cescloud.saas.archive.service.modular.help.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.api.modular.help.entity.HelpConf;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.cescloud.saas.archive.service.modular.help.service.HelpConfService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 基本数据权限
 *
 * @author zhaiyachao
 * @date 2021-05-11 18:32:44
 */
@Api(value = "HelpConf", description = "基本数据权限")
@RestController
@AllArgsConstructor
@RequestMapping("/help-conf")
public class HelpConfController {

	private final HelpConfService helpConfService;

	/**
	 * 分页查询
	 *
	 * @param page     分页对象
	 * @param helpConf 基本数据权限
	 * @return
	 */
	@ApiOperation(value = "分页查询")
	@GetMapping("/page")
	public R getHelpConfPage(Page page, HelpConf helpConf) {
		return new R<>(helpConfService.page(page, Wrappers.query(helpConf)));
	}


	/**
	 * 通过id查询基本数据权限
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询基本数据权限")
	@GetMapping("/{id}")
	public R getById(@PathVariable("id") Long id) {
		return new R<>(helpConfService.getById(id));
	}

	/**
	 * 新增基本数据权限
	 *
	 * @param helpConf 基本数据权限
	 * @return R
	 */
	@ApiOperation(value = "新增基本数据权限")
	@SysLog("新增基本数据权限")
	@PostMapping
	public R save(@RequestBody HelpConf helpConf) {
		return new R<>(helpConfService.save(helpConf));
	}

	/**
	 * 修改基本数据权限
	 *
	 * @param helpConf 基本数据权限
	 * @return R
	 */
	@ApiOperation(value = "修改基本数据权限")
	@SysLog("修改基本数据权限")
	@PutMapping
	public R updateById(@RequestBody HelpConf helpConf) {
		return new R<>(helpConfService.updateById(helpConf));
	}

	/**
	 * 通过id删除基本数据权限
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id删除基本数据权限")
	@SysLog("删除基本数据权限")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable Long id) {
		return new R<>(helpConfService.removeById(id));
	}

	/**
	 * 查询是否打开 开关
	 *
	 * @param fondId 全宗Id
	 * @return
	 */
	@GetMapping("/isOpen/{fondId}")
	public R isOpen(@PathVariable("fondId") Integer fondId) {
		return new R<>(helpConfService.isOpen(fondId));
	}

	/**
	 * 帮助开关
	 *
	 * @param fondId   全宗
	 * @param openType 打开状态 0:开启/1:关闭
	 * @return
	 */
	@PostMapping("/updateOpen")
	public R updateOpen(@RequestParam("fondId") Integer fondId,
						@RequestParam("openType") Integer openType) {

		return new R<>(helpConfService.updateOpen(fondId, openType));
	}

	/**
	 * 应用到其他全宗
	 *
	 * @param fondId  当前全宗Id
	 * @param fondIds 其他全宗Id
	 * @return
	 */
	@PostMapping("/copyOtherFond")
	public R copyOtherFond(@RequestParam("fondId") Integer fondId, @RequestBody List<Integer> fondIds) {
		return new R<>(helpConfService.copyOtherFond(fondId, fondIds));
	}

}
