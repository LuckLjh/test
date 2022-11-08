package com.cescloud.saas.archive.service.modular.account.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.account.dto.AccountTemplateDTO;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplate;
import com.cescloud.saas.archive.api.modular.role.dto.RoleSyncTreeNode;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveDeckNew;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName AccountTemplateController
 * @Author zhangxuehu
 * @Date 2021/2/24 上午9:56
 **/
@Api(value = "AccountTemplateController", tags = "台账模板定义")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/account-template")
public class AccountTemplateController {

    private final AccountTemplateService accountTemplateService;

    @ApiOperation(value = "分页查询", httpMethod = SwaggerConstants.GET)
    @GetMapping("/page")
    public R getPage(Page page, @ApiParam(name = "keyword", value = "条件检索") String keyword) {
        return new R<>(accountTemplateService.getPage(page, keyword));
    }

    @ApiOperation(value = "通过id查询台账模板信息", httpMethod = SwaggerConstants.GET)
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") Long id) {
        return new R<>(accountTemplateService.getInfoById(id));
    }

    @ApiOperation(value = "保存台账模板", httpMethod = SwaggerConstants.POST)
    @PostMapping
    @SysLog("保存台账模板")
    public R saveAccountTemplate(@RequestBody @ApiParam(name = "accountTemplate",value = "保存实体") AccountTemplateDTO accountTemplateDTO) {
		try {
			SysLogContextHolder.setLogTitle(String.format("保存台账模板-操作的模板名称【%s】",accountTemplateDTO.getTemplateName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(accountTemplateService.saveAccountTemplate(accountTemplateDTO));
    }

    @ApiOperation(value = "删除台账信息", httpMethod = SwaggerConstants.DELETE)
    @SysLog("删除台账信息")
    @DeleteMapping("/{ids}")
    public R removeAccountTemplate(@PathVariable @ApiParam(value = "主键ids", name = "ids", required = true) @NotNull(message = "模板id不能为空") String ids) {
        return new R<>(accountTemplateService.removeAccountTemplate(ids));
    }

    @ApiOperation(value = "修改台账模板", httpMethod = SwaggerConstants.PUT)
    @PutMapping
    @SysLog("修改台账模板")
    public R updateAccountTemplate(@RequestBody @ApiParam(name = "accountTemplate",value = "修改实体") AccountTemplateDTO accountTemplateDTO) {
		try {
			SysLogContextHolder.setLogTitle(String.format("修改台账模板-操作的模板名称【%s】",accountTemplateDTO.getTemplateName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(accountTemplateService.updateAccountTemplate(accountTemplateDTO));
    }

    @ApiOperation(value = "设置关联关系", httpMethod = SwaggerConstants.PUT)
    @PutMapping("/association")
    @SysLog("设置关联关系")
    public R settingAssociatedRole(@RequestBody @ApiParam(name = "accountTemplateDTO",value = "修改实体") AccountTemplateDTO accountTemplateDTO){
        return new R<>(accountTemplateService.settingAssociatedRole(accountTemplateDTO));
    }

    @ApiOperation(value = "设置默认模板", httpMethod = SwaggerConstants.PUT)
    @PutMapping("/default")
    @SysLog("设置默认模板")
    public R settingDefaultTemplate(@RequestBody @ApiParam(name = "accountTemplate",value = "修改实体") AccountTemplate accountTemplate){
		try {
			AccountTemplate byId = accountTemplateService.getById(accountTemplate.getId());
			SysLogContextHolder.setLogTitle(String.format("设置默认模板-操作的模板名称【%s】",byId.getTemplateName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        return new R<>(accountTemplateService.settingDefaultTemplate(accountTemplate));
    }

    @ApiOperation(value = "获取用户绑定的模板", httpMethod = SwaggerConstants.GET)
    @GetMapping("/account/{roles}")
    public R getAccountModule(@PathVariable @ApiParam(value = "roles", name = "当前用户的角色ids", required = true) @NotNull(message = "角色id不能为空") String roles){
        return new R<>(accountTemplateService.getAccountModule(roles));
    }

    @ApiOperation(value = "授权角色同步树", httpMethod = "GET")
    @GetMapping("/tree")
    public R<List<RoleSyncTreeNode>> getSysRoleTree(@RequestParam(value = "id") @ApiParam(value = "id", name = "模板id", required = true) Long id,
													@RequestParam(value = "templateName") String templateName) throws ArchiveBusinessException {
        return new R<List<RoleSyncTreeNode>>(accountTemplateService.roleTree(id,templateName));
    }

	@ApiOperation(value = "根据租户id 获取租户台账定义数据", httpMethod = SwaggerConstants.GET, hidden = true)
	@GetMapping("/data/{tenantId}")
	R<List<ArrayList<String>>> getAccountInfo(@PathVariable("tenantId") Long tenantId) {
		return new R(accountTemplateService.getAccountInfo(tenantId));
	}

	@ApiOperation(value = "查询是否显示驾驶舱，领导角色的用户", httpMethod = SwaggerConstants.POST)
	@PostMapping("/show-deck")
	public R showDeck(@RequestBody @ApiParam(name = "accountTemplate",value = "查询条件") ArchiveDeckNew archiveDeckNew) {
		return new R<>(accountTemplateService.showDeck(archiveDeckNew));
	}

	@ApiOperation(value = "查询模板对应的角色", httpMethod = SwaggerConstants.GET)
	@GetMapping("/accountTemplateRole/{templateId}")
	public R getAccountTemplateRole(@PathVariable("templateId") Integer templateId){
		return new R<>(accountTemplateService.getAccountTemplateRole(templateId));
	}
}