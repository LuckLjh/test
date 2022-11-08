package com.cescloud.saas.archive.service.modular.fwimp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaImportDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaLogsDTO;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaImportService;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaLogsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * oa 导入日志
 *
 * @author hyq
 * @date 2019-03-21 12:04:54
 */
@Api(value = "oaLogs", tags = "oa 导入日志")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/oaLogs")
public class OaLogsController {

	private final OaLogsService oaLogsService;

	@ApiOperation(value = "oa 导入日志的分页查询", httpMethod = "GET")
	@GetMapping("/page")
	public R getFondsPage(@ApiParam(value = "分页对象", name = "page", required = true) Page page,
						  @RequestParam("keyword") @ApiParam(value = "检索值", name = "keyword") @PathVariable("keyword") String keyword,
						  @RequestParam("ownerId") @ApiParam(value = "父id", name = "ownerId", required = true) @PathVariable("ownerId") Long ownerId,
						  @RequestParam("status") @ApiParam(value = "状态", name = "status") @PathVariable("status") String status) {
		return new R<>(oaLogsService.getPage(page,ownerId,status,keyword));
	}

	@ApiOperation(value = "oa xml 文件下载", httpMethod = "GET")
	@GetMapping("/downLoadXml")
	public void getXmlFile(HttpServletResponse response ,
						   @RequestParam("ids") @ApiParam(value = "oa日志id", name = "ids", required = true) @PathVariable("ids") String ids) {
		oaLogsService.getXmlFile(response,ids);
	}

	@ApiOperation(value = "oa log文件详细", httpMethod = "GET")
	@GetMapping("/oaLogDetail")
	public R getOaLogDetail(@RequestParam("id") @ApiParam(value = "oa日志id", name = "id", required = true) @PathVariable("id") Long id) {
		return new R<>(oaLogsService.getOaLogDetail(id));
	}
}
