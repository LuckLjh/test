
package com.cescloud.saas.archive.service.modular.metadata.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.metadata.entity.MetadataBase;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.metadata.service.MetadataBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 基础元数据
 *
 * @author liudong1
 * @date 2019-03-27 14:33:25
 */
@Api(value = "metadataBase", tags = "基础元数据管理")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/metadata-base")
public class MetadataBaseController {

	private final MetadataBaseService metadataBaseService;

	/**
	 * 分页查询
	 *
	 * @param page         分页对象
	 * @param metadataBase 基础元数据
	 * @return
	 */
	@ApiOperation(value = "分页查询基础元数据", httpMethod = "GET")
	@GetMapping("/page")
	public R<IPage<MetadataBase>> getMetadataBasePage(@ApiParam(name = "page", value = "分页对象", required = true) Page page,
													  @ApiParam(name = "metadataBase", value = "基础元数据对象", required = false) MetadataBase metadataBase) {
		return new R<>(metadataBaseService.page(page, metadataBase));
	}

	/**
	 * 通过id查询基础元数据
	 *
	 * @param id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询基础元数据", httpMethod = "GET")
	@GetMapping("/{id}")
	public R<MetadataBase> getById(@PathVariable("id") @ApiParam(name = "id", value = "基础元数据ID", required = true) Long id) {
		return new R<>(metadataBaseService.getById(id));
	}

	/**
	 * 新增基础元数据
	 *
	 * @param metadataBase 基础元数据
	 * @return R
	 */
	@ApiOperation(value = "新增基础元数据", httpMethod = "POST")
	@SysLog("新增基础元数据")
	@PostMapping
	public R save(@RequestBody @ApiParam(name = "metadataBase", value = "基础元数据实体", required = true) MetadataBase metadataBase) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增基础元数据-元数据中文名称【%s】", metadataBase.getMetadataChinese()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(metadataBaseService.save(metadataBase));
	}

	/**
	 * 修改基础元数据
	 *
	 * @param metadataBase 基础元数据
	 * @return R
	 */
	@ApiOperation(value = "修改基础元数据", httpMethod = "PUT")
	@SysLog("修改基础元数据")
	@PutMapping
	public R updateById(@RequestBody @ApiParam(name = "metadataBase", value = "基础元数据实体", required = true) MetadataBase metadataBase) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改基础元数据-元数据中文名称【%s】", metadataBase.getMetadataChinese()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(metadataBaseService.updateById(metadataBase));
	}

	/**
	 * 通过id删除基础元数据
	 *
	 * @param id
	 * @return R
	 */
	@ApiOperation(value = "通过id删除基础元数据", httpMethod = "DELETE")
	@SysLog("删除基础元数据")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable @ApiParam(name = "id", value = "基础元数据ID", required = true) Long id) {
		try {
			MetadataBase byId = metadataBaseService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除基础元数据-元数据中文名称【%s】", byId.getMetadataChinese()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(metadataBaseService.removeById(id));
	}

}
