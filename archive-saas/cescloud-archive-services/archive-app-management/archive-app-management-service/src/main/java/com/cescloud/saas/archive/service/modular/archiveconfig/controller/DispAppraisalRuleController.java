package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.api.modular.archiveconfig.entity.DispAppraisalRule;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.archiveconfig.service.DispAppraisalRuleService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.service.modular.common.security.annotation.Inner;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(value = "archiveSort", tags = "应用管理-档案门类管理:档案鉴定规则")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/appraisal-rule")
public class DispAppraisalRuleController {

    private final DispAppraisalRuleService dispAppraisalRuleService;


    @ApiOperation(value = "保存鉴定规则", httpMethod = SwaggerConstants.POST)
    @SysLog("保存鉴定规则")
    @PostMapping
    public R save(@RequestBody @ApiParam(name = "dispAppraisalRule", value = "鉴定规则保存对象", required = true) DispAppraisalRule dispAppraisalRule) {
        return new R(dispAppraisalRuleService.saveDispAppraisalRule(dispAppraisalRule));
    }

    @ApiOperation(value = "清空配置", httpMethod = SwaggerConstants.DELETE)
    @SysLog("清空配置")
    @DeleteMapping
    public R update(@RequestBody @ApiParam(name = "dispAppraisalRule", value = "鉴定规则清空对象", required = true) @Valid DispAppraisalRule dispAppraisalRule) {
        return new R(dispAppraisalRuleService.deleteDispAppraisalRule(dispAppraisalRule));
    }

    @ApiOperation(value = "获取鉴定规则", httpMethod = SwaggerConstants.GET)
    @GetMapping("/data")
    public R<DispAppraisalRule> getByStorageLocate(@RequestParam("storageLocate") @ApiParam(name = "storageLocate", value = "存储表名", required = true) String storageLocate) {
        return new R(dispAppraisalRuleService.getByStorageLocate(storageLocate));
    }

    @ApiOperation(value = "内部调用获取租户所有配置", httpMethod = SwaggerConstants.GET)
    @GetMapping("/inner/list")
    @Inner
    public R<List<DispAppraisalRule>> innerList(){
        return new R(dispAppraisalRuleService.getAll());
    }

    @ApiOperation(value = "校验鉴定规则是否配置", httpMethod = SwaggerConstants.GET)
    @GetMapping("/check")
    public R checkDispAppraisalRule(@RequestParam("typeCode") @ApiParam(name = "typeCode", value = "档案类型", required = true) String typeCode,
                                    @RequestParam("appraisalType") @ApiParam(name = "appraisalType", value = "鉴定类型", required = true) Integer appraisalType) throws ArchiveBusinessException {
        return new R(dispAppraisalRuleService.checkDispAppraisalRule(typeCode,appraisalType));
    }
}
