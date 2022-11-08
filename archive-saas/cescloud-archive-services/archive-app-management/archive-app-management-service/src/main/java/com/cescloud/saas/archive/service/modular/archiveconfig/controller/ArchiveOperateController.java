
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveOperate;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveOperateService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 数据值操作规则
 *
 * @author liudong1
 * @date 2019-04-21 19:30:10
 */
@Api(value = "archiveList", tags = "应用管理-档案门类管理:数据值操作规则控制器")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/operate")
public class ArchiveOperateController {

	private final ArchiveOperateService archiveOperateService;

	/**
	 * 根据业务ID获取已经配置的信息列表
	 * @param businessId
	 * @return
	 */
	@ApiOperation(value = "已配置的数据值操作规则列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/{businessId}")
	public R getArchiveOperateList(@PathVariable("businessId") Long businessId) {
		return new R<>(archiveOperateService.getOperateRule(businessId));
	}

	/**
	 * 保存数据值操作规则
	 *
	 * @param saveOperate 数据值操作保存对象
	 * @return R
	 */
	@ApiOperation(value = "保存数据值操作规则", httpMethod = SwaggerConstants.POST)
	@SysLog("保存数据值操作规则")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "saveOperate", value = "数据值操作保存对象", required = true) SaveOperate saveOperate) {
		return archiveOperateService.saveOperateDefined(saveOperate);
	}

}
