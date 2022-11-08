
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedColumnRuleMetadata;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.LinkColumnRuleService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * 挂接字段组成规则
 *
 * @author liudong1
 * @date 2019-05-14 11:15:33
 */
@Api(value = "linkColumnRule", tags = "应用管理-档案门类管理:挂接字段组成规则")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/link-column-rule")
public class LinkColumnRuleController {

	private final LinkColumnRuleService linkColumnRuleService;

	/**
	 * 已定义的字段列表
	 *
	 * @param storageLocate
	 * @param linkLayerId
	 * @return
	 */
	@ApiOperation(value = "已定义的字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/def")
	public R<List<DefinedColumnRuleMetadata>> getColumnRuleDefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate,
																   @ApiParam(name = "linkLayerId", value = "被设置的层次ID", required = false) Long linkLayerId) {
		return new R<>(linkColumnRuleService.listOfDefined(storageLocate, linkLayerId));
	}

	/**
	 * 未定义的字段列表
	 *
	 * @param storageLocate
	 * @param linkLayerId
	 * @return
	 */
	@ApiOperation(value = "未定义的字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/undef")
	public R<List<DefinedColumnRuleMetadata>> getColumnRuleUnDefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) String storageLocate,
																	 @ApiParam(name = "linkLayerId", value = "被设置的层次ID", required = true) Long linkLayerId) {
		return new R<>(linkColumnRuleService.listOfUnDefined(storageLocate, linkLayerId));
	}

}
