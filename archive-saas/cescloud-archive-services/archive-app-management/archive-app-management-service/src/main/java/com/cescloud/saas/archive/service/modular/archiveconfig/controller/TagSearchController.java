
package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.dto.DefinedSearchTag;
import com.cescloud.saas.archive.api.modular.archiveconfig.dto.SaveSearchTag;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.TagSearchService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
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
 * 标签检索配置
 *
 * @author liudong1
 * @date 2019-05-27 15:55:01
 */
@Api(value = "TagSearch", tags = "应用管理-档案门类管理:标签检索配置")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/tag-search")
public class TagSearchController {

	private final TagSearchService tagSearchService;


	@ApiOperation(value = "已定义的列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/def")
	public R<List<DefinedSearchTag>> getArchiveListDefList() {
		return new R<>(tagSearchService.listOfDefined());
	}



	@ApiOperation(value = "用户内部已定义的列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/defByInner")
	@Inner
	public R<List<DefinedSearchTag>> getArchiveListDefListByInner() {
		return new R<>(tagSearchService.listOfDefined());
	}


	@ApiOperation(value = "未定义的列表字段列表", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/undef")
	public R<List<DefinedSearchTag>> getArchiveListUndefList() {
		return new R<>(tagSearchService.listOfUnDefined());
	}


	@ApiOperation(value = "保存标签检索配置", httpMethod = SwaggerConstants.POST)
	@SysLog("保存标签检索配置")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "saveSearchTag", value = "排序保存对象", required = true) @Valid SaveSearchTag saveSearchTag) {
		return tagSearchService.saveSearchDefined(saveSearchTag);
	}

	@ApiOperation(value = "获取租户元数据标签检索字段的信息", httpMethod = SwaggerConstants.GET, hidden = true)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取租户元数据标签检索字段的信息")
	public R getMetadataTagsSearchFieldInfo(@PathVariable("tenantId") @NotNull(message = "租户id不能为空") Long tenantId) {
		return new R(tagSearchService.getMetadataTagsSearchFieldInfo(tenantId));
	}

	@ApiOperation(value = "获取标签的查询配置来展现表单", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/search-from")
	@SysLog("获取标签的查询配置来展现表单")
	public R getBasicRetrievalForm() {
		return new R(tagSearchService.getBasicRetrievalForm(SecurityUtils.getUser().getTenantId()));
	}
}
