package com.cescloud.saas.archive.service.modular.account.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cescloud.saas.archive.api.modular.account.constant.AccountEnum;
import com.cescloud.saas.archive.api.modular.account.entity.AccountModule;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.account.service.AccountModuleService;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName AccountModuleController
 * @Author zhangxuehu
 * @Date 2021/2/24 上午9:55
 **/
@Api(value = "AccountModuleController", tags = "台账模块")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/account-module")
public class AccountModuleController {

    private final AccountModuleService accountModuleService;

    @ApiOperation(value = "获取所有模块信息", httpMethod = SwaggerConstants.GET)
    @GetMapping("/all")
    public R getAccountModuleAll(){
        return new R(accountModuleService.list(Wrappers.<AccountModule>lambdaQuery().ne(AccountModule::getModuleCode, AccountEnum.ARCHIVES_PROJECT.getCode()).orderByAsc(AccountModule::getSortNo)));
    }

}
