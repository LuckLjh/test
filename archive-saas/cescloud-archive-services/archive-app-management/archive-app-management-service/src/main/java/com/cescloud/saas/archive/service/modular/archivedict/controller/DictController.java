
package com.cescloud.saas.archive.service.modular.archivedict.controller;

import com.cescloud.saas.archive.api.modular.archivedict.entity.Dict;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * 数据字典项
 *
 * @author liudong1
 * @date 2019-03-18 17:44:09
 */
@Api(value = "dict", tags = "数据字典项管理")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/dict")
public class DictController {

	private final DictService dictService;

	/**
	 * 通过id查询数据字典项
	 *
	 * @param id
	 * @return R
	 */
	@ApiOperation(value = "根据ID查询数据字典项", httpMethod = "GET")
	@GetMapping("/{id}")
	public R<Dict> getById(@PathVariable("id") @ApiParam(name = "id", value = "数据字典项ID", required = true) @NotNull(message = "数据字典项ID不能为空") Long id) {
		return new R<>(dictService.getById(id));
	}

	/**
	 * 新增数据字典项
	 *
	 * @param dict 数据字典项
	 * @return R
	 */
	@ApiOperation(value = "新增数据字典项", httpMethod = "POST")
	@SysLog("新增数据字典项")
	@PostMapping
	public R<Dict> save(@Valid @RequestBody @ApiParam(name = "dict", value = "数据字典项对象", required = true) Dict dict)
			throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增数据字典项-字典项编码【%s】,字典分类名称【%s】",dict.getDictCode(),dict.getDictLabel()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(dictService.saveDict(dict));
	}

	/**
	 * 修改数据字典项
	 *
	 * @param dict 数据字典项
	 * @return R
	 */
	@ApiOperation(value = "修改数据字典项", httpMethod = "PUT")
	@SysLog("修改数据字典项")
	@PutMapping
	public R<Boolean> updateById(@Valid @RequestBody @ApiParam(name = "dict", value = "数据字典项对象", required = true) Dict dict) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改数据字典项-字典项编码【%s】,字典分类名称【%s】",dict.getDictCode(),dict.getDictLabel()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return dictService.updateDictById(dict);
	}

	/**
	 * 通过id删除数据字典项
	 *
	 * @param id
	 * @return R
	 */
	@ApiOperation(value = "通过id删除数据字典项", httpMethod = "DELETE")
	@SysLog("删除数据字典项")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable("id") @ApiParam(name = "id", value = "数据字典项ID", required = true) @NotNull(message = "数据字典项ID不能为空") Long id)
			throws ArchiveBusinessException {
		try {
			Dict byId = dictService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除数据字典项-字典项编码【%s】,字典分类名称【%s】",byId.getDictCode(),byId.getDictLabel()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(dictService.removeDict(id));
	}

	/**
	 * 获取字典项树
	 * 树形展示，其实只有一层
	 *
	 * @return
	 */
	@ApiOperation(value = "数据字典项树", httpMethod = "GET")
	@GetMapping("/tree")
	public R<List<Dict>> dictTree() {
		return new R<>(dictService.getDictTree());
	}



	@ApiOperation(value = "初始化数据字典", httpMethod = "POST")
	@PostMapping(value = "/initialize")
	@SysLog("初始化数据字典")
	public R initializeHandle(@RequestParam(value = "templateId", required = false) Long templateId, @RequestParam("tenantId") Long tenantId) throws ArchiveBusinessException, IOException {
		return dictService.initializeHandle(templateId, tenantId);
	}

}

