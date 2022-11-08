/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.controller</p>
 * <p>文件名:TemplateTypeController.java</p>
 * <p>创建时间:2020年2月17日 下午12:19:46</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateType;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTypeService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年2月17日
 */
@Api(value = "template-type", tags = "档案门类模板")
@Validated
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/template-type")
public class TemplateTypeController {

    private final TemplateTypeService templateTypeService;

    @ApiOperation(value = "新增档案门类模板", httpMethod = SwaggerConstants.POST)
    @SysLog("新增档案门类模板")
    @PostMapping
    public R<TemplateType> create(
        @RequestBody @ApiParam(value = "传入json格式", name = "档案门类模板对象", required = true) @Valid TemplateType entity) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增档案门类模板-档案类型模板名称【%s】",entity.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateTypeService.save(entity);
        return new R<TemplateType>(entity);
    }

    @ApiOperation(value = "修改档案门类模板", httpMethod = SwaggerConstants.PUT)
    @SysLog("修改档案门类模板")
    @PutMapping
    public R<TemplateType> update(
        @RequestBody @ApiParam(value = "传入json格式", name = "档案门类模板对象", required = true) @Valid TemplateType entity) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改档案门类模板-档案类型模板名称【%s】",entity.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateTypeService.updateById(entity);
        return new R<TemplateType>(entity);
    }

    @ApiOperation(value = "删除档案门类模板", httpMethod = SwaggerConstants.DELETE)
    @SysLog("删除档案门类模板")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
		try {
			TemplateType byId = templateTypeService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除档案门类模板-档案类型模板名称【%s】",byId.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateTypeService.removeById(id);
        return new R<>(true);
    }

    @ApiOperation(value = "根据ID获取对象", httpMethod = SwaggerConstants.GET)
    @GetMapping("/{id}")
    public R<TemplateType> getById(@PathVariable("id") Long id) {
        return new R<>(templateTypeService.getById(id));
    }

    @ApiOperation(value = "档案门类模板分页列表", httpMethod = SwaggerConstants.GET)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int"),
        @ApiImplicitParam(name = "keyword", value = "档案门类模板名称", paramType = "string")
    })
    @GetMapping("/page")
    public R<?> page(Page<TemplateType> page, String keyword) {
        return new R<>(templateTypeService.page(page, keyword));
    }

    @ApiOperation(value = "根据整理方式获取档案门类模板列表", httpMethod = SwaggerConstants.GET)
    @GetMapping("/list/{filingType}")
    public R<?> listByFilingType(
        @ApiParam(value = "filingType", name = "整理方式") @PathVariable("filingType") String filingType) {
        return new R<>(templateTypeService.getByFilingType(filingType));
    }

    @ApiOperation(value = "复制档案门类表模板", httpMethod = SwaggerConstants.POST)
    @SysLog("复制档案门类表模板")
    @PostMapping("/copy/{copyId}")
    public R<?> copy(@ApiParam(value = "copyId", name = "要复制的档案门类模板ID") @PathVariable("copyId") Long copyId,
        @RequestBody @Valid TemplateType entity) {
		try {
			TemplateType byId = templateTypeService.getById(copyId);
			SysLogContextHolder.setLogTitle(String.format("复制档案门类表模板-档案类型模板名称【%s】",byId.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateTypeService.copy(copyId, entity);
        return new R<>(true);
    }

    @ApiOperation(value = "获取门类-档案门类模板信息", httpMethod = SwaggerConstants.GET)
    @GetMapping(value = "/data/{tenantId}")
    @SysLog("获取门类-档案门类模板信息")
    public R<List<ArrayList<String>>> getArchivesTypeTemplateInfor(@PathVariable("tenantId") Long tenantId) {
        return new R(templateTypeService.getArchivesTypeTemplateInfor(tenantId));
    }

    @ApiOperation(value = "获取档案门类模板树节点集合", httpMethod = SwaggerConstants.GET)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "parentId", value = "父节点ID", required = true, paramType = "int"),
        @ApiImplicitParam(name = "type", value = "节点类型：R为根节点，A为档案门类模板节点，T表模板节点", required = true, paramType = "string")
    })
    @GetMapping("/tree/{parentId}/{type}")
    public R<?> tree(@PathVariable("parentId") Long parentId, @PathVariable("type") String type) {
        return new R<>(templateTypeService.getTreeDataList(parentId, type));
    }

    @GetMapping(value = "/test")
    public R<?> test() {
        return new R<>(templateTypeService.getMaxSortNo());
    }

}
