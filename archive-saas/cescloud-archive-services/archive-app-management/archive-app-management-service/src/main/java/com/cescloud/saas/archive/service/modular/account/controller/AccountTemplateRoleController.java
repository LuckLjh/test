package com.cescloud.saas.archive.service.modular.account.controller;

import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateRoleService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName AccountTemplateRoleController
 * @Author zhangxuehu
 * @Date 2021/2/24 上午9:56
 **/
@Api(value = "AccountTemplateRoleController", tags = "台账模板与角色的关系")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/account-template-role")
public class AccountTemplateRoleController {

    private final AccountTemplateRoleService accountTemplateRoleService;
}
