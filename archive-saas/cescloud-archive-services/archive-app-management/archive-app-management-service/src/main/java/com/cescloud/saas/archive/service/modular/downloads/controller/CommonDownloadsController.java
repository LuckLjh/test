package com.cescloud.saas.archive.service.modular.downloads.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.downloads.dto.CommonDownloadsDTO;
import com.cescloud.saas.archive.api.modular.downloads.entity.CommonDownloads;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.downloads.service.CommonDownloadsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 常用下载Controller
 *
 * @author LS
 * @Date 2021/3/8
 */
@Api(value = "CommonDownloadsController", tags = "常用下载")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/common-downloads")
public class CommonDownloadsController {

	private final CommonDownloadsService commonDownloadsService;

	@ApiOperation(value = "常用下载电子文件分页查询", httpMethod = "GET")
	@GetMapping("/page")
	public R<IPage<CommonDownloads>> getPage(@ApiParam(value = "分页对象", name = "page", required = true) Page<CommonDownloads> page,
	                                         @ApiParam(value = "搜索关键字", name = "keyword") String keyword) {
		return new R<>(commonDownloadsService.getPage(page, keyword));
	}

	@ApiOperation(value = "常用下载电子首页查询", httpMethod = "GET")
	@GetMapping("/main-page")
	public R<IPage<CommonDownloads>> getMainPage(@ApiParam(value = "首页取条数", name = "size", required = true) Long size) {
		return new R<>(commonDownloadsService.getMainPage(size));
	}

	@ApiOperation(value = "上传常用下载电子文件", httpMethod = "POST")
	@SysLog("上传常用下载电子文件")
	@PostMapping("/upload")
	public R<Boolean> upload(@RequestParam("file") @ApiParam(value = "文件", name = "file", required = true) MultipartFile file) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("上传常用下载电子文件-电子文件名称【%s】",file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(commonDownloadsService.upload(file));
	}

	@ApiOperation(value = "修改常用下载文件", httpMethod = "PUT")
	@SysLog("修改常用下载文件")
	@PutMapping
	public R<Boolean> updateById(@RequestBody @ApiParam(value = "修改对象", name = "commonDownloads", required = true)@Valid CommonDownloadsDTO commonDownloads) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改常用下载文件-电子文件名称【%s】",commonDownloads.getFileName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(commonDownloadsService.updateById(commonDownloads));
	}

	@ApiOperation(value = "删除常用下载电子文件", httpMethod = "DELETE")
	@SysLog("删除常用下载电子文件")
	@DeleteMapping("/{id}")
	public R<Boolean> removeById(@PathVariable @ApiParam(value = "id", name = "id", required = true) @NotNull(message = "文件id不能为空") Long id) throws ArchiveBusinessException {
		try {
			CommonDownloads byId = commonDownloadsService.getById(id);
			SysLogContextHolder.setLogTitle(String.format("删除常用下载电子文件-电子文件名称【%s】",byId.getFileName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(commonDownloadsService.deleteById(id));
	}

	@ApiOperation(value = "下载电子文件", httpMethod = "GET")
	@GetMapping("/{id}")
	public R download(@PathVariable @ApiParam(value = "id", name = "id", required = true) @NotNull(message = "文件id不能为空") Long id) throws Exception {
		return new R(commonDownloadsService.download(id));
	}
}
