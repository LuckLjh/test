package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.service.modular.archiveconfig.service.ArchiveConfigManageService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ArchiveConfigManageController
 * @Author zhangxuehu
 * @Date 2020/5/8 10:44
 **/
@Api(value = "archiveConfigManage", tags = "档案门类管理-配置信息")
@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/config-manage")
public class ArchiveConfigManageController {

	@Autowired
	private ArchiveConfigManageService archiveConfigManageService;

	@ApiOperation(value = "获取档案门类配置信息", httpMethod = "GET")
	@GetMapping("/list")
	public R<List<Map<String,Object>>> getArchiveConfigManageList(@ApiParam(name = "storageLocate", value = "档案层级存储表", required = true) @NotBlank(message = "档案层级存储表不能为空") String storageLocate,
																  @ApiParam(name = "sysId", value = "系统的菜单id", required = true) @NotNull(message = "系统的菜单id不能为空") Long sysId) {
		return new R(archiveConfigManageService.getArchiveConfigManageList(storageLocate,sysId));
	}
}
