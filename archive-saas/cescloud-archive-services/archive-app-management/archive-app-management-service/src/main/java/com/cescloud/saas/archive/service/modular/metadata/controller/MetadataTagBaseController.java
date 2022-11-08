
package com.cescloud.saas.archive.service.modular.metadata.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataTagBase;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataTagBaseService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 基础标签
 *
 * @author liudong1
 * @date 2019-09-16 10:14:57
 */
@Api(value = "MetadataTagBase", tags = "基础标签")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/tag-base")
public class MetadataTagBaseController {

	private final MetadataTagBaseService metadataTagBaseService;

	/**
	 * 分页查询
	 *
	 * @param page            分页对象
	 * @param metadataTagBase 基础标签
	 * @return
	 */
	@ApiOperation(value = "分页查询", httpMethod = "GET")
	@GetMapping("/page")
	public R getMetadataTagBasePage(Page page, MetadataTagBase metadataTagBase) {
		return new R<>(metadataTagBaseService.page(page, Wrappers.query(metadataTagBase)));
	}


	/**
	 * 通过id查询基础标签
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询基础标签", httpMethod = "GET")
	@GetMapping("/{id}")
	public R getById(@PathVariable("id") Long id) {
		return new R<>(metadataTagBaseService.getById(id));
	}

	/**
	 * 新增基础标签
	 *
	 * @param metadataTagBase 基础标签
	 * @return R
	 */
	@ApiOperation(value = "新增基础标签", httpMethod = "POST")
	@SysLog("新增基础标签")
	@PostMapping
	public R save(@RequestBody MetadataTagBase metadataTagBase) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增基础标签-元数据标签中文名称【%s】",metadataTagBase.getTagChinese() ));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(metadataTagBaseService.save(metadataTagBase));
	}

	/**
	 * 修改基础标签
	 *
	 * @param metadataTagBase 基础标签
	 * @return R
	 */
	@ApiOperation(value = "修改基础标签", httpMethod = "PUT")
	@SysLog("修改基础标签")
	@PutMapping
	public R updateById(@RequestBody MetadataTagBase metadataTagBase) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改基础标签-元数据标签中文名称【%s】",metadataTagBase.getTagChinese() ));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(metadataTagBaseService.updateById(metadataTagBase));
	}

	/**
	 * 通过id删除基础标签
	 *
	 * @param id id
	 * @return R
	 */
	@ApiOperation(value = "通过id删除基础标签", httpMethod = "DELETE")
	@SysLog("删除基础标签")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable Long id) {
		try {
			MetadataTagBase byId = metadataTagBaseService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除基础标签-元数据标签中文名称【%s】",byId.getTagChinese() ));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(metadataTagBaseService.removeById(id));
	}

	@ApiOperation(value = "初始化基础标签", httpMethod = "POST")
	@SysLog("初始化基础标签")
	@PostMapping("/init")
	public R initSystemMetadataTag(@RequestParam("tenantId") @ApiParam(name = "tenantId", value = "租户ID", required = true) Long tenantId) {
		metadataTagBaseService.insertIntoMetadataTagFromBase(tenantId);
		return new R<>(null,"初始化完成！");
	}

}
