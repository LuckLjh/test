/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.controller</p>
 * <p>文件名:TemplateMetadataController.java</p>
 * <p>创建时间:2020年2月17日 下午12:36:06</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.archivetype.entity.MetadataAutovalue;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateMetadata;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateMetadataService;
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
import java.util.stream.Collectors;

import static cn.hutool.json.XMLTokener.entity;

/**
 * @author qiucs
 * @version 1.0.0 2020年2月17日
 */
@Api(value = "template-metadata", tags = "模板字段")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/template-metadata")
public class TemplateMetadataController {

    private final TemplateMetadataService templateMetadataService;

    @ApiOperation(value = "新增模板字段", httpMethod = SwaggerConstants.POST)
    @SysLog("新增模板字段")
    @PostMapping
    public R<TemplateMetadata> create(
        @RequestBody @ApiParam(value = "传入json格式", name = "模板字段对象", required = true) @Valid TemplateMetadata entity) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增模板字段-字段名称【%s】-元数据类型【%s】",entity.getMetadataChinese(),entity.getMetadataType()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateMetadataService.save(entity);
        return new R<TemplateMetadata>(entity);
    }

    @ApiOperation(value = "修改模板字段", httpMethod = SwaggerConstants.PUT)
    @SysLog("修改模板字段")
    @PutMapping
    public R<TemplateMetadata> update(
        @RequestBody @ApiParam(value = "传入json格式", name = "模板字段对象", required = true) @Valid TemplateMetadata entity) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改模板字段-字段名称【%s】-元数据类型【%s】",entity.getMetadataChinese(),entity.getMetadataType()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateMetadataService.updateById(entity);
        return new R<TemplateMetadata>(entity);
    }

    @ApiOperation(value = "删除模板字段", httpMethod = SwaggerConstants.DELETE)
    @SysLog("删除模板字段")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
		try {
			List<TemplateMetadata> byTemplateTableId = templateMetadataService.getByTemplateTableId(id);
				SysLogContextHolder.setLogTitle(String.format("删除模板字段-字段名称【%s】-元数据类型【%s】",byTemplateTableId.stream().map(TemplateMetadata::getMetadataChinese).collect(Collectors.toList()),byTemplateTableId.stream().map(TemplateMetadata::getMetadataType).collect(Collectors.toList())));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        templateMetadataService.removeById(id);
        return new R<>(true);
    }

    @ApiOperation(value = "根据ID获取对象", httpMethod = SwaggerConstants.GET)
    @GetMapping("/{id}")
    public R<TemplateMetadata> getById(@PathVariable("id") Long id) {
        return new R<>(templateMetadataService.getById(id));
    }

    @ApiOperation(value = "模板字段列表（分页）", httpMethod = SwaggerConstants.GET)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "current", value = "当前页数", required = true, paramType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量，默认为10", paramType = "int"),
        @ApiImplicitParam(name = "templateTableId", value = "表模块ID", required = true, paramType = "int"),
        @ApiImplicitParam(name = "keyword", value = "中文名称或英文名称", paramType = "int")
    })
    @GetMapping("/page/{templateTableId}")
    public R<?> page(Page<TemplateMetadata> page, @PathVariable("templateTableId") Long templateTableId,String keyword) {
        return new R<>(templateMetadataService.page(page, templateTableId, keyword));
    }

    @ApiOperation(value = "模板字段列表", httpMethod = SwaggerConstants.GET)
    @GetMapping("/list/{templateTableId}")
    public R<?> list(
        @ApiParam(value = "templateTableId", name = "表模板ID") @PathVariable("templateTableId") Long templateTableId,
        @ApiParam(value = "keyword", name = "模板字段名称") String keyword) {
        return new R<>(templateMetadataService.list(templateTableId, keyword));
    }

    @ApiOperation(value = "复制模板字段", httpMethod = SwaggerConstants.POST)
    @SysLog("复制模板字段")
    @PostMapping("/copy/{templateTableId}/{copyIds}")
    public R<?> copy(
        @ApiParam(value = "templateTableId", name = "复制到表模板ID") @PathVariable("templateTableId") Long templateTableId,
        @ApiParam(value = "copyIds", name = "要复制的表模板ID") @PathVariable("copyIds") Long[] copyIds) {
        templateMetadataService.copy(copyIds, templateTableId);
        return new R<>(true);
    }

    @ApiOperation(value = "获取门类-档案类型表模板字段信息", httpMethod = SwaggerConstants.GET)
    @GetMapping(value = "/data/{tenantId}")
    @SysLog("获取门类-档案类型表模板字段信息")
    public R<List<ArrayList<String>>> getArchivesTypeTableTemplateMetadataInfor(@PathVariable("tenantId") Long tenantId) {
        return new R(templateMetadataService.getArchivesTypeTableTemplateMetadataInfor(tenantId));
    }
}
