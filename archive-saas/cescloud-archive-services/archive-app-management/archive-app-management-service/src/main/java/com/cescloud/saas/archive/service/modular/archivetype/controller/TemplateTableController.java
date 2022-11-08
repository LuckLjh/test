/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.controller</p>
 * <p>文件名:TemplateTableController.java</p>
 * <p>创建时间:2020年2月17日 下午12:32:17</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateMetadata;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月17日
 */
@Api(value = "template-table", tags = "表模板")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/template-table")
public class TemplateTableController {

    private final TemplateTableService templateTableService;

    @ApiOperation(value = "新增表模板", httpMethod = SwaggerConstants.POST)
    @SysLog("新增表模板")
    @PostMapping
    public R<TemplateTable> create(
        @RequestBody @ApiParam(value = "传入json格式", name = "表模板对象", required = true) @Valid TemplateTable entity) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增表模板-表模板名称【%s】",entity.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateTableService.save(entity);
        return new R<TemplateTable>(entity);
    }

    @ApiOperation(value = "修改表模板", httpMethod = SwaggerConstants.PUT)
    @SysLog("修改表模板")
    @PutMapping
    public R<TemplateTable> update(
        @RequestBody @ApiParam(value = "传入json格式", name = "表模板对象", required = true) @Valid TemplateTable entity) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改表模板-表模板名称【%s】",entity.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateTableService.updateById(entity);
        return new R<TemplateTable>(entity);
    }

    @ApiOperation(value = "删除表模板", httpMethod = SwaggerConstants.DELETE)
    @SysLog("删除表模板")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
		try {
			List<TemplateTable> typeId = templateTableService.getByTemplateTypeId(id);
			SysLogContextHolder.setLogTitle(String.format("删除表模板-表模板名称【%s】",typeId.stream().map(TemplateTable::getName).collect(Collectors.toList())));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateTableService.removeById(id);
        return new R<>(true);
    }

    @ApiOperation(value = "根据ID获取对象", httpMethod = SwaggerConstants.GET)
    @GetMapping("/{id}")
    public R<TemplateTable> getById(@PathVariable("id") Long id) {
        return new R<>(templateTableService.getById(id));
    }

    @ApiOperation(value = "表模板分页列表", httpMethod = SwaggerConstants.GET)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int"),
        @ApiImplicitParam(name = "templateTypeId", value = "档案门类模板ID", required = true, paramType = "int"),
        @ApiImplicitParam(name = "keyword", value = "表模板名称", paramType = "string")
    })
    @GetMapping("/page/{templateTypeId}")
    public R<?> page(Page<TemplateTable> page, @PathVariable("templateTypeId") Long templateTypeId, String keyword) {
        return new R<>(templateTableService.page(page, templateTypeId, keyword));
    }

    @ApiOperation(value = "表模板列表", httpMethod = SwaggerConstants.GET)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "templateTypeId", value = "档案门类模板ID", required = true, paramType = "int"),
        @ApiImplicitParam(name = "keyword", value = "表模板名称", paramType = "string")
    })
    @GetMapping("/list/{templateTypeId}")
    public R<?> list(@PathVariable("templateTypeId") Long templateTypeId, String keyword) {
        return new R<>(templateTableService.list(templateTypeId, keyword));
    }

    @ApiOperation(value = "复制表模板", httpMethod = SwaggerConstants.POST)
    @SysLog("复制表模板")
    @PostMapping("/copy/{templateTypeId}/{copyId}")
    public R<?> copy(
        @ApiParam(value = "templateTypeId", name = "复制到档案门类模板ID") @PathVariable("templateTypeId") Long templateTypeId,
        @ApiParam(value = "copyId", name = "要复制的表模板ID") @PathVariable("copyId") Long copyId) {
		try {
			List<TemplateTable> typeId = templateTableService.getByTemplateTypeId(copyId);
			SysLogContextHolder.setLogTitle(String.format("复制表模板-表模板名称【%s】",typeId.stream().map(TemplateTable::getName).collect(Collectors.toList())));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateTableService.copy(copyId, templateTypeId,new HashMap<>());
        return new R<>(true);
    }

    @ApiOperation(value = "获取门类-档案类型表模板信息", httpMethod = SwaggerConstants.GET)
    @GetMapping(value = "/data/{tenantId}")
    @SysLog("获取门类-档案类型表模板信息")
    public R<List<ArrayList<String>>> getArchivesTypeTableTemplateInfor(@PathVariable("tenantId") Long tenantId) {
        return new R(templateTableService.getArchivesTypeTableTemplateInfor(tenantId));
    }

    @ApiOperation(value = "表模板对应父模板列表", httpMethod = SwaggerConstants.GET, hidden = true)
    @GetMapping("/list/parents/{id}")
    public R<?> getParentListById(
        @ApiParam(value = "id", name = "表模板ID") @PathVariable("id") Long id) {

        return new R<>(templateTableService.getParentListById(id));
    }

    @ApiOperation(value = "表模板对应条目子模板列表", httpMethod = SwaggerConstants.GET)
    @GetMapping("/list/entry-child/{id}")
    public R<List<TemplateTable>> getEntryChildListById(
        @ApiParam(value = "id", name = "表模板ID") @PathVariable("id") Long id) {
        return new R<>(templateTableService.getEntryChildListById(id));
    }

	@ApiOperation(value = "表模板对应条目所有子模板列表（包括条目本身）", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list/entry-childs/{id}")
	public R<List<TemplateTable>> getAllChildListById(
			@ApiParam(value = "id", name = "表模板ID") @PathVariable("id") Long id) {
		return new R<>(templateTableService.getAllChildListById(id));
	}

    @ApiOperation(value = "表模板对应父模板", httpMethod = SwaggerConstants.GET)
    @GetMapping("/parent/{id}")
    public R<TemplateTable> getParentById(
        @ApiParam(value = "id", name = "表模板ID") @PathVariable("id") Long id) {
        return new R<TemplateTable>(templateTableService.getParentById(id));
    }

	@ApiOperation(value = "根据档案类型获取表模板信息", httpMethod = SwaggerConstants.GET)
	@GetMapping("/list-code")
	public R<List<TemplateTable>> getListByArchiveCode(@RequestParam("archiveCode") @ApiParam(value = "archiveCode", name = "档案类型") @NotBlank(message = "档案类型不能为空") String archiveCode){
		return new R<List<TemplateTable>>(templateTableService.getListByArchiveCode(archiveCode));
	}
}
