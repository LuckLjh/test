package com.cescloud.saas.archive.service.modular.archiveconfig.controller;

import com.cescloud.saas.archive.service.modular.archiveconfig.service.MetadataBoxRuleService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName MetadataBoxRuleController
 * @Author zhangxuehu
 * @Date 2020/7/27 10:43
 **/
@Api(value = "metadataBoxRule", tags = "档案门类管理-装盒规则定义：关联字段表")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/box-rule")
public class MetadataBoxRuleController {

    private final MetadataBoxRuleService metadataBoxRuleService;

}
