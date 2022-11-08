package com.cescloud.saas.archive.service.modular.metadata.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTag;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTagBase;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 元数据标签
 *
 * @author liudong1
 * @date 2019-05-23 15:37:43
 */
@Api(value = "MetadataTag", tags = "元数据标签")
@Slf4j
@RestController
@RequestMapping("/metadata-tag")
public class MetadataTagController {

	@Autowired
	private  MetadataTagService metadataTagService;

	/**
	 * 分页查询
	 *
	 * @param page     分页对象
	 * @param keyword 搜索关键字
	 * @return
	 */
	@ApiOperation(value = "分页查询档案层次的元数据标签", httpMethod = "GET")
	@GetMapping("/page")
	public R<IPage<MetadataTag>> getMetadataTagPage(@ApiParam(name = "page", value = "分页对象", required = true) Page page,
														 @ApiParam(name = "metadataTag", value = "搜索关键字", required = false) String keyword) {
		return new R<>(metadataTagService.page(page, keyword));
	}

	/**
	 * 查询标签
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "查询元数据标签", httpMethod = "GET")
	@GetMapping("/{id}")
	public R<MetadataTag> getById(@ApiParam(name = "id", value = "主键ID", required = true) @PathVariable("id") Long id) {
		return new R<>(metadataTagService.getById(id));
	}

	/**
	 * 新增标签
	 *
	 * @param metadataTag 标签
	 * @return R
	 */
	@ApiOperation(value = "新增标签", httpMethod = "POST")
	@SysLog("新增标签")
	@PostMapping
	public R save(@RequestBody MetadataTag metadataTag) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增标签-元数据标签中文名称【%s】",metadataTag.getTagChinese() ));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(metadataTagService.save(metadataTag));
	}

	/**
	 * 修改标签
	 *
	 * @param metadataTag 标签
	 * @return R
	 */
	@ApiOperation(value = "修改标签", httpMethod = "PUT")
	@SysLog("修改标签")
	@PutMapping
	public R updateById(@RequestBody MetadataTag metadataTag) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改标签-元数据标签中文名称【%s】",metadataTag.getTagChinese() ));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(metadataTagService.updateById(metadataTag));
	}

	/**
	 * 通过id删除标签
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id删除标签", httpMethod = "DELETE")
	@SysLog("删除标签")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable Long id) {
		try {
			MetadataTag byId = metadataTagService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除标签-元数据标签中文名称【%s】",byId.getTagChinese() ));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(metadataTagService.removeMetadataTagById(id));
	}

	/**
	 * 获取标签列表，用于字段绑定
	 *
	 * @return
	 */
	@ApiOperation(value = "获取标签列表，用于字段绑定", httpMethod = "GET")
	@GetMapping("/list")
	public R<List<MetadataTag>> getMetadataTagList() {
		return new R<>(metadataTagService.list(Wrappers.<MetadataTag>lambdaQuery()
				.orderByAsc(MetadataTag::getTagSort)));
	}

	@ApiOperation(value = "获取层级树（包括公用标签）", httpMethod = "GET")
	@GetMapping("/layer-tree")
	public R<Layer> getLayerCommonTree(){
		return new R(metadataTagService.getLayerCommonTree());
	}

	@ApiOperation(value = "获取租户元数据标签的信息", httpMethod = SwaggerConstants.GET, hidden = true)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取租户元数据标签的信息")
	public R getMetadataTagsInfo(@PathVariable("tenantId") Long tenantId) {
		return new R(metadataTagService.getMetadataTagsInfo(tenantId));
	}

	@ApiIgnore
	@ApiOperation(value = "元数据初始化", httpMethod = "POST")
	@PostMapping("/initialize")
	@SysLog("元数据初始化")
	public R initializeMetadata(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam(value = "tenantId") Long tenantId) {
		return metadataTagService.initializeMetadata(templateId, tenantId);
	}
}
