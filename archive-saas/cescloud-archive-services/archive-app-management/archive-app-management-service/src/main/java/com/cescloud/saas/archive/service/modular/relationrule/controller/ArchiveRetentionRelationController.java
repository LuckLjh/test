package com.cescloud.saas.archive.service.modular.relationrule.controller;

import com.cescloud.saas.archive.api.modular.relationrule.dto.PostArchiveRetentionRelation;
import com.cescloud.saas.archive.api.modular.relationrule.entity.ArchiveRetentionRelation;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.relationrule.service.ArchiveRetentionRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(value = "retention-relation", tags = "档案门类与保管期限关联")
@Slf4j
@RestController
@Validated
@AllArgsConstructor
@RequestMapping("/retention-relation")
public class ArchiveRetentionRelationController {

    private final ArchiveRetentionRelationService archiveRetentionRelationService;

    @ApiOperation(value = "新增关联关系", httpMethod = SwaggerConstants.POST)
    @PostMapping
    public R save(@Valid @RequestBody @ApiParam(name = "PostArchiveRetentionRelation", value = "关联关系集", required = true) PostArchiveRetentionRelation postArchiveRetentionRelation){
        return new R(archiveRetentionRelationService.saveRelation(postArchiveRetentionRelation));
    }

    @ApiOperation(value = "根据档案类型获取关联关系", httpMethod = "GET")
    @GetMapping("/{typeCode}")
    public R<List<ArchiveRetentionRelation>> getById(@PathVariable("typeCode") @ApiParam(name = "typeCode", value = "档案类型", required = true) @NotNull(message = "档案类型不能为空") String typeCode) {
        return new R<List<ArchiveRetentionRelation>>(archiveRetentionRelationService.getByTypeCode(typeCode));
    }
}
