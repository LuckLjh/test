
package com.cescloud.saas.archive.service.modular.archivedict.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.archivedict.dto.DictItemDTO;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictItem;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictItemService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;


/**
 * 数据字典值
 *
 * @author liudong1
 * @date 2019-03-18 17:47:15
 */
@Api(value = "dictItem", tags = "数据字典值管理")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/dict-item")
public class DictItemController {

	private final DictItemService dictItemService;

	/**
	 * 分页查询
	 *
	 * @param page     分页对象
	 * @param dictItemDTO 数据字典值
	 * @return
	 */
	@ApiOperation(value = "数据字典值分页查询", httpMethod = "GET")
	@GetMapping("/page")
	public R<IPage<DictItem>> getDictItemPage(@ApiParam(name = "page", value = "分页对象", required = true) Page page,
											  @ApiParam(name = "dictItem", value = "数据字典值对象", required = true) DictItemDTO dictItemDTO) {
		return new R<>(dictItemService.getPage(page,dictItemDTO));
	}


	@ApiOperation(value = "根据字典项编码查询数据字典值列表", httpMethod = "GET")
	@GetMapping("/list/codes")
	public R<List<DictItem>> getDictItemListByDictCodes(@ApiParam(name = "dictCodes", value = "字典编码，多个用逗号隔开", required = true) @NotBlank(message = "字典编码不能为空") String dictCodes) {
		return new R<>(dictItemService.getDictItemListByDictCodes(dictCodes));
	}

