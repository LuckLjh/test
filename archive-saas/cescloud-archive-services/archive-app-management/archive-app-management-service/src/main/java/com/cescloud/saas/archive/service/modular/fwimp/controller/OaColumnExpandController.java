package com.cescloud.saas.archive.service.modular.fwimp.controller;

import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnExpListDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaColumnExpandDTO;
import com.cescloud.saas.archive.api.modular.fwimp.dto.OaImportDTO;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaColumnExpandService;
import com.cescloud.saas.archive.service.modular.fwimp.service.OaImportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * oa 导入
 *
 * @author hyq
 * @date 2019-03-21 12:04:54
 */
@Api(value = "oaColumnExp", tags = "oa 列详细")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/oaColumnExp")
public class OaColumnExpandController {

	private final OaColumnExpandService oaColumnExpService;

	@ApiOperation(value = "新增字段详细配置", httpMethod = "POST")
	@SysLog("新增字段详细配置")
	@PostMapping("/save")
	public R save(@RequestBody @ApiParam(value = "传入json格式", name = "oaColumns对象", required = true)  OaColumnExpListDTO oaColumnExpListDTO) {
		return oaColumnExpService.saveOaColumnExp(oaColumnExpListDTO);
	}

	@ApiOperation(value = "根据列名和流程id 查询配置", httpMethod = "GET")
	@SysLog("根据列名和流程id 查询配置")
	@GetMapping("/findByColumnAndFlowId")
	public R findByColumnAndFlowId(
			@RequestParam("columnName") @ApiParam(value = "oa日志id", name = "id", required = true) @PathVariable("columnName") String columnName,
			@RequestParam("flowId") @ApiParam(value = "流程id", name = "flowId", required = true) @PathVariable("flowId") Long flowId	) throws ArchiveBusinessException {
		return oaColumnExpService.findByColumnAndFlowId(columnName,flowId);
	}

}
