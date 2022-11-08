package com.cescloud.saas.archive.service.modular.keyword.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaImport;
import com.cescloud.saas.archive.api.modular.keyword.dto.KeyWordDTO;
import com.cescloud.saas.archive.api.modular.keyword.entity.KeyWord;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.keyword.service.KeyWordService;
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
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 主题词管理
 *
 * @author qianjiang
 * @date 2019-03-22 18:21:28
 */
@Api(value = "keyword", tags = "主题词管理")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/keyword")
public class KeyWordController {

	private final KeyWordService keyWordService;

	/**
	 * 分页查询
	 *
	 * @param page    分页对象
	 * @param keyWordDTO 主题词管理
	 * @return R
	 */
	@ApiOperation(value = "分页查询", httpMethod = "GET")
	@GetMapping("/page")
	public R getKeyWordPage(@ApiParam(value = "分页对象", name = "page", required = true) Page page,
							@ApiParam(value = "查询的实体", name = "keyWordDTO", required = false) KeyWordDTO keyWordDTO) {
		return new R<>(keyWordService.getPage(page,keyWordDTO));
	}


	/**
	 * 通过id查询主题词
	 *
	 * @param keywordId id
	 * @return R
	 */
	@ApiOperation(value = "通过id查询主题词", httpMethod = "GET")
	@GetMapping("/{keywordId}")
	public R getById(@PathVariable("keywordId") @ApiParam(value = "传入主题词主键keywordId" ,name = "keywordId") @NotNull(message = "主题词id不能为空") Long keywordId) {
		return new R<>(keyWordService.getById(keywordId));
	}

	/**
	 * 新增主题词
	 *
	 * @param keyWord 主题词
	 * @return R
	 */
	@ApiOperation(value = "新增主题词", httpMethod = "POST")
	@SysLog("新增主题词")
	@PostMapping
	public R save(@RequestBody @ApiParam(value = "keyWord", name = "主题词对象", required = true) @Valid KeyWord keyWord) {
		try {
		SysLogContextHolder.setLogTitle(String.format("新增主题词-主题词名称【%s】", keyWord.getKeyword()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(keyWordService.saveKeyWord(keyWord));
	}

	/**
	 * 修改主题词
	 *
	 * @param keyWord 主题词
	 * @return R
	 */
	@ApiOperation(value = "修改主题词", httpMethod = "PUT")
	@SysLog("修改主题词")
	@PutMapping
	public R updateById(@RequestBody @ApiParam(value = "keyWord", name = "主题词对象", required = true) @Valid KeyWord keyWord) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改主题词-主题词名称【%s】", keyWord.getKeyword()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(keyWordService.updateKeyWordById(keyWord));
	}

	/**
	 * 通过id删除主题词
	 *
	 * @param keywordId id
	 * @return R
	 */
	@ApiOperation(value = "删除主题词", httpMethod = "DELETE")
	@SysLog("删除主题词")
	@DeleteMapping("/{keywordId}")
	public R removeById(@PathVariable @ApiParam(value = "keywordId" ,name = "主题词id",required = true) @NotNull(message = "主题词id不能为空") Long keywordId) {
		try {
			KeyWord byId = keyWordService.getById(keywordId);
			SysLogContextHolder.setLogTitle(String.format("删除主题词-主题词名称【%s】", byId.getKeyword()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(keyWordService.removeById(keywordId));
	}

	/**
	 * 导出主题词
	 *
	 * @param response
	 */
	@ApiOperation(value = "导出主题词", httpMethod = "GET")
	@GetMapping("/export")
	@SysLog("导出主题词")
	public void exportExcle(HttpServletResponse response) throws IOException {
		keyWordService.exportExcel(response,"主题词信息表");
	}

	/***
	 *  导入主题词
	 * @param file iscover
	 */
	@ApiOperation(value = "导入主题词", httpMethod = "POST")
	@PostMapping("/import")
	@SysLog("导入主题词")
	public R importExcle(@RequestParam("file") @ApiParam(value = "excel文件" ,name = "file",required = true) MultipartFile file) throws IOException {
		try {
			SysLogContextHolder.setLogTitle(String.format("导入主题词-excel文件名称【%s】", file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return keyWordService.insertExcel(file);
	}


	@ApiOperation(value = "导出主题词模板", httpMethod = "GET")
	@SysLog("导出主题词模板")
	@GetMapping("/exportEmpty")
	public void downloadFondsExcle(HttpServletResponse response) {
		keyWordService.downloadExcelTemplate(response,"主题词导入模板");
	}

}