	/**
	 * 通过id查询数据字典值
	 *
	 * @param id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询数据字典值", httpMethod = "GET")
	@GetMapping("/{id}")
	public R<DictItem> getById(@PathVariable("id") @ApiParam(name = "id", value = "数据字典值ID", required = true) Long id) {
		return new R<>(dictItemService.getById(id));
	}

	/**
	 * 新增数据字典值
	 *
	 * @param dictItem 数据字典值
	 * @return R
	 */
	@ApiOperation(value = "新增数据字典值", httpMethod = "POST")
	@SysLog("新增数据字典值")
	@PostMapping
	public R save(@Valid @RequestBody @ApiParam(name = "dictItem", value = "数据字典值对象", required = true) DictItem dictItem) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增数据字典值-字典项编码【%s】,字典值【%s】,字典值标签【%s】",dictItem.getDictCode(),dictItem.getItemValue(),dictItem.getItemLabel()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(dictItemService.saveDictItem(dictItem));
	}

	/**
	 * 修改数据字典值
	 *
	 * @param dictItem 数据字典值
	 * @return R
	 */
	@ApiOperation(value = "修改数据字典值", httpMethod = "PUT")
	@SysLog("修改数据字典值")
	@PutMapping
	public R updateById(@Valid @RequestBody @ApiParam(name = "dictItem", value = "数据字典值对象", required = true) DictItem dictItem) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改数据字典值-字典项编码【%s】,字典值【%s】,字典值标签【%s】",dictItem.getDictCode(),dictItem.getItemValue(),dictItem.getItemLabel()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(dictItemService.updateDictItem(dictItem));
	}

	/**
	 * 通过id删除数据字典值
	 *
	 * @param id
	 * @return R
	 */
	@ApiOperation(value = "删除数据字典值", httpMethod = "DELETE")
	@SysLog("删除数据字典值")
	@DeleteMapping("/{id}")
	public R removeById(@PathVariable @ApiParam(name = "id", value = "数据字典值ID", required = true) @NotNull(message = "数据字典值ID不能为空") Long id) {
		try {
			DictItem byId = dictItemService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除数据字典值-字典项编码【%s】,字典值【%s】,字典值标签【%s】",byId.getDictCode(),byId.getItemValue(),byId.getItemLabel()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(dictItemService.removeDictItem(id));
	}

	/**
	 * 导入数据字典
	 *
	 * @param file
	 * @return
	 */
	@ApiOperation(value = "导入数据字典", httpMethod = "POST")
	@SysLog("导入数据字典")
	@PostMapping("/import")
	public R importExcle(@RequestParam("file") @ApiParam(name = "file", value = "excel文件", required = true) MultipartFile file) throws IOException {
		try {
			SysLogContextHolder.setLogTitle(String.format("导入数据字典-导入数据字典的文件名称【%s】",file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return dictItemService.insertExcel(file);
	}

	/**
	 * 导出数据字典
	 *
	 * @param response
	 */
	@ApiOperation(value = "导出数据字典", httpMethod = "GET")
	@SysLog("导出数据字典")
	@GetMapping("/export")
	public void exportExcel(HttpServletResponse response) {
		try {
			SysLogContextHolder.setLogTitle("导出数据字典-导出数据字典编码信息表");
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		dictItemService.exportExcel(response,"数据字典编码信息表");
	}

	/**
	 * 通过字典项编码查询数据字典值
	 *
	 * @param dictCode
	 * @return R
	 */
	@ApiOperation(value = "通过字典项编码查询数据字典值", httpMethod = "GET")
	@GetMapping("/list/{dictCode}")
	public R<List<DictItem>> getByDictCode(@PathVariable("dictCode") @ApiParam(name = "dictCode", value = "字典项编码", required = true) String dictCode) {
		return new R<>(dictItemService.getItemListByDictCode(dictCode));
	}

	@Inner
	@ApiOperation(value = "通过字典项编码查询数据字典值（内部调用）", httpMethod = "GET")
	@GetMapping("/inner/list/{dictCode}")
	public R<List<DictItem>> getInnerByDictCode(@PathVariable("dictCode") @ApiParam(name = "dictCode", value = "字典项编码", required = true) String dictCode) {
		return new R<>(dictItemService.getItemListByDictCode(dictCode));
	}

	@ApiOperation(value = "获取数据字典信息配置", httpMethod = SwaggerConstants.GET)
	@GetMapping(value = "/data/{tenantId}")
	@SysLog("获取数据字典信息配置")
	public R getDataDictionary(@PathVariable("tenantId") Long tenantId) {
		return new R(dictItemService.getDataDictionary(tenantId));
	}

	@ApiOperation(value = "通过字典项编码查询数据字典值", httpMethod = "GET")
	@GetMapping("/getDictCode")
	public R<DictItem> getDictItemByDictCodeAndItemLabel(@RequestParam("dictCode") @ApiParam(name = "dictCode", value = "字典项编码", required = true) String dictCode,
															   @RequestParam("itemLabel") @ApiParam(name = "itemLabel", value = "字典项名称", required = true) String itemLabel,
															   @RequestParam("tenantId") @ApiParam(name = "tenantId", value = "", required = true) Long tenantId) {
		return new R<>(dictItemService.getDictItemByDictCodeAndItemLabel(dictCode,itemLabel,tenantId));
	}

	@ApiOperation(value = "通过字典项编码查询数据字典值", httpMethod = "GET")
	@GetMapping("/getDictItemByItemCode")
	public R<DictItem> getDictItemByDictCodeAndItemCode(@RequestParam("dictCode") @ApiParam(name = "dictCode", value = "字典项编码", required = true) String dictCode,
														 @RequestParam("itemCode") @ApiParam(name = "itemCode", value = "字典项名称", required = true) String itemCode,
														 @RequestParam("tenantId") @ApiParam(name = "tenantId", value = "", required = true) Long tenantId) {
		return new R<>(dictItemService.getDictItemByDictCodeAndItemCode(dictCode,itemCode,tenantId));
	}

	@ApiOperation(value = "数据字典拖动排序", httpMethod = "POST")
	@PostMapping("/set-order")
	public void setOrder(@RequestBody List<Long> ids) {
		dictItemService.setOrder(ids);
	}
}
