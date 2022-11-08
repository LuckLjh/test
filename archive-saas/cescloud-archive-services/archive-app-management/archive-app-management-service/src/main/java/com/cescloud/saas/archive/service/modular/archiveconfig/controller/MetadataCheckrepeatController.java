
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedRepeatMetadata;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveRepeatMetadata;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.MetadataCheckrepeatService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * 查重设置
 *
 * @author liudong1
 * @date 2019-04-23 12:08:06
 */
@Api(value = "metadataCheckRepeat", tags = "应用管理-档案门类管理:查重配置管理")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/check-repeat")
public class MetadataCheckrepeatController {

	private final MetadataCheckrepeatService metadataCheckrepeatService;


	@ApiOperation(value = "已定义的查重字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/def")
	public R<List<DefinedRepeatMetadata>> getCheckRepeatdefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate) {
		return new R<>(metadataCheckrepeatService.listOfDefined(storageLocate));
	}


	@ApiOperation(value = "未定义的查重字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/undef")
	public R<List<DefinedRepeatMetadata>> getCheckRepeatUndefList(@ApiParam(name = "storageLocate", value = "存储表名", required = true) @NotBlank(message = "存储表名不能为空") String storageLocate) {
		return new R<>(metadataCheckrepeatService.listOfUnDefined(storageLocate));
	}


	@ApiOperation(value = "保存档案列表配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存档案查重配置")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "saveRepeatMetadata", value = "查重保存对象", required = true) @Valid SaveRepeatMetadata saveRepeatMetadata) {
		return metadataCheckrepeatService.saveReportDefined(saveRepeatMetadata);
	}

}
