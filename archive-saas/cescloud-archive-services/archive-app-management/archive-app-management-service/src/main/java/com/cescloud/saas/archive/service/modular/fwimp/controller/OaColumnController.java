package com.cescloud.saas.archive.service.modular.fwimp.controller;

import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaColumnService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * oa 导入
 *
 * @author hyq
 * @date 2019-03-21 12:04:54
 */
@Api(value = "oaColumn", tags = "oa 导入列")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/oaColumn")
public class OaColumnController {

	private final OaColumnService oaColumnService;

	@ApiOperation(value = "oa 列详细", httpMethod = "GET")
	@GetMapping("/oaColumnDetail")
	public R getOaLogDetail(@RequestParam("id") @ApiParam(value = "oa任务id", name = "id", required = true) @PathVariable("id") Long id) {
		return new R<>(oaColumnService.getOaColumnDetail(id));
	}

	@ApiOperation(value = "上传excel")
	@SysLog("上传excel")
	@PostMapping("/excel")
	public R uploadExcel(@RequestBody @ApiParam(name = "上传excel", value = "上传excel", required = true) MultipartFile file,
						 @RequestParam(name="tableName",required = true) @ApiParam(name = "tableName", value = "文件表名", required = true) String tableName ) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("上传excel-上传excel名称【%s】",file.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return oaColumnService.uploadExcel(file, tableName);
	}
	@ApiOperation(value = "根据表名获取字段信息")
	@SysLog("根据表名获取字段信息")
	@GetMapping("/getColumnByName")
	public R getColumnByName(@RequestParam("tableName") @ApiParam(value = "表名", name = "tableName", required = true) @PathVariable("tableName") String tableName) throws ArchiveBusinessException {
		try {
			SysLogContextHolder.setLogTitle(String.format("根据表名获取字段信息-表名【%s】",tableName));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return oaColumnService.getColumnByName(tableName);
	}
}
