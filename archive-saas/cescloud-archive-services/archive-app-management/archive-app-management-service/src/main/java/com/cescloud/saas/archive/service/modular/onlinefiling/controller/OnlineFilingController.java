package com.cescloud.saas.archive.service.modular.onlinefiling.controller;

import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.onlinefiling.service.OnlineFilingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.FormParam;
@Api(value = "online-filing", tags = "在线归档")
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/online-filing")
public class OnlineFilingController {

	@Autowired
	OnlineFilingService onlineFilingService;

	@PostMapping("/startImp")
	public R<?> startImp(@RequestBody @ApiParam(value = "传入json格式", name = "oaImp对象", required = true)String param) {
		onlineFilingService.startImp(param);
		return new R<>().success(null, "发送在线归档成功！");
	}
}
