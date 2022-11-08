
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedListTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveListTag;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.TagListService;
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
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 标签列表配置
 *
 * @author liudong1
 * @date 2019-05-27 15:20:07
 */
@Api(value = "TagList", tags = "应用管理-档案门类管理:标签列表配置")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/tag-list")
public class TagListController {

	private final TagListService tagListService;


	@ApiOperation(value = "已定义的列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/def")
	public R<List<DefinedListTag>> getArchiveListdefList() {
		return new R<>(tagListService.listOfDefined());
	}


	@ApiOperation(value = "未定义的列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/undef")
	public R<List<DefinedListTag>> getArchiveListUndefList() {
		return new R<>(tagListService.listOfUnDefined());
	}


	@ApiOperation(value = "保存标签列表配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存标签列表配置")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "saveListTag", value = "排序保存对象", required = true) @Valid SaveListTag saveListTag) {
		return tagListService.saveListDefined(saveListTag);
	}

	@ApiOperation(value = "获取租户元数据标签检索列表的信息", httpMethod = SwaggerConstants.GET, hidden = true)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取租户元数据标签检索列表的信息")
	public R getMetadataTagsSearchListInfo(@PathVariable("tenantId") @NotNull(message = "租户id不能为空") Long tenantId) {
		return new R(tagListService.getMetadataTagsSearchListInfo(tenantId));
	}
}
