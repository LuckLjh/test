
package com.cescloud.saas.archive.service.modular.fwimp.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsDTO;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaImpAndColumnDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaImportDTO;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaColumn;
import com.cescloud.saas.archive.api.modular.fwimp.entity.OaImport;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaImportService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * oa 导入
 *
 * @author hyq
 * @date 2019-03-21 12:04:54
 */
@Api(value = "oaImport", tags = "oa 导入")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/oaImport")
public class OaImportController {

	private final OaImportService oaImportService;

	/**
	 * 分页查询
	 *
	 * @param page  分页对象
	 * @param oaImport oa 导入
	 * @return
	 */

	@ApiOperation(value = "oa 导入的分页查询", httpMethod = "GET")
	@GetMapping("/page")
	public R getFondsPage(@ApiParam(value = "分页对象", name = "page", required = true) Page page,
						  @ApiParam(value = "查询的实体", name = "fonds", required = false) OaImportDTO oaImport) {
		return new R<>(oaImportService.getPage(page,oaImport));
	}

	@SysLog("任务激活")
	@PutMapping("/activate/{ids}")
	public R activate(@ApiParam(value = "oa任务id", name = "ids", required = true) @PathVariable("ids") String ids) {
		try {
			String[] split = ids.split(",");
			List<String> idList = Arrays.asList(split);
			List<OaImport> oaImports = oaImportService.listByIds(idList);
			SysLogContextHolder.setLogTitle(String.format("任务激活-流程类型【%s】", oaImports.stream().map(OaImport::getName).collect(Collectors.toList()).toString()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return oaImportService.activate(ids);
	}

	@SysLog("任务终止")
	@PutMapping("/disActivate/{ids}")
	public R disActivate(@ApiParam(value = "oa任务id", name = "ids", required = true) @PathVariable("ids") String ids) {
		try {
			String[] split = ids.split(",");
			List<String> idList = Arrays.asList(split);
			List<OaImport> oaImports = oaImportService.listByIds(idList);
			SysLogContextHolder.setLogTitle(String.format("任务终止-流程类型【%s】", oaImports.stream().map(OaImport::getName).collect(Collectors.toList()).toString()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return oaImportService.disActivate(ids);
	}
	@SysLog("删除任务")
	@PutMapping("/disOa/{ids}")
	public R disOa(@ApiParam(value = "oa任务id", name = "ids", required = true) @PathVariable("ids") String ids) {
		try {
			String[] split = ids.split(",");
			List<String> idList = Arrays.asList(split);
			List<OaImport> oaImports = oaImportService.listByIds(idList);
			SysLogContextHolder.setLogTitle(String.format("删除任务-流程类型【%s】", oaImports.stream().map(OaImport::getName).collect(Collectors.toList()).toString()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return oaImportService.disOa(ids);
	}


	@ApiOperation(value = "新增Oa导入任务", httpMethod = "POST")
	@SysLog("新增Oa导入任务")
	@PostMapping("/save")
	public R save(@RequestBody @ApiParam(value = "传入json格式", name = "OaImpAndColumnDTO对象", required = true)  OaImpAndColumnDTO oaImpAndColumn ) {
		try {
			SysLogContextHolder.setLogTitle(String.format("新增Oa导入任务-流程类型【%s】", oaImpAndColumn.oaImport.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return oaImportService.saveOa(oaImpAndColumn);
	}

	@ApiOperation(value = "复制Oa导入任务", httpMethod = "POST")
	@SysLog("复制Oa导入任务")
	@PostMapping("/copyOaTask")
	public R copyOaTask(@RequestBody @ApiParam(value = "传入json格式", name = "oaImp对象", required = true) @Valid OaImportDTO oaImp ) {
		try {
			SysLogContextHolder.setLogTitle(String.format("复制Oa导入任务-流程类型【%s】", oaImp.getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		String id = oaImp.getSelectId();
		return oaImportService.copyOa(oaImp,id);
	}

	@ApiOperation(value = "修改Oa导入任务", httpMethod = "PUT")
	@SysLog("修改Oa导入任务")
	@PostMapping("/updateOaById")
	public R updateOaById(@RequestBody @ApiParam(value = "传入json格式", name = "OaImpAndColumnDTO对象", required = true)  OaImpAndColumnDTO oaImpAndColumn ) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改Oa导入任务-流程类型【%s】", oaImpAndColumn.getOaImport().getName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return oaImportService.updateOaById(oaImpAndColumn);
	}

	@ApiOperation(value = "导出oa导入模板", httpMethod = "GET")
	@GetMapping("/template")
	public void downloadOaColumnTemplate(HttpServletResponse response) {
		oaImportService.downloadOaColumnTemplate(response);
	}

	@GetMapping("/startImp/{param}")
	@Inner(value = false)
	public R<?> startImp(@PathVariable("param") String param) {
		oaImportService.startImp(param);
		return new R<>().success(null, "发送oa 导入定时任务完成！");
	}
}